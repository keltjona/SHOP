
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class POSSystem {
    private ArrayList<Transaction> transactions;
    private final String filePath = "transactions.bin";

    public POSSystem() {
        transactions = new ArrayList<>();
        loadTransactions();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        saveTransactions();
    }

    private void saveTransactions() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(transactions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTransactions() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            Object data = in.readObject();
            if (data instanceof ArrayList<?>) {
                this.transactions = (ArrayList<Transaction>) data;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Transaction> getAllTransactions() {
        return transactions;
    }


    public ArrayList<Transaction> createDailyReport(Date date) {
        ArrayList<Transaction> dailyTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (isSameDay(transaction.getDate(), date)) {
                dailyTransactions.add(transaction);
            }
        }
        return dailyTransactions;
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }


    public ArrayList<Transaction> createMonthlyReport(int year, int month) {
        ArrayList<Transaction> monthlyTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            Date transactionDate = transaction.getDate();
            if (isSameMonthAndYear(transactionDate, year, month)) {
                monthlyTransactions.add(transaction);
            }
        }
        return monthlyTransactions;
    }

    private boolean isSameMonthAndYear(Date date, int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month;
    }

    public ArrayList<Transaction> createYearlyReport(int year) {
        ArrayList<Transaction> yearlyTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            Date transactionDate = transaction.getDate();
            if (isSameYear(transactionDate, year)) {
                yearlyTransactions.add(transaction);
            }
        }
        return yearlyTransactions;
    }

    private boolean isSameYear(Date date, int year) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR) == year;
    }




}
