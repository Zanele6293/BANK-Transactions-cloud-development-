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
    private String surname; // Added surname for the digital card
    private String password;
    private String type;     // "INITIAL_DEPOSIT", "CURRENT_INCOME", "CURRENT_EXPENSE", "SAVINGS_INCOME", "SAVINGS_EXPENSE", "INVESTMENT"
    private double amount;
    private LocalDate date;

    private boolean activeInvestment = false;
    private LocalDate maturityDate;

    @Column(length = 500000)
    private String profileImage;

    public Transaction() {}

    // Registration constructor
    public Transaction(String username, String surname, String password, String type, double amount, LocalDate date, String profileImage) {
        this.username = username;
        this.surname = surname;
        this.password = password;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.profileImage = profileImage;
    }

    // Standard transaction constructor
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