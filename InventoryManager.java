
import java.io.*;
import java.util.ArrayList;

public class InventoryManager {
    private ArrayList<InventoryItem> items;
    private final String filePath = "inventory.bin";

    public InventoryManager() {
        items = new ArrayList<>();
        loadItems();
    }

    public void addItem(InventoryItem item) {
        items.add(item);
        saveItems();
    }

    private void saveItems() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadItems() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            Object data = in.readObject();
            if (data instanceof ArrayList<?>) {
                this.items = (ArrayList<InventoryItem>) data;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<InventoryItem> getAllItems() {
        return items;
    }

    public void removeItem(InventoryItem item) {
        items.remove(item);
        saveItems();
    }

    public InventoryItem findItem(String itemName) {
        return items.stream()
                .filter(item -> item.getName().equals(itemName))
                .findFirst()
                .orElse(null);
    }


    public void updateItem(InventoryItem updatedItem) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getName().equals(updatedItem.getName())) {
                items.set(i, updatedItem);
                saveItems();
                return;
            }
        }
    }

    public void sellItem(InventoryItem item, int amount){
        item.sellItem(amount);
        updateItem(item);
    }
}
