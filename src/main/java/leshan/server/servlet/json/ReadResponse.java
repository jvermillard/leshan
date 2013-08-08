package leshan.server.servlet.json;

/**
 * JSON bean for a client read response
 */
public class ReadResponse {

    private String status;

    private Object value;

    public ReadResponse(String status, Object value) {
        this.status = status;
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
