package com.community;

import com.slaagent.services.SlaService;

public class HourlyChecker {
    public static void main(String[] args) {
        SlaService ss = new SlaService();
        System.out.println("********* HOURLY CHECKER HAS BEEN STARTED ***********");
        ss.getAllOpenedTopicsCurrentSlaStatus();
    }
 
            
}
