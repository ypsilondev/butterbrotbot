package tech.ypsilon.bbbot.util;

/**
 * Utility methods for handling KIT students and their campus credentials
 *
 * @author Christian Schliz
 * @version 1.0
 */
public final class StudentUtil {

    /**
     * Regex matches for all personal student u-codes, which contain
     * one 'u', followed by four lowercase english letters
     */
    public static final String STUDENT_CODE_REGEX = "^u[a-z]{4}$";

    private StudentUtil() {
        throw new RuntimeException();
    }

    /**
     * Checks, whether any given string is a personal student u-code.
     *
     * @param input The input to check
     * @return true or false whether the input is a valid u-code
     */
    public static boolean isStudentCode(String input) {
        return input.matches(STUDENT_CODE_REGEX);
    }

}
