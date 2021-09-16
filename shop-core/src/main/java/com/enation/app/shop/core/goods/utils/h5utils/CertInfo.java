package com.enation.app.shop.core.goods.utils.h5utils;


import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * 证书
 *
 * @author admin
 */

public class CertInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private PrivateKey priKey;
    private PublicKey pubKey;

    public CertInfo() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 读私钥
     *
     * @param pfxFileName
     * @param passowrd
     * @throws Exception
     * @see
     */
    public void readKeyFromPKCS12(String pfxFileName, String passowrd)
            throws Exception {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(pfxFileName);
            KeyStore keystoreCA = KeyStore.getInstance("PKCS12", "BC");
            keystoreCA.load(fis, passowrd.toCharArray());
            Enumeration aliases = keystoreCA.aliases();
            String keyAlias = null;
            X509Certificate cer = null;
            do {
                if ((aliases == null) || (!(aliases.hasMoreElements()))) {
                    break;
                }
                keyAlias = (String) aliases.nextElement();

                this.priKey = ((PrivateKey) (PrivateKey) keystoreCA
                        .getKey(keyAlias, passowrd.toCharArray()));

                cer = ((X509Certificate) (X509Certificate) keystoreCA
                        .getCertificate(keyAlias));
            } while (this.priKey == null);

            this.pubKey = cer.getPublicKey();

        } catch (Exception e) {
            if (fis != null) {
                fis.close();
            }
            throw e;
        }
        if (fis != null) {
            fis.close();
        }
    }

    /**
     * 读公钥
     *
     * @param certFileName
     * @throws Exception
     * @see
     */
    public void readPublicKeyFromX509Certificate(String certFileName)
            throws Exception {
        FileInputStream inputStream = new FileInputStream(certFileName);
        readPublicKeyFromX509Certificate(inputStream);
        inputStream.close();
    }


    /**
     * 读公钥
     *
     * @param inputStream
     * @throws Exception
     * @see
     */
    public void readPublicKeyFromX509Certificate(InputStream inputStream)
            throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cac = cf
                .generateCertificate(inputStream);
        inputStream.close();
        pubKey = cac.getPublicKey();
    }

    public PrivateKey getPriKey() {
        return this.priKey;
    }

    public PublicKey getPubKey() {
        return this.pubKey;
    }


}