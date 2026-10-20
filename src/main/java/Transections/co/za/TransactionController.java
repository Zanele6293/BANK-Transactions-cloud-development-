package transections.co.za;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository repository;

    public TransactionController(TransactionRepository repository) {
        this.repository = repository;
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        text = text.toLowerCase();
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private Transaction findUserAccount(String username) {
        List<Transaction> all = repository.findAll();
        for (Transaction tx : all) {
            if (tx.getUsername() != null && tx.getUsername().equalsIgnoreCase(username) && "INITIAL_DEPOSIT".equals(tx.getType())) {
                return tx;
            }
        }
        return null;
    }

    @PostMapping("/register")
    public Object register(@RequestParam String username, @RequestParam String surname, @RequestParam String password, @RequestParam double initialDeposit, @RequestParam(required = false) String profileImage) {
        if (findUserAccount(username) != null) {
            return "ERROR:Account already exists! Please log in.";
        }
        if (initialDeposit < 150.0) {
            return "ERROR:Minimum opening deposit is R150.00";
        }

        // Capitalize Name and Surname properly
        String cleanName = capitalize(username.trim());
        String cleanSurname = capitalize(surname.trim());

        Transaction tx = new Transaction(cleanName, cleanSurname, password, "INITIAL_DEPOSIT", initialDeposit, LocalDate.now(), profileImage);
        return repository.save(tx);
    }

    @PostMapping("/login")
    public Object login(@RequestParam String username, @RequestParam String password) {
        String cleanName = capitalize(username.trim());
        Transaction user = findUserAccount(cleanName);
        if (user == null) {
            return "ERROR:Account does not exist. Please register first.";
        }
        if (!user.getPassword().equals(password)) {
            return "ERROR:Incorrect password! Please try again.";
        }
        return "SUCCESS";
    }

    @GetMapping("/balance/current")
    public double getCurrentBalance(@RequestParam String username) {
        String cleanName = capitalize(username.trim());
        List<Transaction> transactions = repository.findAll();
        double balance = 0.0;
        for (Transaction tx : transactions) {
            if (tx.getUsername() != null && tx.getUsername().equalsIgnoreCase(cleanName)) {
                if ("INITIAL_DEPOSIT".equals(tx.getType()) || "CURRENT_INCOME".equals(tx.getType()) || "MATURED_PAYOUT".equals(tx.getType())) {
                    balance += tx.getAmount();
                } else if ("CURRENT_EXPENSE".equals(tx.getType()) || "INVESTMENT".equals(tx.getType()) || "SAVINGS_TRANSFER_OUT".equals(tx.getType())) {
                    balance -= tx.getAmount();
                }
            }
        }
        return balance;
    }

    @GetMapping("/balance/savings")
    public double getSavingsBalance(@RequestParam String username) {
        String cleanName = capitalize(username.trim());
        List<Transaction> transactions = repository.findAll();
        LocalDate today = LocalDate.now();
        double balance = 0.0;

        for (Transaction tx : transactions) {
            if (tx.getUsername() != null && tx.getUsername().equalsIgnoreCase(cleanName)) {
                if ("SAVINGS_INCOME".equals(tx.getType()) || "SAVINGS_TRANSFER_IN".equals(tx.getType())) {
                    double actualAmount = tx.getAmount();

                    // CHECK IF 2 MONTHS (60 DAYS) HAVE PASSED FOR INTEREST PAYOUT
                    if (tx.getDate() != null && today.isAfter(tx.getDate().plusDays(60))) {
                        actualAmount *= 1.089; // Apply the 8.9% interest after 2 months!
                    }
                    balance += actualAmount;
                } else if ("SAVINGS_EXPENSE".equals(tx.getType())) {
                    balance -= tx.getAmount();
                }
            }
        }
        return balance;
    }

    @PostMapping("/current/income")
    public Object currentDeposit(@RequestParam String username, @RequestParam double amount) {
        if (amount <= 0) return "ERROR:Amount must be greater than zero.";
        Transaction tx = new Transaction(capitalize(username.trim()), null, "CURRENT_INCOME", amount, LocalDate.now(), null, null);
        return repository.save(tx);
    }

    @PostMapping("/current/expense")
    public Object currentWithdraw(@RequestParam String username, @RequestParam double amount) {
        if (amount <= 0) return "ERROR:Amount must be greater than zero.";
        double currentBalance = getCurrentBalance(username);
        if (currentBalance < amount) {
            return "ERROR:Insufficient Current Account Balance!";
        }
        Transaction tx = new Transaction(capitalize(username.trim()), null, "CURRENT_EXPENSE", amount, LocalDate.now(), null, null);
        return repository.save(tx);
    }

    @PostMapping("/savings/income")
    public Object savingsDeposit(@RequestParam String username, @RequestParam double amount) {
        if (amount <= 0) return "ERROR:Amount must be greater than zero.";
        double currentBalance = getCurrentBalance(username);
        if (currentBalance < amount) {
            return "ERROR:Insufficient funds in Current Account to transfer to Savings.";
        }
        String cleanName = capitalize(username.trim());

        // Deduct raw amount from current account
        repository.save(new Transaction(cleanName, null, "SAVINGS_TRANSFER_OUT", amount, LocalDate.now(), null, null));

        // Save raw amount into savings (Interest will trigger mathematically after 60 days)
        Transaction savingsTx = new Transaction(cleanName, null, "SAVINGS_INCOME", amount, LocalDate.now(), null, null);
        return repository.save(savingsTx);
    }


    @PostMapping("/invest")
    public Object invest(@RequestParam String username, @RequestParam double amount) {
        if (amount <= 0) return "ERROR:Invalid amount.";
        double currentBalance = getCurrentBalance(username);
        if (currentBalance < amount) {
            return "ERROR:Insufficient funds for this 7-day investment.";
        }

        LocalDate maturity = LocalDate.now().plusDays(7);
        Transaction tx = new Transaction(capitalize(username.trim()), "INVESTMENT", amount, LocalDate.now(), true, maturity);
        return repository.save(tx);
    }

    @GetMapping("/history")
    public List<Transaction> getHistory(@RequestParam String username) {
        String cleanName = capitalize(username.trim());
        return repository.findAll().stream()
                .filter(tx -> tx.getUsername() != null && tx.getUsername().equalsIgnoreCase(cleanName))
                .collect(Collectors.toList());
    }
}