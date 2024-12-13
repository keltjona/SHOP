public class AuthController {
    private UserManager userManager;

    public AuthController() {
        userManager = new UserManager();
    }

    public User login(String username, String password) {
        for (User user : userManager.getAllUsers()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
}
