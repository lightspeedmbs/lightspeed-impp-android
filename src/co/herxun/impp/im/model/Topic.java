package co.herxun.impp.im.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.herxun.impp.model.User;
import co.herxun.impp.utils.DBug;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Column.ForeignKeyAction;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Set;

@Table(name = "Topic")
public class Topic extends Model implements Serializable{
	@Column(name = "ownerClientId")
	public String ownerClientId;
	@Column(name = "topicId")
	public String topicId;
	@Column(name = "topicName")
	public String topicName;
	
	public Topic update() {
		Topic userExisit = new Select().from(Topic.class)
				.where("topicId = ? ", topicId).executeSingle();
		// 不存在
		if (userExisit == null) {
			save();
			return this;
		} else {
			if(topicName!=null){
				userExisit.topicName = topicName;
			}
			if(ownerClientId!=null){
				userExisit.ownerClientId = ownerClientId;
			}
			userExisit.save();
			return userExisit;
		}
	}

	public void parseJSON(JSONObject json){
		try {
			topicId = json.getString("id");
			topicName = json.getString("name");
			ownerClientId = json.getString("owner");
			JSONArray parties = json.getJSONArray("parties");
			removeAllMember();
			for(int i =0;i<parties.length();i++){
				TopicMember topicMember = new TopicMember();
				String clientId = parties.getString(i);
				topicMember.topicId = topicId;
				topicMember.clientId = clientId;
				topicMember.update();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addMember(String clientId){
		TopicMember topicMember = new TopicMember();
		topicMember.clientId = clientId;
		topicMember.topicId = topicId;
		topicMember.update();
	}
	
	public void removeMember(String clientId){
		new Delete().from(TopicMember.class).where("topicId = \""+topicId+"\" and clientId = \""+clientId+"\"").executeSingle();
	}
	
	private void removeAllMember(){
		new Delete().from(TopicMember.class).where("topicId = ?" , topicId).execute();
	}
	
	public Topic getFromTable(){
		return new Select().from(Topic.class).where("topicId = ? ",topicId).executeSingle();
    }
	    
	public List<TopicMember> members(){
		List<TopicMember> topicMembers = new Select().from(TopicMember.class).where("topicId = ?" , topicId).execute();
		return  topicMembers;
		
	}
}
