package ski.crunch.auth.utils;

import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.SodiumJava;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.interfaces.PwHash;
import com.sun.jna.NativeLong;

public class PasswordUtil {

    public static final long MEM_LIMIT = 131072;
    public static final long OPS_LIMIT = 20L;
    public static String hashPassword(String password) {
        try {
            LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());
            PwHash.Lazy pwHashLazy = (PwHash.Lazy) lazySodium;
            return pwHashLazy.cryptoPwHashStr(password, OPS_LIMIT, new NativeLong(MEM_LIMIT));
        } catch (SodiumException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean verifyPassword(String hash, String password) {
            LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());
            PwHash.Lazy pwHashLazy = (PwHash.Lazy) lazySodium;
            return pwHashLazy.cryptoPwHashStrVerify(hash, password);
    }
}
