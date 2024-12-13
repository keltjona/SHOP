
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerManager {

    ArrayList<Customer> customers;
    private final String filePath = "customers.bin";
    private POSSystem POSSystem;
    public CustomerManager(){
        customers = new ArrayList<Customer>();
        loadCustomers();
        POSSystem = new POSSystem();
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
        saveCustomers();
    }

    public void removeCustomer(int id) {
        customers.removeIf(customer -> customer.getId() == id);
        saveCustomers();
    }

    public Customer findCustomer(int id) {
        for (Customer customer : customers){
            if(customer.getId()==id){
                return customer;
            }
        }
        return null;
    }

    private void saveCustomers() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(customers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Customer> getAllCustomers() {
        return customers;
    }

    @SuppressWarnings("unchecked")
    void loadCustomers() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            Object data = in.readObject();
            if (data instanceof ArrayList<?>) {
                this.customers = (ArrayList<Customer>) data;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void updateCustomer(Customer updatedCustomer) {
        for (int i = 0; i < customers.size(); i++) {
            // Check if the customer ID matches
            if (customers.get(i).getId() == updatedCustomer.getId()) {
                // If it matches, replace the old customer data with the updated customer data
                customers.set(i, updatedCustomer);
                saveCustomers();  // Save the updated customers list to the file
                return; // Exit the method as we've found and updated the customer
            }
        }
    }

    public List<Transaction> getTransactionsForCustomer(int customerId) {
        List<Transaction> customerTransactions = new ArrayList<>();
        for (Transaction transaction : POSSystem.getAllTransactions()) {
            if (transaction.getBuyerID() == customerId) {
                customerTransactions.add(transaction);
            }
        }
        return customerTransactions;
    }
}
