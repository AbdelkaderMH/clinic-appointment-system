-- Insert sample patients (10 patients) - use INSERT IGNORE to be idempotent in MySQL
INSERT IGNORE INTO patients (id, name, email, phone, medical_history) VALUES 
(1, 'John Doe', 'john.doe@example.com', '1234567890', 'No known allergies'),
(2, 'Jane Smith', 'jane.smith@example.com', '0987654321', 'Diabetic'),
(3, 'Bob Johnson', 'bob.johnson@example.com', '5555555555', 'High blood pressure'),
(4, 'Alice Williams', 'alice.williams@example.com', '4444444444', 'Asthma'),
(5, 'Charlie Brown', 'charlie.brown@example.com', '3333333333', 'No known conditions'),
(6, 'Diana Prince', 'diana.prince@example.com', '2222222222', 'Thyroid issues'),
(7, 'Edward Norton', 'edward.norton@example.com', '1111111111', 'Migraine prone'),
(8, 'Fiona Apple', 'fiona.apple@example.com', '6666666666', 'Lactose intolerant'),
(9, 'George Lucas', 'george.lucas@example.com', '7777777777', 'High cholesterol'),
(10, 'Hannah Montana', 'hannah.montana@example.com', '8888888888', 'No known allergies');

-- Insert sample doctors (8 doctors) - use INSERT IGNORE to be idempotent
INSERT IGNORE INTO doctors (id, name, specialization, license_number, email) VALUES 
(1, 'Dr. Sarah Williams', 'Cardiology', 'CARD001', 'sarah.williams@clinic.com'),
(2, 'Dr. Michael Brown', 'Dermatology', 'DERM001', 'michael.brown@clinic.com'),
(3, 'Dr. Emily Davis', 'Pediatrics', 'PEDI001', 'emily.davis@clinic.com'),
(4, 'Dr. James Wilson', 'Orthopedics', 'ORTH001', 'james.wilson@clinic.com'),
(5, 'Dr. Lisa Anderson', 'Neurology', 'NEUR001', 'lisa.anderson@clinic.com'),
(6, 'Dr. Robert Taylor', 'General Practice', 'GP001', 'robert.taylor@clinic.com'),
(7, 'Dr. Patricia White', 'Ophthalmology', 'OPTH001', 'patricia.white@clinic.com'),
(8, 'Dr. Christopher Lee', 'ENT', 'ENT001', 'christopher.lee@clinic.com');

-- Insert sample appointments (12 appointments with different statuses)
-- Use INSERT IGNORE and MySQL-compatible timestamp expressions (relative to now)
INSERT IGNORE INTO appointments (id, patient_id, doctor_id, appointment_date, notes, status, created_at, updated_at) VALUES 
(1, 1, 1, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Regular checkup for heart condition', 'SCHEDULED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 6, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 3 DAY), 'Annual diabetes screening', 'SCHEDULED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, 1, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 4 DAY), 'Blood pressure follow-up', 'SCHEDULED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 4, 2, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 5 DAY), 'Skin rash consultation', 'SCHEDULED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 5, 3, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Pediatric wellness visit', 'SCHEDULED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 6, 5, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 6 DAY), 'Thyroid check', 'SCHEDULED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 7, 2, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY), 'Migraine treatment plan', 'CANCELLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 8, 4, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 8 DAY), 'Orthopedic consultation', 'SCHEDULED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 9, 6, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 9 DAY), 'Cholesterol management', 'SCHEDULED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 10, 7, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 10 DAY), 'Eye examination', 'SCHEDULED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 1, 8, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 11 DAY), 'ENT consultation', 'SCHEDULED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 2, 3, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 12 DAY), 'Child immunization', 'SCHEDULED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);