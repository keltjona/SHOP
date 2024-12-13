
import javafx.application.Application;
import javafx.stage.Stage;

/*
*
* Notice to code reader: Start your journey here. This code was written past the early hours of the morning.
* DO NOT ASK WHAT THE CODE DOES I DO NOT KNOW I WAS BORDERLINE DELUSIONAL
*
* CODE FINISHED AT 5:17AM 4/6/2024
*
* send help
* */



public class MainApp extends Application {
    private UserManager userManager;
    private InventoryManager inventoryManager;
    private POSController posController;
    private AuthController authController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        userManager = new UserManager();
        inventoryManager = new InventoryManager();
        authController = new AuthController();

        posController = new POSController(inventoryManager);

        LoginView loginView = new LoginView(userManager, posController, authController);
        loginView.start(primaryStage);
        loginView.showLoginScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
