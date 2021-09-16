package com.enation.app.shop.core.goods.utils.h5utils;




import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import java.security.Key;
import java.util.UUID;

/**
 * @author admin
 */
public class AesSecurityUtil {
    private static final String ALGORITHM = "AES";

    /**
     * 用来进行加密的操作
     *
     * @param data 明文
     * @return 密文
     */
    public static String encrypt(String keyString, String data, String charset)
            throws Exception {
        Key key = new SecretKeySpec(keyString.getBytes(charset), ALGORITHM);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(data.getBytes(charset));
        return new String(Base64.encodeBase64(encVal), charset);
    }

    /**
     * 用来进行解密的操作
     *
     * @param encryptedData 密文
     * @return 明文
     */
    public static String decrypt(String keyString, String encryptedData,
                                 String charset) throws Exception {
        Key key = new SecretKeySpec(keyString.getBytes(charset), ALGORITHM);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64
                .decodeBase64(encryptedData.getBytes(charset));
        byte[] decValue = c.doFinal(decodedValue);
        return new String(decValue, charset);
    }

    /**
     * 生成16位uuid用来做加密的key
     *
     * @return aes key
     */
    public static String generateKeyString() {
        // 必须长度为16
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0,
                16);
    }
}
