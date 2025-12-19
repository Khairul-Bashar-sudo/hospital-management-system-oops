public class ConsoleColors {

    public static final String RESET = "\033[0m";
    public static final String GREEN_BOLD = "\033[1;32m";
    public static final String RED_BOLD = "\033[1;31m";
    public static final String CYAN_BOLD = "\033[1;36m";
    public static final String YELLOW_BOLD = "\033[1;33m";

    public static void printlnSuccess(String msg) {
        System.out.println(GREEN_BOLD + msg + RESET);
    }

    public static void printlnError(String msg) {
        System.out.println(RED_BOLD + msg + RESET);
    }

    public static void printlnBold(String msg) {
        System.out.println(YELLOW_BOLD + msg + RESET);
    }

    public static void printlnTitle(String msg) {
        System.out.println(CYAN_BOLD + msg + RESET);
    }
}
