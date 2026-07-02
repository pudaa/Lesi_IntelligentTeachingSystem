// 创建加密工具类
package utils;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import tools.ConfigUtil;

public class EncryptionUtil {
    private static final String ALGORITHM = "AES";
    private static final String KEY;

    static {
        String configKey = ConfigUtil.getProperty("encryption.key");
        if (configKey == null || configKey.isEmpty() || "null".equals(configKey)) {
            throw new IllegalStateException("加密密钥未配置，请在配置文件中设置 encryption.key");
        }
        KEY = configKey;
    }

    public static String encrypt(String data) { // 加密方法
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return data; // 加密失败时返回原文
        }
    }

    public static String decrypt(String encryptedData) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return encryptedData; // 解密失败时返回密文
        }
    }
}
