package Shai;

import customes.Account;
import customes.Client;
import customes.Lenders;
import data.Database;
import data.schema.generated.AbsDescriptor;
import loan.Loan;
import loan.enums.eLoanFilters;
import loan.enums.eLoanStatus;
import Money.operations.Payment;
import Money.operations.Transaction;
import time.Timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static utills.BackgroundFunc.*;

public class PrintFuncs {


    //NIKOL: WTF????


    // func2 helpers:
    public static void printLenderList(List<Lenders> lendersList) {
        for (Lenders lender:lendersList)
        {
            System.out.println(lender);
        }
    }
    public static void printACTIVEstatus(Loan currLoan) {
        Timeline startLoanYaz = currLoan.getStartLoanYaz();
        Timeline paymentFrequency = currLoan.getPaymentFrequency();
        List<Payment> paymentsList = currLoan.getPaymentsList();
        double payedFund =currLoan.getPayedFund();
        double payedInterest = currLoan.getPayedInterest();
        double currFundDepth = currLoan.calculateFundDepth();
        double currInterestDepth =currLoan.calculateCurrInterestDepth();
        System.out.println("Loan start time: " + startLoanYaz + " Yaz");
        int T = (Timeline.getCurrTime() - startLoanYaz.getTimeStamp()) % paymentFrequency.getTimeStamp();
        System.out.println("next payment: " + T);
        for(Payment pay:paymentsList)
        {
            System.out.println(pay.toString());
        }
        System.out.println("total payed fund: " + payedFund);
        System.out.println("total payed interest: " + payedInterest);
        System.out.println("remaining fund: " + currFundDepth);//currFundDepth);
        System.out.println("remaining interest: " + currInterestDepth);//currInterestDepth);
    }
    public static void printRISKstatus(Loan currLoan){

        List<Payment> paymentsList = currLoan.getPaymentsList();
        int sumNotPayed = 0;
        int numNotPayed=0;
        for(Payment pays:paymentsList)
        {
            if(pays.isPayed() == false)
            {
                sumNotPayed+=pays.getFundPlusInterest();
                ++numNotPayed;
            }

        }
        System.out.println("num of delayed payments: " + numNotPayed);
        System.out.println("sum of delayed: " + sumNotPayed);
    }
    public static void printFINISHEDstatus(Loan currLoan){
        Timeline startLoanYaz = currLoan.getStartLoanYaz();
        Timeline endLoanYaz = currLoan.getEndLoanYaz();
        List<Payment> paymentsList = currLoan.getPaymentsList();

        System.out.println("start loan yaz: "+startLoanYaz);
        System.out.println("end loan yaz" + endLoanYaz);
        for(Payment pay:paymentsList)
        {
            System.out.println(pay.toString());
        }
    }
    //func3 helpers
    public static void printAccountInfo(Client client) {
        Account account = client.getMyAccount();
        List<Transaction> transactionList = account.getTnuaList();
        if(!transactionList.isEmpty()){
            System.out.println("Transactions:");
        }
        double beforeBalance=account.getCurrBalance();
        double afterBalance=account.getCurrBalance();;
        for (Transaction transaction : transactionList) {
            System.out.println("yaz of tnua: " + transaction.getTimeOfMovement() + "yazes");
            if (transaction.getSum() > 0) {
                System.out.println("schum tnua: +" + transaction.getSum());
            }
            else {
                System.out.println("schum tnua: " + transaction.getSum());
            }

            afterBalance += transaction.getSum();
            System.out.println("balance before the tnua: " + beforeBalance);
            System.out.println("balance after the tnua: " + afterBalance);
            beforeBalance=afterBalance;
            System.out.println("@@@@@@@@@@@@@@@@@@@@@");
        }
    }
    public static void printConnectedLoans(Client client) {
        String name = client.getFullName();
        List<Loan> lenderLoanList = client.getClientAsLenderLoanList();
        List<Loan> borrowLoanList = client.getClientAsBorrowLoanList();

        if(!lenderLoanList.isEmpty()) {
            System.out.println("those are the Loans that " + name + " is a lender:");
            for (Loan loan:lenderLoanList)
            {
                printLoanInfo(loan);
                System.out.println("********************************");
            }
        }
        else{
            System.out.println("there are no Loans that " + name + " is a lender");
        }
        if(!borrowLoanList.isEmpty()) {
            System.out.println("those are the Loans that " + name + " is a borrower:");
            for (Loan loan:borrowLoanList)
            {
                printLoanInfo(loan);
                System.out.println("********************************");
            }
        }
        else{
            System.out.println("there are no Loans that " + name + " is a borrower");
        }
        System.out.println("________________________________");
    }
    public static void PrintStatusConnectedLoans(Loan loan) {
        eLoanStatus status=loan.getStatus();
        switch (status)
        {
            case PENDING:
            {
                double missingMoney = loan.getLoanOriginalDepth() - calculateDeposit(loan.getLendersList());
                System.out.println(missingMoney + "is missing in order to turn this loan active");
                break;
            }
            case ACTIVE:
            {
                System.out.println("next payment is in " + loan.nextYazToPay() + "yazes");
                System.out.println("borrower will pay in the next payment: " + loan.nextExpectedPaymentAmount());
                break;
            }
            case RISK:
            {
                printRISKstatus(loan);
                break;
            }
            case FINISHED:
            {
                System.out.println("start loan yaz: "+loan.getStartLoanYaz());
                System.out.println("end loan yaz" + loan.getEndLoanYaz());
                break;
            }
            default:
                break;
        }
    }
    public static void printLoanInfo(Loan loan){
        System.out.println("Loan Id: " + loan.getLoanID());
        System.out.println("Loan category: " + loan.getLoanCategory());
        System.out.println("loan original fund: " + loan.getLoanOriginalDepth());
        System.out.println("loan payment Frequency: " + loan.getPaymentFrequency());
        System.out.println("loan interest: " + loan.getOriginalInterest());
        System.out.println("total Loan Cost, Interest Plus Original Depth: " + loan.getTotalLoanCostInterestPlusOriginalDepth());
        System.out.println("loan status: " + loan.getStatus());
        PrintStatusConnectedLoans(loan);
    }

