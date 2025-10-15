package is.hi.hbv501g.demo.dto;

import is.hi.hbv501g.demo.entity.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreatePostRequest {

    @NotBlank(message = "community is required")
    private String communityName;

    @NotBlank(message = "title is required")
    @Size(max = 300, message = "title cannot exceed 300 characters")
    private String title;

    @NotNull(message = "post type is required")
    private PostType type;

    @Size(max = 4000, message = "body cannot exceed 4000 characters")
    private String body;

    @Size(max = 1024, message = "url cannot exceed 1024 characters")
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
        return PostType.MEDIA.equals(this.type);
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
