package is.hi.hbv501g.demo.dto;

public class UpdatePostRequest {

    private String title;
    private String body;
    private String mediaBase64;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMediaBase64() {
        return mediaBase64;
    }

    public void setMediaBase64(String mediaBase64) {
        this.mediaBase64 = mediaBase64;
    }
}
