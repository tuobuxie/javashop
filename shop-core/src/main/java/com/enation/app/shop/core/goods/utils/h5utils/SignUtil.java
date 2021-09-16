package com.enation.app.shop.core.goods.utils.h5utils;



import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import java.io.FileInputStream;

/**
 * 证书签名、验签工具类
 *
 * @author admin
 */

public class SignUtil {
    /**
     * 签名算法
     * 还有一种MD5withRSA
     */
    public static final String SHA1WithRSA = "SHA1WithRSA";
    public static final String CHARSET = "utf-8";

    /**
     * 文本签名
     *
     * @param privateCert    rsa证书-私钥
     * @param data           明文信息
     * @param signAlgorithms 签名算法可为空，默认是SHA1_WITH_RSA
     * @param charset        字符串->byte或者byte->字符串指定编码,默认是utf-8
     * @return 返回new String(Base64.encodeBase64(signed), charset)
     */
    public synchronized static String signMsg(CertInfo privateCert, String data,
                                              String signAlgorithms, String charset) throws Exception {
        if (StringUtils.isBlank(signAlgorithms)) {
            signAlgorithms = SHA1WithRSA;
        }
        if (StringUtils.isBlank(charset)) {
            charset = CHARSET;
        }
        byte[] msgByte = data.getBytes(charset);
        java.security.Signature signet = java.security.Signature
                .getInstance(signAlgorithms);
        signet.initSign(privateCert.getPriKey());
        signet.update(msgByte);
        byte[] signed = signet.sign();
        return new String(Base64.encodeBase64(signed), charset);

    }

    /**
     * 文本验签
     *
     * @param publicCert     rsa证书-公钥
     * @param data           明文信息
     * @param sign           base64编码过的签名串
     * @param signAlgorithms 签名算法可为空，默认是SHA1_WITH_RSA
     * @param charset        字符串->byte或者byte->字符串指定编码,默认是utf-8
     * @return true or false
     */
    public static boolean verifyMsg(CertInfo publicCert, String data,
                                    String sign, String signAlgorithms, String charset)
            throws Exception {
        if (StringUtils.isBlank(signAlgorithms)) {
            signAlgorithms = SHA1WithRSA;
        }
        if (StringUtils.isBlank(charset)) {
            charset = CHARSET;
        }
        byte[] msgByte = data.getBytes(charset);
        byte[] signByte = Base64.decodeBase64(sign.getBytes(charset));
        java.security.Signature signatureCheck = java.security.Signature
                .getInstance(signAlgorithms);
        signatureCheck.initVerify(publicCert.getPubKey());
        signatureCheck.update(msgByte);
        return signatureCheck.verify(signByte);
    }

    /**
     * 文件签名
     *
     * @param privateCert    rsa证书-私钥
     * @param fileName       文件路径
     * @param signAlgorithms 签名算法可为空，默认是SHA1_WITH_RSA
     * @param charset        字符串->byte或者byte->字符串指定编码,默认是utf-8
     * @return 返回new String(Base64.encodeBase64(signed), charset)
     */
    public synchronized static String signFile(CertInfo privateCert,
                                               String fileName, String signAlgorithms, String charset)
            throws Exception {
        if (StringUtils.isBlank(signAlgorithms)) {
            signAlgorithms = SHA1WithRSA;
        }
        if (StringUtils.isBlank(charset)) {
            charset = CHARSET;
        }
        java.security.Signature signet = java.security.Signature
                .getInstance(signAlgorithms);
        signet.initSign(privateCert.getPriKey());
        byte[] buf = new byte[1024];
        int num;
        FileInputStream fin = new FileInputStream(fileName);
        while ((num = fin.read(buf, 0, buf.length)) != -1) {
            signet.update(buf, 0, num);
        }
        byte[] signed = signet.sign();
        fin.close();
        return new String(Base64.encodeBase64(signed), charset);
    }

    /**
     * 文件验签
     *
     * @param publicCert     rsa证书-公钥
     * @param fileName       文件路径
     * @param sign           base64编码过的签名串
     * @param signAlgorithms 签名算法可为空，默认是SHA1_WITH_RSA
     * @param charset        字符串->byte或者byte->字符串指定编码,默认是utf-8
     * @return true or false
     */
    public static boolean verifyFile(CertInfo publicCert, String fileName,
                                     String sign, String signAlgorithms, String charset)
            throws Exception {
        if (StringUtils.isBlank(signAlgorithms)) {
            signAlgorithms = SHA1WithRSA;
        }
        if (StringUtils.isBlank(charset)) {
            charset = CHARSET;
        }
        byte[] signByte = Base64.decodeBase64(sign.getBytes(charset));
        byte[] buf = new byte[1024];
        int num;
        FileInputStream fin = new FileInputStream(fileName);
        java.security.Signature signatureCheck = java.security.Signature
                .getInstance(signAlgorithms);
        signatureCheck.initVerify(publicCert.getPubKey());
        while ((num = fin.read(buf, 0, buf.length)) != -1) {
            signatureCheck.update(buf, 0, num);
        }
        fin.close();
        return signatureCheck.verify(signByte, 0, signByte.length);
    }

}
