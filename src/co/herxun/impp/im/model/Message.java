package co.herxun.impp.im.model;

import java.io.Serializable;
import java.util.Calendar;

import co.herxun.impp.utils.DBug;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "Message")
public class Message extends Model {
	
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_RECORD = "record";
	public static final String TYPE_NOTICE = "notice";

	public static final String STATUS_SENDING = "sending";
	public static final String STATUS_SENT = "sent";
	public static final String STATUS_FAILED = "failed";

    @Column(name = "Chat", onDelete = Column.ForeignKeyAction.CASCADE)
	public Chat chat;
	
    @Column(name = "msgId")
    public String msgId; 
    
    @Column(name = "content")
    public byte[] content; 
    
    @Column(name = "message")
    public String message; 
    
    @Column(name = "currentClientId")
    public String currentClientId; 

    @Column(name = "timestamp")
    public long timestamp; 
    
    @Column(name = "fileURL")
    public String fileURL; 
    
    @Column(name = "fromClient")
    public String fromClient; 
    
    @Column(name = "fromUsername")
    public String fromUsername; 
    
    @Column(name = "fromUserIconUrl")
    public String fromUserIconUrl; 
    
    @Column(name = "topicId")
    public String topicId; 
    
    @Column(name = "type")
    public String type; 
    
    @Column(name = "status")
    public String status; 
    
    @Column(name = "latitude")
    public String latitude; 
    @Column(name = "longitude")
    public String longitude; 
    
    @Column(name = "readACK")
    public boolean readACK; 
    
    @Column(name = "senderName")
    public String senderName; 
    
    @Column(name = "targetUserId")
    public String targetUserId; 
    
    @Column(name = "readed")
    public boolean readed; 
    
    public void update(){
    	Calendar c = Calendar.getInstance();
    	timestamp = c.getTimeInMillis();	
    	final boolean isReaded = readed;
    	
    	Message exist = getFromTable();
    	if(exist == null){
    		save();
    	}else{
    		exist.timestamp = timestamp;
			exist.status = status;
			if(!exist.readed)
				exist.readed = isReaded;
			if(!exist.readACK)
				exist.readACK = readACK;
    		exist.save();
    	}
    	
    }
    
    public Message getFromTable(){
    	return new Select().from(Message.class).where("msgId = \""+msgId+"\" and currentClientId = \""+currentClientId+"\"").executeSingle(); 
    }
    
    public boolean isMine(){
    	return currentClientId.equals(fromClient);
    }
}