    //func4 helpers
    /**
     * prints all clients in database to UI with index attached
     */
    public static void printAllClientsFromDatabase() {
        //creating index i , and printing all existing clients in database
        int i = 1;
        for (Client client : Database.getClientMap().values()) {
            System.out.println(i + ". " + client.getFullName());
            i++;
        }
    }
    /**
     * asking user and getting wanted deposit amount
     * @param full_name
     * @return
     */
    public  static int getDepositAmount(String full_name){

        System.out.println("How much would you like to to deposit into"+full_name+"'s account ?");
        System.out.println("(please enter a positive integer number)");
        int deposit = readIntFromUser(1,Integer.MAX_VALUE);
        return deposit;
    }
    public static Client ChooseClientFromDatabase () {
        //asking user to choose a client from database ,and getting input value of wanted client index
        List<Client> clientsList = Database.getClientsList();
        int clientListSize =clientsList.size();
        System.out.println("Please enter wanted client index for deposit\n(index must be an integer number between 1 - "+clientListSize+" )");
        int userClientIndexChoice = PrintFuncs.readIntFromUser(1,clientListSize);
        //getting client
        Client wantedClient =clientsList.get(userClientIndexChoice-1);
        return wantedClient;
    }

    //func5 helpers
    public  static int getWithdrawalAmount(String full_name){
        //asking user and getting wanted deposit amount // S
        System.out.println("How much would you like to withdraw from "+full_name+"'s account ?");
        System.out.println("(please enter a positive integer number)");
        int withdraw = -(readIntFromUser(0,(int)Database.getClientMap().get(full_name).getMyAccount().getCurrBalance()));
        return withdraw;
    }


