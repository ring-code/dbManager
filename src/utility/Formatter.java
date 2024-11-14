package utility;


/** Formats SQL names to look more friendly to the User.
 * @author ring-code
 */
public class Formatter {
	
	/**Formats SQL names to look more friendly to the User
	 * @param tableName 
	 * @return friendlyName, _ replaced by empty space and Capitalized
	 */
	public static String formatName(String tableName) {
        
		if (tableName == null || tableName.isEmpty()) {
            return "";
        }
        
        String[] words = tableName.split("_");
        StringBuilder friendlyName = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                friendlyName.append(Character.toUpperCase(word.charAt(0)))
                             .append(word.substring(1).toLowerCase())
                             .append(" ");
            }
        }
        return friendlyName.toString().trim(); 
    }
	
	

}
