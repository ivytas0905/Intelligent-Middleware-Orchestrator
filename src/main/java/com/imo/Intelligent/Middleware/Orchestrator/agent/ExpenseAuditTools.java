package com.imo.Intelligent.Middleware.Orchestrator.agent;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.ArrayList;

@Component
public class ExpenseAuditTools {

    private static final double MAX_HOTEL_PER_NIGHT = 200.0;
    private static final double MAX_AIRFARE_PER_PERSON = 1000.0;
    private static final double MAX_MEALS_PER_DAY = 100.0;
    private static final double MAX_TAXI_PER_DAY = 150.0;
    private static final double FLIGHT_PER_PERSON = 2800.0;
    private static final double TRAIN_PER_PERSON = 1200.0;
    private static final double MANAGER_LIMIT = 5000.00;
    private static final double DIRECTOR_LIMIT = 10000.00;

    @Tool("validate the expense item according to the company policy and return the status.")
    public String validateExpenseItem(
        @P("Type of expense: HOTEL, MEAL, TAXI, FLIGHT, TRAIN, OTHER") String expenseType,
        @P("Amount in USD") double amount, 
        @P("Description of the expense") String description
    ){
       return switch (expenseType.toUpperCase()) {
        case "HOTEL" -> amount <= MAX_HOTEL_PER_NIGHT
            ? "Compliant: Hotel $" + amount + "is within $" + MAX_HOTEL_PER_NIGHT + " per night limit"
            : "Review: Hotel $" + amount + "exceeds the limit of $" + MAX_HOTEL_PER_NIGHT + " per night limit";
        case "MEAL" -> amount <= MAX_MEALS_PER_DAY
            ? "Compliant: Meal $" + amount + "is within $" + MAX_MEALS_PER_DAY + " per day limit"
            : "Review: Meal $" + amount + "exceeds the limit of $" + MAX_MEALS_PER_DAY + " per day limit";
        case "TAXI" -> amount <= MAX_TAXI_PER_DAY
            ? "Compliant: Taxi $" + amount + "is within $" + MAX_TAXI_PER_DAY + " per day limit"
            : "Review: Taxi $" + amount + "exceeds the limit of $" + MAX_TAXI_PER_DAY + " per day limit";
        case "FLIGHT"-> amount <= FLIGHT_PER_PERSON
            ? "Compliant: Flight $" + amount + "is within $" + FLIGHT_PER_PERSON + " per person limit"
            : "Review: Flight $" + amount + "exceeds the limit of $" + FLIGHT_PER_PERSON + " per person limit";
        case "TRAIN"-> amount <= TRAIN_PER_PERSON
            ? "Compliant: Train $" + amount + "is within $" + TRAIN_PER_PERSON + " per person limit"
            : "Review: Train $" + amount + "exceeds the limit of $" + TRAIN_PER_PERSON + " per person limit";
        case "OTHER"-> 
            "Review: Miscellaneous $" + amount + "("+ description + ") . Flagged for manual review";   
        default -> "Unknown expense type: " + expenseType + " . Manual review required";
       };
    }  
    @Tool("Calculate the total amount of expenses and summarize by catgegory")
    public String calculateTotalExpenses(
             @P("Comma-seperated list of amounts, e.g. 100.0, 200.0") String amounts, 
             @P("Comma-seperated list of catagories matching the amount") String categories 
        ){
        
        String[] amountArr = amounts.split(",");
        String[] categoryArr = categories.split(",");
        
        double total = 0.0;
        StringBuilder summary = new StringBuilder("Expense Summary:\n");
        for(int i = 0; i < amountArr.length; i++){
            double amount = Double.parseDouble(amountArr[i].trim());
            total += amount;
            summary.append(String.format("- %-16s $%.2f%n",categoryArr[i].trim(),amount));

        }
        summary.append("--------------------\n");
        summary.append(String.format("   TOTAL:      %.2f%n",total));
        return summary.toString();
    
}
    @Tool("Determine the required approval level based on total expense amount")
    public String checkApprovalLevel(
             @P("Total expense amount in USD") double totalAmount){

        if(totalAmount <= MANAGER_LIMIT){
           return String.format("Approval: %.2f is within the %.2f manager limit.", totalAmount, MANAGER_LIMIT);
        }else if (totalAmount <= DIRECTOR_LIMIT){
            return String.format("Director Limit Approval: %.2f is within the director limit %.2f but exceeds the manager limit. ", totalAmount, DIRECTOR_LIMIT);
        }else{
            return String.format("Exceed the Director Limit.");

        }
     }      
    @Tool("Flag the suspicious expenses that may indicate fraud.") 
    public String flagSuspiciousPatterns(
            @P("Full description of the expense claim to analyze") String expenseDescription) {

        List<String> redFlags = new ArrayList<>();
        String lower = expenseDescription.toLowerCase();

        if (lower.contains("weekend") || lower.contains("saturday") || lower.contains("sunday"))
            redFlags.add("⚠️  Weekend expense — requires business justification.");
        if (lower.contains("alcohol") || lower.contains("bar") || lower.contains("liquor"))
            redFlags.add("🚫 Alcohol — not reimbursable per policy.");
        if (lower.contains("spa") || lower.contains("entertainment") || lower.contains("game"))
            redFlags.add("🚫 Personal entertainment — not reimbursable per policy.");
        if (lower.contains("family") || lower.contains("spouse") || lower.contains("child"))
            redFlags.add("⚠️  Possible personal guest — requires justification.");
        if (lower.contains("first class") || lower.contains("business class"))
            redFlags.add("⚠️  Premium cabin detected — only economy is reimbursable per policy.");

        return redFlags.isEmpty()
                ? "✅ No suspicious patterns detected."
                : "🔍 Red Flags:\n" + String.join("\n", redFlags);   

    }
}
 
