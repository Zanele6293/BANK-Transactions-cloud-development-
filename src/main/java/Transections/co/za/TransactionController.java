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

    @PostMapping("/expense")
    public Object withdraw(@RequestParam String username, @RequestParam double amount) {
        if (amount <= 0) return "Error: Amount must be greater than zero.";
        double currentBalance = calculateBalanceForUser(username);

        // Checking if there are sufficient funds!
        if (currentBalance < amount) {
            return "❌ Insufficient Balance! Current Balance is R" + currentBalance;
        }

        Transaction tx = new Transaction(username, "EXPENSE", amount, LocalDate.now());
        return repository.save(tx);
    }

    @GetMapping("/balance")
    public double getBalance(@RequestParam String username) {
        return calculateBalanceForUser(username);
    }

    @GetMapping("/history")
    public List<Transaction> getHistory(@RequestParam String username) {
        List<Transaction> allTransactions = repository.findAll();
        return allTransactions.stream()
                .filter(tx -> tx.getUsername() != null && tx.getUsername().equalsIgnoreCase(username))
                .toList();
    }
}