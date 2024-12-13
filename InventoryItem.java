
import java.io.Serializable;

public class InventoryItem implements Serializable {
    private String name;
    private double price;
    private int quantity;
    private String supplier;
    private String category;
    private int amountSold;
    private int amountBought;
    private double purchasePrice;
    private double totalSpent;
    private double totalEarned;

    public InventoryItem(String name, String category, double price, int quantity, String supplier, double purchasePrice, int amountBought) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.supplier = supplier;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.amountBought = amountBought;
        amountSold = 0;
        totalSpent = purchasePrice*amountBought;
        totalEarned = 0;
    }

    public String getName() {
        return name;
    }

    public String getSupplier() {
        return supplier;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }


    public int getAmountBought() {
        return amountBought;
    }

    public void addStock(int newStock, double purchasePrice){
        quantity+=newStock;
        this.purchasePrice = purchasePrice;
        totalSpent+=(newStock*purchasePrice);
        amountBought+=newStock;
    }

    public void sellItem(int amount){
        quantity-=amount;
        totalEarned += price*amount;
        amountSold+=amount;
    }

    public double getTotalEarned() {
        return totalEarned;
    }

    public int getAmountSold() {
        return amountSold;
    }

    public double getTotalSpent() {
        return totalSpent;
    }



}
