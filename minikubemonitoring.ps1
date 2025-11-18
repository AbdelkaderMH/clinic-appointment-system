# minikubemonitoring.ps1
# Automates Prometheus + Grafana installation for Minikube monitoring

param(
    [string]$GrafanaPassword = "admin",
    [switch]$EnablePersistence = $false,
    [switch]$NoPortForward,
    [switch]$NoDashboards,
    [switch]$Verbose
)

if ($Verbose) { $VerbosePreference = "Continue" }

Write-Output "=== Minikube Monitoring Setup Started ==="

# Step 1: Wait for cluster readiness
Write-Output "Waiting for Minikube cluster to be ready..."
$maxRetries = 6
$retryCount = 0
$nodeStatus = $null

while ($retryCount -lt $maxRetries) {
    $nodeStatus = kubectl get nodes --no-headers 2>$null | Select-String "Ready"
    if ($nodeStatus) {
        break
    }
    $retryCount++
    Start-Sleep -Seconds 5
}

if (-not $nodeStatus) {
    Write-Warning "Cluster not ready after retries - skipping monitoring setup"
    exit 0
}

Write-Output "Cluster ready! Node: $($nodeStatus.Line)"

# Step 2: Check if Helm is installed
Write-Output "Checking for Helm installation..."
$helmVersion = helm version --short 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Warning "Helm is not installed or not accessible - skipping monitoring setup"
    Write-Output "To install Helm, visit: https://helm.sh/docs/intro/install/"
    exit 0
}
Write-Output "Helm found: $helmVersion"

# Step 3: Create namespace
Write-Output "Creating/ensuring monitoring namespace..."
try {
    kubectl create namespace monitoring --dry-run=client -o yaml | kubectl apply -f -
} catch {
    Write-Warning "Namespace creation warning: $($_.Exception.Message)"
}

# Step 4: Helm repo setup
Write-Output "Setting up Helm repository..."
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Warning "Repository may already exist"
}
helm repo update

# Step 5: Build values.yaml
$persistenceEnabled = $EnablePersistence.ToString().ToLower()
$promStorage = if ($EnablePersistence) { "volumeClaimTemplate:`n        spec:`n          accessModes: [ 'ReadWriteOnce' ]`n          resources:`n            requests:`n              storage: 8Gi" } else { "volumeClaimTemplate: null" }
$valuesContent = @"
grafana:
  adminUser: admin
  adminPassword: $GrafanaPassword
  persistence:
    enabled: $persistenceEnabled
prometheus:
  prometheusSpec:
    storageSpec:
      $promStorage
    serviceMonitorSelectorNilUsesHelmValues: false
    serviceMonitorSelector: {}
alertmanager:
  alertmanagerSpec:
    storage:
      volumeClaimTemplate: null
"@

$valuesFile = "$env:TEMP\grafana-values.yaml"
$valuesContent | Out-File -FilePath $valuesFile -Encoding UTF8
Write-Output "Created temporary values file: $valuesFile"

# Step 6: Helm install/upgrade
Write-Output "Installing/upgrading kube-prometheus-stack..."
try {
    helm upgrade --install prometheus-stack prometheus-community/kube-prometheus-stack `
        --namespace monitoring `
        --values $valuesFile `
        --wait --timeout=300s
    Write-Output "Helm release successful"
} catch {
    Write-Warning "Helm install failed (non-blocking): $($_.Exception.Message)"
}

# Step 7: Wait for pods
Write-Output "Waiting for monitoring pods to launch (up to 5 mins)..."
$timeout = 300
$startTime = Get-Date
do {
    $podLines = kubectl get pods -n monitoring --no-headers 2>$null
    $totalPods = ($podLines | Measure-Object).Count
    $readyPods = 0
    foreach ($line in $podLines) {
        if ($line -match '(\d+)/(\d+)\s+Running') {
            if ($matches[1] -eq $matches[2]) { $readyPods++ }
        }
    }
    $elapsed = ((Get-Date) - $startTime).TotalSeconds
    Write-Output "Pods: $totalPods total, $readyPods ready... (Elapsed: $([math]::Round($elapsed))s)"
    Start-Sleep -Seconds 10
} while (($readyPods -lt $totalPods -or $totalPods -lt 4) -and $elapsed -lt $timeout)

if ($readyPods -ge $totalPods -and $totalPods -ge 4) {
    Write-Output "All pods ready! Summary:"
    kubectl get pods -n monitoring
} else {
    Write-Warning "Some pods may still be pending"
}

# Step 7.5: Apply ServiceMonitor for clinic app
Write-Output "Applying ServiceMonitor for clinic application..."
try {
    kubectl apply -f k8s/servicemonitor.yaml
    Write-Output "ServiceMonitor applied successfully"
} catch {
    Write-Warning "ServiceMonitor apply failed: $($_.Exception.Message)"
}

# Step 7: Setup port-forwards
if (-not $NoPortForward) {
    Write-Output "Setting up port-forwards..."
    $grafanaJob = Start-Job -ScriptBlock { param($ns); kubectl port-forward -n $ns svc/prometheus-stack-grafana 3000:80 } -ArgumentList "monitoring"
    $promJob = Start-Job -ScriptBlock { param($ns); kubectl port-forward -n $ns svc/prometheus-stack-kube-prom-prometheus 9090:9090 } -ArgumentList "monitoring"
    $clinicJob = Start-Job -ScriptBlock { param($ns); kubectl port-forward -n $ns svc/clinic-appointment-system 8090:8090 } -ArgumentList "clinic"
    Write-Output "Port-forwards started (jobs: $($grafanaJob.Id), $($promJob.Id), $($clinicJob.Id))"
    Write-Output ""
    Write-Output "Access URLs:"
    Write-Output "  Prometheus: http://localhost:9090"
    Write-Output "  Grafana: http://localhost:3000 (admin/admin)"
    Write-Output "  Clinic API: http://localhost:8090"
    Write-Output "  Clinic Swagger: http://localhost:8090/swagger-ui/index.html"
} else {
    Write-Output "Skipping port-forwards (CI mode)"
}

Write-Output "=== Monitoring setup complete ==="

# Cleanup temp file
Remove-Item $valuesFile -Force -ErrorAction SilentlyContinue
