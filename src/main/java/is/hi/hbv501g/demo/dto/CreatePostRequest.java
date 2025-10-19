package is.hi.hbv501g.demo.dto;

import is.hi.hbv501g.demo.entity.PostType;

public class CreatePostRequest {

    private String communityName;
    private String title;
    private PostType type = PostType.TEXT;
    private String body;
    private String url;

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PostType getType() {
        return type;
    }

    public void setType(PostType type) {
        this.type = type;
    }

    public boolean isMedia() {
        return PostType.MEDIA.equals(type);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
