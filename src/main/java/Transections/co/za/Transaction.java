package transections.co.za;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String surname;
    private String password;
    private String type;
    private double amount;
    private LocalDate date;

    private boolean activeInvestment = false;
    private LocalDate maturityDate;

    @Column(length = 500000)
    private String profileImage;

    // 1. Default empty constructor required by JPA
    public Transaction() {}

    // 2. Constructor for Registration (User profile creation)
    public Transaction(String username, String surname, String password, String type, double amount, LocalDate date, String profileImage) {
        this.username = username;
        this.surname = surname;
        this.password = password;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.profileImage = profileImage;
    }

    // 3. Constructor for Standard Transactions (Income, Expense, Savings, Payouts)
    public Transaction(String username, String surname, String type, double amount, LocalDate date, String profileImage, Object dummy) {
        this.username = username;
        this.surname = surname;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.profileImage = profileImage;
    }

    // 4. Constructor for 7-Day Investments
    public Transaction(String username, String type, double amount, LocalDate date, boolean activeInvestment, LocalDate maturityDate) {
        this.username = username;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.activeInvestment = activeInvestment;
        this.maturityDate = maturityDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getSurname() { return surname; }
    public String getPassword() { return password; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public boolean isActiveInvestment() { return activeInvestment; }
    public void setActiveInvestment(boolean activeInvestment) { this.activeInvestment = activeInvestment; }
    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }
}