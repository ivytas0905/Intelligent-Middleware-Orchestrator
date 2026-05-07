package com.imo.Intelligent.Middleware.Orchestrator.controller;

import com.imo.Intelligent.Middleware.Orchestrator.agent.OrchestrationAgent;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final OrchestrationAgent agent;

    public AgentController(OrchestrationAgent agent){
        this.agent = agent;
    }
    @PostMapping("/audit")
    public String auditExpense(@RequestBody String expenseReport) {
        return agent.auditExpenseReport(expenseReport);
    }

}

