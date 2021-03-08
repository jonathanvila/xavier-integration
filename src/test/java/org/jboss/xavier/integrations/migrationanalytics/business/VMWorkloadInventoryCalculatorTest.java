package org.jboss.xavier.integrations.migrationanalytics.business;

import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.UseAdviceWith;
import org.apache.commons.io.IOUtils;
import org.jboss.xavier.Application;
import org.jboss.xavier.analytics.pojo.input.workload.inventory.VMWorkloadInventoryModel;
import org.jboss.xavier.integrations.route.RouteBuilderExceptionHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CamelSpringBootRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = {Application.class})
@UseAdviceWith // Disables automatic start of Camel context
@ActiveProfiles("test")
public class VMWorkloadInventoryCalculatorTest {
    @Inject
    VMWorkloadInventoryCalculator calculator;

    @Test
    public void calculate_jsonGiven_ShouldReturnCalculatedValues() throws IOException {
        String cloudFormsJson = IOUtils.resourceToString("cloudforms-export-v1.json", StandardCharsets.UTF_8, VMWorkloadInventoryCalculatorTest.class.getClassLoader());
        Map<String, Object> headers = new HashMap<>();
        Long analysisId = 30L;
        headers.put(RouteBuilderExceptionHandler.ANALYSIS_ID, analysisId.toString());

        Collection<VMWorkloadInventoryModel> modelList = calculator.calculate(cloudFormsJson, headers);
        assertThat(Integer.valueOf(modelList.size())).isEqualTo(21);
        assertThat(modelList.stream().filter(e -> e.getNicsCount() == 2).count()).isEqualTo(4);
        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("james-db-03-copy")).count()).isEqualTo(2);
        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("dev-windows-server-2008-TEST")).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getGuestOSFullName().equalsIgnoreCase("Microsoft Windows Server 2008 R2 (64-bit)")).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getDiskSpace() == (17179869184L + 10737418240L)).count()).isEqualTo(2);

        VMWorkloadInventoryModel expectedModel = new VMWorkloadInventoryModel();
        expectedModel.setVmName("dev-windows-server-2008-TEST");
        expectedModel.setProvider("VMware");
        expectedModel.setOsProductName("ServerNT");
        expectedModel.setNicsCount(1);
        expectedModel.setMemory(4294967296L);
        expectedModel.setHasRdmDisk(false);
        expectedModel.setGuestOSFullName("Microsoft Windows Server 2008 R2 (64-bit)");
        expectedModel.setDiskSpace(7437787136L);
        expectedModel.setDatacenter("V2V-DC");
        expectedModel.setCpuCores(1);
        expectedModel.setCluster("V2V_Cluster");
        expectedModel.setSystemServicesNames(Arrays.asList("{02B0078E-2148-45DD-B7D3-7E37AAB3B31D}","xmlprov","wudfsvc"));
        expectedModel.setVmDiskFilenames(Collections.singletonList("[NFS_Datastore] dev-windows-server-2008/dev-windows-server-2008.vmdk"));
        expectedModel.setAnalysisId(analysisId);
        expectedModel.setHost_name("esx13.v2v.bos.redhat.com");
        expectedModel.setVersion("6.5");
        expectedModel.setProduct("VMware vCenter");
        HashMap<String, String> files = new HashMap<>();
        files.put("/root/.bash_profile","# .bash_profile\n\n# Get the aliases and functions\nif [ -f ~/.bashrc ]; then\n\t. ~/.bashrc\nfi\n\n# User specific environment and startup programs\n\nPATH=$PATH:$HOME/bin\nexport PATH\nexport JAVA_HOME=/usr/java/jdk1.5.0_07/bin/java\nexport WAS_HOME=/opt/IBM/WebSphere/AppServer\n");
        files.put("/opt/IBM", null);
        expectedModel.setFiles(files);

        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("dev-windows-server-2008-TEST"))
                .findFirst().get()).isEqualToIgnoringNullFields(expectedModel);
    }

    @Test
    public void calculate_jsonV1_0_0_Given_ShouldReturnCalculatedValues() throws IOException, ParseException {
        String cloudFormsJson = IOUtils.resourceToString("cloudforms-export-v1_0_0.json", StandardCharsets.UTF_8, VMWorkloadInventoryCalculatorTest.class.getClassLoader());
        Map<String, Object> headers = new HashMap<>();
        Long analysisId = 30L;
        headers.put(RouteBuilderExceptionHandler.ANALYSIS_ID, analysisId.toString());

        Collection<VMWorkloadInventoryModel> modelList = calculator.calculate(cloudFormsJson, headers);
        assertThat(Integer.valueOf(modelList.size())).isEqualTo(8);
        assertThat(modelList.stream().filter(e -> e.getNicsCount() == 2).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("hana")).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("tomcat")).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getOsProductName().equalsIgnoreCase("Linux")).count()).isEqualTo(7);
        assertThat(modelList.stream().filter(e -> e.getOsProductName().equalsIgnoreCase("CentOS 7 (64-bit)")).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getGuestOSFullName().equalsIgnoreCase("CentOS 7 (64-bit)")).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getGuestOSFullName().equalsIgnoreCase("Red Hat Enterprise Linux Server release 7.6 (Maipo)")).count()).isEqualTo(6);
        assertThat(modelList.stream().filter(e -> e.getDiskSpace() == (17980588032L)).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getHasCpuHotAdd() != null).count()).isEqualTo(2);
        assertThat(modelList.stream().filter(e -> e.getHasCpuHotRemove() != null).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getHasMemoryHotAdd() != null).count()).isEqualTo(2);
        assertThat(modelList.stream().filter(e -> "0,2".equals(e.getCpuAffinity())).count()).isEqualTo(1);

        VMWorkloadInventoryModel expectedModel = new VMWorkloadInventoryModel();
        expectedModel.setVmName("oracle_db");
        expectedModel.setProvider("vSphere");
        expectedModel.setOsProductName("Linux");
        expectedModel.setNicsCount(1);
        expectedModel.setMemory(8589934592L);
        expectedModel.setHasRdmDisk(false);
        expectedModel.setGuestOSFullName("CentOS Linux release 7.6.1810 (Core) ");
        expectedModel.setDiskSpace(17980588032L);
        expectedModel.setDatacenter("JON TEST DC");
        expectedModel.setCpuCores(2);
        expectedModel.setCluster("VMCluster");
        expectedModel.setSystemServicesNames(Arrays.asList("NetworkManager-dispatcher","NetworkManager-wait-online","NetworkManager"));
        expectedModel.setVmDiskFilenames(Arrays.asList("[NFS-Storage] oracle_db_1/", "[NFS-Storage] oracle_db_1/oracle_db.vmdk", "[NFS-Storage] oracle_db_1/"));
        expectedModel.setAnalysisId(analysisId);
        expectedModel.setHost_name("host-47");
        expectedModel.setVersion("6.7.2");
        expectedModel.setProduct("VMware vCenter");
        expectedModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));
        expectedModel.setHasCpuHotRemove(false);
        expectedModel.setHasCpuHotAdd(true);
        expectedModel.setHasMemoryHotAdd(false);

        HashMap<String, String> files = new HashMap<>();
        files.put("/etc/GeoIP.conf","dummy content");
        files.put("/etc/asound.conf", null);
        files.put("/etc/autofs.conf", null);
        expectedModel.setFiles(files);

        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("oracle_db"))
                .findFirst().get())
                .isEqualToIgnoringNullFields(expectedModel);

        VMWorkloadInventoryModel expectedModel2 = new VMWorkloadInventoryModel();
        expectedModel2.setVmName("jboss1");
        expectedModel2.setProvider("vSphere");
        expectedModel2.setOsProductName("Linux");
        expectedModel2.setNicsCount(1);
        expectedModel2.setMemory(2147483648L);
        expectedModel2.setHasRdmDisk(false);
        expectedModel2.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        expectedModel2.setDiskSpace(4833341440L);
        expectedModel2.setDatacenter("JON TEST DC");
        expectedModel2.setCpuCores(1);
        expectedModel2.setCluster("VMCluster");
        expectedModel2.setSystemServicesNames(Arrays.asList("NetworkManager-dispatcher", "NetworkManager-wait-online", "NetworkManager"));
        expectedModel2.setVmDiskFilenames(Arrays.asList("[NFS-Storage] jboss1/", "[NFS-Storage] jboss1/jboss1.vmdk"));
        expectedModel2.setAnalysisId(analysisId);
        expectedModel2.setHost_name("host-47");
        expectedModel2.setVersion("6.7.2");
        expectedModel2.setProduct("VMware vCenter");
        expectedModel2.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        HashMap<String, String> files2 = new HashMap<>();
        files2.put("/etc/GeoIP.conf",null);
        files2.put("/etc/asound.conf", null);
        files2.put("/etc/chrony.conf", null);
        expectedModel2.setFiles(files2);

        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("jboss1"))
                .findFirst().get())
                .isEqualToIgnoringNullFields(expectedModel2);
    }


    @Test
    public void calculate_jsonV1_0_0_GivenWithMissingAttributes_ShouldReturnCalculatedValues() throws IOException, ParseException {
        String cloudFormsJson = IOUtils.resourceToString("cloudforms-export-v1_0_0.json", StandardCharsets.UTF_8, VMWorkloadInventoryCalculatorTest.class.getClassLoader());
        cloudFormsJson = cloudFormsJson.replace("ems_ref", "XXems_ref");

        Map<String, Object> headers = new HashMap<>();
        Long analysisId = 30L;
        headers.put(RouteBuilderExceptionHandler.ANALYSIS_ID, analysisId.toString());

        Collection<VMWorkloadInventoryModel> modelList = calculator.calculate(cloudFormsJson, headers);
        assertThat(Integer.valueOf(modelList.size())).isEqualTo(8);
        assertThat(modelList.stream().filter(e -> e.getNicsCount() == 2).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("hana")).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("tomcat")).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getOsProductName().equalsIgnoreCase("Linux")).count()).isEqualTo(7);
        assertThat(modelList.stream().filter(e -> e.getOsProductName().equalsIgnoreCase("CentOS 7 (64-bit)")).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getGuestOSFullName().equalsIgnoreCase("CentOS 7 (64-bit)")).count()).isEqualTo(1);
        assertThat(modelList.stream().filter(e -> e.getGuestOSFullName().equalsIgnoreCase("Red Hat Enterprise Linux Server release 7.6 (Maipo)")).count()).isEqualTo(6);
        assertThat(modelList.stream().filter(e -> e.getDiskSpace() == (17980588032L)).count()).isEqualTo(1);

        VMWorkloadInventoryModel expectedModel = new VMWorkloadInventoryModel();
        expectedModel.setVmName("oracle_db");
        expectedModel.setProvider("vSphere");
        expectedModel.setOsProductName("Linux");
        expectedModel.setNicsCount(1);
        expectedModel.setMemory(8589934592L);
        expectedModel.setHasRdmDisk(false);
        expectedModel.setGuestOSFullName("CentOS Linux release 7.6.1810 (Core) ");
        expectedModel.setDiskSpace(17980588032L);
        expectedModel.setCpuCores(2);
        expectedModel.setSystemServicesNames(Arrays.asList("NetworkManager-dispatcher","NetworkManager-wait-online","NetworkManager"));
        expectedModel.setVmDiskFilenames(Arrays.asList("[NFS-Storage] oracle_db_1/", "[NFS-Storage] oracle_db_1/oracle_db.vmdk", "[NFS-Storage] oracle_db_1/"));
        expectedModel.setAnalysisId(analysisId);
        expectedModel.setVersion("6.7.2");
        expectedModel.setProduct("VMware vCenter");
        expectedModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));
        expectedModel.setHasCpuHotRemove(false);

        // These are the params missing because of the replace above
        expectedModel.setCluster(null);
        expectedModel.setHost_name(null);
        expectedModel.setDatacenter(null);

        HashMap<String, String> files = new HashMap<>();
        files.put("/etc/GeoIP.conf","dummy content");
        files.put("/etc/asound.conf", null);
        files.put("/etc/autofs.conf", null);
        expectedModel.setFiles(files);

        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("oracle_db"))
                .findFirst().get())
                .isEqualToIgnoringNullFields(expectedModel);
        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("oracle_db"))
                .findFirst().get().getProvider()).isEqualTo("vSphere");
        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("oracle_db"))
                .findFirst().get().getCluster()).isNull();
    }

    @Test
    public void calculate_jsonV1_0_0_GivenWithCoresEquals0_ShouldReturn0CpuCores() throws IOException {
        String cloudFormsJson = IOUtils.resourceToString("cloudforms-export-v1_0_0.json", StandardCharsets.UTF_8, VMWorkloadInventoryCalculatorTest.class.getClassLoader());
        cloudFormsJson = cloudFormsJson.replace("\"cpu_cores_per_socket\": 1,\n                    \"cpu_total_cores\": 4,",
                                           "\"cpu_cores_per_socket\": 0,\n                    \"cpu_total_cores\": 4,");

        Map<String, Object> headers = new HashMap<>();
        Long analysisId = 30L;
        headers.put(RouteBuilderExceptionHandler.ANALYSIS_ID, analysisId.toString());

        Collection<VMWorkloadInventoryModel> modelList = calculator.calculate(cloudFormsJson, headers);
        assertThat(Integer.valueOf(modelList.size())).isEqualTo(8);

        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("hana"))
                .findFirst().get().getCpuCores())
                .isEqualTo(0);
    }

    @Test

    public void calculate_jsonV1_0_0_TestHasUSBcontrollers_True() throws IOException {
        String cloudFormsJson = IOUtils.resourceToString("cloudforms-export-v1_0_0.json", StandardCharsets.UTF_8, VMWorkloadInventoryCalculatorTest.class.getClassLoader());
        cloudFormsJson = cloudFormsJson.replace("\"cpu_cores_per_socket\": 1,\n                    \"cpu_total_cores\": 4,",
                                           "\"cpu_cores_per_socket\": 1,\n                    \"cpu_total_cores\": 4, \"has_usb_controller\":true, ");

        Map<String, Object> headers = new HashMap<>();
        Long analysisId = 30L;
        headers.put(RouteBuilderExceptionHandler.ANALYSIS_ID, analysisId.toString());

        Collection<VMWorkloadInventoryModel> modelList = calculator.calculate(cloudFormsJson, headers);
        assertThat(Integer.valueOf(modelList.size())).isEqualTo(8);

        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("hana"))
                .findFirst().get().getHasUSBcontrollers())
                .isTrue();
   }    
   
    @Test
    public void calculate_jsonV1_0_0_TestHasUSBcontrollers_False() throws IOException {
        String cloudFormsJson = IOUtils.resourceToString("cloudforms-export-v1_0_0.json", StandardCharsets.UTF_8, VMWorkloadInventoryCalculatorTest.class.getClassLoader());
        cloudFormsJson = cloudFormsJson.replace("\"cpu_cores_per_socket\": 1,\n                    \"cpu_total_cores\": 4,",
                                           "\"cpu_cores_per_socket\": 1,\n                    \"cpu_total_cores\": 4, \"has_usb_controller\":false, ");

        Map<String, Object> headers = new HashMap<>();
        Long analysisId = 30L;
        headers.put(RouteBuilderExceptionHandler.ANALYSIS_ID, analysisId.toString());

        Collection<VMWorkloadInventoryModel> modelList = calculator.calculate(cloudFormsJson, headers);
        assertThat(Integer.valueOf(modelList.size())).isEqualTo(8);

        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("hana"))
                .findFirst().get().getHasUSBcontrollers())
                .isFalse();
   }


   @Test
   public void calculate_jsonV1_0_0_TestHasUSBcontrollers_Null() throws IOException {
        String cloudFormsJson = IOUtils.resourceToString("cloudforms-export-v1_0_0.json", StandardCharsets.UTF_8, VMWorkloadInventoryCalculatorTest.class.getClassLoader());
        Map<String, Object> headers = new HashMap<>();
        Long analysisId = 30L;
        headers.put(RouteBuilderExceptionHandler.ANALYSIS_ID, analysisId.toString());

        Collection<VMWorkloadInventoryModel> modelList = calculator.calculate(cloudFormsJson, headers);
        assertThat(Integer.valueOf(modelList.size())).isEqualTo(8);
        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("hana"))
                .findFirst().get().getHasUSBcontrollers())
                .isNull();
    }

   @Test
   public void calculate_jsonV1_0_0_GivenWithHasPassthroughDeviceShouldReturnNotNullValue() throws Exception {
        String cloudFormsJson = IOUtils.resourceToString("cloudforms-export-v1_0_0.json", StandardCharsets.UTF_8, VMWorkloadInventoryCalculatorTest.class.getClassLoader());
        cloudFormsJson = cloudFormsJson.replace("\"name\": \"jboss0\",",
                                                "\"name\": \"jboss0\",\n                    \"has_passthrough_device\": true,  \"has_vm_affinity_config\" : false, \"numa_node_affinity\" : \"nothing\", \"firmware\" : \"UEFI\", \"has_vm_drs_config\" : false, \"has_vm_ha_config\" : true, \"ballooned_memory\" : 9, \"has_encrypted_disk\" : false, \"has_opaque_network\" : true, \"has_sriov_nic\" : true, \"has_shared_vmdk\" : true, \n");
        cloudFormsJson = cloudFormsJson.replace("\"name\": \"db\",",
                                                "\"name\": \"db\",\n                    \"has_passthrough_device\": null,  \"has_vm_affinity_config\" : null, \"numa_node_affinity\" : \"something\", \"firmXXware\" : \"BIOS\", \"has_vm_drs_config\" : true, \"has_vm_ha_config\" : false, \"ballooned_memory\" : 1, \"has_encrypted_disk\" : true, \"has_opaqueXX_network\" : false, \"has_sriov_nic\" : false, \"has_shared_vmdk\" : false, \n");
        // oracle-db : missing
       
        Map<String, Object> headers = new HashMap<>();
        Long analysisId = 30L;
        headers.put(RouteBuilderExceptionHandler.ANALYSIS_ID, analysisId.toString());

        Collection<VMWorkloadInventoryModel> modelList = calculator.calculate(cloudFormsJson, headers);
        assertThat(Integer.valueOf(modelList.size())).isEqualTo(8);

        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("hana")).filter(e -> e.getHasPassthroughDevice() && !e.getHasVmAffinityConfig() &&
                                             e.getNumaNodeAffinity() == null && e.getFirmware().equalsIgnoreCase("BIOS") && !e.getHasVmDrsConfig() &&
                                            e.getHasVmHaConfig() && e.getBalloonedMemory() == 0 && e.getHasEncryptedDisk() && !e.getHasOpaqueNetwork() && e.getHasSriovNic() == null && e.getHasSharedVmdk() == null)
        .count()).isEqualTo(1);

        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("jboss0")).filter(e -> e.getHasPassthroughDevice() && !e.getHasVmAffinityConfig() &&
                                            (e.getNumaNodeAffinity().equalsIgnoreCase("nothing")) && e.getFirmware().equalsIgnoreCase("UEFI") && !e.getHasVmDrsConfig() &&
                                            e.getHasVmHaConfig() && e.getBalloonedMemory() == 9 && !e.getHasEncryptedDisk() && e.getHasOpaqueNetwork() && e.getHasSriovNic() && e.getHasSharedVmdk())
        .count()).isEqualTo(1);

        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("db")).filter(e -> e.getHasPassthroughDevice() == null && e.getHasVmAffinityConfig() == null &&
                                            (e.getNumaNodeAffinity().equalsIgnoreCase("something")) && e.getFirmware()==null && e.getHasVmDrsConfig() &&
                                             !e.getHasVmHaConfig() && e.getBalloonedMemory() == 1 && e.getHasEncryptedDisk() && e.getHasOpaqueNetwork() ==null && !e.getHasSriovNic() && !e.getHasSharedVmdk())
        .count()).isEqualTo(1);

        assertThat(modelList.stream().filter(e -> e.getVmName().equalsIgnoreCase("oracle_db")).filter(e-> e.getHasPassthroughDevice() == null && e.getHasVmAffinityConfig()==null &&
                                                (e.getNumaNodeAffinity() == null) && e.getFirmware()==null && e.getHasVmDrsConfig() == null &&
                                                e.getHasVmHaConfig() == null && e.getBalloonedMemory() == null && e.getHasEncryptedDisk() == null && e.getHasOpaqueNetwork() == null && e.getHasOpaqueNetwork() == null)
        .count()).isEqualTo(1);
    }

}
