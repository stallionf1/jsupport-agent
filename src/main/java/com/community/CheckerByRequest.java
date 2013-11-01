package com.community;

import com.slaagent.services.SlaService;

public class CheckerByRequest {

    public static void main(String[] args) {
        //getOpenedTopicsLastCommentPartner
        SlaService ss = new SlaService();
        System.out.println("********* CHECKER HAS BEEN STARTED ***********");
        ss.getOpenedTopicsLastCommentPartner();
    }
    
}
