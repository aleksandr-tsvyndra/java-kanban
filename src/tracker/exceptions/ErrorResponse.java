package tracker.exceptions;

import java.net.URI;

public class ErrorResponse {
    private String errorMessage;
    private int errorCode;
    private URI url;

    public ErrorResponse(String errorMessage, int errorCode, URI url) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.url = url;
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public URI getUrl() {
        return url;
    }

    public void setUrl(URI url) {
        this.url = url;
    }
}
