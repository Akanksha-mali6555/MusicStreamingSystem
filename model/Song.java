package model;

public class Song {
    private int id;
    private String title;
    private String artist;
    private String genre;
    private int duration;
    private String releaseDate;
    private String imagePath;
    private String audioPath;

    public Song(int id, String title, String artist, String genre, int duration, String releaseDate, String imagePath, String audioPath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.imagePath = imagePath;
        this.audioPath = audioPath;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getGenre() { return genre; }
    public int getDuration() { return duration; }
    public String getReleaseDate() { return releaseDate; }
    public String getImagePath() { return imagePath; }
    public String getAudioPath() { return audioPath; }
}
