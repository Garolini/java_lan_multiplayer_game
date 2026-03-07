package io.github.java_lan_multiplayer.common.messages.login;

public class PlayerInfo {

    private String username;
    private int[] profilePicIds;
    private String color;
    private boolean admin = false;
    private boolean ready = false;

    public PlayerInfo() {}

    public PlayerInfo(String username, int[] profilePicIds, String color, boolean admin, boolean ready) {
        this.username = username;
        this.profilePicIds = profilePicIds;
        this.color = color;
        this.admin = admin;
        this.ready = ready;
    }

    public String getUsername() {
        return username;
    }

    public int[] getProfilePicIds() {
        return profilePicIds;
    }

    public String getColor() {
        return color;
    }


    public boolean isAdmin() {
        return admin;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isReady() {
        return ready;
    }
}
