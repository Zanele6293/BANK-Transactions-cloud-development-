package transections.co.za;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // "INCOME" or "EXPENSE"
    private double amount;
    private LocalDate date;

    public Transaction() {}

    public Transaction(String type, double amount, LocalDate date) {
        this.type = type;
        this.amount = amount;
        this.date = date;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
}