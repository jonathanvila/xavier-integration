INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('SR-IOV passthrough adapter', '', 'SR-IOV passthrough adapter configuration is not supported by OpenShift Virtualization.', 'SR-IOV passthrough adapter configuration detected.') ON CONFLICT DO NOTHING;
