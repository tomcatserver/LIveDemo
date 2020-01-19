package com.example.livedemo.hotfix;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * com.yixia.live.hotfix.PatchUtil
 *
 * @author Just.T
 * @since 2018/7/23
 */
public class PatchUtil {
    private static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2QiNID1YQYoGS8rBhcnj" +
                    "aroag2/mN9vGQiydQTJuMgAi04nP2NTcrnQwN0+P7xIMY7uhWDknW1KbOwYhi1cM" +
                    "g57gXvzHjG8lwUnzdQy0sLVU2OFROMLZxMtuGLZ4sbypkjoMRp+c2V+D3VhsRZHe" +
                    "o93jdZd1fo7nFVZmdaRuV7BaMy4aSn4NxvPKtSlKbgvvY1Yk3LxdK2J9++a+vRRR" +
                    "rr6tKuVqiY+gL//FLsK002mMe7yp3JHKtQCk02NgNE+1w3p0RUh1fwduGBLuYfeN" +
                    "8p92r4zp593K8B/l8L5t+CLjZaMZoWwZ0h3wyW3YZpZ3dkK2SkH+wEwKsyw4DvCn" +
                    "wwIDAQAB";
    private static final String KEY_ALGORITHM = "RSA";

    /**
     * 文件MD5
     */
    public static String MD5(File file) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            return bufferToHex(md5.digest());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";

    }

    public static boolean verify(String data, String sign) throws Exception {
        KeyFactory keyf = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicKey = keyf.generatePublic(new X509EncodedKeySpec(Base64.decode(PUBLIC_KEY, Base64.DEFAULT)));
        java.security.Signature signet = java.security.Signature.getInstance("SHA1withRSA");
        signet.initVerify(publicKey);
        signet.update(data.getBytes());
        return signet.verify(Base64.decode(sign, Base64.DEFAULT));
    }

    private static String bufferToHex(byte bytes[]) {
        int n = bytes.length;
        StringBuilder sb = new StringBuilder(2 * n);
        for (byte bt : bytes) {
            char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中高 4 位的数字转换, >>>
            // 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
            char c1 = hexDigits[bt & 0xf];// 取字节中低 4 位的数字转换
            sb.append(c0);
            sb.append(c1);
        }
        return sb.toString();
    }
}
