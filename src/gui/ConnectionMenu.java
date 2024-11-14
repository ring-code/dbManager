package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** The initial GUI element of this application to allow the user to create a custom MySQLConnector to connect to different Databases. 
 * Will construct itself again asking for new input if the user disconnects using the button in the MainFrame and close the Application
 * only when the exit Button is pressed.
 * 
 * @author ring-code
 */

public class ConnectionMenu {

    private JFrame frame;
    private String url;
    private String dbName;
    private String user;
    private String password;
    private boolean connectClicked = false;

    
    
    /**
     * Constructs the ConnectionMenu and initializes the GUI components.
     */   
    public ConnectionMenu() {
        // Create the frame
        frame = new JFrame("Verbindung herstellen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create a panel to hold buttons
        JPanel buttonPanel = new JPanel(); // Initialize the panel first
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS)); // Set layout after initialization
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Set a common size for all buttons
        Dimension buttonSize = new Dimension(200, 50); // Example size: width 200, height 50

        // Create the buttons
        JButton connectButton = new JButton("Verbinden");
        JButton exitButton = new JButton("Beenden");

        // Apply common size to all buttons
        connectButton.setPreferredSize(buttonSize);        
        exitButton.setPreferredSize(buttonSize);

        // Force button size by setting maximum and minimum sizes
        connectButton.setMaximumSize(buttonSize);      
        exitButton.setMaximumSize(buttonSize);

        connectButton.setMinimumSize(buttonSize);    
        exitButton.setMinimumSize(buttonSize);

        // Set font and action listener for each button
        connectButton.setFont(new Font("Arial", Font.PLAIN, 16));
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create a panel for user input
                JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
                inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                JTextField urlField = new JTextField();
                JTextField dbNameField = new JTextField();
                JTextField userField = new JTextField();
                JPasswordField passwordField = new JPasswordField();

                // Add labels and fields to the panel
                inputPanel.add(new JLabel("Server URL:"));
                inputPanel.add(urlField);
                inputPanel.add(new JLabel("Name der Datenbank:"));
                inputPanel.add(dbNameField);
                inputPanel.add(new JLabel("Benutzername:"));
                inputPanel.add(userField);
                inputPanel.add(new JLabel("Passwort:"));
                inputPanel.add(passwordField);

                // Create a custom dialog panel
                JPanel dialogPanel = new JPanel(new BorderLayout());

                // Create a panel for the title and center it
                JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JLabel titleLabel = new JLabel("Verbinde mit MySQL-Server");
                titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
                titlePanel.add(titleLabel);

                // Add the title panel and input panel to the dialog panel
                dialogPanel.add(titlePanel, BorderLayout.NORTH);
                dialogPanel.add(inputPanel, BorderLayout.CENTER);

                int option = JOptionPane.showConfirmDialog(
                        frame,
                        dialogPanel,
                        "Datenbankverbindung",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (option == JOptionPane.OK_OPTION) {
                    url = urlField.getText();
                    dbName = dbNameField.getText();
                    user = userField.getText();
                    password = new String(passwordField.getPassword());

                    connectClicked = true;
                    frame.dispose(); // Close the connection menu only on a successful connection
                } else {
                    JOptionPane.showMessageDialog(frame, "Verbindung abgebrochen.", "Abbruch", JOptionPane.WARNING_MESSAGE);
                    // Do not exit; just keep the frame open
                }
            }
        });

        
        exitButton.setFont(new Font("Arial", Font.PLAIN, 16));
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // Close the application
            }
        });

        // Center-align the buttons in the panel
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add buttons to the panel
        buttonPanel.add(connectButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Space between buttons
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Space between buttons
        buttonPanel.add(exitButton);

        // Add the panel to the frame
        frame.add(buttonPanel, BorderLayout.CENTER);

        // Set preferred and minimum dimensions for the frame to make it taller and less wide
        frame.setPreferredSize(new Dimension(400, 250)); // Width, height
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    /**
     * Checks if the connect button was clicked.
     *
     * @return {@code true} if the connect button was clicked, {@code false} otherwise.
     */
    public boolean isConnectClicked() {
        return connectClicked;
    }

    /**
     * Returns the URL of the MySQL server.
     *
     * @return The MySQL server URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the name of the database to connect to.
     *
     * @return The database name.
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * Returns the username for the database connection.
     *
     * @return The username.
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the password for the database connection.
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

}
