package co.herxun.impp.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import co.herxun.impp.controller.UserManager;
import co.herxun.impp.utils.DBug;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

@Table(name = "Post")
public class Post extends Model {
	@Column(name = "postId")
	public String postId; 
	
	@Column(name = "content")
	public String content; 
	
	@Column(name = "photoUrls")
	public String photoUrls; 
	
	@Column(name = "createdAt")
	public long createdAt; 
	
	@Column(name = "Owner")
	public User owner; 
	
	@Column(name = "wallId")
	public String wallId; 

	@Column(name = "likeCount")
	public int likeCount; 
	
	@Column(name = "commentCount")
	public int commentCount; 
	
	
	public Like myLike(User user){
		user = user.getFromTable();
		return new Select().from(Like.class).where("Post = \""+getFromTable().getId()+"\" and Owner = \""+user.getId()+"\"").executeSingle();
	}
	
	public List<Like> likes(){
		return new Select().from(Like.class).where("Post = ?",getFromTable().getId()).execute();
	}
	
	public void deleteAllLikes(){
		new Delete().from(Like.class).where("Post = ?",getFromTable().getId()).execute();
	}
	
	public void parseJSON(JSONObject json){
		try {
			wallId = json.getString("parentId");
			postId = json.getString("id");
			likeCount = json.getInt("likeCount"); 
			commentCount = json.getInt("commentCount");
			
			Calendar c = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
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
			photoUrls = json.getJSONObject("customFields").getString("photoUrls"); 
		}catch (JSONException e) {
			e.printStackTrace();
		} 
	}
	
	public Post getFromTable(){
		Post exisit = new Select().from(Post.class).where("postId = ?",postId).executeSingle();
		return exisit;
	}
	
	public void update(){
		Post exisit = new Select().from(Post.class).where("postId = ?",postId).executeSingle();
		
		// 不存在
		if (exisit == null) {
			save();
		}else{
			exisit.likeCount = likeCount;
			exisit.commentCount = commentCount;
			exisit.save();
		}
	}
}