    //func6 helpers
    /**
     * THIS FUNC PRINTS ALL THE CLIENTS IN THE SYSTEM AND ASK THE USER TO CHOOSE ONE, IT RETURNS THE CLIENT USER CHOSE
     * @return
     */
    public static Client printAndChooseClientsInTheSystem(){
        ArrayList<Client> v = new ArrayList<>();
        int i=1;
        for(Client client: Database.getClientMap().values()) {
            System.out.println(i + ". " + client.getFullName());
            System.out.println("current balance: " + client.getMyAccount().getCurrBalance());
            v.add(client);
            ++i;
        }
        i =readIntFromUser(1,Database.getClientMap().size());

        return v.get(i-1);//todo might be i instead of i-1 becasue array starts from 0?
    }
    /**
     * THIS FUNC INITIALLIZE THE CLIENT MENU
     * @return
     */
    public static Client customersMenu(){
        Scanner sc = new Scanner(System.in);
        System.out.println("please choose a customer to invest with");
        return printAndChooseClientsInTheSystem();
    }
    /**
     * THIS FUNC GETS A CLIENT AND THEN ASK THE USER WHAT LOANS DO THEY WANT THE CLIENT TO PARTICIPATE ACCORDING TO PARAMETERS
     * @param client
     * @return ArrayList <Loan>
     */
    //TODO ADD OPTION FOR CHOOSING NO CATEGORY AT ALL in loanToInvest!!!!
    public static ArrayList<Loan> loanToInvest (Client client) {
        ArrayList<Loan> result = new ArrayList<>();
        ArrayList<String> loanCategoryUserList = new ArrayList<>();
        double balance = client.getMyAccount().getCurrBalance(), minYazTimeFrame = 0;
        ArrayList<Integer> loanFilters;
        Double minInterestPerYaz = Double.valueOf(0);
        //part 2 in word document

        loanFilters = getLoanFilters();
        if (loanFilters.get(eLoanFilters.LOAN_CATEGORY.ordinal()) == 1) {
            loanCategoryUserList = chooseCategoryToInvest();
        }
        if (loanFilters.get(eLoanFilters.MINIMUM_INTEREST_PER_YAZ.ordinal()) == 1) {
            System.out.println("Please choose the minimum interest per yaz ");
            minInterestPerYaz = readDoubleFromUser(0, Double.MAX_VALUE);
        }
        if (loanFilters.get(eLoanFilters.MINIMUM_YAZ_TIME_FRAME.ordinal()) == 1) {
            System.out.println("Please choose the minimum yaz time frame ");
            minYazTimeFrame = readIntFromUser(0, Integer.MAX_VALUE);
        }
        //part 3 in word document:
        for (Loan loan : Database.getLoanList()) {
            if (loan.getStatus() == eLoanStatus.NEW || loan.getStatus() == eLoanStatus.PENDING)//if the loan is new or pending
                if (client.getFullName() != loan.getBorrowerName())//If the client's name is not the borrower
                        if (minInterestPerYaz <= loan.getInterestPercentagePerTimeUnit())
                            if (minYazTimeFrame <= loan.getOriginalLoanTimeFrame().getTimeStamp())
                                if (checkCategoryList(loanCategoryUserList, loan.getLoanCategory()))
                                    result.add(loan);
        }
        return result;
    }
    /**
     * this func gets client and ASK THE USER WHAT LOANS IT WILL BE PARTICIPATE and returns list of the filtered loans that the user chose
     * @param client
     */
    public static ArrayList<Loan> ChooseLoans(Client client) {
        int  index = 1;;
        ArrayList<Integer> chosenLoansNumb = new ArrayList<>();
        ArrayList<Loan> Loanslist = loanToInvest(client);
        ArrayList<Loan> result ;
        for (Loan loan : Loanslist) {
            System.out.println(index + ". " + loan.toString());
            ++index;
        }
        boolean valid =true;
        do {
            System.out.println("please choose loans that the client would like to invest in: \n" +
                    "\"(Your answer must be returned in the above format: \"Desired loan number\", \"Desired loan number\", etc.)\"");
            Scanner br = new Scanner(System.in);
            String lines = br.nextLine();
            String[] userInputs = lines.trim().split(",");
            for (String userInput : userInputs) {
                try {
                    chosenLoansNumb.add(Integer.parseInt(userInput)-1);
                } catch (NumberFormatException exception) {
                    System.out.println("Please enter only vaild inputs: (inputs must be numbers only!)");
                    chosenLoansNumb.clear();
                    valid=false;
                }
            }
        }while(!valid);
        result = getResultedArray(Loanslist,chosenLoansNumb);// RETURNS new array that is the user's chosen loans.
        return result;
    }
    public static ArrayList<Integer> getLoanFilters (){
        Scanner sc = new Scanner(System.in);
        ArrayList<Integer> result = new ArrayList<>();
        System.out.println("Would you like to filter by Loan category? press 0 or 1");
        result.add(readIntFromUser(0,1));
        System.out.println("Thank you, would you like to filter by minimum interest per yaz? press 0 or 1");
        result.add(readIntFromUser(0,1));
        System.out.println("Thank you, would you like to filter by minimum yaz time frame? press 0 or 1");
        result.add(readIntFromUser(0,1));
        System.out.println("Thank you");
        return result;
    }
    public static ArrayList<String> chooseCategoryToInvest() {
        boolean valid = true;
        ArrayList<String> userSelectedCategories = new ArrayList<>();
        List <String> allCategoryList = Database.getAllCategories();
        do {
            System.out.println("Please select from the following list of options, the desired categories for investment:\n" +
                    "(Your answer must be returned in the above format: \"Desired category number\", \"Desired category number\", etc.)\n" +
                    "press 0 for choosing all categories");
            int index=1;
            for (String category : allCategoryList) {
                System.out.println(index+". "+category);
                ++index;
            }
            Scanner br = new Scanner(System.in);
            String lines = br.nextLine();
            String[] userInputs = lines.trim().split(",");

            if (lines.equals("0"))
            {
                valid =true;
                userSelectedCategories.addAll(allCategoryList);
            }
            else{
                for (String userInput : userInputs) {
                    try {
                        userSelectedCategories.add(allCategoryList.get(Integer.parseInt(userInput) - 1));
                        //todo: valid ==== true
                    } catch (NumberFormatException exception) {
                        System.out.println("Please enter only valid inputs: (inputs must be numbers only!)");
                        userSelectedCategories.clear();
                        valid = false;
                    }
                }
            }

        }while(!valid);

        return userSelectedCategories;

    }
    /**
     * this function gets a loan and a client AS LENDER and connects the loan to the client
     * @param loan
     * @param client
     */
    public static void ClientToLoan(Loan loan,Client client,double investment){
        //investing the money
        TransferMoneyBetweenAccounts(client.getMyAccount(),investment,loan.getLoanAccount());
        //adding lender to loans lender list
        addLenderToLoanList(client,loan,investment);
        //adding lender to his Client -> clientAsLenderLoanList data member.
        client.addLoanAsLender(loan);
        //checks if loands status needs an update
        loan.UpdateLoanStatusIfNeeded();
    }
    /**
     *  func's gets amountofmoney to invest and wanted loans to invest in , and return the amount of money to invest in each loan so the money will be splitted equaliy
     * @param amountOfLoansToInvest
     * @param amountOfMoney
     * @return
     */
    public static double amountOfMoneyPerLoan(int amountOfLoansToInvest,double amountOfMoney) {
        return (amountOfMoney/amountOfLoansToInvest);
    }

