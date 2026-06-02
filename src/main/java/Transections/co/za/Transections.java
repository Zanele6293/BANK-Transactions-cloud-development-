package transections.co.za;

import java.util.Scanner;
import java.time.LocalDate;

public class Transections {

    private int Balance;
    private boolean isRunning = true;
    private final StringBuilder history = new StringBuilder();
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BOLD = "\u001B[1m";

   public Transections(int Balance){
       this.Balance = Balance;
   }

   public int addBalance(int deposit){
       String deposited = Integer.toString(deposit);
       for (int i = 0; i < deposited.length(); i++){
           System.out.println(deposited.charAt(i));
       }
       return this.Balance+deposit;
   }

    public int subBalance(int withdraw){
        return  this.Balance-withdraw;

    }

    //This method gives us what we typed on the terminal
    public int cleanData(){
       Scanner input = new Scanner(System.in);
       System.out.println("Amount: ");
       String inputFromUser = input.nextLine();
       boolean isInt = true;
       for (int i = 0; i < inputFromUser.length(); i++){
           if (!Character.isDigit(inputFromUser.charAt(i))){
               isInt = false;
               break;
           } else {
           }
       }
       if(isInt){
       return Integer.parseInt(inputFromUser);      // If this whole it is an integer, It will be turned into an Integer.
       }else{
        System.out.println(RED+"Enter the correct Amount!" + RESET);
        return 0;}  
    }

    public void income(int money){
        this.Balance=addBalance(money);
        history.append(GREEN + "+ ").append(money).append(RESET).append("\n");
        System.out.println(YELLOW+"Successfully deposited R"+money+RESET);
    }

    public void expense (int money){
        int decreaseAmount =subBalance(money);
        // In here we check if the balance is sufficient first
        if (this.Balance<money){
            System.out.println(RED+"Insufficient Balance"+RESET);
        }else {
            this.Balance=decreaseAmount;
            history.append(RED + "- ").append(money).append(RESET).append("\n");
            System.out.println(RED+ "Successfully withdraw R"+money +RESET);}
    }

    public void getHistory() {
        System.out.println(BOLD+"Transections History:"+RESET);
        LocalDate today = LocalDate.now();
        System.out.println(YELLOW+today+RESET);
        System.out.println(this.history);
    }


    public void isRunning(){
       this.isRunning =false;
        System.out.println("\nThank you for using " + BOLD+ "FINANCE TRACKER"+ RESET);
    }

    public void getBalance() {
        System.out.println(BOLD+"\nCurrent Balance :"+ GREEN+"R" +  this.Balance+ RESET+"\n");
    }

    public void Running(){
        Scanner scanner = new Scanner(System.in);
        while (isRunning) {
            System.out.println("""
                    ========== FINANCE TRACKER ==========
                    1. Add Income
                    2. Add Expense
                    3. View Transaction History
                    4. View Total Balance
                    5. Exit
                    =====================================
                    Choose an option: _""");
            String option = scanner.nextLine();

            String newString = option.trim().strip();
            switch (newString) {
                case "1":
                    income(cleanData());
                    break;
                case "2":
                   expense(cleanData());
                    break;
                case "3":
                    getHistory();
                    break;
                case "4":
                    getBalance();
                    break;
                case "5":
                    isRunning();
                    break;
                default:
                    System.out.println("Error! Enter a number between 1-5.\n");
            }
        }
    }
}



