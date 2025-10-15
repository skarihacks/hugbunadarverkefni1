package is.hi.hbv501g.demo.controller.view;

import is.hi.hbv501g.demo.entity.Post;

public record PostView(
        String id,
        String community,
        String author,
        String title,
        String type,
        String body,
        String url,
        String mediaUrl,
        int score,
        String createdAt) {

    public static PostView from(Post post) {
        return new PostView(
                post.getId().toString(),
                post.getCommunity().getName(),
                post.getAuthor().getUsername(),
                post.getTitle(),
                post.getType().name(),
                post.getBody(),
                post.getUrl(),
                post.getMediaUrl(),
                post.getScore(),
                post.getCreatedAt().toString());
    }
}
