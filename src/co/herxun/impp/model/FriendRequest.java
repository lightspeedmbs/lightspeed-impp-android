package co.herxun.impp.model;

import org.json.JSONException;
import org.json.JSONObject;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "FriendRequest")
public class FriendRequest extends Model{
	public final static String STATUS_PENDING = "pending";
	public final static String STATUS_APPROVED = "approved";
	public final static String STATUS_REJECTED = "rejected";

	@Column(name = "status")
	public String status ;
	@Column(name = "friendRequestId")
	public String friendRequestId;
	@Column(name = "fromUserClientId")
	public String fromUserClientId;
	@Column(name = "currentUserClientId")
	public String currentUserClientId;
	
	public FriendRequest(){}
	
	public FriendRequest(String currentClientId,JSONObject json){
		try {
			friendRequestId = json.getString("id");
			status = json.getString("status");
			fromUserClientId = json.getJSONObject("from").getString("clientId");
			currentUserClientId = currentClientId;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public User user(){
		return new Select().from(User.class).where("clientId = ?",fromUserClientId).executeSingle();
	}
	
	
	public void update(){
		FriendRequest exisit = new Select().from(FriendRequest.class)
				.where("friendRequestId = ? ", friendRequestId).executeSingle();
		
		// 不存在
		if (exisit == null) {
			exisit = this;
		}else{
			if(fromUserClientId!=null)
				exisit.fromUserClientId = fromUserClientId;
			if(status!=null)
				exisit.status = status;
		}

		exisit.save();
	}
}
