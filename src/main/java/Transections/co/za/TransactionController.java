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
        return 0;
    }

}