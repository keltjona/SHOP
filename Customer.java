
import java.io.Serializable;

public class Customer implements Serializable {
    private String name;
    private String surname;
    private String phone;
    private int loyaltyPoints;
    private int id;

    public Customer(String name, String surname, String phone, int loyaltyPoints, int id){
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.loyaltyPoints = loyaltyPoints;
        this.id = id;
    }

    public String getSurname() {
        return surname;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }


    public void addLoyaltyPoints(int points){

        loyaltyPoints+=points;

    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

}
