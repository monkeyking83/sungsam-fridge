package com.sungsam.smartfridge.service;

public class InvalidItemIdException extends IllegalArgumentException {

    private static final long serialVersionUID = -7257341684722294975L;
    private String invalidItemId;

    public InvalidItemIdException(Throwable t, String invalidItemId) {
        super(t);
        this.invalidItemId = invalidItemId;
    }

    @Override
    public String getMessage() {
        return String.format("Detected invalid item ID: %s", invalidItemId);
    }

    public String getInvalidItemId() {
        return invalidItemId;
    }

}
