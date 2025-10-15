package is.hi.hbv501g.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class CreateCommentRequest {

    @NotNull(message = "postId is required")
    private UUID postId;

    @NotBlank(message = "body is required")
    @Size(max = 2000, message = "body cannot exceed 2000 characters")
    private String body;

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
