package org.jboss.xavier.analytics.pojo.input.workload.inventory;

import org.jboss.xavier.analytics.pojo.input.AbstractInputModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VMWorkloadInventoryModel extends AbstractInputModel implements Serializable {
    //common/name
    private String provider;
    //common/ems_clusters/v_parent_datacenter
    private String datacenter;
    //common/ems_clusters/name
    private String cluster;
    //vms/name
    private String vmName;
    //sum of vms/hardware/disks/size_on_disk
    private Long diskSpace;
    //vms/ram_size_in_bytes
    private Long memory;
    //vms/num_cpu
    private Integer cpuCores;
    //vms/operating_system/product_name
    private String osProductName;
    //hardware/guest_os_full_name
    private String guestOSFullName;
    //vms/has_rdm_disk
    private boolean hasRdmDisk;
    //count of nics object within the vms/hardware
    private Integer nicsCount;

    private String product;
    private String version;
    private String host_name;
    private String cpuAffinity;

    private Date scanRunDate;

    //hardware/disks/filename
    private Collection<String> vmDiskFilenames;
    private Collection<String> systemServicesNames;
    private Map<String,String> files;

    private Boolean hasMemoryHotAdd;
    private Boolean hasCpuHotAdd;
    private Boolean hasCpuHotRemove;
  
    private Boolean hasUSBcontrollers;
    private Boolean hasPassthroughDevice;
    private Boolean hasVmAffinityConfig;
    private String numaNodeAffinity;
    private String firmware;
    private Boolean hasVmDrsConfig;
    private Boolean hasVmHaConfig;
    private Integer balloonedMemory;
    private Boolean hasEncryptedDisk;
    private Boolean hasOpaqueNetwork;
    private Boolean hasSriovNic;
    private Boolean hasSharedVmdk;

    public VMWorkloadInventoryModel() 
    {
        this.systemServicesNames = new ArrayList<> ();
        this.files = new HashMap<>();
        this.vmDiskFilenames = new ArrayList<>();
        nicsCount = 0;
        diskSpace = 0L;
    }


    public Boolean getHasUSBcontrollers() {
        return hasUSBcontrollers;
    }

    public void setHasUSBcontrollers(Boolean hasUSBcontrollers) {
        this.hasUSBcontrollers = hasUSBcontrollers;
    }

    public Boolean getHasOpaqueNetwork() {
        return hasOpaqueNetwork;
    }

    public void setHasOpaqueNetwork(Boolean hasOpaqueNetwork) {
        this.hasOpaqueNetwork = hasOpaqueNetwork;
    }

    public Boolean getHasEncryptedDisk() {
        return hasEncryptedDisk;
    }

    public void setHasEncryptedDisk(Boolean hasEncryptedDisk) {
        this.hasEncryptedDisk = hasEncryptedDisk;
    }

    public Integer getBalloonedMemory() {
        return balloonedMemory;
    }

    public void setBalloonedMemory(Integer ballonedMemory) {
        this.balloonedMemory = ballonedMemory;
    }

    public Boolean getHasVmHaConfig() {
        return hasVmHaConfig;
    }

    public void setHasVmHaConfig(Boolean hasVmHaConfig) {
        this.hasVmHaConfig = hasVmHaConfig;
    }

    public Boolean getHasVmDrsConfig() {
        return hasVmDrsConfig;
    }

    public void setHasVmDrsConfig(Boolean hasVmDrsConfig) {
        this.hasVmDrsConfig = hasVmDrsConfig;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public String getNumaNodeAffinity() {
        return numaNodeAffinity;
    }

    public void setNumaNodeAffinity(String numaNodeAffinity) {
        this.numaNodeAffinity = numaNodeAffinity;
    }

    public Boolean getHasVmAffinityConfig() {
        return hasVmAffinityConfig;
    }

    public void setHasVmAffinityConfig(Boolean hasVmAffinityConfig) {
        this.hasVmAffinityConfig = hasVmAffinityConfig;
    }

    public Boolean getHasPassthroughDevice() {
        return hasPassthroughDevice;
    }

    public void setHasPassthroughDevice(Boolean hasPassthroughDevice) {
        this.hasPassthroughDevice = hasPassthroughDevice;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public Long getDiskSpace() {
        return diskSpace;
    }

    public void setDiskSpace(Long diskSpace) {
        this.diskSpace = diskSpace;
    }

    public void addDiskSpace(Long nextDiskSpace) {
        this.diskSpace += nextDiskSpace;
    }

    public Long getMemory() {
        return memory;
    }

    public void setMemory(Long memory) {
        this.memory = memory;
    }

    public Integer getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(Integer cpuCores) {
        this.cpuCores = cpuCores;
    }

    public String getOsProductName() {
        return osProductName;
    }

    public void setOsProductName(String osProductName) {
        this.osProductName = osProductName;
    }

    public String getGuestOSFullName() {
        return guestOSFullName;
    }

    public void setGuestOSFullName(String guestOSFullName) {
        this.guestOSFullName = guestOSFullName;
    }

    public boolean isHasRdmDisk() {
        return hasRdmDisk;
    }

    public void setHasRdmDisk(boolean hasRdmDisk) {
        this.hasRdmDisk = hasRdmDisk;
    }

    public Integer getNicsCount() {
        return nicsCount;
    }

    public void setNicsCount(Integer nicsCount) {
        this.nicsCount = nicsCount;
    }

    public void addNicsCount() {
        this.nicsCount++;
    }

    public Collection<String> getVmDiskFilenames() {
        return vmDiskFilenames;
    }

    public void setVmDiskFilenames(Collection<String> vmDiskFilenames) {
        this.vmDiskFilenames = vmDiskFilenames;
    }

    public void addVmDiskFilename(String vmDiskFilename) {
        this.vmDiskFilenames.add(vmDiskFilename);
    }

    public Collection<String> getSystemServicesNames() {
        return systemServicesNames;
    }

    public void setSystemServicesNames(Collection<String> systemServicesNames) {
        this.systemServicesNames = systemServicesNames;
    }

    public void addSystemService(String systemServiceName) {
        this.systemServicesNames.add(systemServiceName);
    }

    public Map<String, String> getFiles() {
        return files;
    }


    public void setFiles(Map<String, String> files) {
        this.files = files;
    }

    public void addFile(String name, String contents) {
        this.files.put(name,contents);
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHost_name() {
        return host_name;
    }

    public void setHost_name(String host_name) {
        this.host_name = host_name;
    }

    public Date getScanRunDate() {
        return scanRunDate;
    }

    public void setScanRunDate(Date scanRunDate) {
        this.scanRunDate = scanRunDate;
    }
  
    public Boolean getHasMemoryHotAdd() {
        return hasMemoryHotAdd;
    }

    public void setHasMemoryHotAdd(Boolean hasMemoryHotAdd) {
        this.hasMemoryHotAdd = hasMemoryHotAdd;
    }

    public Boolean getHasCpuHotAdd() {
        return hasCpuHotAdd;
    }

    public void setHasCpuHotAdd(Boolean hasCpuHotAdd) {
        this.hasCpuHotAdd = hasCpuHotAdd;
    }

    public Boolean getHasCpuHotRemove() {
        return hasCpuHotRemove;
    }

    public void setHasCpuHotRemove(Boolean hasCpuHotRemove) {
        this.hasCpuHotRemove = hasCpuHotRemove;
    }
  
    public String getCpuAffinity() {
        return cpuAffinity;
    }

    public void setCpuAffinity(String cpuAffinity) {
        this.cpuAffinity = cpuAffinity;
    }

    public Boolean getHasSriovNic() {
        return hasSriovNic;
    }

    public void setHasSriovNic(Boolean hasSriovNic) {
        this.hasSriovNic = hasSriovNic;
    }

    public Boolean getHasSharedVmdk() {
        return hasSharedVmdk;
    }

    public void setHasSharedVmdk(Boolean hasSharedVmdk) {
        this.hasSharedVmdk = hasSharedVmdk;
    }
}
