package co.herxun.impp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.model.TopicMember;
import co.herxun.impp.utils.DBug;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "User")
public class User extends Model implements Serializable{
	@Column(name = "userId")
	public String userId; 
	@Column(name = "clientId")
	public String clientId; 
	@Column(name = "userName")
	public String userName; 
	@Column(name = "userPhotoUrl")
	public String userPhotoUrl; 
	
	public User(){
	}
	
	public User(JSONObject json){
		parseJSON(json);
	}
	
	public void addFriend(String targetClientId,boolean isMutual){
		Friend friend = new Friend();
		friend.userClientId = clientId;
		friend.targetClientId = targetClientId;
		friend.isMutual = isMutual;
		friend.update();
	}
	
	public List<User> friends(){
		List<User> users = new ArrayList<User>();
		List<Friend> fs = new Select().from(Friend.class).where("userClientId = \""+clientId+"\" and isMutual = 1").execute();
		for(Friend f :fs){
			User user = new Select().from(User.class).where("clientId = ?",f.targetClientId).executeSingle();
			users.add(user);
		}
		return users;
	}
	
	public boolean isFriend(String targetClientId){
		List<Friend> fs = new Select().from(Friend.class).where("userClientId = \""+clientId+"\" and targetClientId = \""+targetClientId+"\" and isMutual = 1").execute();
		return fs.size()>0;
	}
	
	public User update() {
		User userExisit = new Select().from(User.class)
				.where("clientId = ? ", clientId).executeSingle();
		
		// 不存在
		if (userExisit == null) {
			userExisit = this;
		}else{
			if(userId!=null)
				userExisit.userId = userId;
			if(clientId!=null)
				userExisit.clientId = clientId;
			if(userName!=null)
				userExisit.userName = userName;
			if(userPhotoUrl!=null)
				userExisit.userPhotoUrl = userPhotoUrl;
		}

		userExisit.save();
		return userExisit;
	}
	
	public boolean same(){
		User userExisit = new Select().from(User.class)
				.where("clientId = ? ", clientId).executeSingle();
		if( (userExisit!=null) && userExisit.userName!=null && userExisit.userId!=null && (userExisit.userId.equals(userId)) && (userExisit.userName.equals(userName))){
			return (userExisit.userPhotoUrl==null && userPhotoUrl==null);
		}else{
			return false;
		}
		
	}
	
	public User getFromTable(){
		User userExisit = new Select().from(User.class).where("clientId = ? ", clientId).executeSingle();
		return userExisit;
	}
	
	public void parseJSON(JSONObject json){
		try {
			clientId = json.getString("clientId");
			userId = json.getString("id");
			userName = json.getString("username");
			if(json.has("photo")){
				userPhotoUrl = json.getJSONObject("photo").getString("url");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
