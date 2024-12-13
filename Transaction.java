
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Transaction implements Serializable {
    private ArrayList<SaleItem> items;
    private Date date;
    private int buyerID;
    private User cashier;
    private double totalCost;
    private int discount;
    private int transactionID;

    public Transaction(User cashier, int buyerID, double totalCost, int discount, ArrayList<SaleItem> items, ArrayList<Transaction> transactions) {
        this.items = items;
        this.date = new Date();
        this.cashier = cashier;
        this.buyerID = buyerID;
        this.totalCost = totalCost;
        this.discount = discount;
        transactionID = transactions.size()+1;
    }

    public int getBuyerID() {
        return buyerID;
    }


    public int getTransactionID() {
        return transactionID;
    }

    public Date getDate() {
        return date;
    }

    public User getCashier(){
        return cashier;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public ArrayList<SaleItem> getItems() {
        return items;
    }

    public int getDiscount() {
        return discount;
    }
}
