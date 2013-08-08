package leshan.server.servlet.json;

/**
 * JSON bean for a client read response
 */
public class ReadResponse {

    private String status;

    private String value;

    public ReadResponse(String status, String content) {
        this.status = status;
        this.value = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return value;
    }

    public void setContent(String content) {
        this.value = content;
    }

}
