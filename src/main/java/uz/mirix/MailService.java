package uz.mirix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.search.SearchTerm;
import java.util.Properties;

@Service
public class MailService {
    private final Logger log = LoggerFactory.getLogger(MailService.class);

    public MailService() {
    }

    /**
     * Creates a session for connecting to the mail server.
     *
     * @param protocol The protocol (e.g., "imaps", "smtp").
     * @param host     The mail server host.
     * @param port     The port for connection.
     * @return A session for interacting with the mail server.
     */
    public Session getSession(String protocol, String host, String port) {
        Properties properties = new Properties();
        properties.setProperty("mail.store.protocol", protocol);
        properties.setProperty("mail." + protocol + ".host", host);
        properties.setProperty("mail." + protocol + ".port", port);

        return Session.getDefaultInstance(properties);
    }

    /**
     * Retrieves the Store object (mailbox) for the given session and protocol.
     *
     * @param session  The mail session.
     * @param protocol The protocol (e.g., "imaps", smtp).
     * @return The store object (mailbox).
     */
    public Store getStore(Session session, String protocol) {
        try {
            return session.getStore(protocol);
        } catch (NoSuchProviderException e) {
            log.error("Error getting Store for protocol {}: {}", protocol, e.getMessage());
            return null;
        }
    }

    /**
     * Connects to the mail store using the given credentials.
     *
     * @param store    The mail store.
     * @param host     The mail server host.
     * @param username The username for authentication.
     * @param password The password for authentication.
     */
    public void connectToStore(Store store, String host, String username, String password) {
        try {
            store.connect(host, username, password);
        } catch (MessagingException e) {
            log.error("Error connecting to store at {}: {}", host, e.getMessage());
        }
    }

    /**
     * Retrieves a folder from the store.
     *
     * @param store      The mail store.
     * @param folderName The name of the folder.
     * @return The folder object.
     */
    public Folder getFolder(Store store, String folderName) {
        try {
            return store.getFolder(folderName);
        } catch (MessagingException e) {
            log.error("Error retrieving folder {}: {}", folderName, e.getMessage());
            return null;
        }
    }

    /**
     * Opens the given folder with the specified mode (READ_ONLY or READ_WRITE).
     *
     * @param folder The folder to open.
     * @param mode   The mode to open the folder with.
     */
    public void openFolder(Folder folder, int mode) {
        try {
            folder.open(mode);
        } catch (MessagingException e) {
            log.error("Error opening folder {}: {}", folder.getName(), e.getMessage());
        }
    }

    /**
     * Creates the folder if it does not exist and opens it in the given mode.
     *
     * @param folder     The folder to create and open.
     * @param createMode The mode to create the folder with (e.g., HOLDS_MESSAGES).
     * @param openMode   The mode to open the folder with (READ_ONLY or READ_WRITE).
     */
    public void createAndOpenFolder(Folder folder, int createMode, int openMode) {
        try {
            if (!folder.exists()) {
                folder.create(createMode);
            }
            folder.open(openMode);
        } catch (MessagingException e) {
            log.error("Error creating or opening folder {}: {}", folder.getName(), e.getMessage());
        }
    }

    /**
     * Fetches all emails from the given folder.
     *
     * @param folder The folder to fetch messages from.
     * @return An array of messages from the folder.
     */
    public Message[] fetchEmails(Folder folder) {
        try {
            return folder.getMessages();
        } catch (MessagingException e) {
            log.error("Error fetching messages from folder {}: {}", folder.getName(), e.getMessage());
            return new Message[0]; // Returning an empty array if there's an error
        }
    }

    /**
     * Fetches emails from the folder that match the given search term.
     *
     * @param folder     The folder to search.
     * @param searchTerm The search term to filter messages.
     * @return An array of messages that match the search term.
     */
    public Message[] fetchEmails(Folder folder, SearchTerm searchTerm) {
        try {
            return folder.search(searchTerm);
        } catch (MessagingException e) {
            log.error("Error searching messages in folder {}: {}", folder.getName(), e.getMessage());
            return new Message[0]; // Returning an empty array if there's an error
        }
    }

    /**
     * Expunges (permanently deletes) messages from the folder that are marked for deletion.
     *
     * @param folder The folder to expunge.
     * @return An array of messages that were expunged.
     */
    public Message[] expungeFolder(Folder folder) {
        try {
            return folder.expunge();
        } catch (MessagingException e) {
            log.error("Error expunging folder {}: {}", folder.getName(), e.getMessage());
            return new Message[0]; // Returning an empty array if there's an error
        }
    }

    /**
     * Closes the folder and optionally saves changes or expunges messages.
     *
     * @param folder      The folder to close.
     * @param saveChanges Whether to save changes before closing.
     * @param expunge     Whether to expunge (clear) deleted messages before closing.
     */
    public void closeFolder(Folder folder, boolean saveChanges, boolean expunge) {
        try {
            if (folder != null && folder.isOpen()) {
                if (expunge) {
                    expungeFolder(folder);
                }
                folder.close(saveChanges);
            }
        } catch (MessagingException e) {
            log.error("Error closing folder {}: {}", folder.getName(), e.getMessage());
        }
    }

    /**
     * Closes the mail store if it's connected.
     *
     * @param store The mail store to close.
     */
    public void closeStore(Store store) {
        try {
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (MessagingException e) {
            log.error("Error while closing store: {}", e.getMessage());
        }
    }
}

