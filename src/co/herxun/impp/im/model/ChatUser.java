package co.herxun.impp.im.model;

public class ChatUser {
    private String clientId;
    private String username;
    private String iconUrl;

    public ChatUser(String clientId, String username, String iconUrl) {
        this.clientId = clientId;
        this.username = username;
        this.iconUrl = iconUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUsername() {
        return username;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

}
