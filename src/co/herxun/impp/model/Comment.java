package co.herxun.impp.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import co.herxun.impp.controller.UserManager;
import co.herxun.impp.utils.DBug;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

@Table(name = "Comment")
public class Comment extends Model {
	@Column(name = "commentId")
	public String commentId; 
	
	@Column(name = "content")
	public String content; 
	
	@Column(name = "createdAt")
	public long createdAt; 
	
	@Column(name = "Owner")
	public User owner; 
	
	@Column(name = "replyUser")
	public User replyUser; 
	
	@Column(name = "Post")
	public Post post; 

	
	public void parseJSON(JSONObject json){
		try {
			commentId = json.getString("id"); 
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		try {
			post = new Post();
			post.postId = json.getString("parentId"); 
			post = post.getFromTable();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		try {
			Calendar c = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			c.setTime(sdf.parse(json.getString("created_at")));
			createdAt = c.getTimeInMillis();   
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			content = json.getString("content"); 
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		try {	
			User user = new User();
			user.parseJSON(json.getJSONObject("user"));
			user = user.update();
			owner = user;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		try {	
			User user = new User();
			user.parseJSON(json.getJSONObject("replyUser"));
			user = user.update();
			replyUser = user;
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public Comment getFromTable(){
		Comment exisit = new Select().from(Comment.class).where("commentId = ?",commentId).executeSingle();
		return exisit;
	}
	
	public void update(){
		Comment exisit = getFromTable();
		
		// 不存在
		if (exisit == null) {
			save();
		}else{
		}
	}
}
