
import java.io.Serializable;

public class SaleItem implements Serializable {

    public InventoryItem item;
    public int amount;

    SaleItem(InventoryItem item, int amount){
        this.item = item;
        this.amount = amount;
    }

    public double getPrice(){
        return item.getPrice();
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public InventoryItem getItem() {
        return item;
    }
}
