package model;

public class Playlist {
    private int id;
    private int userId;
    private String name;

    public Playlist(int id, int userId, String name) {
        this.id = id;
        this.userId = userId;
        this.name = name;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }

    @Override
    public String toString() { return name; }
}
