package edu.nus.sunlabitro.peernus;

public class Message {

    private String id;
    private String text;
    private String name;
    private long timestamp;
    private String imageUrl;

    public Message() {

    }

    public Message(String text, String name, long timestamp, String imageUrl) {
        this.text = text;
        this.name = name;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
