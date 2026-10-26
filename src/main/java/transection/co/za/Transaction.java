package transection.co.za;

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

    @Column(length = 1024)
    private String profileImage;

    // Empty constructor
    public Transaction() {}

    // Full constructor (for Registration / User Profiles)
    public Transaction(String username, String surname, String password, String type, double amount, LocalDate date, String profileImage) {
        this.username = username;
        this.surname = surname;
        this.password = password;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.profileImage = profileImage;
    }

    // Lightweight constructor (for Deposits, Withdrawals, Transfers, & Payouts)
    public Transaction(String username, String type, double amount, LocalDate date) {
        this.username = username;
        this.type = type;
        this.amount = amount;
        this.date = date;
    }

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getSurname() { return surname; }
    public String getPassword() { return password; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getProfileImage() { return profileImage; }
    public boolean isActiveInvestment() { return activeInvestment; }
    public LocalDate getMaturityDate() { return maturityDate; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setPassword(String password) { this.password = password; }
    public void setType(String type) { this.type = type; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public void setActiveInvestment(boolean activeInvestment) { this.activeInvestment = activeInvestment; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }
}