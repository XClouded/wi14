package message;

import java.util.UUID;

/**
 * client send this message to notify the server that this client is still alive.
 *
 */
public class PingAliveMessage extends MessageFromClient{

    /**
     * 
     */
    private static final long serialVersionUID = 2922847691112110062L;

    public PingAliveMessage(UUID clientID) {
        super(clientID);
    }

    
}
