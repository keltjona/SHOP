import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.*;
import java.util.prefs.Preferences;

public class AdminView extends Application {

    private UserManager userManager;
    private InventoryManager inventoryManager;
    private POSSystem posSystem;
    private CustomerManager customerManager;

    public AdminView(UserManager userManager){
        this.userManager = userManager;
        this.inventoryManager= new InventoryManager();
        this.posSystem=new POSSystem();
        this.customerManager = new CustomerManager();
    }

    private Tab createTab(String title, boolean closable) {
        Tab tab = new Tab(title);
        tab.setClosable(closable);
        return tab;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Admin Dashboard - Grocery Store Management System");

        TabPane tabPane = new TabPane();

        Tab inventoryTab = createTab("Inventory Management", false);
        setupInventoryTab(inventoryTab);

        Tab userManagementTab = createTab("User Management", false);
        setupUserManagementTab(userManagementTab);

        Tab customerManagementTab = createTab("Customer Management", false);
        setupCustomerManagementTab(customerManagementTab);

        Tab salesReportTab = createTab("Sales Reports", false);
        setupSalesReportTab(salesReportTab);

        Tab settingsTab = createTab("Settings", false);
        setupSettingsTab(settingsTab);

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> {
            customerManager = null;
            inventoryManager = null;
            posSystem = null;
            primaryStage.close();
            launchLoginView();
        });

        tabPane.getTabs().addAll(inventoryTab, userManagementTab, customerManagementTab, salesReportTab, settingsTab);



        HBox logoutLayout = new HBox(10, logoutButton);
        logoutLayout.setAlignment(Pos.TOP_RIGHT);

        BorderPane layout = new BorderPane();
        layout.setCenter(tabPane);
        layout.setTop(logoutLayout);

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        applySettings(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void launchLoginView() {
        Platform.runLater(() -> {
            try {
                new LoginView(userManager, new POSController(inventoryManager), new AuthController()).start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void setupSettingsTab(Tab tab) {
        Preferences prefs = Preferences.userNodeForPackage(AdminView.class);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        ComboBox<String> fontSizeComboBox = new ComboBox<>();
        fontSizeComboBox.getItems().addAll("Small", "Medium", "Large");
        fontSizeComboBox.setValue(prefs.get("fontSize", "Medium"));

        ColorPicker bgColorPicker = new ColorPicker(Color.web(prefs.get("bgColor", "#FFFFFF")));

        Button applyButton = new Button("Apply Settings");
        applyButton.setOnAction(e -> {
            prefs.put("fontSize", fontSizeComboBox.getValue());

            prefs.put("bgColor", toHexString(bgColorPicker.getValue()));
            applySettings(layout.getScene());
        });

        layout.getChildren().addAll(new Label("Font Size:"), fontSizeComboBox,
                new Label("Background Color:"), bgColorPicker,
                applyButton);

        tab.setContent(layout);
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public void applySettings(Scene scene) {
        Preferences prefs = Preferences.userNodeForPackage(AdminView.class);
        String fontSize = prefs.get("fontSize", "Medium");
        String fontColor = prefs.get("fontColor", "#000000");
        String bgColor = prefs.get("bgColor", "#FFFFFF");


        String style = "";

        switch (fontSize) {
            case "Small":
                style += "-fx-font-size: 16pt;";
                break;
            case "Medium":
                style += "-fx-font-size: 18pt;";
                break;
            case "Large":
                style += "-fx-font-size: 20pt;";
                break;
        }

        style += "-fx-background-color: " + bgColor + ";";

        scene.getRoot().setStyle(style);
    }

    private void setupInventoryTab(Tab tab) {
        InventoryManager inventoryManager = new InventoryManager();
        TableView<InventoryItem> inventoryTable = new TableView<>();
        setupInventoryTableColumns(inventoryTable);

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> showAddItemDialog(inventoryTable, inventoryManager));

        Button updateButton = new Button("Update");
        updateButton.setOnAction(e -> showUpdateItemDialog(inventoryTable, inventoryManager));

        Button restockButton = new Button("Restock");
       restockButton.setOnAction(e -> showRestockItemDialog(inventoryTable, inventoryManager));

        Button removeButton = new Button("Remove");
        removeButton.setOnAction(e -> {
            InventoryItem selectedItem = inventoryTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                inventoryManager.removeItem(selectedItem);
                inventoryTable.setItems(FXCollections.observableArrayList(inventoryManager.getAllItems()));
            }
        });

        inventoryTable.setItems(FXCollections.observableArrayList(inventoryManager.getAllItems()));

        HBox buttonBar = new HBox(10, addButton, updateButton, removeButton, restockButton);
        buttonBar.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, inventoryTable, buttonBar);
        layout.setAlignment(Pos.CENTER);

        tab.setContent(layout);
    }

    private void setupInventoryTableColumns(TableView<InventoryItem> table) {

        double columnMultiplier = 1.0/4.0;

        TableColumn<InventoryItem, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));

        TableColumn<InventoryItem, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryColumn.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));

