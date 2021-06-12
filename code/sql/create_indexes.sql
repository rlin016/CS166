create index patient_ID_index
on patient (patient_ID, name);

create index hospital_ID_index
on hospital (hospital_ID, name);

create index department_ID_index
on department (dept_ID, hid);

create index staff_ID_index
on staff (staff_ID, hid);

create index doctor_ID_index
on doctor (doctor_ID, name, did);

create index appointment_ID_index
on appointment(appnt_ID, status);

create index request_maintenance_index
on request_maintenance (did, sid, dept_name);

create index searches_index
on searches (hid, pid, aid);

create index schedules_index
on schedules (appt_id, staff_id);

create index has_appointment_index
on has_appointment (appt_id, doctor_id);
