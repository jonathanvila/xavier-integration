delete from flag_assessment_model
WHERE flag = 'Passthrough Device';

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('Passthrough device','','Passthrough devices are not supported by OpenShift Virtualization. The VM cannot be migrated unless the passthrough device is removed.','Passthrough device detected.') ON CONFLICT DO NOTHING;

delete from  flag_assessment_model
WHERE flag = 'VM/Host Affinity Configured';

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('VM-Host affinity','','VM-Host affinity is not supported by OpenShift Virtualization. The VM can be migrated but it will not have this feature in the target environment.','VM-Host affinity detected.') ON CONFLICT DO NOTHING;


delete from flag_assessment_model
WHERE flag = 'CPU/Memory hotplug';

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('CPU/memory hotplug','','Review VM CPU or memory configuration after migration.','CPU/memory hotplug.') ON CONFLICT DO NOTHING;


delete from flag_assessment_model
WHERE flag = 'CPU Affinity';

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('CPU affinity','','CPU affinity is not supported by OpenShift Virtualization. The VM can be migrated but it will not have this feature in the target environment.','CPU affinity detected.') ON CONFLICT DO NOTHING;


delete from flag_assessment_model
WHERE flag = 'Numa Node Affinity';

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('NUMA node affinity','','NUMA node affinity is not supported by OpenShift Virtualization. The VM can be migrated but it will not have this feature in the target environment.','NUMA node affinity detected.') ON CONFLICT DO NOTHING;


delete from flag_assessment_model
WHERE flag = 'UEFI Boot';

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('UEFI secure boot','','UEFI secure boot is not supported by OpenShift Virtualization. The VM cannot be migrated unless UEFI secure boot is disabled.','UEFI secure boot detected.') ON CONFLICT DO NOTHING;


delete from flag_assessment_model
WHERE flag = 'VMWare DRS';

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('VMware DRS','','VMware Distributed Resource Scheduler is not supported by OpenShift Virtualization. The VM can be migrated but it will not have this feature in the target environment.','VMware DRS detected.') ON CONFLICT DO NOTHING;

delete from flag_assessment_model
WHERE flag = 'VM HA';

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('HA','','HA is not supported by OpenShift Virtualization. The VM can be migrated but it will not have this feature in the target environment.','VM running on HA host detected.') ON CONFLICT DO NOTHING;


delete from flag_assessment_model
WHERE flag = 'Ballooned memory';

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('Memory ballooning','','Memory ballooning is not supported by OpenShift Virtualization. The VM can be migrated but it will not have this feature in the target environment.','Memory ballooning detected.') ON CONFLICT DO NOTHING;


delete from flag_assessment_model
WHERE flag = 'Encrypted Disk';

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('Encrypted disk','','Encrypted disks are not supported by OpenShift Virtualization.  The VM cannot be migrated unless the disk is unencrypted.','Encrypted disk detected.') ON CONFLICT DO NOTHING;

delete from flag_assessment_model
WHERE flag = 'Opaque Network';

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('Opaque network','','Opaque networks are not supported by OpenShift Virtualization. The VM can be migrated but the network must be configured after migration.','Opaque network detected.') ON CONFLICT DO NOTHING;


delete from flag_assessment_model
WHERE flag = 'USB device';

INSERT INTO flag_assessment_model
(flag, os_name, assessment, flag_label) VALUES ('USB controller','','USB controllers are not supported by OpenShift Virtualization. The VM can be migrated but the devices attached to the USB controller will not be migrated.','USB controller detected.') ON CONFLICT DO NOTHING;
