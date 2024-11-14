package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import utility.Formatter;
import utility.StringInputValidation;


/** Can be opened from the MainFrame and enables the User to search (exact) in the current Table for a primary Key. 
 *  Empty or non-integer input is checked by StringInputValidation and will deactivate the search Button.
 * 	
 * 	@author ring-code	
 *  
 */

public class SimpleSearchWindow extends JDialog {

    private static final long serialVersionUID = 1L;
    private String input;
    private JTextField inputField;
    private JButton searchButton;
    private JButton cancelButton;
    private JLabel promptLabel;

    public SimpleSearchWindow(JFrame parentFrame, String currentTableName) {
        
    	super(parentFrame, "Suche Nummer", true); // Modal dialog
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Add padding around components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Create the prompt label with the table name
        promptLabel = new JLabel("Suche in: " + Formatter.formatName(currentTableName), SwingConstants.CENTER);
        promptLabel.setFont(new Font("SansSerif", Font.BOLD, 14)); // Set a larger, bold font
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(promptLabel, gbc);

        inputField = new JTextField(25);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(inputField, gbc);

        // Create a panel for buttons with GridBagLayout for better control
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.insets = new Insets(5, 5, 5, 5); // Padding around buttons
        buttonGbc.fill = GridBagConstraints.HORIZONTAL;
        buttonGbc.weightx = 1.0;

        searchButton = new JButton("Suchen");
        buttonGbc.gridx = 0;
        buttonGbc.gridy = 0;
        buttonPanel.add(searchButton, buttonGbc);
        searchButton.setEnabled(false);

        cancelButton = new JButton("Abbrechen");
        buttonGbc.gridx = 1;
        buttonGbc.gridy = 0;
        buttonPanel.add(cancelButton, buttonGbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        // Add action listeners
        searchButton.addActionListener(e -> {
            input = inputField.getText();
            dispose();
        });

        cancelButton.addActionListener(e -> {
            input = null;
            dispose();
        });

        
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateButtonState();
                if (e.getKeyCode() == KeyEvent.VK_ENTER && searchButton.isEnabled()) {
                    searchButton.doClick(); // Simulate button click
                }
            }
        });

        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(parentFrame);
    }

    public String showInputDialog() {
        setVisible(true);
        return input;
    }

    private void updateButtonState() {
        boolean validInput = StringInputValidation.onlyNumbers(inputField.getText()); //this is the validation to enable the button
        searchButton.setEnabled(validInput);
    }
}
