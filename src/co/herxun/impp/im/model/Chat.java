package co.herxun.impp.im.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import co.herxun.impp.utils.DBug;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "Chat")
public class Chat extends Model implements Serializable{
    @Column(name = "currentClientId")
    public String currentClientId;
    
    @Column(name = "updateTime")
    public long updateTime; 

    @Column(name = "targetClientId")
    public String targetClientId;
    
    @Column(name = "Topic", onDelete = Column.ForeignKeyAction.CASCADE)
    public Topic topic; 
    
    public Chat(){
        super();
    }
    
    public List<Message> messages(){
        return new Select().from(Message.class).where("Chat = \""+getId()+"\" and currentClientId = \""+currentClientId+"\"").execute();
    }
    
    public Message lastMessage(){
    	return new Select().from(Message.class).where("Chat = \""+getId()+"\" and currentClientId = \""+currentClientId+"\"").orderBy("timestamp DESC").executeSingle();
    }
    
    public List<Message> unReadedMessages(){
        return new Select().from(Message.class).where("Chat = \""+getId()+"\" and readed = 0 and currentClientId = \""+currentClientId+"\"").execute();
    }
    
    public Chat getFromTable(){
    	if(topic!=null){
    		return new Select().from(Chat.class).where("Topic = \""+topic.getFromTable().getId()+"\" and currentClientId = \""+currentClientId+"\"").executeSingle();
    	}else{
    		return new Select().from(Chat.class).where("targetClientId = \""+targetClientId+"\" and currentClientId = \""+currentClientId+"\"").executeSingle();
    	}
    }
    
    public void update(){
    	Calendar c = Calendar.getInstance();
    	updateTime = c.getTimeInMillis();
    			
    	Chat exist;
    	if(topic!=null){
    		exist = new Select().from(Chat.class).where("Topic = \""+topic.getId()+"\" and currentClientId = \""+currentClientId+"\"").executeSingle();
    	}else{
    		exist = new Select().from(Chat.class).where("targetClientId = \""+targetClientId+"\" and currentClientId = \""+currentClientId+"\"").executeSingle();
    	}
    	
    	if(exist == null){
    		save();
    	}else{
    		exist.updateTime = updateTime;
    		exist.save();
    	}
    }
}
