package za.co.simplitate.reactivespring.users.model;

import java.util.UUID;

public class AlbumRest {

    private String userId;
    private UUID id;
    private String title;

    public AlbumRest() {

    }

    public AlbumRest(String userId, UUID id, String title) {
        this.userId = userId;
        this.id = id;
        this.title = title;
    }

    public String getUserId() {
        return userId;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }


    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setId(UUID albumId) {
        this.id = albumId;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
