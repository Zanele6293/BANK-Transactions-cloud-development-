package za.co.zionex.banking;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class AccountStatementService {

    private static final Logger LOGGER = Logger.getLogger(AccountStatementService.class.getName());
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private final TransactionRepository repository;

    public AccountStatementService(TransactionRepository repository) {
        this.repository = repository;
    }

    public byte[] generateStatement(String username) throws IOException {
        List<Transaction> history = repository.findByUsernameIgnoreCase(username).stream()
                .filter(this::isCurrentAccountTransaction)
                .sorted(Comparator.comparing(Transaction::getDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Transaction::getId, Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 42, 42, 42, 42);
        try {
            PdfWriter.getInstance(document, output);
            document.open();
            addStatementContent(document, username, history);
        } catch (DocumentException e) {
            throw new IOException("Could not generate PDF statement.", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return output.toByteArray();
    }

    private boolean isCurrentAccountTransaction(Transaction tx) {
        return switch (tx.getType()) {
            case "INITIAL_DEPOSIT", "CURRENT_INCOME", "CURRENT_EXPENSE", "SAVINGS_TRANSFER_OUT",
                    "INVESTMENT", "MATURED_PAYOUT" -> true;
            default -> false;
        };
    }

    private String getDescription(Transaction tx) {
        return switch (tx.getType()) {
            case "INITIAL_DEPOSIT" -> "Opening deposit";
            case "CURRENT_INCOME" -> "Deposit";
            case "CURRENT_EXPENSE" -> "Withdrawal";
            case "SAVINGS_TRANSFER_OUT" -> "Transfer to savings";
            case "INVESTMENT" -> "Investment";
            case "MATURED_PAYOUT" -> "Investment maturity payout";
            default -> "Transaction";
        };
    }

    private double getDebit(Transaction tx) {
        return switch (tx.getType()) {
            case "CURRENT_EXPENSE", "SAVINGS_TRANSFER_OUT", "INVESTMENT" -> tx.getAmount();
            default -> 0.0;
        };
    }

    private double getCredit(Transaction tx) {
        return switch (tx.getType()) {
            case "INITIAL_DEPOSIT", "CURRENT_INCOME", "MATURED_PAYOUT" -> tx.getAmount();
            default -> 0.0;
        };
    }

    private String formatAmount(double amount) {
        return String.format(Locale.US, "R %,.2f", amount);
    }



    private void addSummary(Document document, Color background, Color borderColor, Font labelFont,
                            Font valueFont, double totalDebits, double totalCredits, double closingBalance)
            throws DocumentException {
        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{1, 1, 1, 1});

        String[][] summaries = {
                {"OPENING BALANCE", formatAmount(0.0)},
                {"TOTAL DEBITS", formatAmount(totalDebits)},
                {"TOTAL CREDITS", formatAmount(totalCredits)},
                {"CLOSING BALANCE", formatAmount(closingBalance)}
        };
        for (String[] summary : summaries) {
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(background);
            cell.setBorderColor(borderColor);
            cell.setPadding(10);
            cell.addElement(new Paragraph(summary[0], labelFont));
            cell.addElement(new Paragraph(summary[1], valueFont));
            summaryTable.addCell(cell);
        }
        document.add(summaryTable);
        document.add(new Paragraph(" "));
    }

    private void addTransactions(Document document, List<Transaction> history, Color bankGreen,
                                Color borderColor, Font tableHeaderFont, Font normalFont, Font amountFont)
            throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.6f, 3.2f, 1.7f, 1.7f, 1.8f});
        table.setHeaderRows(1);

        String[] headers = {"Date", "Description", "Debit (R)", "Credit (R)", "Balance (R)"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, tableHeaderFont));
            cell.setBackgroundColor(bankGreen);
            cell.setPadding(7);
            table.addCell(cell);
        }

        double runningBalance = 0.0;
        int rowIndex = 0;
        for (Transaction tx : history) {
            double debit = getDebit(tx);
            double credit = getCredit(tx);
            runningBalance += credit - debit;
            String date = tx.getDate() != null ? tx.getDate().format(DATE_FORMAT) : "—";
            String[] values = {
                    date,
                    getDescription(tx),
                    debit == 0 ? "—" : formatAmount(debit),
                    credit == 0 ? "—" : formatAmount(credit),
                    formatAmount(runningBalance)
            };
            for (int column = 0; column < values.length; column++) {
                PdfPCell cell = new PdfPCell(new Paragraph(values[column], column == 4 ? amountFont : normalFont));
                cell.setPadding(6);
                cell.setBorderColor(borderColor);
                if (rowIndex % 2 == 1) {
                    cell.setBackgroundColor(new Color(248, 250, 248));
                }
                if (column >= 2) {
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                }
                table.addCell(cell);
            }
            rowIndex++;
        }
        if (history.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(new Paragraph("No transactions for this statement period.", normalFont));
            emptyCell.setColspan(5);
            emptyCell.setPadding(12);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(emptyCell);
        }
        document.add(table);
    }
}
