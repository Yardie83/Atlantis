import java.io.Serializable;

/**
 * Created by herma on 19.07.2016.
 */
public class Message implements Serializable{

    private MessageType messageType;
    private Object messageObject;

    public Message(MessageType messageType){
        this.messageType = messageType;
    }

    public Message(MessageType messageType, Object messageObject){
        this.messageType = messageType;
        this.messageObject = messageObject;
    }

    public Object getMessageObject() {
        return messageObject;
    }
    public void setMessageObject(String messageObject){
        this.messageObject = messageObject;
    }

    public MessageType getMessageType(){
        return messageType;
    }

    public void setMessageType(MessageType messageType){
        this.messageType = messageType;
    }
}
