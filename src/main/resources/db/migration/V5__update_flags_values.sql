UPDATE flag_assessment_model
SET flag_label = 'High Availability (HA) detected' , assessment= 'HA disk locking detected and is unsupported in OpenShift Virtualization'
WHERE flag = 'VM HA';

UPDATE flag_assessment_model
SET flag_label = 'CPU affinity detected' , assessment= 'CPU affinity rules detected; unable to assign to specific nodes/CPU''s'
WHERE flag = 'CPU Affinity';

UPDATE flag_assessment_model
SET assessment = 'VM distributed resource scheduling between nodes detected and is unsupported in OpenShift Virtualization'
WHERE flag = 'VMWare DRS';
