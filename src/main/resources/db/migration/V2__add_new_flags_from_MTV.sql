INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('VMWare DRS', '', 'VM disgributed resource scheduling between nodes detected and is unsupported in OpenShift Virtualization', 'VMware DRS rule detected') ON CONFLICT DO NOTHING;

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('VM HA', '', 'VM HA detected and is unsupported in OpenShift Virtualization', 'VM HA rule detected') ON CONFLICT DO NOTHING;

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('Ballooned memory', '', 'VM memory ballooning detected and is unsupported in OpenShift Virtualization', 'VM memory ballooning detected') ON CONFLICT DO NOTHING;

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('Encrypted Disk', '', 'VM disk encryption detected and is unsupported in OpenShift Virtualization', 'VM disk encryption detected') ON CONFLICT DO NOTHING;

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('Opaque Network', '', 'Opaque Network detected and is unsupported in OpenShift Virtualization', 'Opaque Network detected') ON CONFLICT DO NOTHING;

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('Passthrough Device', '', 'VM contains passthrough devices which are incompatible with OpenShift Virtualization', 'Passthrough device detected') ON CONFLICT DO NOTHING;
