package notifications;

/**
 * Interface for the Subscribers
 */
public interface Observer {
    /**
     * Updates the notifications of the Subscriber
     */
    void update(String message);
}
