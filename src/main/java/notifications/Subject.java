package notifications;

/**
 * Interface for the Publisher
 */
public interface Subject {
    /**
     * Adds subscriber to list
     */
    void addObserver(Observer observer);

    /**
     * Removes subscriber from list
     */
    void removeObserver(Observer observer);

    /**
     * Sends the update to all the subscribers
     */
    void notifyObservers(String message);
}
