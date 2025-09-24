package com.lalaalal.mimo;

import com.lalaalal.mimo.logging.MessageComponent;

public class MessageComponentException extends RuntimeException {
    protected final MessageComponent component;

    public MessageComponentException(MessageComponent component) {
        super(component.plainText());
        this.component = component;
    }

    public MessageComponentException(MessageComponent component, Throwable cause) {
        super(component.plainText(), cause);
        this.component = component;
    }

    public MessageComponentException(String message) {
        super(message);
        this.component = MessageComponent.withDefault(message);
    }

    public MessageComponentException(String message, Throwable cause) {
        super(message, cause);
        this.component = MessageComponent.withDefault(message);
    }

    public MessageComponent getMessageComponent() {
        return component;
    }
}
