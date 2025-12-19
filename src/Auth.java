import java.util.HashMap;

public class Auth {

    private static class User {
        String password;
        String role;
        User(String pass, String role) {
            this.password = pass;
            this.role = role;
        }
    }

    private static HashMap<String, User> users = new HashMap<>();

    static {
        users.put("admin", new User("1234", "ADMIN"));
        users.put("doctor1", new User("1111", "DOCTOR"));
        users.put("reception", new User("2222", "RECEPTION"));
    }

    public static String login(String username, String password) {
        if (!users.containsKey(username)) return null;
        User u = users.get(username);

        if (u.password.equals(password))
            return u.role;  // return role on success

        return null;
    }
}
