package message;

import java.util.UUID;

/**
 * client send this message to notify the server that this client is still alive.
 *
 */
public class PingAliveMessage extends HadroidMessage{

    /**
     * 
     */
    private static final long serialVersionUID = 2922847691112110062L;
    
    private UUID clientUUID;

    public PingAliveMessage(UUID clientUUID) {
        this.clientUUID = clientUUID;
    }

    public UUID getClientUUID() {
        return clientUUID;
    }

    public void setClientUUID(UUID clientUUID) {
        this.clientUUID = clientUUID;
    }
}
