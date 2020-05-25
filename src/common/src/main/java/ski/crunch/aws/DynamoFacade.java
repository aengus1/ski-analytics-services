package ski.crunch.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.dao.SerialScanner;
import ski.crunch.utils.CryptoFileOutputStream;
import ski.crunch.utils.Jsonable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by aengusmccullough on 2018-09-19.
 */
public class DynamoFacade {

    private AmazonDynamoDB client;
    private DynamoDB dynamo;
    private DynamoDBMapper mapper;
    private DynamoDBMapperConfig config;
    private AWSCredentialsProvider credentialsProvider;
    private static final Logger logger = LoggerFactory.getLogger(DynamoFacade.class);

    public DynamoFacade(String region, String tableName) {
        this.client = AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
        config = new DynamoDBMapperConfig.Builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .build();
        this.dynamo = new DynamoDB(client);
        this.mapper = new DynamoDBMapper(client, config);

    }

    public DynamoFacade(String region, String tableName, DynamoDBMapperConfig.SaveBehavior saveBehavior) {
        this.client = AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
        config = new DynamoDBMapperConfig.Builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .withSaveBehavior(saveBehavior)
                .build();
        this.dynamo = new DynamoDB(client);
        this.mapper = new DynamoDBMapper(client, config);

    }


    /**
     * Use this constructor if you need to use the S3link
     *
     * @param region              String aws region
     * @param tableName           String name of table
     * @param credentialsProvider AWSCredentialsProvider
     */
    public DynamoFacade(String region, String tableName, AWSCredentialsProvider credentialsProvider) {
        this.client = AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
        config = new DynamoDBMapperConfig.Builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .build();

        this.mapper = new DynamoDBMapper(client, config, credentialsProvider);
        this.dynamo = new DynamoDB(client);
        this.credentialsProvider = credentialsProvider;
    }

    public DynamoFacade(String region, String tableName, AWSCredentialsProvider credentialsProvider, DynamoDBMapperConfig.SaveBehavior saveBehavior) {
        this.client = AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
        config = new DynamoDBMapperConfig.Builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .withSaveBehavior(saveBehavior)
                .build();

        this.mapper = new DynamoDBMapper(client, config, credentialsProvider);
        this.dynamo = new DynamoDB(client);
        this.credentialsProvider = credentialsProvider;
    }

    public void updateTableName(String tableName) {
        DynamoDBMapperConfig.SaveBehavior saveBehavior = config.getSaveBehavior();
        config = new DynamoDBMapperConfig.Builder().withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .withSaveBehavior(saveBehavior)
                .build();
        this.mapper = credentialsProvider != null ? new DynamoDBMapper(client, config, credentialsProvider) : new DynamoDBMapper(client, config);
    }


    public DynamoDBMapper getMapper() {
        return this.mapper;
    }

    public DynamoDB getClient() {
        return dynamo;
    }

    public Table getTable(String tableName) {
        return this.dynamo.getTable(tableName);
    }

    /**
     * Performs a full backup of specified table and copies results to local fs
     *
     * @param tableName   String name of table to backup
     * @param destination File destination directory
     * @param fileName    String name of output file
     * @throws IOException on ioerror
     */
    public void fullTableBackup(Class mapperClass, String tableName, File destination, String fileName, String encryptionKey) throws IOException, GeneralSecurityException {

        // stream all results into a single temporary file
        File output = new File(destination, fileName);
        //File tempOutput = new File(System.getProperty("java.io.tmpdir"), fileName+"tmp");
        Function<Stream<Jsonable>, Void> fileWriter = items -> {
            try (FileWriter fw = new FileWriter(output, true)) {
                try (BufferedWriter bufferedWriter = new BufferedWriter(fw)) {
                    bufferedWriter.write("[" + System.lineSeparator());
                    String res = items.map(x -> {
                        try {
                            return x.toJsonString() + System.lineSeparator();
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }).collect(Collectors.joining(","));

                    bufferedWriter.write(res);
                    bufferedWriter.write(System.lineSeparator() + "]");
                    bufferedWriter.flush();
                }
            } catch (IOException ex) {
                logger.error("IO Exception", ex);
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                return null;
            }
        };

        Function<Stream<Jsonable>, Void> encryptedFileWriter = items -> {
            try (CryptoFileOutputStream fw = new CryptoFileOutputStream(output, encryptionKey)) {
                fw.write(("[" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
                String res = items.map(x -> {
                    try {
                        return x.toJsonString() + System.lineSeparator();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).collect(Collectors.joining(","));

                fw.write(res.getBytes(StandardCharsets.UTF_8));
                fw.write((System.lineSeparator() + "]").getBytes(StandardCharsets.UTF_8));
                fw.flush();

            } catch (IOException ex) {
                logger.error("IO Exception", ex);
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                return null;
            }
        };

        SerialScanner.scan(this, encryptionKey == null ? fileWriter : encryptedFileWriter, tableName, mapperClass);
    }


    public DynamoDBMapperConfig getConfig() {
        return this.config;
    }

    public boolean tableExists(String tableName) {
        try {
            client.describeTable(tableName);
            return true;
        } catch (ResourceNotFoundException ex) {
            return false;
        }
    }
}
