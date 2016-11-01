package ch.atlantis.util;

import java.io.Serializable;

/**
 * Created by Hermann Grieder on 19.07.2016.
 */
public class Message implements Serializable {

    private MessageType messageType;
    private Object messageObject;
    private static final long serialVersionUID = 7526472295622776147L;

    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public Message(MessageType messageType, Object messageObject) {
        this.messageType = messageType;
        this.messageObject = messageObject;

    }

    public Object getMessageObject() {
        return messageObject;
    }

    public MessageType getMessageType() {
        return messageType;
    }

}

