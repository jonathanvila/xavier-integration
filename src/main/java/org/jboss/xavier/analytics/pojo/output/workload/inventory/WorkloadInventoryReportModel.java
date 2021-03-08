package org.jboss.xavier.analytics.pojo.output.workload.inventory;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.apache.camel.dataformat.bindy.annotation.FormatFactories;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.jboss.xavier.analytics.pojo.BindyStringSetFormatFactory;
import org.jboss.xavier.analytics.pojo.output.AnalysisModel;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@FormatFactories({BindyStringSetFormatFactory.class})
@CsvRecord(separator = ",", crlf = "UNIX", generateHeaderColumns = true)
@Entity
@Transactional
@Table(
        indexes = {
                @Index(name = "WorkloadInventoryReportModel_" +
                        WorkloadInventoryReportModel.ANALYSIS_ID_COLUMN + "_index",
                        columnList = WorkloadInventoryReportModel.ANALYSIS_ID_COLUMN, unique = false),
                @Index(name = "WorkloadInventoryReportModel_" +
                        WorkloadInventoryReportModel.VM_NAME_COLUMN + "_index",
                        columnList = WorkloadInventoryReportModel.VM_NAME_COLUMN, unique = false),
                @Index(name = "WorkloadInventoryReportModel_" +
                        WorkloadInventoryReportModel.OS_NAME_COLUMN + "_index",
                        columnList = WorkloadInventoryReportModel.OS_NAME_COLUMN, unique = false),
                @Index(name = "WorkloadInventoryReportModel_" +
                        WorkloadInventoryReportModel.COMPLEXITY_COLUMN + "_index",
                        columnList = WorkloadInventoryReportModel.COMPLEXITY_COLUMN, unique = false)
        }
)
public class WorkloadInventoryReportModel
{
    static final long serialVersionUID = 1L;

    static final String ANALYSIS_ID_COLUMN = "analysis_id";
    static final String VM_NAME_COLUMN = "vm_name";
    static final String OS_NAME_COLUMN = "os_name";
    static final String COMPLEXITY_COLUMN = "complexity";

    public static final String PROVIDER_FIELD = "provider";
    public static final String DATACENTER_FIELD = "datacenter";
    public static final String CLUSTER_FIELD = "cluster";
    public static final String VM_NAME_FIELD = "vmName";
    public static final String OS_NAME_FIELD = "osName";
    public static final String OS_DESCRIPTION_FIELD = "osDescription";
    public static final String DISK_SPACE_FIELD = "diskSpace";
    public static final String MEMORY_FIELD = "memory";
    public static final String CPU_CORES_FIELD = "cpuCores";
    public static final String COMPLEXITY_NAME_FIELD = "complexity";
    public static final String PRODUCT_FIELD = "product";
    public static final String VERSION_FIELD = "version";
    public static final String CREATION_DATE_FIELD = "creationDate";
    public static final String SSA_ENABLED_FIELD = "ssaEnabled";
    public static final String INSIGHTS_ENABLED_FIELD = "insightsEnabled";
    public static final String OS_FAMILY_FIELD = "osFamily";

