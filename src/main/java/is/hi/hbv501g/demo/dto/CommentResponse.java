package is.hi.hbv501g.demo.dto;

import is.hi.hbv501g.demo.entity.Comment;
import java.time.format.DateTimeFormatter;

public record CommentResponse(
        String id,
        String postId,
        String authorId,
        String authorName,
        String body,
        int score,
        String state,
        String createdAt) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId().toString(),
                comment.getPost().getId().toString(),
                comment.getAuthor().getId().toString(),
                comment.getAuthor().getUsername(),
                comment.getBody(),
                comment.getScore(),
                comment.getState().name(),
                comment.getCreatedAt().format(FORMATTER));
    }
}
