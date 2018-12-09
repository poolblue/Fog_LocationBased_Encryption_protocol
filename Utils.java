import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;

class Utils {
    //    server IP
    static final String ip_addr = "192.168.1.8";
    //    secret key length
    static final int keyLength = 16;
    //    Simulated geographic location variables E,N (UTM location format);
    static final int e = 7420;
    static final int n = 98265;
    static final int l = 100;

    static String getRandomString() {
        //定义一个字符串（A-Z，a-z，0-9）即62位；
        String str = "zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        //由Random生成随机数
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        //长度为几就循环几次
        for (int i = 0; i < 16; ++i) {
            //产生0-61的数字
            int number = random.nextInt(62);
            //将产生的数字通过length次承载到sb中
            sb.append(str.charAt(number));
        }
        //将承载的字符转换成字符串
        return sb.toString();
    }

    static byte[] generateGeoLock(int e, int n, int l) {
        LocalDateTime time = LocalDateTime.now();

        int year = time.getYear();
        int month = time.getMonthValue();
        int day = time.getDayOfMonth();
        int hour = time.getHour();

        time = LocalDateTime.of(year, month, day, hour, 0, 0, 0);
        long thisHour = LocalDateTime.from(time).toEpochSecond(ZoneOffset.of("+8"));

        System.out.println("Location E: " + e + ", Location N: " + n + ", Timestamp T: " + thisHour);

        String geoLock = Long.toString(thisHour) + e / l + n / l;

        System.out.println("Multiplex value: " + geoLock);

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e1) {
            System.out.println(e);
        }
        messageDigest.update(geoLock.getBytes());
        byte[] result = messageDigest.digest();

        System.out.println("Generating GeoLock (SHA1(Mux)): " + new String(result));

        return result;
    }

    static Key createKey(byte[] password) {
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(password);
            KeyGenerator keyGenerator;
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128, sr);
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] keyBytes = secretKey.getEncoded();
            Key key = new SecretKeySpec(keyBytes, "AES");
            return key;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    static byte[] encryptAES(byte[] result, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            result = cipher.doFinal(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static byte[] decryptAES(byte[] result, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            result = cipher.doFinal(result);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    static byte[] copyArray(byte a[], int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = a[i];
        }
        return result;
    }

}
