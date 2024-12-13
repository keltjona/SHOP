
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginView extends Application {
    private UserManager userManager;
    private POSController posController;
    private Stage primaryStage;

    private AuthController authController;

    AdminView managerView;


    public LoginView(UserManager userManager, POSController posController, AuthController authController) {
        this.userManager = userManager;
        this.posController = posController;
        this.authController = authController;
        managerView = new AdminView(userManager);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLoginScreen();
    }

    public void showLoginScreen() {
        primaryStage.setTitle("Login - Grocery Store Management System");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Button loginButton = new Button("Login");
        Text actionText = new Text();

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 1, 2);
        grid.add(actionText, 1, 3);

        loginButton.setOnAction(e -> {
            User user = authController.login(usernameField.getText(), passwordField.getText());
            if (user != null) {
                primaryStage.close();
                if (user.isManager()) {
                    launchManagerUI(user);
                } else {
                    launchCashierUI(user);
                }
            } else {
                actionText.setText("Login failed. Please try again.");
            }
        });

        Scene scene = new Scene(grid, 1280, 720);
        primaryStage.setScene(scene);
        managerView.applySettings(scene);
        primaryStage.show();
    }

    private void launchManagerUI(User loggedUser) {
        managerView.start(primaryStage);
    }

    private void launchCashierUI(User loggedUser) {
        CashierView cashierView = new CashierView(loggedUser);
        cashierView.start(primaryStage);
    }
}
