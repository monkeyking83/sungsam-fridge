package com.sungsam.smartfridge.service;

public class ItemValidationException extends IllegalArgumentException {

    private static final long serialVersionUID = -7257341684722294975L;

    private String validationMessage;

    public ItemValidationException(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    @Override
    public String getMessage() {
        return this.validationMessage;
    }

}
