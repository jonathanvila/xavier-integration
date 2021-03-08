package org.jboss.xavier.integrations.migrationanalytics.business;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jboss.xavier.analytics.pojo.input.workload.inventory.VMWorkloadInventoryModel;
import org.jboss.xavier.integrations.route.RouteBuilderExceptionHandler;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class VMWorkloadInventoryCalculator extends AbstractVMWorkloadInventoryCalculator implements Calculator<Collection<VMWorkloadInventoryModel>> {
    private static final String NULL_VALUE_TEXT = "Setting value to null.";
    @Override
    public Collection<VMWorkloadInventoryModel> calculate(String cloudFormsJson, Map<String, Object> headers) {

        manifestVersion = getManifestVersion(cloudFormsJson);
        jsonParsed = JsonPath.parse(cloudFormsJson);
        scanRunDate = getScanRunDate();

        List<Map> vmList = readListValuesFromExpandedEnvVarPath(VMPATH, null);

        List<VMWorkloadInventoryModel> vmWorkloadInventoryModels = vmList.stream()
                .peek(e -> {
                    e.put("_analysisId", headers.get(RouteBuilderExceptionHandler.ANALYSIS_ID).toString());
                    e.put("vmEmsCluster", readValueFromExpandedEnvVarPath(VMEMSCLUSTERPATH, e));
                    e.put("ems_cluster_id", readValueFromExpandedEnvVarPath(EMSCLUSTERIDPATH, e));
                })
                .peek(e -> {
                    if (log.isDebugEnabled()) {
                        log.debug("------- Treating Analysis {} VM :{} from {} : ", headers.get(RouteBuilderExceptionHandler.ANALYSIS_ID).toString(), vmList.indexOf(e), vmList.size());
                    }
                })
                .map(this::createVMWorkloadInventoryModel)
                .collect(Collectors.toList());
        log.info(" Instance AnalysisID {} VMs parsed {} vs VMs calculated {}", headers.get(RouteBuilderExceptionHandler.ANALYSIS_ID).toString(), vmList.size(), vmWorkloadInventoryModels.size());

        return vmWorkloadInventoryModels;
    }

    private Date getScanRunDate() {
        Date scanrundate;
        try {
            scanrundate = new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse(readValueFromExpandedEnvVarPath(DATACOLLECTEDON, null));
        } catch (Exception e) {
            scanrundate = new Date();
            log.warn("Using now date as fallback for Scan Run Date");
        }
        return scanrundate;
    }

    private VMWorkloadInventoryModel createVMWorkloadInventoryModel(Map vmStructMap) {
        VMWorkloadInventoryModel model = new VMWorkloadInventoryModel();
        model.setProvider(readValueFromExpandedEnvVarPath(PROVIDERPATH, vmStructMap));

        model.setDatacenter(readValueFromExpandedEnvVarPath(DATACENTERPATH, vmStructMap));

        model.setCluster(readValueFromExpandedEnvVarPath(CLUSTERPATH, vmStructMap));

        model.setVmName(readValueFromExpandedEnvVarPath(VMNAMEPATH, vmStructMap ));
        model.setMemory(readValueFromExpandedEnvVarPath(RAMSIZEINBYTES, vmStructMap, Long.class));

        Integer numCPU = readValueFromExpandedEnvVarPath(NUMCPUPATH, vmStructMap, Integer.class);
        Integer numCORES = readValueFromExpandedEnvVarPath(NUMCORESPERSOCKETPATH, vmStructMap, Integer.class);
        if (numCPU != null && numCORES != null) {
            model.setCpuCores(numCORES > 0 ? (numCPU / numCORES) : 0);
        } else {
            analysisIssuesHandler.record(vmStructMap.get("_analysisId").toString(), "VM", vmStructMap.get("name").toString(), getExpandedPath(NUMCORESPERSOCKETPATH, vmStructMap), "CpuCores could not be calculated.");
        }

        model.setOsProductName(StringUtils.defaultIfEmpty(readValueFromExpandedEnvVarPath(PRODUCTNAMEPATH, vmStructMap), readValueFromExpandedEnvVarPath(PRODUCTNAME_FALLBACKPATH, vmStructMap )));
        model.setGuestOSFullName(StringUtils.defaultIfEmpty(readValueFromExpandedEnvVarPath(GUESTOSFULLNAMEPATH, vmStructMap ), readValueFromExpandedEnvVarPath(GUESTOSFULLNAME_FALLBACKPATH, vmStructMap )));
        Boolean hasRdmDisk = readValueFromExpandedEnvVarPath(HASRDMDISKPATH, vmStructMap);
        if (hasRdmDisk != null) {
            model.setHasRdmDisk(hasRdmDisk);
        }
        Object cpuHotAddObject = getValueForExpandedPathAndHandlePathNotPresent(CPUHOTADDENABLEDPATH, vmStructMap, "Setting value to null.");
        model.setHasCpuHotAdd(cpuHotAddObject != null? (Boolean)cpuHotAddObject: null);
        Object memoryHotAddObject = getValueForExpandedPathAndHandlePathNotPresent(MEMORYHOTADDENABLEDPATH, vmStructMap, "Setting value to null.");
        model.setHasMemoryHotAdd(memoryHotAddObject != null? (Boolean)memoryHotAddObject: null);
        Object cpuHotRemoveObject = getValueForExpandedPathAndHandlePathNotPresent(CPUHOTREMOVEENABLEDPATH, vmStructMap, "Setting value to null.");
        model.setHasCpuHotRemove(cpuHotRemoveObject != null? (Boolean)cpuHotRemoveObject: null);

        Object cpuAffinity = getValueForExpandedPathAndHandlePathNotPresent(CPUAFFINITYPATH, vmStructMap, "Setting value to null.");
        model.setCpuAffinity(cpuAffinity != null && !((String)cpuAffinity).isEmpty()? (String)cpuAffinity: null);
        model.setHasUSBcontrollers((Boolean) getValueForExpandedPathAndHandlePathNotPresent(USBCONTROLLERS, vmStructMap, NULL_VALUE_TEXT));

        model.setDiskSpace(getDiskSpaceList(vmStructMap));

        model.setNicsCount(readValueFromExpandedEnvVarPath(NICSPATH, vmStructMap, Integer.class));

        model.setFiles(readMapValuesFromExpandedEnvVarPath(FILESCONTENTPATH, vmStructMap, getExpandedPath(FILESCONTENTPATH_FILENAME, vmStructMap), getExpandedPath(FILESCONTENTPATH_CONTENTS, vmStructMap)));
        model.setSystemServicesNames(readListValuesFromExpandedEnvVarPath(SYSTEMSERVICESNAMESPATH, vmStructMap));
        model.setVmDiskFilenames(readListValuesFromExpandedEnvVarPath(VMDISKSFILENAMESPATH, vmStructMap));

        model.setProduct(readValueFromExpandedEnvVarPath(PRODUCTPATH, vmStructMap));
        model.setVersion(readValueFromExpandedEnvVarPath(VERSIONPATH, vmStructMap));
        model.setHost_name(readValueFromExpandedEnvVarPath(HOSTNAMEPATH, vmStructMap));

        model.setScanRunDate(scanRunDate);

        model.setHasPassthroughDevice(getValueForBooleanFieldHandlingPathNotPresent(HASPASSTHROUGHDEVICEPATH, vmStructMap, "Setting value to null."));
        model.setHasVmAffinityConfig(getValueForBooleanFieldHandlingPathNotPresent(HASVMAFFINITYCONFIG, vmStructMap, "Setting value to null."));
        model.setHasVmDrsConfig(getValueForBooleanFieldHandlingPathNotPresent(HASVMDRSCONFIG, vmStructMap, "Setting value to null."));
        model.setHasVmHaConfig(getValueForBooleanFieldHandlingPathNotPresent(HASVMHACONFIG, vmStructMap, "Setting value to null."));
        model.setHasEncryptedDisk(getValueForBooleanFieldHandlingPathNotPresent(HASENCRYPTEDDISK, vmStructMap, "Setting value to null."));
        model.setHasOpaqueNetwork(getValueForBooleanFieldHandlingPathNotPresent(HASOPAQUENETWORK, vmStructMap, "Setting value to null."));
        model.setHasSriovNic(getValueForBooleanFieldHandlingPathNotPresent(HASSRIOVNIC, vmStructMap, "Setting value to null."));

        model.setNumaNodeAffinity(getValueForExpandedPathAndHandlePathNotPresent(NUMANODEAFFINITY, vmStructMap, "Setting value to null."));
        model.setFirmware(getValueForExpandedPathAndHandlePathNotPresent(FIRMWARE, vmStructMap, "Setting value to null."));
        model.setBalloonedMemory(getValueForExpandedPathAndHandlePathNotPresent(BALLOONEDMEMORY, vmStructMap, "Setting value to null."));
        model.setHasSharedVmdk(getValueForBooleanFieldHandlingPathNotPresent(HASSHAREDVMDK, vmStructMap, "Setting value to null."));

        model.setAnalysisId(Long.parseLong(vmStructMap.get("_analysisId").toString()));


        return model;
    }

    private Boolean getValueForBooleanFieldHandlingPathNotPresent(String path, Map vmStructMap, String errorMessage) {

        Object value = getValueForExpandedPathAndHandlePathNotPresent(path, vmStructMap, errorMessage);
        return (value != null ? Boolean.valueOf(value.toString()) : null);
    }

    private <T> T getValueForExpandedPathAndHandlePathNotPresent(String path, Map vmStructMap, String errorMessage)
    {
        try {
            String foundPath = getExpandedPath(path, vmStructMap);
            Object returnValue = vmStructMap.get(foundPath);
            if (returnValue!= null)
                return (T) returnValue;

        } catch (Exception e) {
            // In versions previous to 1_0_0 it will fail because there is no such property
            log.warn("Using an old version of payload. " + errorMessage);
        }
        return null;
    }

    private Long getDiskSpaceList(Map vmStructMap) {
        // If the VM.used_disk_storage is present use it, if not use VM.DISK[*].size_on_disk

        Object used_disk_storage = getValueForExpandedPathAndHandlePathNotPresent(USEDDISKSTORAGEPATH, vmStructMap,
                "Calculating size with sum of vm.hardware.disks.size_on_disk");
        if (used_disk_storage != null) {
            return ((Number) used_disk_storage).longValue();
        }

        List<Number> hardwareDisksList = readListValuesFromExpandedEnvVarPath(DISKSIZEPATH, vmStructMap);
        return hardwareDisksList.stream().filter(Objects::nonNull).mapToLong(Number::longValue).sum();
    }
}
