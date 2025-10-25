package model;

public class Artist {
    private int id;
    private String name;
    private String country;
    private String imagePath;

    public Artist() {}
    public Artist(int id, String name, String country, String imagePath){
        this.id=id;this.name=name;this.country=country;this.imagePath=imagePath;
    }
    // getters/setters
    public int getId(){return id;}
    public void setId(int id){this.id=id;}
    public String getName(){return name;}
    public void setName(String name){this.name=name;}
    public String getCountry(){return country;}
    public void setCountry(String country){this.country=country;}
    public String getImagePath(){return imagePath;}
    public void setImagePath(String imagePath){this.imagePath=imagePath;}
}
