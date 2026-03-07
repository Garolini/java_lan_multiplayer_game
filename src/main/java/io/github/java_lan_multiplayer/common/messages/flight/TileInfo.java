package io.github.java_lan_multiplayer.common.messages.flight;

public class TileInfo {

    private int id;
    private int rotation;
    private int x;
    private int y;

    public TileInfo() {}

    public TileInfo(int id, int rotation, int x, int y) {
        this.id = id;
        this.rotation = rotation;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }
    public int getRotation() {
        return rotation;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
}
