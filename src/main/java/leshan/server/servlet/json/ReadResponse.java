package leshan.server.servlet.json;

/**
 * JSON bean for a client read response
 */
public class ReadResponse {

    private String status;

    private String content;

    public ReadResponse(String status, String content) {
        this.status = status;
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
