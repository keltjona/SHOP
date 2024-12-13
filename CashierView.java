
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.NumberStringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CashierView extends Application {
    private TableView<InventoryItem> inventoryTable;
    private TableView<SaleItem> cartTable;
    private TextField searchField, customerIdField;
    private Label totalCostLabel, loyaltyPointsLabel;
    private Button applyLoyaltyPointsButton, loadCustomerButton;
    private ObservableList<InventoryItem> inventoryItems;
    private ObservableList<SaleItem> cartItems;
    private double totalCost = 0.0;

    private Button newCustomerButton;
    private int discount = 0;

    Button finishTransactionButton;

    private InventoryManager inventoryManager;
    private CustomerManager customerManager;
    private POSSystem posSystem;
    private POSController posController;
    private Customer currentCustomer;
    private User currentCashier;
    private LoginView loginView;
    Label customerInfoLabel;
    AdminView adminView;

    public CashierView(User user) {
        this.currentCashier = user;
        loginView = new LoginView(new UserManager(), new POSController(inventoryManager), new AuthController());
    }

    @Override
    public void start(Stage primaryStage) {
        adminView = new AdminView(new UserManager());
        inventoryManager = new InventoryManager();
        customerManager = new CustomerManager();
        posSystem = new POSSystem();
        posController = new POSController(inventoryManager);

        inventoryItems = FXCollections.observableArrayList(inventoryManager.getAllItems());
        cartItems = FXCollections.observableArrayList();
        newCustomerButton = new Button("New Customer");
        newCustomerButton.setOnAction(e -> showNewCustomerDialog());

        BorderPane root = new BorderPane();

        searchField = new TextField();
        searchField.setPromptText("Search for items...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            inventoryTable.setItems(FXCollections.observableArrayList(
                    inventoryManager.getAllItems().stream()
                            .filter(item -> item.getName().toLowerCase().contains(newValue.toLowerCase()))
                            .collect(Collectors.toList())));
        });

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.close();
            Stage loginStage = new Stage();
            try {
                loginView.start(loginStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox topPanel = new HBox(searchField, logoutButton);
        topPanel.setAlignment(Pos.CENTER);

        inventoryTable = new TableView<>();
        cartTable = new TableView<>();
        cartTable.setEditable(true);
        setupInventoryTable();
        setupCartTable();

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(inventoryTable, cartTable);
        splitPane.setDividerPositions(0.5f);

        totalCostLabel = new Label("Total Cost: $0.00");
        customerIdField = new TextField();
        customerIdField.setPromptText("Enter customer ID");
        loadCustomerButton = new Button("Load");
        loadCustomerButton.setOnAction(e -> loadCustomerData());
        loyaltyPointsLabel = new Label("Loyalty Points: 0");
        applyLoyaltyPointsButton = new Button("+");
        applyLoyaltyPointsButton.setOnAction(e -> applyLoyaltyPoints());

        customerInfoLabel = new Label();
        finishTransactionButton = new Button("Finish");
        finishTransactionButton.setOnAction(e -> finalizeTransaction());

        HBox customerPanel = createCustomerPanel();
        customerPanel.setAlignment(Pos.CENTER);

        HBox transactionPanel = createTransactionPanel(customerPanel);
        transactionPanel.setAlignment(Pos.CENTER);

        root.setTop(topPanel);
        root.setCenter(splitPane);
        root.setBottom(transactionPanel);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        adminView.applySettings(scene);
        primaryStage.setTitle("Cashier View");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void setupInventoryTable() {

        double columnMultiplier = 1.0/4.0;

        TableColumn<InventoryItem, String> nameColumn = new TableColumn<>("Item Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(columnMultiplier));

        TableColumn<InventoryItem, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(columnMultiplier));

        TableColumn<InventoryItem, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(columnMultiplier));

        TableColumn<InventoryItem, Void> addActionColumn = new TableColumn<>("Add");
        addActionColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(columnMultiplier));
        addActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button addButton = new Button("+");

            {
                addButton.setOnAction(e -> {
                    InventoryItem inventoryItem = getTableView().getItems().get(getIndex());
                    SaleItem saleItem = findSaleItemInCart(inventoryItem)
                            .orElse(new SaleItem(inventoryItem, 0));
                    int newQuantity = saleItem.getAmount() + 1;
                    if (newQuantity <= inventoryItem.getQuantity()) {
                        updateCartWithItem(inventoryItem, newQuantity);
                        refreshTotalCost();
                        cartTable.refresh();
                    } else {
                        showAlert("Stock Limit", "Cannot add more than available stock.");
                    }
                });

            }


            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : addButton);
            }
        });

        inventoryTable.getColumns().addAll(nameColumn, priceColumn, quantityColumn, addActionColumn);
        refreshInventoryTable();
    }

    private void refreshInventoryTable() {
        inventoryItems.setAll(inventoryManager.getAllItems().stream()
                .filter(item -> item.getQuantity() > 0)
                .collect(Collectors.toList()));
        inventoryTable.setItems(inventoryItems);
    }


    private void setupCartTable() {

        double columnMultiplier = 1.0/5.0;

        TableColumn<SaleItem, String> itemNameColumn = new TableColumn<>("Item Name");
        itemNameColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getItem().getName()));
        itemNameColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(columnMultiplier));

        TableColumn<SaleItem, Double> itemPriceColumn = new TableColumn<>("Price");
        itemPriceColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getItem().getPrice()));
        itemPriceColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(columnMultiplier));

        TableColumn<SaleItem, Double> totalCostColumn = new TableColumn<>("Total Cost");
        totalCostColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                cellData.getValue().getAmount() * cellData.getValue().getItem().getPrice()));
        totalCostColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(columnMultiplier));

        TableColumn<SaleItem, Void> removeActionColumn = new TableColumn<>("Remove");
        removeActionColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(columnMultiplier));
        removeActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button removeButton = new Button("-");

            {
                removeButton.setOnAction(e -> {
                    SaleItem saleItem = getTableView().getItems().get(getIndex());
                    removeCartItem(saleItem, 1);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });

        TableColumn<SaleItem, Integer> itemQuantityColumn = new TableColumn<>("Quantity");
       itemQuantityColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(columnMultiplier));
        itemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        itemQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        itemQuantityColumn.setOnEditCommit(event -> {
            SaleItem saleItem = event.getRowValue();
            int newAmount = event.getNewValue();
            if (newAmount > 0 && newAmount <= saleItem.getItem().getQuantity()) {
                saleItem.setAmount(newAmount);
                refreshTotalCost();
            } else {
                event.getTableView().refresh();
                showAlert("Invalid Quantity", "Please enter a valid quantity within stock limits.");
            }
        });

        cartTable.getColumns().addAll(itemNameColumn, itemPriceColumn, itemQuantityColumn, totalCostColumn, removeActionColumn);
        cartTable.setItems(cartItems);

    }

    private void removeCartItem(SaleItem saleItem, int quantity) {
        if (saleItem.getAmount() >= quantity) {
            saleItem.setAmount(saleItem.getAmount() - quantity);
            refreshTotalCost();
        }
        cartItems.removeIf(item -> item.getAmount() <= 0);
        cartTable.refresh();
    }



    private void refreshTotalCost() {
        totalCost = cartItems.stream()
                .mapToDouble(saleItem -> saleItem.getAmount() * saleItem.getItem().getPrice())
                .sum() - discount;
        totalCostLabel.setText("Total Cost: $" + String.format("%.2f", totalCost));
    }


    private void loadCustomerData() {
        customerInfoLabel.setText("");
        String customerId = customerIdField.getText();
        if (customerId == null || customerId.isEmpty() || customerId.equals(" ")){
            currentCustomer = customerManager.findCustomer(0);
        }else{
            currentCustomer = customerManager.findCustomer(Integer.parseInt(customerId));
        }
        if (currentCustomer != null) {
            loyaltyPointsLabel.setText("Loyalty Points: " + currentCustomer.getLoyaltyPoints());
            customerInfoLabel.setText("Name: " + currentCustomer.getName() + " " + currentCustomer.getSurname());
        }else{
            showAlert("Customer not found", "Try again or contact management.");
        }

    }

    private void applyLoyaltyPoints() {
        if (currentCustomer != null && currentCustomer.getLoyaltyPoints() >= 1000 && totalCost + discount >= 1000) {
            currentCustomer.setLoyaltyPoints(currentCustomer.getLoyaltyPoints() - 1000);
            discount += 1000;
            refreshTotalCost();
            loyaltyPointsLabel.setText("Loyalty Points: " + currentCustomer.getLoyaltyPoints());
        } else {
            showAlert("Loyalty Points Error", "Insufficient loyalty points or total cost.");
        }
    }


    private void finalizeTransaction() {
        if (currentCustomer == null) {
            loadCustomerData();
        }

        List<SaleItem> saleItems = cartItems.stream()
                .filter(item -> item.getAmount() > 0)
                .collect(Collectors.toList());

        if (!saleItems.isEmpty()) {
            showPaymentDialog(totalCost, saleItems);
        } else {
            showAlert("Transaction Error", "No items have been selected for the transaction.");
        }
    }

    private HBox createCustomerPanel() {
        HBox customerPanel = new HBox(15, customerIdField, loadCustomerButton, customerInfoLabel, loyaltyPointsLabel, applyLoyaltyPointsButton);
        customerPanel.setAlignment(Pos.CENTER);
        customerPanel.setPadding(new Insets(10));
        return customerPanel;
    }
    private HBox createTransactionPanel(HBox customerPanel) {
        HBox transactionPanel = new HBox(15, customerPanel, totalCostLabel, finishTransactionButton, newCustomerButton);
        transactionPanel.setAlignment(Pos.CENTER);
        transactionPanel.setPadding(new Insets(10, 0, 10, 0));
        return transactionPanel;
    }
    private void showPaymentDialog(double totalCost, List<SaleItem> saleItems) {
        Dialog<Pair<Double, Double>> dialog = new Dialog<>();
        dialog.setTitle("Payment");


        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField amountGivenField = new TextField();
        amountGivenField.setPromptText("Amount Received");
        Label changeLabel = new Label("Change: $0.00");

        grid.add(new Label("Amount Received:"), 0, 0);
        grid.add(amountGivenField, 1, 0);
        grid.add(new Label("Change:"), 0, 1);
        grid.add(changeLabel, 1, 1);

        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);


        Node confirmButton = dialog.getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setDisable(true);


        Platform.runLater(amountGivenField::requestFocus);


        amountGivenField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                try {
                    double amountGiven = Double.parseDouble(newValue.trim());
                    double change = amountGiven - totalCost;
                    if (change < 0) {
                        confirmButton.setDisable(true);
                        changeLabel.setText("Insufficient amount!");
                    } else {
                        confirmButton.setDisable(false);
                        changeLabel.setText(String.format("Change: $%.2f", change));
                    }
                } catch (NumberFormatException e) {
                    confirmButton.setDisable(true);
                    changeLabel.setText("Invalid amount!");
                }
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                double amountGiven = Double.parseDouble(amountGivenField.getText().trim());
                double change = amountGiven - totalCost;
                return new Pair<>(amountGiven, change);
            }
            return null;
        });

        Optional<Pair<Double, Double>> result = dialog.showAndWait();
        result.ifPresent(paymentResult -> finalizeSale(saleItems, totalCost, paymentResult.getValue()));
    }

    private void finalizeSale(List<SaleItem> saleItems, double totalCost, double change) {

        posController.processTransaction(new ArrayList<>(saleItems), currentCashier, currentCustomer.getId(), totalCost, discount);


        saleItems.forEach(saleItem -> inventoryManager.sellItem(saleItem.getItem(), saleItem.getAmount()));


        if (currentCustomer != null) {
            int loyaltyPointsEarned = (int) (totalCost * 0.1);
            currentCustomer.addLoyaltyPoints(loyaltyPointsEarned);
            customerManager.updateCustomer(currentCustomer);
        }

        cartItems.clear();
        totalCost = 0.0;
        totalCostLabel.setText("Total Cost: $0.00");
        customerIdField.clear();
        loyaltyPointsLabel.setText("Loyalty Points: 0");
        cartTable.setItems(FXCollections.observableArrayList(cartItems));
        inventoryTable.setItems(FXCollections.observableArrayList(inventoryManager.getAllItems()));
        showAlert("Transaction Completed", "The transaction has been processed successfully.");
        reloadScene();
    }

    private void reloadScene() {

        CashierView cashierView = new CashierView(currentCashier);

        Stage stage = (Stage) searchField.getScene().getWindow();

        stage.close();

        Platform.runLater(() -> {
            try {
                Stage newStage = new Stage();
                cashierView.start(newStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Optional<SaleItem> findSaleItemInCart(InventoryItem item) {
        return cartItems.stream().filter(saleItem -> saleItem.item.equals(item)).findFirst();
    }

    private void updateCartWithItem(InventoryItem item, int quantity) {
        Optional<SaleItem> existingSaleItemOpt = findSaleItemInCart(item);
        if (existingSaleItemOpt.isPresent()) {
            existingSaleItemOpt.get().setAmount(quantity);
        } else {
            cartItems.add(new SaleItem(item, quantity));
        }
        refreshTotalCost();
    }

    private void showNewCustomerDialog() {

        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("New Customer");
        dialog.setHeaderText("Enter New Customer Details");


        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField surnameField = new TextField();
        surnameField.setPromptText("Surname");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        TextField loyaltyPointsField = new TextField();
        loyaltyPointsField.setPromptText("Loyalty Points");
        loyaltyPointsField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Surname:"), 0, 1);
        grid.add(surnameField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                int nextId = customerManager.getAllCustomers().size();
                return new Customer(
                        nameField.getText(),
                        surnameField.getText(),
                        phoneField.getText(),
                        0,
                        nextId
                );
            }
            return null;
        });

        Optional<Customer> result = dialog.showAndWait();
        result.ifPresent(newCustomer -> {
            customerManager.addCustomer(newCustomer);
            showAlert("Customer Added", "New customer has been added successfully.");
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
