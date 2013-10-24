package com.community;

import com.slaagent.services.SqlService;

public class CustomQueryExecuter {

    public static void main(String[] args) {
       new SqlService().executeCustomQuery(); //accroding to property agent.sql.custom-query
    }
}
