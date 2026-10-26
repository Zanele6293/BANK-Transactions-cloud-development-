package transection.co.za;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository repository;
    private final S3Service s3Service; // S3 Integration

    public TransactionController(TransactionRepository repository, S3Service s3Service) {
        this.repository = repository;
        this.s3Service = s3Service;
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        text = text.toLowerCase();
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private Transaction findUserAccount(String username) {
        List<Transaction> all = repository.findByUsernameIgnoreCase(username);
        for (Transaction tx : all) {
            if ("INITIAL_DEPOSIT".equals(tx.getType())) {
                return tx;
            }
        }
        return null;
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

    @GetMapping("/profile")
    public Transaction getProfile(@RequestParam String username) {
        return findUserAccount(capitalize(username.trim()));
    }

    @GetMapping("/balance/current")
    public double getCurrentBalance(@RequestParam String username) {
        String cleanName = capitalize(username.trim());
        double credits = repository.getCreditsForCurrent(cleanName);
        double debits = repository.getDebitsForCurrent(cleanName);
        return credits - debits;
    }

    @GetMapping("/balance/savings")
    public double getSavingsBalance(@RequestParam String username) {
        String cleanName = capitalize(username.trim());
        List<Transaction> transactions = repository.findByUsernameIgnoreCase(cleanName);
        LocalDate today = LocalDate.now();
        double balance = 0.0;

        for (Transaction tx : transactions) {
            if ("SAVINGS_INCOME".equals(tx.getType()) || "SAVINGS_TRANSFER_IN".equals(tx.getType())) {
                double actualAmount = tx.getAmount();
                if (tx.getDate() != null && today.isAfter(tx.getDate().plusDays(60))) {
                    actualAmount *= 1.089;
                }
                balance += actualAmount;
            } else if ("SAVINGS_EXPENSE".equals(tx.getType())) {
                balance -= tx.getAmount();
            }
        }
        return balance;
    }

    @PostMapping("/current/income")
    public Object currentDeposit(@RequestParam String username, @RequestParam double amount) {
        if (amount <= 0) return "ERROR:Amount must be greater than zero.";
        Transaction tx = new Transaction(capitalize(username.trim()), "CURRENT_INCOME", amount, LocalDate.now());
        return repository.save(tx);
    }

    @PostMapping("/current/expense")
    public Object currentWithdraw(@RequestParam String username, @RequestParam double amount) {
        if (amount <= 0) return "ERROR:Amount must be greater than zero.";
        double currentBalance = getCurrentBalance(username);
        if (currentBalance < amount) {
            return "ERROR:Insufficient Current Account Balance!";
        }
        Transaction tx = new Transaction(capitalize(username.trim()), "CURRENT_EXPENSE", amount, LocalDate.now());
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

        repository.save(new Transaction(cleanName, "SAVINGS_TRANSFER_OUT", amount, LocalDate.now()));
        Transaction savingsTx = new Transaction(cleanName, "SAVINGS_INCOME", amount, LocalDate.now());
        return repository.save(savingsTx);
    }

    @PostMapping("/savings/expense")
    public Object savingsWithdraw(@RequestParam String username, @RequestParam double amount) {
        if (amount <= 0) return "ERROR:Amount must be greater than zero.";
        double savingsBalance = getSavingsBalance(username);
        if (savingsBalance < amount) {
            return "ERROR:Insufficient Savings Balance!";
        }
        Transaction tx = new Transaction(capitalize(username.trim()), "SAVINGS_EXPENSE", amount, LocalDate.now());
        return repository.save(tx);
    }

    @PostMapping("/invest")
    public Object invest(@RequestParam String username, @RequestParam double amount) {
        if (amount <= 0) return "ERROR:Invalid amount.";
        double currentBalance = getCurrentBalance(username);
        if (currentBalance < amount) {
            return "ERROR:Insufficient funds for this 7-day investment.";
        }

        Transaction tx = new Transaction(capitalize(username.trim()), "INVESTMENT", amount, LocalDate.now());
        tx.setActiveInvestment(true);
        tx.setMaturityDate(LocalDate.now().plusDays(7));
        return repository.save(tx);
    }

    @GetMapping("/investments")
    public List<Transaction> getInvestments(@RequestParam String username) {
        String cleanName = capitalize(username.trim());
        List<Transaction> all = repository.findByUsernameIgnoreCase(cleanName);
        LocalDate today = LocalDate.now();

        for (Transaction tx : all) {
            if ("INVESTMENT".equals(tx.getType()) && tx.isActiveInvestment()) {
                if (today.isAfter(tx.getMaturityDate()) || today.isEqual(tx.getMaturityDate())) {
                    tx.setActiveInvestment(false);
                    repository.save(tx);

                    double payout = tx.getAmount() * 1.05;
                    Transaction reward = new Transaction(cleanName, "MATURED_PAYOUT", payout, LocalDate.now());
                    repository.save(reward);
                }
            }
        }

        return repository.findByUsernameIgnoreCase(cleanName).stream()
                .filter(tx -> "INVESTMENT".equals(tx.getType()))
                .collect(Collectors.toList());
    }

    @GetMapping("/history")
    public List<Transaction> getHistory(@RequestParam String username) {
        return repository.findByUsernameIgnoreCase(capitalize(username.trim()));
    }
}