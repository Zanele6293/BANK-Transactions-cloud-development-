# How to run the project
# N/B Make sure the target folder exist
        mkdir target
# First run this
        javac -d target src/main/java/transections/co/za/*.java    --------> write once

Add *.java to the end of your command. This tells Java: "Compile every Java file in this folder."


# Secondly run this
         java -cp target transections.co.za.Main   ------------->  this is the command we will use to run our entire project 

mvn spring-boot:run
Your application will start up on http://localhost:8080.
Open another terminal and test it:
Deposit: curl -X POST "http://localhost:8080/api/transactions/income?amount=500"
Check Balance: curl -X GET "http://localhost:8080/api/transactions/balance"

WTC-Z2R5GGJK