package ski.crunch.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JsonUtils {

    /**
     * Writes list of Jsonable objects to file*
     *
     * @param file File to write to
     * @param <E>  Dynamodbv2 mapper type that implements Jsonable
     * @throws IOException on ioerror
     */
    public static <E> void writeJsonToFile(E result, File file, boolean append) throws IOException {
        List<E> list = new ArrayList<>();
        list.add(result);
        writeJsonListToFile(list, file, append);
    }

    /**
     * Writes list of objects to file
     *
     * @param resultSet List<E> resultset to write
     * @param file      File to write to
     * @param <E>       Dynamodbv2 mapper type that implements Jsonable
     * @throws IOException on io error
     */
    public static <E> void writeJsonListToFile(List<E> resultSet, File file, boolean append) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        try (FileWriter fw = new FileWriter(file, append)) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(fw)) {
                writeJsonList(bufferedWriter, resultSet, objectMapper);
            }
        }
    }


//    /**
//     * Writes list of Jsonable objects to file*
//     *
//     * @param file File to write to
//     * @param <E>  Dynamodbv2 mapper type that implements Jsonable
//     * @throws IOException on ioerror
//     */
//    public static <E> void writeJsonToEncryptedFile(E result, File file, boolean append, String encryptionKey) throws IOException, GeneralSecurityException {
//        List<E> list = new ArrayList<>();
//        list.add(result);
//        writeJsonListToEncryptedFile(list, file, append, encryptionKey);
//    }
//
//    public static <E> void writeJsonListToEncryptedFile(List<E> resultSet, File file, boolean append, String encryptionKey) throws IOException, GeneralSecurityException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//
//        try( CryptoFileOutputStream cryptoFileOutputStream = new CryptoFileOutputStream(file, encryptionKey) ) {
//            writeJsonList();
//        }
////        try (FileWriter fw = new FileWriter(file, append)) {
////            try (EncryptedBufferedWriter bufferedWriter = new EncryptedBufferedWriter(fw, encryptionKey)) {
////                writeJsonList(bufferedWriter, resultSet, objectMapper);
////            }
////        }
//    }

    private static <E> void writeJsonList(BufferedWriter writer, List<E> result, ObjectMapper objectMapper) throws IOException {
        writer.write("[" + System.lineSeparator());
        String res = result.stream().map(x -> {
                    try {
                        return objectMapper.writeValueAsString(x) + System.lineSeparator();
                    } catch (JsonProcessingException ex) {
                        ex.printStackTrace();
                        return "";
                    }
                }
        ).collect(Collectors.joining(","));
        writer.write( res );
        writer.write( "]" + System.lineSeparator());
        writer.flush();

    }
}
