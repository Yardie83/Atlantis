import java.io.Serializable;

/**
 * Created by herma on 19.07.2016.
 */
public class Message implements Serializable{

    private MessageType messageType;
    private Object message;

    public Message(MessageType messageType){
        this.messageType = messageType;
    }

    public Message(MessageType messageType, Object message){
        this.messageType = messageType;
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }

    public MessageType getMessageType(){
        return messageType;
    }

    public void setMessageType(MessageType messageType){
        this.messageType = messageType;
    }
}
