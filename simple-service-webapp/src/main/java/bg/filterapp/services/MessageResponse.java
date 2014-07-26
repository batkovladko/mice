package bg.filterapp.services;

public class MessageResponse {

    private boolean status;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(final boolean status) {
        this.status = status;
    }

    public MessageResponse() {
    }

    MessageResponse(final boolean status) {
        this.status = status;
    }


}
