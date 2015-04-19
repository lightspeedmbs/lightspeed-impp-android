 package co.herxun.impp.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "Friend")
public class Friend extends Model {
	@Column(name = "userClientId")
	public String userClientId; 
	@Column(name = "targetClientId")
	public String targetClientId; 
	@Column(name = "isMutual")
	public boolean isMutual; 
	
	public void update(){
		Friend exisit = new Select().from(Friend.class)
				.where("userClientId = \""+userClientId+"\" and targetClientId = \""+targetClientId+"\"").executeSingle();
		
		// 不存在
		if (exisit == null) {
			save();
		}else{
			exisit.isMutual = isMutual;
			exisit.save();
		}
	}
}
