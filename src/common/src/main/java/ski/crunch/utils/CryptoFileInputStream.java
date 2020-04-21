package ski.crunch.utils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;

/**
 * Decrypt from file into memory
 */
public class CryptoFileInputStream extends InputStream {

    private static String salt = "ssshhhhhhhhhhh!!!!";
    private static byte[] iv = {8, -36, 70, 20, -80, -80, 116, 41, -46, -50, 59, 98, 8, 73, -27, 80};

    //private InputStream  base64;
    private CipherInputStream cipherInputStream;
    private FileInputStream fileInputStream;
    private final Cipher cipher;


    public CryptoFileInputStream(File input, String secret) throws FileNotFoundException, GeneralSecurityException {

            fileInputStream = new FileInputStream(input);
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            cipherInputStream = new CipherInputStream(fileInputStream, cipher);
            //base64 = Base64.getDecoder().wrap(cipherInputStream);
    }


    @Override
    public int read() throws IOException {
        return cipherInputStream.read();
    }

    @Override
    public void close() throws IOException {
        cipherInputStream.close();
    }


}
