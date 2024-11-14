package utility;

import javax.swing.JOptionPane;

/** 
 *  Various Messages to refer to for informing the User (not the developer), has also Methods to show Custom Messages.
 	@author ring-code
 * 
*/


public class Messages {
    
    public static final String DATABASE_INITIALIZATION_ERROR = "Fehler bei der Initialisierung der Datenbank.";
    public static final String TABLE_LOAD_ERROR = "Fehler beim Laden der Tabelle.";
    public static final String DETAILS_LOAD_ERROR = "Fehler beim Anzeigen der Details.";
    public static final String ENTRY_DELETION_ERROR = "Fehler beim Löschen des Datensatzes.";    
    public static final String EXIT_PROGRAM_ERROR = "Fehler beim Beenden des Programms.";
    public static final String RECORD_SELECTION_ERROR = "Bitte wählen Sie einen Datensatz aus der Tabelle aus.";
    public static final String DELETE_FAILED_ERROR = "Löschen des Datensatzes fehlgeschlagen";
    public static final String CONNECTION_FAILED_ERROR = "Verbindung zur Datenbank konnte nicht hergestellt werden";
    public static final String CHOOSE_ENTRY_ERROR = "Bitte wählen sie einen Datensatz aus der Tabelle aus";    
    public static final String SHOW_DETAILS_ERROR = "Fehler beim Anzeigen der Details";
    
    
    public static final String INVALID_NAME_ERROR = "Bitte geben Sie eine gültige ID (nur Zahlen größer Null) ein.";
    public static final String INVALID_NACHNAME_ERROR = "Bitte geben Sie einen gültigen Namen ein.";
    public static final String INVALID_ADDRESS_ERROR = "Bitte geben Sie eine gültige Adresse ein.";
    public static final String INVALID_PHONE_ERROR = "Bitte geben Sie eine gültige Telefonnummer ein. ";
    public static final String INVALID_ZIP_CODE_ERROR = "Bitte geben Sie eine gültige Postleitzahl ein.";
    public static final String INVALID_EMAIL_ERROR = "Bitte geben Sie eine gültige E-Mail-Adresse ein.";
    public static final String INVALID_BRANCHE_ERROR = "Bitte geben Sie eine gültige Branche ein.";
    public static final String INVALID_FIRMA_ERROR = "Bitte geben Sie einen gültigen Firmennamen ein.";
    public static final String INVALID_INPUT_ERROR = "Bitte geben sie einen gültigen Wert ein";
    
    public static final String NOTHING_FOUND_ERROR = "Keine Einträge gefunden";
	public static final String PRIMARY_KEY_NOT_FOUND_ERROR = "Kein Eintrag mit dieser Nummer gefunden.";
    
    public static final String DELETE_ENTRY_CONFIRM = "Sind sie sicher, dass der Eintrag gelöscht werden soll?";
	
	/**
     * Shows an error message dialog.
     *
     * @param errorMessage The error message to display.
     * @param title        An optional title for the dialog. If null, "Error" is used.
     */
    
    public static void showErrorMessage(String errorMessage, String title) {
        
        if (title == null || title.trim().isEmpty()) {
            title = "Error";
        }

        // Create a JOptionPane to display the error message
        JOptionPane.showMessageDialog(
            null,                    // This makes the dialog modal to the entire application
            errorMessage,            // Message to display
            title,                   // Title of the dialog
            JOptionPane.ERROR_MESSAGE // Message type for error dialog
        );
    }
    
    
    /**
     * Shows an informational message dialog.
     *
     * @param message The message to display.
     * @param title   An optional title for the dialog. If null, "Information" is used.
     */
           
    public static void showInfoMessage(String message, String title) {
        if (title == null || title.trim().isEmpty()) {
            title = "Information";
        }

        // Create a JOptionPane to display the message
        JOptionPane.showMessageDialog(
            null,                    // This makes the dialog modal to the entire application
            message,                 // Message to display
            title,                   // Title of the dialog
            JOptionPane.INFORMATION_MESSAGE // Message type for neutral dialog
        );
    }

}
