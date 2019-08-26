package com.qunar.qchat.admin.util.http.ext;

/**
 *
 * @since 1.0.0
 * @author kris.zhang
 */
public class AuthSSLInitializationError extends Error {
    public AuthSSLInitializationError() {
    }

    public AuthSSLInitializationError(String message) {
        super(message);
    }

    public AuthSSLInitializationError(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthSSLInitializationError(Throwable cause) {
        super(cause);
    }
}
