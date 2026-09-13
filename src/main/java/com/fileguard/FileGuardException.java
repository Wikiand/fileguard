package com.fileguard;

public class FileGuardException extends Exception {

    public FileGuardException(String message) {
        super(message);
    }

    public FileGuardException(String message, Throwable cause) {
        super(message, cause);
    }
}
