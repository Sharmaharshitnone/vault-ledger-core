package com.harshit.vaultledger.exception;

public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException(String identifier) {
        super("Already registered: " + identifier);
    }
}
