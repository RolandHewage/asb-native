package com.roland.asb;

import com.roland.asb.connection.ConUtils;

public class AsbConnector {

    private static final String connectionString = "Endpoint=sb://roland1.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=OckfvtMMw6GHIftqU0Jj0A0jy0uIUjufhV5dCToiGJk=";
    private static final String entityPath = "roland1queue";

    public static void main(String[] args) throws Exception {

//        // Basic send and receive message functionality to Azure service bus queue
//        ConUtils conUtils = new ConUtils();
//        String inputString = "roland";
//        byte[] byteArray = inputString.getBytes();
//        conUtils.send(connectionString,entityPath, inputString);
//        conUtils.receive(connectionString,entityPath);
//        System.exit(0);

//        // Basic publish and subscribe message functionality to Azure service bus topis and subscriptions
//        ConUtils conUtils = new ConUtils();
//        String inputString = "roland";
//        byte[] byteArray = inputString.getBytes();
//        conUtils.send(connectionString,"roland1topic", inputString);
//        conUtils.receive(connectionString,"roland1topic/subscriptions/roland1subscription1");
//        conUtils.receive(connectionString,"roland1topic/subscriptions/roland1subscription2");
//        conUtils.receive(connectionString,"roland1topic/subscriptions/roland1subscription3");
//        System.exit(0);

//        // Basic send and receive batch of messages functionality to Azure service bus queue
//        ConUtils conUtils = new ConUtils();
//        String inputString = "roland";
//        conUtils.sendBatch(connectionString,entityPath, inputString, 4);
//        conUtils.receiveBatch(connectionString,entityPath, 4);
//        System.exit(0);

//        // Basic publish and subscribe batch of messages functionality to Azure service bus topis and subscriptions
//        ConUtils conUtils = new ConUtils();
//        String inputString = "roland";
//        byte[] byteArray = inputString.getBytes();
//        conUtils.sendBatch(connectionString,"roland1topic", inputString, 3);
//        conUtils.receiveBatch(connectionString,"roland1topic/subscriptions/roland1subscription1", 3);
//        conUtils.receiveBatch(connectionString,"roland1topic/subscriptions/roland1subscription2", 3);
//        conUtils.receiveBatch(connectionString,"roland1topic/subscriptions/roland1subscription3", 3);
//        System.exit(0);

//        // Basic complete messages & delete based on messageLockToken functionality to Azure service bus queue
//        ConUtils conUtils = new ConUtils();
//        String inputString = "roland";
//        conUtils.send(connectionString,entityPath, inputString);
//        conUtils.complete(connectionString,entityPath);
//        System.exit(0);

//        // Basic complete messages & delete based on messageLockToken functionality to Azure service bus subscriptions
//        ConUtils conUtils = new ConUtils();
//        String inputString = "roland";
//        conUtils.send(connectionString,"roland1topic", inputString);
//        conUtils.complete(connectionString,"roland1topic/subscriptions/roland1subscription1");
//        conUtils.complete(connectionString,"roland1topic/subscriptions/roland1subscription2");
//        conUtils.complete(connectionString,"roland1topic/subscriptions/roland1subscription3");
//        System.exit(0);

//        // Basic complete single message & delete based on messageLockToken functionality to Azure service bus queue
//        ConUtils conUtils = new ConUtils();
//        String inputString = "roland";
//        conUtils.send(connectionString,entityPath, inputString);
//        conUtils.completeMessage(connectionString,entityPath);
//        conUtils.completeMessage(connectionString,entityPath);
//        System.exit(0);

        // Basic complete single messages & delete based on messageLockToken functionality to Azure service bus subscriptions
        ConUtils conUtils = new ConUtils();
        String inputString = "roland";
        conUtils.send(connectionString,"roland1topic", inputString);
        conUtils.completeMessage(connectionString,"roland1topic/subscriptions/roland1subscription1");
        conUtils.completeMessage(connectionString,"roland1topic/subscriptions/roland1subscription2");
        conUtils.completeMessage(connectionString,"roland1topic/subscriptions/roland1subscription3");
        conUtils.completeMessage(connectionString,"roland1topic/subscriptions/roland1subscription1");
        conUtils.completeMessage(connectionString,"roland1topic/subscriptions/roland1subscription2");
        conUtils.completeMessage(connectionString,"roland1topic/subscriptions/roland1subscription3");
        System.exit(0);

//        // Basic abandon messages & make available again for processing based on messageLockToken functionality to
//        // Azure service bus queue
//        ConUtils conUtils = new ConUtils();
//        String inputString = "roland";
//        conUtils.send(connectionString,entityPath, inputString);
//        conUtils.abandon(connectionString,entityPath);
//        conUtils.complete(connectionString,entityPath);
//        System.exit(0);

//        // Basic abandon messages & abandon messages & make available again for processing  based on
//        // messageLockToken functionality to Azure service bus subscriptions
//        ConUtils conUtils = new ConUtils();
//        String inputString = "roland";
//        conUtils.send(connectionString,"roland1topic", inputString);
//        conUtils.abandon(connectionString,"roland1topic/subscriptions/roland1subscription1");
//        conUtils.abandon(connectionString,"roland1topic/subscriptions/roland1subscription2");
//        conUtils.abandon(connectionString,"roland1topic/subscriptions/roland1subscription3");
//        conUtils.complete(connectionString,"roland1topic/subscriptions/roland1subscription1");
//        conUtils.complete(connectionString,"roland1topic/subscriptions/roland1subscription2");
//        conUtils.complete(connectionString,"roland1topic/subscriptions/roland1subscription3");
//        System.exit(0);


    }
}
