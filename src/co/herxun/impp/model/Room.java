package co.herxun.impp.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "Room")
public class Room extends Model {
    @Column(name = "roomId")
    public String roomId;

    @Column(name = "name")
    public String name;

    @Column(name = "type")
    public String type;

    @Column(name = "description")
    public String description;

    @Column(name = "photoUrl")
    public String photoUrl;

    @Column(name = "createdAt")
    public long createdAt;

    @Column(name = "Owner")
    public User owner;

    @Column(name = "usersIds")
    public String usersIds;

    @Column(name = "isJoin")
    public boolean isJoin;

    @Column(name = "topicId")
    public String topicId;

    public void parseJSON(JSONObject json) {
        try {
            roomId = json.getString("id");
            type = json.getString("type");
            name = json.getString("name");
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            c.setTime(sdf.parse(json.getString("created_at")));
            createdAt = c.getTimeInMillis();
            JSONArray usersArray = json.getJSONArray("users");
            usersIds = "";
            for (int i = 0; i < usersArray.length(); i++) {
                usersIds = usersIds + usersArray.getString(i) + ",";
            }
            if (usersIds != null && usersIds.length() > 0) {
                usersIds = usersIds.substring(0, usersIds.length() - 1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
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
            if (json.has("customFields")) {
                if (json.getJSONObject("customFields").has("photoUrls")) {
                    photoUrl = json.getJSONObject("customFields").getString("photoUrls");
                }
                if (json.getJSONObject("customFields").has("topic_id")) {
                    topicId = json.getJSONObject("customFields").getString("topic_id");
                }
                if (json.getJSONObject("customFields").has("description")) {
                    description = json.getJSONObject("customFields").getString("description");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Room getFromTable() {
        Room exisit = new Select().from(Room.class).where("roomId = ?", roomId).executeSingle();
        return exisit;
    }
    
    public Room isRoomExists() {
       return new Select().from(Room.class).where("topicId = ?", topicId).executeSingle();
    }

    public void update() {
        Room exisit = new Select().from(Room.class).where("roomId = ?", roomId).executeSingle();

        // 不存在
        if (exisit == null) {
            save();
        } else {
            exisit.usersIds = usersIds;
            exisit.save();
        }
    }
}
