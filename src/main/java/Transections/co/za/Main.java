package Transections.co.za;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Transections newTransection = new Transections(7000);
        Scanner scanner = new Scanner(System.in);
        while (Transections.isRunning) {
            System.out.println("======= FINANCE TRACKER v1.0 =======\n" +
                    "1. Add Income\n" +
                    "2. Add Expense\n" +
                    "3. View Transaction History\n" +
                    "4. View Total Balance\n" +
                    "5. Exit\n" +
                    "=====================================\n" +
                    "Choose an option: _");
            String option = scanner.nextLine();

            String newString = option.trim();
            switch (newString) {
                case "1":
                    newTransection.income(newTransection.cleanData());
                    break;
                case "2":
                    newTransection.expense(newTransection.cleanData());
                    break;
                case "3":
                    newTransection.getHistory();
                    break;
                case "4":
                    newTransection.getBalance();
                case "5":
                    newTransection.isRunning();
                    break;
                default:
                    System.out.println("Error! Enter a number between 1-5.\n");
            }
        }
    }

}
