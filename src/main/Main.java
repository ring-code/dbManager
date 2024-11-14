package main;

import java.sql.SQLException;

import gui.ConnectionMenu;
import gui.MainFrame;
import mySQL.MySQLConnector;
import mySQL.MySQLDataHandler;
import utility.Messages;


/** Constructs the Connection Menu to allow the User to set up a MySQLConnector and a Handler for that Connector or exit the Application.
 *  Opens up the MainFrame if the Connection is established.
 *  
 *  @author ring-code
 */
public class Main {

    public static void main(String[] args) {
        
    	

        while (true) { 
        	 //open up the connection menu
            ConnectionMenu connMenu = new ConnectionMenu();
           
            while (!connMenu.isConnectClicked()) {
                try {
                    Thread.sleep(100); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Attempt to create MySQLConnector with user input
            try {
                MySQLConnector connector = new MySQLConnector(
                        connMenu.getUrl(),
                        connMenu.getUser(),
                        connMenu.getPassword(),
                        connMenu.getDbName()
                );

                // If connection is successful, create QueryHandler and MainFrame
                MySQLDataHandler sqlHandler = new MySQLDataHandler(connector);
                MainFrame mainFrame = new MainFrame(sqlHandler); 


                // Listen for when the main frame is closed to reinitialize the connection menu
                while (mainFrame.isOpen()) {
                    try {
                        Thread.sleep(100); 
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            } catch (SQLException e) {
                Messages.showErrorMessage("Verbindung zur Datenbank fehlgeschlagen", null);;
                // The connection menu will be reopened for retry
            }
        }
    }
}
