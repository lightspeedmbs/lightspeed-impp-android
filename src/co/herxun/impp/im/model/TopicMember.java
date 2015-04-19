package co.herxun.impp.im.model;


import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "TopicMember")
public class TopicMember extends Model {
	@Column(name = "clientId")
	public String clientId;
	@Column(name = "topicId")
	public String topicId;
	
	public void update(){
		TopicMember exisit = new Select().from(TopicMember.class)
				.where("clientId = \""+clientId+"\" and topicId = \""+topicId+"\"").executeSingle();
		
		// 不存在
		if (exisit == null) {
			save();
		}
	}
	
	public static List<TopicMember> getAllTopicMember(){
		return new Select().from(TopicMember.class).execute();
	}
}