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
                bufferedWriter.write("[" + System.lineSeparator());
                String res = resultSet.stream().map(x -> {
                            try {
                                return objectMapper.writeValueAsString(x) + System.lineSeparator();
                            } catch (JsonProcessingException ex) {
                                ex.printStackTrace();
                                return "";
                            }
                        }
                ).collect(Collectors.joining(","));
                bufferedWriter.write(res);
                bufferedWriter.write("]" + System.lineSeparator());
                bufferedWriter.flush();
            }
        }
    }
}
