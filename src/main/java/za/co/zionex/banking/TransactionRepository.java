package za.co.zionex.banking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Declared the method that was missing, fixing the red lines in the controller
    List<Transaction> findByUsernameIgnoreCase(String username);

    // Database-level calculation queries (using JPQL aggregation)
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE LOWER(t.username) = LOWER(:username) AND t.type IN ('INITIAL_DEPOSIT', 'CURRENT_INCOME', 'MATURED_PAYOUT')")
    double getCreditsForCurrent(@Param("username") String username);

    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE LOWER(t.username) = LOWER(:username) AND t.type IN ('CURRENT_EXPENSE', 'INVESTMENT', 'SAVINGS_TRANSFER_OUT')")
    double getDebitsForCurrent(@Param("username") String username);

    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE LOWER(t.username) = LOWER(:username) AND t.type IN ('SAVINGS_INCOME', 'SAVINGS_TRANSFER_IN')")
    double getCreditsForSavings(@Param("username") String username);

    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE LOWER(t.username) = LOWER(:username) AND t.type IN ('SAVINGS_EXPENSE')")
    double getDebitsForSavings(@Param("username") String username);

    @Query("SELECT t FROM Transaction t WHERE LOWER(t.username) = LOWER(:username) AND t.type IN ('SAVINGS_INCOME', 'SAVINGS_TRANSFER_IN', 'SAVINGS_EXPENSE')")
    List<Transaction> getSavingsTransactionsOnly(@Param("username") String username);
}