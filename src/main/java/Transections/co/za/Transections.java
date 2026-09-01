package Transections.co.za;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Transections {
    private int Balance;
    static boolean isRunning = true;
    private StringBuilder history = new StringBuilder();

   public Transections(int Balance){
       this.Balance = Balance;
   }

   public int addBalance(int deposit){
       return this.Balance+deposit;
   }

    public int subBalance(int withdraw){
        return this.Balance-=withdraw;
    }

    //This method gives us what we typed on the terminal
    public int cleanData(){

       Scanner input = new Scanner(System.in);
       System.out.println("Amount: ");
        String amount = input.nextLine();
        System.out.println("Describe: ");
        String description = input.nextLine();
        int cleanAmount = Integer.parseInt(amount);
        history.append(description+"           ");
        return cleanAmount;
    }

    public int income(int money){
        int appending = addBalance(money);
        this.Balance=appending;
        history.append("+"+money+"\n");
        System.out.println("Succesfully deposited R"+money);
        return appending;
    }

    public int expense (int money){
        int decreaseAmount =subBalance(money);
        this.Balance=decreaseAmount;
        history.append("-"+money);
        System.out.println("Succesfully withdrawed R"+money);
        return decreaseAmount;
    }

    public StringBuilder getHistory() {
        System.out.println("Your Transection History:");
        System.out.println(this.history);
        return history;
    }


    public boolean isRunning(){
       this.isRunning =false;
        System.out.println("Thank you Bye");
       return this.isRunning;
    }

    public double getBalance() {
        System.out.println("Balance :"+"R" + this.Balance);
        return this.Balance;
    }
}



