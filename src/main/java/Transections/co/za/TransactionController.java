package transections.co.za;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository repository;
    private static final double INITIAL_BALANCE = 0.0; // Changed to 0.0 for new users

    public TransactionController(TransactionRepository repository) {
        this.repository = repository;
    }

    private double calculateBalanceForUser(String username) {
        List<Transaction> transactions = repository.findAll();
        double balance = INITIAL_BALANCE; // Starts at 0.0
        for (Transaction tx : transactions) {
            if (tx.getUsername() != null && tx.getUsername().equalsIgnoreCase(username)) {
                if (tx.getType().equals("INCOME")) {
                    balance += tx.getAmount();
                } else {
                    balance -= tx.getAmount();
                }
            }
        }
        return balance;
    }

    @PostMapping("/income")
    public Object deposit(@RequestParam String username, @RequestParam double amount) {
        if (amount <= 0) return "Error: Amount must be greater than zero.";
        Transaction tx = new Transaction(username, "INCOME", amount, LocalDate.now());
        return repository.save(tx);
    }

}