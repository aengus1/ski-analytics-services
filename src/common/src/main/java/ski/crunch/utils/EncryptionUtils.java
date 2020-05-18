package ski.crunch.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class EncryptionUtils {

    //    private static String secretKey = "boooooooooom!!!!";
    private static String salt = "ssshhhhhhhhhhh!!!!";
    private static byte[] iv = {8, -36, 70, 20, -80, -80, 116, 41, -46, -50, 59, 98, 8, 73, -27, 80};

    public static String encrypt(byte[] bytesToEncrypt, String secret) {
        try {
            // byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(bytesToEncrypt));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String encrypt(String strToEncrypt, String secret) {
        return encrypt(strToEncrypt.getBytes(StandardCharsets.UTF_8), secret);
    }

    public static String decrypt(byte[] bytesToDecrypt, String secret) {

        try {

            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(bytesToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String secret) {
        return decrypt(strToDecrypt.getBytes(StandardCharsets.UTF_8), secret);
    }

    public static void copyEncrypt(File sourceFile, File destFile, String secretKey) throws IOException, GeneralSecurityException {
        try (FileReader fileReader = new FileReader(sourceFile)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                try (CryptoFileOutputStream cryptoFileOutputStream = new CryptoFileOutputStream(destFile, secretKey)) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        cryptoFileOutputStream.write(line.getBytes(StandardCharsets.UTF_8));
                    }
                    cryptoFileOutputStream.flush();
                }
            }
        }
    }

    public static void copyDecrypt(File sourceFile, File destFile, String secretKey) throws IOException, GeneralSecurityException {


        try (CryptoFileInputStream cryptoFileInputStream = new CryptoFileInputStream(sourceFile, secretKey)) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(cryptoFileInputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                    try (FileWriter fileWriter = new FileWriter(destFile)) {
                        try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                bufferedWriter.write(line);
                            }
                        }
                    }
                }
            }
        }
    }
}