package Server.utils;
public class Message {
   public enum MESSAGE_TYPE{
       STRING, INT
   }

   private MESSAGE_TYPE message_type;
   private Object message;
   private ResultCode rescode;

   public Message(MESSAGE_TYPE message_type, Object message, ResultCode rescode) {
       this.message_type = message_type;
       this.message = message;
       this.rescode = rescode;
   }

    public MESSAGE_TYPE getMessage_type() {
        return this.message_type;
    }

    public void setMessage_type(MESSAGE_TYPE message_type) {
        this.message_type = message_type;
    }
    public void setMessage(Object message) {
        this.message = message;
    }

    public ResultCode getRescode() {
        return this.rescode;
    }

    public void setRescode(ResultCode rescode) {
        this.rescode = rescode;
    }

   public MESSAGE_TYPE getMessageType() {
       return message_type;
   }

   public Object getMessage() {
       return message;
   }
}