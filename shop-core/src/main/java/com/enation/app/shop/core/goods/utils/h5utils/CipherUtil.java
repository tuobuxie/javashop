package com.enation.app.shop.core.goods.utils.h5utils;




import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;

/**
 * 证书加密、解密工具类
 *@author admin
 */
public class CipherUtil {
    public static final String PKCS1Padding = "RSA/ECB/PKCS1Padding";
    public static final String CHARSET = "utf-8";
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * 随机生成一个key，用该key对内容进行AES加密 <br>
     * 再用公钥加密key保护key<br>
     * 每次用公钥加密的字节数，不能超过密钥的长度值减去11
     *
     * @param publicCert     公钥
     * @param data           需加密数据的byte数据
     * @param signAlgorithms rsa加密算法
     * @param charset        字符集
     * @return json:
     * {key:rsa公钥加密后的byte通过base64编码的字符串,content:AES加密内容需要先将key进行rsa解密}
     */
    public static String encryptData(CertInfo publicCert, String data,
                                     String signAlgorithms, String charset) throws Exception {
        if (StringUtils.isBlank(signAlgorithms)) {
            signAlgorithms = PKCS1Padding;
        }
        if (StringUtils.isBlank(charset)) {
            charset = CHARSET;
        }
        // 1、产生AES密钥
        String keyString = AesSecurityUtil.generateKeyString();
        // 2、用AES法加密数据
        String encryptedData = AesSecurityUtil.encrypt(keyString, data,
                charset);
        // 3、用RSA加密AES密钥进行保护
        byte[] keyByte = keyString.getBytes(charset);
        Cipher cipher = Cipher.getInstance(signAlgorithms);
        // 编码前设定编码方式及密钥
        cipher.init(Cipher.ENCRYPT_MODE, publicCert.getPubKey());
        // 传入编码数据并返回编码结果
        int inputLen = keyByte.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(keyByte, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(keyByte, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedKey = out.toByteArray();
        out.close();
        // 受保护的key
        String finalKey = new String(Base64.encodeBase64(encryptedKey),
                charset);
        return String.format("{\"key\":\"%s\",\"content\":\"%s\"}", finalKey,
                encryptedData);
    }

    /**
     * 用私钥解密
     *
     * @param privateCert    私钥
     * @param encryptedKey   rsa公钥加密后的byte通过base64编码的字符串
     * @param encryptedData  AES加密内容需要先将key进行rsa解密
     * @param signAlgorithms rsa加密算法
     * @param charset        字符集
     * @return 明文
     */
    public static String decryptData(CertInfo privateCert, String encryptedKey,
                                     String encryptedData, String signAlgorithms, String charset)
            throws Exception {
        if (StringUtils.isBlank(signAlgorithms)) {
            signAlgorithms = PKCS1Padding;
        }
        if (StringUtils.isBlank(charset)) {
            charset = CHARSET;
        }
        // 1、解密密钥
        byte[] keyByte = Base64.decodeBase64(encryptedKey.getBytes(charset));
        Cipher cipher = Cipher.getInstance(signAlgorithms);
        cipher.init(Cipher.DECRYPT_MODE, privateCert.getPriKey());
        String keyString = new String(cipher.doFinal(keyByte), charset);

        // 3、返回
        return AesSecurityUtil.decrypt(keyString, encryptedData, charset);
    }
}
