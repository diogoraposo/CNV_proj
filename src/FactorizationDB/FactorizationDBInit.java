package FactorizationDB;
/*
 * Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

/**
 * This sample demonstrates how to perform a few simple operations with the
 * Amazon DynamoDB service.
 */
public class FactorizationDBInit {
    

    public static void main(String[] args) throws Exception {
    	FactorizationDB_API dbAPI = new FactorizationDB_API();
    	dbAPI.initialize();
    	
    	dbAPI.createTable("JEDI");
    	try{
    		System.out.println("Waiting for 10s");
    		Thread.sleep(10000);
    		System.out.println("Done waiting");
    	}catch(Exception e){
    		
    	}
    	
    	dbAPI.putMetric("JEDI", 1232, 666, 7, 9000, 5);
    	dbAPI.putMetric("JEDI", 2321, 999, 32, 1337, 52);
    	

    }
}

