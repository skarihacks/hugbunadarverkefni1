package is.hi.hbv501g.demo.dto;

import java.util.UUID;

public class CreateCommentRequest {

    private UUID parentCommentId;  // optional, if you ever support nested comments
    private String body;

    public UUID getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(UUID parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    // helper for validation
    public boolean hasParent() {
        return parentCommentId != null;
    }

    public boolean isValid() {
        return body != null && !body.trim().isEmpty();
    }
}
