package com.roland.asb.connection;

import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ConUtils {

    private static final Logger LOG = Logger.getLogger(ConUtils.class.getName());

    private String connectionString;

    public ConUtils() {
    }

    public ConUtils(String connectionString) {
        this.connectionString = connectionString;
    }

    // Send message to Queue or Topic
    public static void send(String connectionString, String entityPath, String content) throws Exception {
        IMessageSender sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(new ConnectionStringBuilder(connectionString, entityPath));

        String messageId = UUID.randomUUID().toString();
        // Send messages to queue
        System.out.printf("\tSending messages to %s ...\n", sender.getEntityPath());
        IMessage message = new Message();
        message.setMessageId(messageId);
        message.setTimeToLive(Duration.ofMinutes(1));
        byte[] byteArray = content.getBytes();
        message.setBody(byteArray);
        sender.send(message);
        System.out.printf("\t=> Sent a message with messageId %s\n", message.getMessageId());

        sender.close();
    }

    // Receive message from Queue or Subscription
    public static void receive(String connectionString, String entityPath) throws Exception {
        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);

        // receive messages from queue
        String receivedMessageId = "";

        System.out.printf("\n\tWaiting up to 5 seconds for messages from %s ...\n", receiver.getEntityPath());
        while (true) {
            IMessage receivedMessage = receiver.receive(Duration.ofSeconds(5));

            if (receivedMessage == null) {
                break;
            }
            System.out.printf("\t<= Received a message with messageId %s\n", receivedMessage.getMessageId());
            System.out.printf("\t<= Received a message with messageBody %s\n", new String(receivedMessage.getBody(), UTF_8));
            receiver.complete(receivedMessage.getLockToken());
            if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                throw new Exception("Received a duplicate message!");
            }
            receivedMessageId = receivedMessage.getMessageId();
        }
        System.out.printf("\tDone receiving messages from %s\n", receiver.getEntityPath());

        receiver.close();
    }

    // Send batch of messages to Queue or Topic
    public static void sendBatch(String connectionString, String entityPath, String content, int maxMessageCount) throws Exception {
        IMessageSender sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(new ConnectionStringBuilder(connectionString, entityPath));

        List<IMessage> messages = new ArrayList<>();

        for(int i=0; i<maxMessageCount; i++){
            String messageId = UUID.randomUUID().toString();
            IMessage message = new Message();
            message.setMessageId(messageId);
            message.setTimeToLive(Duration.ofMinutes(1));
            String contentMod = content + Integer.toString(i);
            byte[] byteArray = contentMod.getBytes();
            message.setBody(byteArray);

            messages.add(message);
            System.out.printf("\t=> Sending a message with messageId %s\n", message.getMessageId());
        }

        // Send messages to queue
        System.out.printf("\tSending messages to %s ...\n", sender.getEntityPath());
        sender.sendBatch(messages);
        System.out.printf("\t=> Sent %s messages\n", messages.size());

        sender.close();
    }

    // Receive batch of messages from Queue or Subscription
    public static void receiveBatch(String connectionString, String entityPath, int maxMessageCount) throws Exception {
        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);

        // receive messages from queue
        String receivedMessageId = "";

        System.out.printf("\n\tWaiting up to 5 seconds for messages from %s ...\n", receiver.getEntityPath());
        for(int i=0; i<maxMessageCount; i++) {
            IMessage receivedMessage = receiver.receive(Duration.ofSeconds(5));

            if (receivedMessage == null) {
                continue;
            }
            System.out.printf("\t<= Received a message with messageId %s\n", receivedMessage.getMessageId());
            System.out.printf("\t<= Received a message with messageBody %s\n", new String(receivedMessage.getBody(), UTF_8));
            receiver.complete(receivedMessage.getLockToken());
            if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                throw new Exception("Received a duplicate message!");
            }
            receivedMessageId = receivedMessage.getMessageId();
        }
        System.out.printf("\tDone receiving messages from %s\n", receiver.getEntityPath());

        receiver.close();
    }

    // Completes messages from Queue or Subscription based on messageLockToken
    public static void complete(String connectionString, String entityPath) throws Exception {
        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);

        // receive messages from queue
        String receivedMessageId = "";

        System.out.printf("\n\tWaiting up to 5 seconds for messages from %s ...\n", receiver.getEntityPath());
        while (true) {
            IMessage receivedMessage = receiver.receive(Duration.ofSeconds(5));

            if (receivedMessage == null) {
                break;
            }
            System.out.printf("\t<= Received a message with messageId %s\n", receivedMessage.getMessageId());
            System.out.printf("\t<= Completes a message with messageLockToken %s\n", receivedMessage.getLockToken());
            receiver.complete(receivedMessage.getLockToken());
            if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                throw new Exception("Received a duplicate message!");
            }
            receivedMessageId = receivedMessage.getMessageId();
        }
        System.out.printf("\tDone completing a message using its lock token from %s\n", receiver.getEntityPath());

        receiver.close();
    }

    // Completes message from Queue or Subscription based on messageLockToken
    public static void completeMessage(String connectionString, String entityPath) throws Exception {
        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);

        System.out.printf("\nWaiting up to default server wait time for messages from %s ...\n", receiver.getEntityPath());

        IMessage receivedMessage = receiver.receive();

        if (receivedMessage != null) {
            System.out.printf("\t<= Received a message with messageId %s\n", receivedMessage.getMessageId());
            System.out.printf("\t<= Completes a message with messageLockToken %s\n", receivedMessage.getLockToken());
            receiver.complete(receivedMessage.getLockToken());

            System.out.printf("\tDone completing a message using its lock token from %s\n", receiver.getEntityPath());
        } else {
            System.out.println("\tNo message in the queue\n");
        }

        receiver.close();
    }

    // Abandon message & make available again for processing from Queue or Subscription based on messageLockToken
    public static void abandon(String connectionString, String entityPath) throws Exception {
        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);

        System.out.printf("\n\tWaiting up to default server wait time for messages from %s ...\n", receiver.getEntityPath());
        IMessage receivedMessage = receiver.receive();

        if (receivedMessage != null) {
            System.out.printf("\t<= Received a message with messageId %s\n", receivedMessage.getMessageId());
            System.out.printf("\t<= Abandon a message with messageLockToken %s\n", receivedMessage.getLockToken());
            receiver.abandon(receivedMessage.getLockToken());

            System.out.printf("\tDone abandoning a message using its lock token from %s\n", receiver.getEntityPath());

        } else {
            System.out.println("\t<= No message in the queue \n");;
        }

        receiver.close();
    }

}
