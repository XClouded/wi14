package message;

import java.util.UUID;

public class MessageFromClient extends HadroidMessage{

    private UUID clientID;
    
    public MessageFromClient(UUID clientID){
        this.clientID = clientID;
    }

    public UUID getClientID() {
        return clientID;
    }

    public void setClientID(UUID clientID) {
        this.clientID = clientID;
    }
    
    
}
