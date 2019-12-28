package ski.crunch.cloudformation.rockset.model;

import ski.crunch.cloudformation.ResourceProperties;
import ski.crunch.utils.MissingRequiredParameterException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ski.crunch.cloudformation.rockset.model.RocksetIntegrationResourceProperties.DEFAULT_ROCKSET_API_SERVER;

public class RocksetCollectionResourceProperties extends ResourceProperties {

    private String name;
    private String workspace;
    private String region;
    private String apiKeySSM;
    private String integrationName;
    private String stage;
    private String apiServer;
    private String description;
    private List<DataSource> dataSources;
    private Optional<String> eventTimeField;
    private Optional<String> eventTimeZone;
    private Optional<String> eventTimeFormat;
    private Optional<Long> retentionTime;
    private Optional<List<FieldMapping>> fieldMappingList;

    public RocksetCollectionResourceProperties(Map<String, Object> input){

        List<String> requiredParameters = Stream.of(
                "Name",
                "Workspace",
                "ApiKeySSM",
                "Region",
                "IntegrationName",
                "Stage"
        ).collect(Collectors.toList());

        checkRequiredParameters(input, requiredParameters);
        try {
            this.name = (String) input.get("Name");
            this.description = (String) input.getOrDefault("Description","");
            this.workspace = (String) input.getOrDefault("Workspace", "commons");
            this.region = (String) input.get("Region");
            this.apiKeySSM = (String) input.get("ApiKeySSM");
            this.integrationName = (String) input.get("IntegrationName");
            this.stage = (String) input.get("Stage");
            this.apiServer = (String) input.getOrDefault("ApiServer", DEFAULT_ROCKSET_API_SERVER);
            this.eventTimeField =  input.containsKey("EventTimeField") ? Optional.of((String)input.get("EventTimeField")): Optional.empty();
            this.eventTimeZone =  input.containsKey("EventTimeZone") ? Optional.of((String)input.get("EventTimeZone")): Optional.empty();
            this.eventTimeFormat =  input.containsKey("EventTimeFormat") ? Optional.of((String)input.get("EventTimeFormat")): Optional.empty();
            this.retentionTime = input.containsKey("RetentionTime") ? Optional.of(((Integer)input.get("RetentionTime")).longValue()) : Optional.empty();
            this.dataSources = new ArrayList<>();
            for (RocksetIntegrationType integrationType : RocksetIntegrationType.values()) {
                if(input.containsKey(integrationType.getDataSourceName())) {
                    DataSource ds = DataSourceFactory.getInstance(integrationType);
                    ds.parse((Map<String, Object>)input.get(integrationType.getDataSourceName()));
                    dataSources.add(ds);
                }
            }
//            this.dynamoDbDataSource = (input.containsKey("DynamoDbDataSource") ?
//                    Optional.of(new DynamoDbDataSource((Map<String, Object> ) input.get("DynamoDbDataSource"))) :
//                    Optional.empty());
//
//
//            this.s3DataSource = (input.containsKey("S3DataSource") ?
//                    Optional.of(new S3DataSource((Map<String, Object>) input.get("S3DataSource"))) :
//                    Optional.empty());
//
//            this.kinesisDataSource = (input.containsKey("KinesisDataSource") ?
//                    Optional.of(new KinesisDataSource((Map<String, Object>) input.get("KinesisDataSource"))):
//                    Optional.empty());

            this.fieldMappingList = ( input.containsKey("FieldMappings") ?
                    Optional.of(FieldMapping.parse((List) input.get("FieldMappings"))) :
                    Optional.empty());

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new MissingRequiredParameterException("Parameter missing in resourceProperties", ex);
        }

    }


    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }


    public String getWorkspace() {
        return workspace;
    }

    public String getRegion() {
        return region;
    }

    public String getApiKeySSM() {
        return apiKeySSM;
    }

    public String getIntegrationName() {
        return integrationName;
    }

    public String getStage() {
        return stage;
    }

    public Optional<String> getEventTimeField() {
        return eventTimeField;
    }

    public Optional<String> getEventTimeZone() {
        return eventTimeZone;
    }

    public Optional<String> getEventTimeFormat() {
        return eventTimeFormat;
    }

    public Optional<Long> getRetentionTime() {
        return retentionTime;
    }

    public void setRetentionTime(Optional<Long> retentionTime) {
        this.retentionTime = retentionTime;
    }

    public Optional<List<FieldMapping>> getFieldMappingList() {
        return fieldMappingList;
    }

    public void setFieldMappingList(Optional<List<FieldMapping>> fieldMappingList) {
        this.fieldMappingList = fieldMappingList;
    }

    public String getApiServer() {
        return apiServer;
    }

    public List<DataSource> getDataSources() {
        return dataSources;
    }
}
