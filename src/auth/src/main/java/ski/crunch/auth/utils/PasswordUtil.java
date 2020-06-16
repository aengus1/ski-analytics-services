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

    public static void main(String[] args) {
        System.out.println(
        PasswordUtil.verifyPassword("246172676F6E32696424763D3139246D3D3132382C743D32302C703D312474587932787061463345597934656A593554586D3241247A6678377253637832464F3855474B75654A744F3776517956784F766C514C4E37726B72673375616A75670000000000000000000000000000000000000000000000000000000000000000", "authTestPassword123" )
        );
    }
}