    public static final String[] SUPPORTED_SORT_FIELDS = {
            PROVIDER_FIELD,
            DATACENTER_FIELD,
            CLUSTER_FIELD,
            VM_NAME_FIELD,
            OS_NAME_FIELD,
            OS_DESCRIPTION_FIELD,
            DISK_SPACE_FIELD,
            MEMORY_FIELD,
            CPU_CORES_FIELD,
            COMPLEXITY_NAME_FIELD,
            PRODUCT_FIELD,
            VERSION_FIELD,
            CREATION_DATE_FIELD,
            SSA_ENABLED_FIELD,
            INSIGHTS_ENABLED_FIELD,
            OS_FAMILY_FIELD
    };

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO, generator = "WORKLOADINVENTORYREPORTMODEL_ID_GENERATOR")
    @GenericGenerator(
            name = "WORKLOADINVENTORYREPORTMODEL_ID_GENERATOR",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "WORKLOADINVENTORYREPORT_SEQUENCE")
            }
    )
    private Long id;

    @XStreamOmitField
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ANALYSIS_ID_COLUMN)
    @JsonBackReference
    private AnalysisModel analysis;

    @DataField(pos = 1, columnName = "Provider")
    private String provider;

    @DataField(pos = 2, columnName = "Datacenter")
    private String datacenter;

    @DataField(pos = 3, columnName = "Cluster")
    private String cluster;

    @DataField(pos = 4 , columnName = "VM name")
    @Column(name = VM_NAME_COLUMN)
    private String vmName;

    @DataField(pos = 5, columnName = "OS type")
    @Column(name = OS_NAME_COLUMN)
    private String osName;

    @DataField(pos = 6, columnName = "Operating system description")
    private String osDescription;

    @DataField(pos = 7, precision = 2, columnName = "Disk space")
    private Long diskSpace;

    @DataField(pos = 8, columnName = "Memory")
    private Long memory;

    @DataField(pos = 9, columnName = "CPU cores")
    private Integer cpuCores;

    @DataField(pos = 10, columnName = "Workload")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "workload_inventory_report_model_workloads",
            indexes = {
                    @Index(columnList = "workload_inventory_report_model_id", unique = false)
            }
    )
    private Set<String> workloads;

    @DataField(pos = 11, columnName = "Effort")
    @Column(name = COMPLEXITY_COLUMN)
    private String complexity;

    @DataField(pos = 12, columnName = "Recommended targets")
    // with "IMS" suffix in case one day we will have
    // their "AMM" counterparts
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "workload_inventory_report_model_recommended_targetsims",
            indexes = {
                    @Index(columnList = "workload_inventory_report_model_id", unique = false)
            }
    )
    private Set<String> recommendedTargetsIMS;

    @DataField(pos = 13, columnName = "Flags IMS")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "workload_inventory_report_model_flagsims",
            indexes = {
                    @Index(columnList = "workload_inventory_report_model_id", unique = false)
            }
    )
    private Set<String> flagsIMS;

    @DataField(pos = 14, columnName = "Product")
    private String product;

    @DataField(pos = 15, columnName = "Version")
    private String version;

    @DataField(pos = 16, columnName = "HostName")
    private String host_name;

    private Date creationDate; //TODO to be consistent with input bean, refactor to scanRunDate

    private Boolean ssaEnabled;

    private Boolean insightsEnabled;

    private String osFamily;

    private String vmCategory;

    public WorkloadInventoryReportModel() {}

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AnalysisModel getAnalysis() {
        return analysis;
    }

    public void setAnalysis(AnalysisModel analysis) {
        this.analysis = analysis;
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

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsDescription() {
        return osDescription;
    }

    public void setOsDescription(String osDescription) {
        this.osDescription = osDescription;
    }

    public Long getDiskSpace() {
        return diskSpace;
    }

    public void setDiskSpace(Long diskSpace) {
        this.diskSpace = diskSpace;
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

    public Set<String> getWorkloads() {
        return workloads;
    }

    public void setWorkloads(Set<String> workloads) {
        this.workloads = workloads;
    }

    public void addWorkload(String workload)
    {
        if (this.workloads == null) this.workloads = new HashSet<>();
        this.workloads.add(workload);
    }

    public String getComplexity() {
        return complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    public Set<String> getRecommendedTargetsIMS() {
        return recommendedTargetsIMS;
    }

    public void setRecommendedTargetsIMS(Set<String> recommendedTargetsIMS) {
        this.recommendedTargetsIMS = recommendedTargetsIMS;
    }

    public void addRecommendedTargetsIMS(String recommendedTargetIMS)
    {
        if (this.recommendedTargetsIMS == null) this.recommendedTargetsIMS = new HashSet<>();
        this.recommendedTargetsIMS.add(recommendedTargetIMS);
    }

    public Set<String> getFlagsIMS() {
        return flagsIMS;
    }

    public void setFlagsIMS(Set<String> flagsIMS) {
        this.flagsIMS = flagsIMS;
    }

    public void addFlagIMS(String flagIMS)
    {
        if (this.flagsIMS == null) this.flagsIMS = new HashSet<>();
        this.flagsIMS.add(flagIMS);
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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

    public Boolean getSsaEnabled() {
        return ssaEnabled;
    }

    public void setSsaEnabled(Boolean ssaEnabled) {
        this.ssaEnabled = ssaEnabled;
    }

    public Boolean getInsightsEnabled() {
        return insightsEnabled;
    }

    public void setInsightsEnabled(Boolean insightsEnabled) {
        this.insightsEnabled = insightsEnabled;
    }

    public String getOsFamily() {
        return osFamily;
    }

    public void setOsFamily(String osFamily) {
        this.osFamily = osFamily;
    }

    public String getVmCategory() {
        return vmCategory;
    }

    public void setVmCategory(String vmCategory) {
        this.vmCategory = vmCategory;
    }
}
