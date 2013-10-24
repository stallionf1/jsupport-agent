package com.community;

import com.slaagent.services.SlaService;

public class DailyChecker {
    public static void main(String[] args) {
        SlaService ss = new SlaService();
        System.out.println("********* DAILY CHECKER HAS BEEN STARTED ***********");
        ss.getAllUsolvedTopicsWithoutJAnswer();
    }
}
