package ski.crunch.utils;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;

public class CryptoFileOutputStream extends OutputStream {

    private static String salt = "ssshhhhhhhhhhh!!!!";
    private static byte[] iv = {8, -36, 70, 20, -80, -80, 116, 41, -46, -50, 59, 98, 8, 73, -27, 80};

    //private OutputStream base64;
    private CipherOutputStream cipherOutputStream;
    private FileOutputStream fileOutputStream;
    private final Cipher cipher;

    public CryptoFileOutputStream(File output, String secret) throws FileNotFoundException, GeneralSecurityException {
        fileOutputStream = new FileOutputStream(output);
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
        cipherOutputStream = new CipherOutputStream(fileOutputStream, cipher);
        //base64 = Base64.getEncoder().wrap(cipherOutputStream);
    }

    @Override
    public void write(int b) throws IOException {
         cipherOutputStream.write(b);
    }

    @Override
    public void flush() throws IOException {
        //fileOutputStream.flush();
        cipherOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        //fileOutputStream.close();
        cipherOutputStream.close();
    }
}
