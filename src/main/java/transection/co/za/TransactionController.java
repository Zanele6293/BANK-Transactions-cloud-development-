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

    @PostMapping("/register")
    public Object register(@RequestParam("username") String username,
                           @RequestParam("surname") String surname,
                           @RequestParam("password") String password,
                           @RequestParam("initialDeposit") double initialDeposit,
                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        String cleanName = capitalize(username.trim());

        // Strict guard: Check if user already exists
        if (findUserAccount(cleanName) != null) {
            return "ERROR:Account already exists! Please log in.";
        }

        if (initialDeposit < 150.0) {
            return "ERROR:Minimum opening deposit is R150.00";
        }

        // Upload picture to S3 (or fallback)
        String imageUrl = s3Service.uploadProfilePicture(profileImage);
        String cleanSurname = capitalize(surname.trim());

        Transaction tx = new Transaction(cleanName, cleanSurname, password, "INITIAL_DEPOSIT", initialDeposit, LocalDate.now(), imageUrl);
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


    @GetMapping("/statement")
    public void downloadStatement(@RequestParam String username, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        String cleanName = capitalize(username.trim());
        List<Transaction> history = repository.findByUsernameIgnoreCase(cleanName);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Zionex_Financial_Statement_" + cleanName + ".pdf");

        try {
            com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4);
            com.lowagie.text.pdf.PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // --- HEADER TABLE: LOGO (LEFT) & TITLE (RIGHT SIDE-BY-SIDE) ---
            com.lowagie.text.pdf.PdfPTable headerTable = new com.lowagie.text.pdf.PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1.5f, 8.5f});
            headerTable.getDefaultCell().setBorder(com.lowagie.text.pdf.PdfPCell.NO_BORDER);

            // Left Cell: Logo Image
            com.lowagie.text.pdf.PdfPCell logoCell = new com.lowagie.text.pdf.PdfPCell();
            logoCell.setBorder(com.lowagie.text.pdf.PdfPCell.NO_BORDER);
            try {
                java.net.URL logoUrl = getClass().getClassLoader().getResource("static/logo.png");
                if (logoUrl != null) {
                    com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(logoUrl);
                    logo.scaleToFit(45, 45);
                    logoCell.addElement(logo);
                }
            } catch (Exception imgEx) {
                // Fallback if logo is missing
            }
            headerTable.addCell(logoCell);

            // Right Cell: Title & Tagline next to the logo
            com.lowagie.text.pdf.PdfPCell titleCell = new com.lowagie.text.pdf.PdfPCell();
            titleCell.setBorder(com.lowagie.text.pdf.PdfPCell.NO_BORDER);
            titleCell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD, new java.awt.Color(0, 75, 35));
            com.lowagie.text.Font subFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.NORMAL, java.awt.Color.DARK_GRAY);

            titleCell.addElement(new com.lowagie.text.Phrase("ZIONEX FINANCIAL", titleFont));
            titleCell.addElement(new com.lowagie.text.Phrase("Growing your wealth, securing your future.", subFont));
            headerTable.addCell(titleCell);

            document.add(headerTable);
            document.add(new com.lowagie.text.Paragraph("\n"));

            // --- FETCH FULL USER DETAILS (NAME & SURNAME) ---
            String fullName = cleanName;
            for (Transaction tx : history) {
                if (tx.getSurname() != null && !tx.getSurname().isEmpty()) {
                    fullName = cleanName + " " + tx.getSurname();
                    break;
                }
            }

            // Document Account Holder Meta Info
            com.lowagie.text.Font metaFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD, java.awt.Color.BLACK);
            document.add(new com.lowagie.text.Paragraph("Official Account Statement", metaFont));
            document.add(new com.lowagie.text.Paragraph("Account Holder: " + fullName, subFont));
            document.add(new com.lowagie.text.Paragraph("Generated On: " + java.time.LocalDateTime.now().toString().replace("T", " "), subFont));
            document.add(new com.lowagie.text.Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));
            document.add(new com.lowagie.text.Paragraph("\n"));

            // Transaction Table
            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 4f, 3f});

            // Table Headers
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD, java.awt.Color.WHITE);
            String[] headers = {"Date & Time", "Transaction Type", "Amount (ZAR)"};
            for (String header : headers) {
                com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Paragraph(header, headerFont));
                cell.setBackgroundColor(new java.awt.Color(0, 168, 89));
                cell.setPadding(8);
                table.addCell(cell);
            }

            // Table Data Rows
            com.lowagie.text.Font normalFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL, java.awt.Color.BLACK);
            for (Transaction tx : history) {
                String dateStr = tx.getDate() != null ? tx.getDate().toString().replace("T", " ") : "N/A";

                com.lowagie.text.pdf.PdfPCell dateCell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Paragraph(dateStr, normalFont));
                dateCell.setPadding(6);
                table.addCell(dateCell);

                com.lowagie.text.pdf.PdfPCell typeCell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Paragraph(tx.getType(), normalFont));
                typeCell.setPadding(6);
                table.addCell(typeCell);

                com.lowagie.text.pdf.PdfPCell amountCell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Paragraph("R " + String.format("%.2f", tx.getAmount()), normalFont));
                amountCell.setPadding(6);
                table.addCell(amountCell);
            }

            document.add(table);

            // Footer Note
            document.add(new com.lowagie.text.Paragraph("\n\nThank you for banking with Zionex Financial. This is a secure computer-generated official document.", subFont));

            document.close();
        } catch (Exception e) {
            response.sendError(jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not generate PDF statement");
        }
    }
}