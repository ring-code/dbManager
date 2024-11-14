package utility;

import java.util.regex.Pattern;

/** Used to validate User String inputs into the Database. Will accept a few letters for ZIP code for non-german locations.
 *  Empty inputs are only accepted for telephone and email in validateInputByColumnName().
 *  
 *  @author ring-code
 */ 



public class StringInputValidation {

    /**
     * Checks if the given string contains only numbers and is not just zeros.
     *
     * @param str the string to be checked
     * @return {@code true} if the string is non-empty, contains only digits, and is not all zeros;
     *         {@code false} otherwise
     */
    public static boolean onlyNumbers(String str) {
        return !str.isEmpty() && str.matches("\\d+") && !str.matches("0+"); 
    }

    /**
     * Validates if the given string is a valid name. A valid name contains only letters, spaces, periods, or hyphens,
     * and does not contain consecutive periods or hyphens.
     *
     * @param str the string to be validated
     * @return {@code true} if the string is non-empty and matches the name pattern;
     *         {@code false} otherwise
     */
    public static boolean validName(String str) {
        return !str.isEmpty() && str.matches("^(?=.*[\\p{L}])[\\p{L}\\s.-]*$") && !str.matches(".*[.-]{2,}.*");
    }

    /**
     * Validates if the given string is a valid address. A valid address must contain at least one letter, one digit,
     * and should not contain consecutive periods or hyphens.
     *
     * @param str the string to be validated
     * @return {@code true} if the string is non-empty, contains both letters and digits, and matches the address pattern;
     *         {@code false} otherwise
     */
    public static boolean validAddress(String str) {
        return !str.isEmpty() && str.matches(".*\\p{L}.*") && str.matches(".*\\d.*") && !str.matches(".*[.-]{2,}.*");
    }

    /**
     * Validates if the given string is a valid phone number. A valid phone number can contain digits, spaces, slashes,
     * hyphens, and a single plus sign. No consecutive slashes or hyphens.
     * 
     *
     * @param str the string to be validated
     * @return {@code true} if the string is non-empty and matches the phone number pattern;
     *         {@code false} otherwise
     */
    public static boolean validPhoneNumber(String str) {
        // Check if the string is non-empty and matches the phone number pattern
        // The pattern allows digits, spaces, slashes, hyphens, and at most one plus sign (+)
        return !str.isEmpty() && 
               str.matches("^[\\d\\s/\\-+]+$") && 
               !str.matches(".*[\\s/\\-+]{2,}.*") &&
               str.chars().filter(c -> c == '+').count() <= 1;
    }

    /**
     * Validates if the given string is a valid ZIP code. A valid ZIP code can contain digits, letters, spaces, or hyphens,
     * must not contain consecutive hyphens, and should have no more than three letters with at least one digit.
     *
     * @param str the string to be validated
     * @return {@code true} if the string is non-empty, matches the ZIP code pattern, and meets the criteria for letters and digits;
     *         {@code false} otherwise
     */
    public static boolean validZipCode(String str) {
        return !str.isEmpty() && 
               str.matches("^[\\dA-Za-z\\s-]+$") && 
               !str.matches(".*-{2,}.*") && 
               str.chars().filter(Character::isLetter).count() <= 3 && 
               str.chars().anyMatch(Character::isDigit);
    }

    /**
     * Validates if the given string is a valid email address. A valid email address must match a standard email pattern.
     *
     * @param str the string to be validated
     * @return {@code true} if the string matches the email address pattern and is not empty;
     *         {@code false} otherwise
     */
    public static boolean validEmail(String str) {
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return !str.isEmpty() && Pattern.compile(emailRegex).matcher(str).matches();
    }
    
    /**
     * Validates the input value based on the specified column name.
     * This method performs different validation checks depending on the column name.
     * For example, it checks if the input is a valid name, address, zip code, phone number, etc.
     * 
     * <p>The following validations are performed for each column name:</p>
     * <ul>
     *   <li>"vorname", "nachname", "ort", "firma", "branche": Validates that the input is a valid name.</li>
     *   <li>"straße": Validates that the input is a valid address.</li>
     *   <li>"postleitzahl": Validates that the input is a valid zip code.</li>
     *   <li>"telefon": Validates that the input is a valid phone number (or empty).</li>
     *   <li>"email": Validates that the input is a valid email address (or empty).</li>
     * </ul>
     * 
     * <p>If the column name is not recognized, the method returns {@code false} by default.</p>
     * 
     * @param columnName The name of the column to validate the input against. This value determines which validation rule will be applied.
     * @param inputValue The input value to be validated for the specified column. This is typically the value entered by the user.
     * @return {@code true} if the input value is valid for the given column name, {@code false} otherwise.
     */
    public static boolean validateInputByColumnName(String columnName, String inputValue) {
        switch (columnName.toLowerCase()) {
            case "vorname":
                return validName(inputValue);
            case "nachname":
                return validName(inputValue);
            case "straße":
                return validAddress(inputValue);
            case "postleitzahl":
                return validZipCode(inputValue);
            case "ort":
                return validName(inputValue);
            case "telefon":
                return inputValue.isEmpty() || validPhoneNumber(inputValue);    
            case "email":
                return inputValue.isEmpty() || validEmail(inputValue);
            case "firma":
            	return validName(inputValue);
            case "branche":
            	return validName(inputValue);
            default:
                return false; // add more cases as needed
        }
    }

    
}