        TableColumn<InventoryItem, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));

        TableColumn<InventoryItem, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));

        table.getColumns().addAll(nameColumn, categoryColumn, priceColumn, quantityColumn);
    }


    private void showAddItemDialog(TableView<InventoryItem> table, InventoryManager manager) {
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Item");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        TextField supplierField = new TextField();
        supplierField.setPromptText("Supplier");
        TextField purchasePriceField = new TextField();
        purchasePriceField.setPromptText("Purchase Price");
        TextField amountBoughtField = new TextField();
        amountBoughtField.setPromptText("Amount Bought");
        Label warningLabel = new Label();
        warningLabel.setTextFill(Color.RED);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Supplier:"), 0, 3);
        grid.add(supplierField, 1, 3);
        grid.add(new Label("Purchase Price:"), 0, 5);
        grid.add(purchasePriceField, 1, 5);
        grid.add(new Label("Amount Bought:"), 0, 6);
        grid.add(amountBoughtField, 1, 6);
        grid.add(warningLabel, 1, 7);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String itemName = nameField.getText();
                if (manager.findItem(itemName) != null) {
                    warningLabel.setText("Item already exists!");
                    return null;
                }else{
                return new InventoryItem(
                        nameField.getText(),
                        categoryField.getText(),
                        Double.parseDouble(priceField.getText()),
                        Integer.parseInt(amountBoughtField.getText()),
                        supplierField.getText(),
                        Double.parseDouble(purchasePriceField.getText()),
                        Integer.parseInt(amountBoughtField.getText())
                );
            }}
            return null;
        });

        while (true) {
            Optional<InventoryItem> result = dialog.showAndWait();
            if (result.isPresent()) {
                InventoryItem newItem = result.get();
                manager.addItem(newItem);
                table.setItems(FXCollections.observableArrayList(manager.getAllItems()));
                break;
            } else if (!result.isPresent() || warningLabel.getText().isEmpty()) {
                break;
            }
        }
    }

    private void showRestockItemDialog(TableView<InventoryItem> table, InventoryManager manager) {
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Restock Item");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<InventoryItem> itemComboBox = new ComboBox<>();
        itemComboBox.setItems(FXCollections.observableArrayList(manager.getAllItems()));
        itemComboBox.setPromptText("Select Item");

        itemComboBox.setCellFactory(lv -> new ListCell<InventoryItem>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
        itemComboBox.setButtonCell(new ListCell<InventoryItem>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });

        TextField purchasePriceField = new TextField();
        purchasePriceField.setPromptText("Purchase Price");
        TextField amountBoughtField = new TextField();
        amountBoughtField.setPromptText("Amount Bought");

        grid.add(new Label("Item:"), 0, 0);
        grid.add(itemComboBox, 1, 0);
        grid.add(new Label("Purchase Price:"), 0, 1);
        grid.add(purchasePriceField, 1, 1);
        grid.add(new Label("Amount Bought:"), 0, 2);
        grid.add(amountBoughtField, 1, 2);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                InventoryItem selectedItem = itemComboBox.getValue();
                if (selectedItem != null) {
                    try {
                        double purchasePrice = Double.parseDouble(purchasePriceField.getText());
                        int amountBought = Integer.parseInt(amountBoughtField.getText());
                        selectedItem.addStock(amountBought, purchasePrice);
                        manager.updateItem(selectedItem);
                    } catch (NumberFormatException e) {
                        showAlert("Input Error", "Please enter valid numbers for purchase price and amount bought.");
                        return null;
                    }
                } else {
                    showAlert("Selection Error", "Please select an item to restock.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
        table.refresh();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    private void showUpdateItemDialog(TableView<InventoryItem> table, InventoryManager manager) {
        InventoryItem selectedItem = table.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Update Item");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selectedItem.getName());
        TextField categoryField = new TextField(selectedItem.getCategory());
        TextField priceField = new TextField(String.valueOf(selectedItem.getPrice()));
        TextField quantityField = new TextField(String.valueOf(selectedItem.getQuantity()));
        TextField supplierField = new TextField(selectedItem.getSupplier());


        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(quantityField, 1, 3);
        grid.add(new Label("Supplier:"), 0, 4);
        grid.add(supplierField, 1, 4);


        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                    selectedItem.setName(nameField.getText());
                    selectedItem.setCategory(categoryField.getText());
                    selectedItem.setPrice(Double.parseDouble(priceField.getText()));
                    selectedItem.setQuantity(Integer.parseInt(quantityField.getText()));
                    selectedItem.setSupplier(supplierField.getText());
                    return selectedItem;
                }
                return null;

        });

        Optional<InventoryItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            manager.updateItem(item);
            table.setItems(FXCollections.observableArrayList(manager.getAllItems()));
        });
    }



    private void setupUserManagementTab(Tab tab) {
        TableView<User> userTable = new TableView<>();
        setupUserTableColumns(userTable);

        Button addUserButton = new Button("Add User");
        addUserButton.setOnAction(e -> showAddUserDialog(userTable));

        Button removeUserButton = new Button("Remove User");
        removeUserButton.setOnAction(e -> removeSelectedUser(userTable));

        HBox buttonBar = new HBox(10, addUserButton, removeUserButton);
        buttonBar.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, userTable, buttonBar);
        layout.setAlignment(Pos.CENTER);

        tab.setContent(layout);

        refreshUserTable(userTable);
    }

    private void setupUserTableColumns(TableView<User> table) {

        double columnMultiplier = 1.0/2.0;

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));
        roleCol.setCellValueFactory(cellData -> {
            boolean isManager = cellData.getValue().isManager();
            return new ReadOnlyStringWrapper(isManager ? "Manager" : "Cashier");
        });

        table.getColumns().addAll(usernameCol, roleCol);
    }


    private void showAddUserDialog(TableView<User> table) {

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Enter New User Details");


        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        CheckBox isManager = new CheckBox("Is Manager");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        grid.add(isManager, 1, 2);


        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);


        dialog.getDialogPane().setContent(grid);


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new User(username.getText(), password.getText(), isManager.isSelected());
            }
            return null;
        });


        Optional<User> result = dialog.showAndWait();

        result.ifPresent(newUser -> {
            userManager.addUser(newUser);
            refreshUserTable(table);
        });
    }


    private void removeSelectedUser(TableView<User> table) {
        User selectedUser = table.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            userManager.removeUser(selectedUser.getUsername());
            refreshUserTable(table);
        }
    }

    private void refreshUserTable(TableView<User> table) {
        table.setItems(FXCollections.observableArrayList(userManager.getAllUsers()));
    }



    private void setupCustomerManagementTab(Tab tab) {

        TableView<Customer> customerTable = new TableView<>();
        setupCustomerTableColumns(customerTable);

        tab.setOnSelectionChanged(event -> {
            if (tab.isSelected()) {
                customerManager.loadCustomers();
                refreshCustomerTable(customerTable);
            }
        });

        Button addCustomerButton = new Button("Add Customer");
        addCustomerButton.setOnAction(e -> showAddCustomerDialog(customerTable));

        Button updateCustomerButton = new Button("Update Customer");
        updateCustomerButton.setOnAction(e -> showUpdateCustomerDialog(customerTable));

        Button removeCustomerButton = new Button("Remove Customer");
        removeCustomerButton.setOnAction(e -> {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                customerManager.removeCustomer(selectedCustomer.getId());
                refreshCustomerTable(customerTable);
            }
        });
        Button viewTransactionsButton = new Button("View Transactions");
        viewTransactionsButton.setOnAction(e -> {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                showCustomerTransactions(selectedCustomer);
            }
        });


        customerTable.setItems(FXCollections.observableArrayList(customerManager.getAllCustomers()));

        HBox buttonBar = new HBox(10, addCustomerButton, updateCustomerButton, removeCustomerButton, viewTransactionsButton);
        buttonBar.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, customerTable, buttonBar);
        layout.setAlignment(Pos.CENTER);

        tab.setContent(layout);
    }
    private void showCustomerTransactions(Customer customer) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Customer Transactions");


        TableView<Transaction> transactionTable = new TableView<>();
        setupCustomerTransactionTableColumns(transactionTable);


        List<Transaction> customerTransactions = customerManager.getTransactionsForCustomer(customer.getId());
        transactionTable.setItems(FXCollections.observableArrayList(customerTransactions));


        double totalSpent = customerTransactions.stream()
                .mapToDouble(Transaction::getTotalCost)
                .sum();
        Label totalSpentLabel = new Label("Total Spent: $" + String.format("%.2f", totalSpent));

        VBox layout = new VBox(10, totalSpentLabel, transactionTable);
        layout.setAlignment(Pos.CENTER);

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    private void setupCustomerTransactionTableColumns(TableView<Transaction> table) {
        TableColumn<Transaction, Integer> idColumn = new TableColumn<>("Transaction ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("transactionID"));

        TableColumn<Transaction, Double> totalCostColumn = new TableColumn<>("Total Cost");
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));

        TableColumn<Transaction, String> cashierColumn = new TableColumn<>("Cashier");
        cashierColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getCashier().getUsername()));

        table.getColumns().addAll(idColumn, totalCostColumn, cashierColumn);
    }


    private void setupCustomerTableColumns(TableView<Customer> table) {

        double columnMultiplier = 1.0/5.0;

        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));

        TableColumn<Customer, String> surnameCol = new TableColumn<>("Surname");
        surnameCol.setCellValueFactory(new PropertyValueFactory<>("surname"));
        surnameCol.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));

        TableColumn<Customer, Integer> loyaltyPointsCol = new TableColumn<>("Loyalty Points");
        loyaltyPointsCol.setCellValueFactory(new PropertyValueFactory<>("loyaltyPoints"));
        loyaltyPointsCol.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));

        TableColumn<Customer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));

        table.getColumns().addAll(idCol, nameCol, surnameCol, phoneCol, loyaltyPointsCol);
    }

    private void showItemReportPopup() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Item Report");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<InventoryItem> itemComboBox = new ComboBox<>();
        itemComboBox.setItems(FXCollections.observableArrayList(inventoryManager.getAllItems()));
        itemComboBox.setPromptText("Select an item");

        itemComboBox.setCellFactory(lv -> new ListCell<InventoryItem>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });

        itemComboBox.setButtonCell(new ListCell<InventoryItem>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });

        TextField totalPurchasedField = new TextField();
        totalPurchasedField.setEditable(false);
        TextField totalSoldField = new TextField();
        totalSoldField.setEditable(false);
        TextField totalProfitField = new TextField();
        totalProfitField.setEditable(false);
        TextField totalEarned = new TextField();
        totalEarned.setEditable(false);
        TextField totalSpent = new TextField();
        totalSpent.setEditable(false);

        itemComboBox.setOnAction(event -> {
            InventoryItem selectedItem = itemComboBox.getValue();
            if (selectedItem != null) {

                int totalPurchased = selectedItem.getAmountBought();
                double totalEarnedVal = selectedItem.getTotalEarned();
                int totalSold = selectedItem.getAmountSold();
                double totalSpentVal = selectedItem.getTotalSpent();
                double totalProfit = totalEarnedVal-totalSpentVal;

                totalPurchasedField.setText(String.valueOf(totalPurchased));
                totalSpent.setText(String.valueOf(totalSpentVal));
                totalSoldField.setText(String.valueOf(totalSold));
                totalEarned.setText(String.valueOf(totalEarnedVal));
                totalProfitField.setText(String.format("%.2f", totalProfit));
            }
        });

        grid.add(new Label("Item:"), 0, 0);
        grid.add(itemComboBox, 1, 0);
        grid.add(new Label("Amount Purchased:"), 0, 1);
        grid.add(totalPurchasedField, 1, 1);
        grid.add(new Label("Total Spent:"), 0, 2);
        grid.add(totalSpent, 1, 2);
        grid.add(new Label("Amount Sold:"), 0, 3);
        grid.add(totalSoldField, 1, 3);
        grid.add(new Label("Total Earned:"), 0, 4);
        grid.add(totalEarned, 1, 4);
        grid.add(new Label("Total Profit:"), 0, 5);
        grid.add(totalProfitField, 1, 5);


        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait();
    }
    private void showAddCustomerDialog(TableView<Customer> table) {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Add New Customer");


        GridPane grid = createCustomerDialogGrid();


        TextField nameField = new TextField();
        TextField surnameField = new TextField();
        TextField phoneField = new TextField();
        TextField loyaltyPointsField = new TextField();


        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Surname:"), 0, 1);
        grid.add(surnameField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Loyalty Points:"), 0, 3);
        grid.add(loyaltyPointsField, 1, 3);


        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        Platform.runLater(nameField::requestFocus);


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Customer(
                        nameField.getText(),
                        surnameField.getText(),
                        phoneField.getText(),
                        Integer.parseInt(loyaltyPointsField.getText()),
                        customerManager.getAllCustomers().size()
                );
            }
            return null;
        });


        Optional<Customer> result = dialog.showAndWait();
        result.ifPresent(newCustomer -> {
            customerManager.addCustomer(newCustomer);
            refreshCustomerTable(table);
        });
    }

    private void showUpdateCustomerDialog(TableView<Customer> table) {
        Customer selectedCustomer = table.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showAlert("No Selection", "No Customer Selected", "Please select a customer in the table.");
            return;
        }

        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Edit Customer");


        GridPane grid = createCustomerDialogGrid();


        TextField nameField = new TextField(selectedCustomer.getName());
        TextField surnameField = new TextField(selectedCustomer.getSurname());
        TextField phoneField = new TextField(selectedCustomer.getPhone());
        TextField loyaltyPointsField = new TextField(String.valueOf(selectedCustomer.getLoyaltyPoints()));


        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Surname:"), 0, 1);
        grid.add(surnameField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Loyalty Points:"), 0, 3);
        grid.add(loyaltyPointsField, 1, 3);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                selectedCustomer.setName(nameField.getText());
                selectedCustomer.setSurname(surnameField.getText());
                selectedCustomer.setPhone(phoneField.getText());
                selectedCustomer.setLoyaltyPoints(Integer.parseInt(loyaltyPointsField.getText()));
                return selectedCustomer;
            }
            return null;
        });


        Optional<Customer> result = dialog.showAndWait();
        result.ifPresent(customer -> {
            customerManager.updateCustomer(customer);
            refreshCustomerTable(table);
        });
    }

    private GridPane createCustomerDialogGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        return grid;
    }

    private void refreshCustomerTable(TableView<Customer> table) {
        table.setItems(FXCollections.observableArrayList(customerManager.getAllCustomers()));
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


    private void setupSalesReportTab(Tab tab) {
        POSSystem posSystem = new POSSystem();
        TableView<Transaction> transactionTable = new TableView<>();
        setupTransactionTableColumns(transactionTable);

        Button dailyReportButton = new Button("Daily Report");
        dailyReportButton.setOnAction(e -> showReportPopup("Daily Report", posSystem.createDailyReport(new Date())));

        Button itemReportButton = new Button("Item Report");
        itemReportButton.setOnAction(e -> showItemReportPopup());

        Button monthlyReportButton = new Button("Monthly Report");
        monthlyReportButton.setOnAction(e -> {
            Calendar cal = Calendar.getInstance();
            showReportPopup("Monthly Report", posSystem.createMonthlyReport(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)));
        });

        Button yearlyReportButton = new Button("Yearly Report");
        yearlyReportButton.setOnAction(e -> {
            Calendar cal = Calendar.getInstance();
            showReportPopup("Yearly Report", posSystem.createYearlyReport(cal.get(Calendar.YEAR)));
        });

        Button expandButton = new Button("Expand");
        expandButton.setOnAction(e -> {
            Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
            if (selectedTransaction != null) {
                showTransactionDetailsPopup(selectedTransaction);
            }
        });

        expandButton.disableProperty().bind(
                transactionTable.getSelectionModel().selectedItemProperty().isNull()
        );


        transactionTable.setItems(FXCollections.observableArrayList(posSystem.getAllTransactions()));

        HBox buttonBar = new HBox(10, dailyReportButton, monthlyReportButton, yearlyReportButton, itemReportButton, expandButton);
        buttonBar.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, transactionTable, buttonBar);
        layout.setAlignment(Pos.CENTER);

        tab.setContent(layout);
    }

    private void showTransactionDetailsPopup(Transaction transaction) {
        Stage detailsStage = new Stage();
        detailsStage.initModality(Modality.APPLICATION_MODAL);
        detailsStage.setTitle("Transaction Details");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10));


        layout.getChildren().add(new Label("Transaction ID: " + transaction.getTransactionID()));
        layout.getChildren().add(new Label("Date: " + transaction.getDate()));
        layout.getChildren().add(new Label("Total Cost: $" + transaction.getTotalCost()));
        layout.getChildren().add(new Label("Discount: " + transaction.getDiscount()));


        TableView<SaleItem> itemsTable = new TableView<>();
        setupSaleItemsTableColumns(itemsTable);
        itemsTable.setItems(FXCollections.observableArrayList(transaction.getItems()));

        layout.getChildren().add(itemsTable);


        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> detailsStage.close());
        layout.getChildren().add(closeButton);

        Scene scene = new Scene(layout);
        detailsStage.setScene(scene);
        detailsStage.showAndWait();
    }

    private void setupSaleItemsTableColumns(TableView<SaleItem> table) {

        TableColumn<SaleItem, String> nameColumn = new TableColumn<>("Item Name");
        nameColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().item.getName()));


        TableColumn<SaleItem, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));


        TableColumn<SaleItem, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().item.getPrice()));


        TableColumn<SaleItem, Double> subtotalColumn = new TableColumn<>("Subtotal");
        subtotalColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                cellData.getValue().getAmount() * cellData.getValue().item.getPrice()
        ));

        table.getColumns().addAll(nameColumn, quantityColumn, priceColumn, subtotalColumn);
    }

    public void showReportPopup(String title, List<Transaction> transactions) {

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle(title);


        Map<String, Double> cashierEarnings = new HashMap<>();
        Map<String, Integer> cashierTransactions = new HashMap<>();


        for (Transaction transaction : transactions) {
            String cashierName = transaction.getCashier().getUsername();
            cashierEarnings.merge(cashierName, transaction.getTotalCost(), Double::sum);
            cashierTransactions.merge(cashierName, 1, Integer::sum);
        }


        TableView<CashierReport> table = new TableView<>();
        TableColumn<CashierReport, String> nameColumn = new TableColumn<>("Cashier");
        TableColumn<CashierReport, Integer> transactionsColumn = new TableColumn<>("Transactions");
        TableColumn<CashierReport, String> earningsColumn = new TableColumn<>("Earnings");


        nameColumn.setCellValueFactory(new PropertyValueFactory<>("cashierName"));
        transactionsColumn.setCellValueFactory(new PropertyValueFactory<>("transactionCount"));
        earningsColumn.setCellValueFactory(new PropertyValueFactory<>("totalEarnings"));


        table.getColumns().addAll(nameColumn, transactionsColumn, earningsColumn);


        DecimalFormat currencyFormat = new DecimalFormat("$#.00");
        for (String cashier : cashierEarnings.keySet()) {
            CashierReport report = new CashierReport(
                    cashier,
                    cashierTransactions.get(cashier),
                    currencyFormat.format(cashierEarnings.get(cashier))
            );
            table.getItems().add(report);
        }


        VBox layout = new VBox(10, table);


        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> popupStage.close());


        layout.getChildren().add(closeButton);


        Scene scene = new Scene(layout);
        popupStage.setScene(scene);


        popupStage.showAndWait();
    }


    public class CashierReport {
        private String cashierName;
        private Integer transactionCount;
        private String totalEarnings;

        public CashierReport(String cashierName, Integer transactionCount, String totalEarnings) {
            this.cashierName = cashierName;
            this.transactionCount = transactionCount;
            this.totalEarnings = totalEarnings;
        }


        public String getCashierName() {
            return cashierName;
        }

        public void setCashierName(String cashierName) {
            this.cashierName = cashierName;
        }

        public Integer getTransactionCount() {
            return transactionCount;
        }

        public void setTransactionCount(Integer transactionCount) {
            this.transactionCount = transactionCount;
        }

        public String getTotalEarnings() {
            return totalEarnings;
        }

        public void setTotalEarnings(String totalEarnings) {
            this.totalEarnings = totalEarnings;
        }
    }

    private void setupTransactionTableColumns(TableView<Transaction> table) {

        double columnMultiplier = 1.0/4.0;

        TableColumn<Transaction, Integer> idCol = new TableColumn<>("Transaction ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("transactionID"));
        idCol.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));

        TableColumn<Transaction, Double> totalCostCol = new TableColumn<>("Total Cost");
        totalCostCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        totalCostCol.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));


        TableColumn<Transaction, String> cashierCol = new TableColumn<>("Cashier Name");
        cashierCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getCashier().getUsername()));
        cashierCol.prefWidthProperty().bind(table.widthProperty().multiply(columnMultiplier));

        table.getColumns().addAll(idCol, dateCol, cashierCol, totalCostCol);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
