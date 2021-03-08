INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('Shared VMDK', '', 'A VM with a shared VMDK cannot be migrated automatically and may require reconfiguration pre/post migration.', 'Shared VMDK detected.') ON CONFLICT DO NOTHING;
