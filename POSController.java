
import java.util.ArrayList;

public class POSController {
    private POSSystem posSystem;
    private InventoryManager inventoryManager;


    public POSController(InventoryManager inventoryManager) {
        posSystem = new POSSystem();
        this.inventoryManager = inventoryManager;
    }

    public void processTransaction(ArrayList<SaleItem> items, User cashier, int id, double total, int discount) {
        Transaction transaction = new Transaction(cashier, id, total, discount, items, posSystem.getAllTransactions());
        posSystem.addTransaction(transaction);
    }
}
