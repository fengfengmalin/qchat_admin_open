package com.qunar.qchat.admin.util.http.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class AuthSSLX509TrustManager implements X509TrustManager {

    private X509TrustManager defaultTrustManager = null;

    /** Log object for this class. */
    private final static Logger logger = LoggerFactory.getLogger(AuthSSLX509TrustManager.class);
    /**
     * Constructor for AuthSSLX509TrustManager.
     */
    public AuthSSLX509TrustManager(final X509TrustManager defaultTrustManager) {
        super();
        if (defaultTrustManager == null) {
            throw new IllegalArgumentException("Trust manager may not be null");
        }
        this.defaultTrustManager = defaultTrustManager;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        if (logger.isInfoEnabled() && x509Certificates != null) {
            for (int c = 0; c < x509Certificates.length; c++) {
                X509Certificate cert = x509Certificates[c];
                logger.info(" Client certificate " + (c + 1) + ":");
                logger.info("  Subject DN: " + cert.getSubjectDN());
                logger.info("  Signature Algorithm: " + cert.getSigAlgName());
                logger.info("  Valid from: " + cert.getNotBefore() );
                logger.info("  Valid until: " + cert.getNotAfter());
                logger.info("  Issuer: " + cert.getIssuerDN());
            }
        }
        this.defaultTrustManager.checkServerTrusted(x509Certificates,s);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        if (logger.isInfoEnabled() && x509Certificates != null) {
            for (int c = 0; c < x509Certificates.length; c++) {
                X509Certificate cert = x509Certificates[c];
                logger.info("  Server certificate " + (c + 1) + ":");
                logger.info("  Subject DN: " + cert.getSubjectDN());
                logger.info("  Signature Algorithm: " + cert.getSigAlgName());
                logger.info("  Valid from: " + cert.getNotBefore() );
                logger.info("  Valid until: " + cert.getNotAfter());
                logger.info("  Issuer: " + cert.getIssuerDN());
            }
        }
        this.defaultTrustManager.checkServerTrusted(x509Certificates,s);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return defaultTrustManager.getAcceptedIssuers();
    }

}