    //func7 helpers
    public static void printYazAfterPromote(){
        System.out.println("Yaz was: " );
        Timeline.printPreviousCurrTime();
        System.out.println("Yaz now: " );
        Timeline.printStaticCurrTime();
    }
    //todo: add excepetion
    //func1 helpers
    public static boolean CheckAndPrintInvalidFile(AbsDescriptor descriptor) throws Exception {
        boolean isValid =true;
        String s = new String();

        if(!checkValidCategories(descriptor)){
            s+= "\nthere is loan category that does not exist";
            isValid = false;
        }
        if(!checkValidCustomersList(descriptor)){
            s+="\nthere is two customers with the same name";
            isValid =false;
        }
        if(!checkValidLoanOwner(descriptor)){
            s+="\nthere is a loan with a loan owner name that does not exist";
            isValid = false;
        }
        if(!checkValidPaymentFrequency(descriptor)){
            s+="\npayment frequency is not fully divided by the total time of the loan";
            isValid = false;
        }

        if(!isValid){
            s="File not valid!\n" +s;
            throw new Exception(s);
        }
        return isValid;
    }


    //general
    public static int readIntFromUser(int min, int max){
        Scanner sc = new Scanner(System.in);
        int number;
        do {
            System.out.println("Please enter an Integer number between: " + min + " - " + max);

            while (!sc.hasNextInt()) {
                System.out.println("Input is not valid, please enter a valid number!");
                sc.next(); // this is important!
            }
            number = sc.nextInt();
        } while (number < min || number > max);
        return number;
    }
    public static double readDoubleFromUser(double min, double max){
        Scanner sc = new Scanner(System.in);
        Double number;
        do {
            System.out.println("Please enter a number between" + min + "and " + max);
            while (!sc.hasNextInt()) {
                System.out.println("Please enter a number!");
                sc.next(); // this is important!
            }
            number = sc.nextDouble();
        } while (number < min || number > max);
        return number;
    }



}