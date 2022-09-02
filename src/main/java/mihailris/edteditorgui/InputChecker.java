package mihailris.edteditorgui;

public class InputChecker {
    /**
     * Class of return value may be different with cls (example: cls=Long, return=Integer)
     * @param input string input from text field
     * @param cls class of data required
     * @return null if input is incorrect
     */
    public static Object checkAndParse(String input, Class<?> cls){
        if (cls == String.class)
            return input;
        if (cls == Integer.class || cls == Long.class){
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e){
                return null;
            }
        }
        if (cls == Float.class){
            try {
                return Float.parseFloat(input);
            } catch (NumberFormatException e){
                return null;
            }
        }
        if (cls == Double.class){
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e){
                return null;
            }
        }
        if (cls == Boolean.class){
            if (input.equals("true"))
                return true;
            else if (input.equals("false"))
                return false;
            return null;
        }
        return null;
    }
}
