package com.roland.asb;

import com.google.gson.JsonParser;
import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.BValueCreator;
import org.ballerinalang.jvm.api.values.*;
import org.ballerinalang.jvm.scheduling.StrandMetadata;
import org.ballerinalang.jvm.types.AnnotatableType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.runtime.AsyncFunctionCallback;
import org.ballerinalang.jvm.api.BRuntime;


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.roland.asb.AsbConstants.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class MessageDispatcher {
    private String consumerTag;
    private BObject service;
    private String queueName;
    private String connectionKey;
    private BRuntime runtime;
    private static final StrandMetadata ON_MESSAGE_METADATA = new StrandMetadata(ORG_NAME, ASB,
            ASB_VERSION, FUNC_ON_MESSAGE);

    public MessageDispatcher(BObject service, BRuntime runtime) {
        this.service = service;
        this.queueName = getQueueNameFromConfig(service);
        this.connectionKey = getConnectionStringFromConfig(service);
        this.consumerTag = service.getType().getName();
        this.runtime = runtime;
    }

    private String getQueueNameFromConfig(BObject service) {
        BMap serviceConfig = (BMap) ((AnnotatableType) service.getType())
                .getAnnotation(BStringUtils.fromString(AsbConstants.PACKAGE_RABBITMQ_FQN + ":"
                        + AsbConstants.SERVICE_CONFIG));
        @SuppressWarnings(AsbConstants.UNCHECKED)
        BMap<BString, Object> queueConfig =
                (BMap) serviceConfig.getMapValue(AsbConstants.ALIAS_QUEUE_CONFIG);
        return queueConfig.getStringValue(AsbConstants.QUEUE_NAME).getValue();
    }

    private String getConnectionStringFromConfig(BObject service) {
        BMap serviceConfig = (BMap) ((AnnotatableType) service.getType())
                .getAnnotation(BStringUtils.fromString(AsbConstants.PACKAGE_RABBITMQ_FQN + ":"
                        + AsbConstants.SERVICE_CONFIG));
        @SuppressWarnings(AsbConstants.UNCHECKED)
        BMap<BString, Object> queueConfig =
                (BMap) serviceConfig.getMapValue(AsbConstants.ALIAS_QUEUE_CONFIG);
        return queueConfig.getStringValue(CONNECTION_STRING).getValue();
    }

    public void receiveMessages(BObject listener) {
        String connectionString = connectionKey;
        String entityPath = queueName;
        System.out.println("[ballerina/rabbitmq] Consumer service started for queue " + queueName);

        try{
            QueueClient receiveClient = new QueueClient(new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            this.registerReceiver(receiveClient, executorService);

            System.out.printf("\tDone receiving messages from %s\n", receiveClient.getEntityPath());
        } catch (Exception e) {

        }

        ArrayList<BObject> startedServices =
                (ArrayList<BObject>) listener.getNativeData(AsbConstants.STARTED_SERVICES);
        startedServices.add(service);
        service.addNativeData(AsbConstants.QUEUE_NAME.getValue(), queueName);
    }

    public void registerReceiver(QueueClient queueClient, ExecutorService executorService) throws Exception {


        // register the RegisterMessageHandler callback with executor service
        queueClient.registerMessageHandler(new IMessageHandler() {
                                               // callback invoked when the message handler loop has obtained a message
                                               public CompletableFuture<Void> onMessageAsync(IMessage message) {

                                                   byte[] body = message.getBody();
                                                   System.out.printf("\t<= Received a message with messageId %s\n", message.getMessageId());
                                                   System.out.printf("\t<= Received a message with messageBody %s\n", new String(message.getBody(), UTF_8));
                                                   handleDispatch(message.getBody());

                                                   return CompletableFuture.completedFuture(null);
                                               }

                                               // callback invoked when the message handler has an exception to report
                                               public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
                                                   System.out.printf(exceptionPhase + "-" + throwable.getMessage());
                                               }
                                           },
                // 1 concurrent call, messages are auto-completed, auto-renew duration
                new MessageHandlerOptions(1, true, Duration.ofMinutes(1)),
                executorService);

    }

    private void waitForEnter(int seconds) {
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            executor.invokeAny(Arrays.asList(() -> {
                System.in.read();
                return 0;
            }, () -> {
                Thread.sleep(seconds * 1000);
                return 0;
            }));
        } catch (Exception e) {
            // absorb
        }
    }

    private void handleDispatch(byte[] message) {
        AttachedFunction[] attachedFunctions = service.getType().getAttachedFunctions();
        System.out.println(attachedFunctions[0].getName());
        AttachedFunction onMessageFunction;
        if (FUNC_ON_MESSAGE.equals(attachedFunctions[0].getName())) {
            onMessageFunction = attachedFunctions[0];
        } else {
            return;
        }
        BType[] paramTypes = onMessageFunction.getParameterType();
        int paramSize = paramTypes.length;
        dispatchMessage(message);
    }

    private void dispatchMessage(byte[] message) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            AsyncFunctionCallback callback = new AsbResourceCallback(countDownLatch, queueName,
                    message.length);
//            ResponseCallback callback = new ResponseCallback();
            BObject messageBObject = getMessageBObject(message);
            executeResourceOnMessage(callback, messageBObject, true);
            countDownLatch.await();
        } catch (InterruptedException e) {

        } catch (BError exception) {

        }
    }

    private BObject getMessageBObject(byte[] message)  {
        System.out.printf("\t<= Received a message with messageBody %s\n", new String(message, UTF_8));

        BObject messageBObject = BValueCreator.createObjectValue(AsbConstants.PACKAGE_ID_ASB,
                AsbConstants.MESSAGE_OBJECT);
        messageBObject.set(AsbConstants.MESSAGE_CONTENT, BValueCreator.createArrayValue(message));

        return messageBObject;
    }

    private Object getMessageContentForType(byte[] message, BType dataType) throws UnsupportedEncodingException {
        int dataTypeTag = dataType.getTag();
        switch (dataTypeTag) {
            case TypeTags.STRING_TAG:
                return BStringUtils.fromString(new String(message, StandardCharsets.UTF_8.name()));
            case TypeTags.JSON_TAG:
                return JsonParser.parseString(new String(message, StandardCharsets.UTF_8.name()));
            case TypeTags.XML_TAG:
                return XMLFactory.parse(new String(message, StandardCharsets.UTF_8.name()));
            case TypeTags.FLOAT_TAG:
                return Float.parseFloat(new String(message, StandardCharsets.UTF_8.name()));
            case TypeTags.INT_TAG:
                return Integer.parseInt(new String(message, StandardCharsets.UTF_8.name()));
//            case TypeTags.RECORD_TYPE_TAG:
//                return JSONUtils.convertJSONToRecord(JsonParser.parseString(new String(message,
//                                StandardCharsets.UTF_8.name())),
//                        (StructureType) dataType);
            case TypeTags.ARRAY_TAG:
                if (((BArray) dataType).getElementType().getTag() == TypeTags.BYTE_TAG) {
                    return message;
                } else {

                }
            default:
                return "";
        }
    }

    private void executeResourceOnMessage(AsyncFunctionCallback callback, Object... args) {
        executeResource(AsbConstants.FUNC_ON_MESSAGE, callback, ON_MESSAGE_METADATA, args);
    }

    private void executeResource(String function, AsyncFunctionCallback callback, StrandMetadata metaData,
                                 Object... args) {
        runtime.invokeMethodAsync(service, function, null, metaData, callback,args);
    }
}

