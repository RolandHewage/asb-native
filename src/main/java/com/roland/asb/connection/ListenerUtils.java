package com.roland.asb.connection;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.roland.asb.AsbConstants;
import com.roland.asb.AsbUtils;
import com.roland.asb.MessageDispatcher;
import org.ballerinalang.jvm.api.BRuntime;
import org.ballerinalang.jvm.api.values.BObject;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;

import static com.roland.asb.MessageDispatcher.getConnectionStringFromConfig;
import static com.roland.asb.MessageDispatcher.getQueueNameFromConfig;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ListenerUtils {
    private static BRuntime runtime;

    private static boolean started = false;
    private static ArrayList<BObject> services = new ArrayList<>();
    private static ArrayList<BObject> startedServices = new ArrayList<>();

    public static void init(BObject listenerBObject, IMessageReceiver iMessageReceiver) {
        listenerBObject.addNativeData(AsbConstants.CONSUMER_SERVICES, services);
        listenerBObject.addNativeData(AsbConstants.STARTED_SERVICES, startedServices);
//        listenerBObject.addNativeData(AsbConstants.CONNECTION_NATIVE_OBJECT, iMessageReceiver);
    }

    public static Object registerListener(BObject listenerBObject, BObject service) {
        runtime = BRuntime.getCurrentRuntime();
//        IMessageReceiver iMessageReceiver = (IMessageReceiver) listenerBObject.getNativeData(AsbConstants.CONNECTION_NATIVE_OBJECT);
        try {
            String connectionString = getConnectionStringFromConfig(service);
            String entityPath = getQueueNameFromConfig(service);
            QueueClient receiveClient = new QueueClient(new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);
            listenerBObject.addNativeData(AsbConstants.CONNECTION_NATIVE_OBJECT, receiveClient);
        } catch (Exception e) {
            return AsbUtils.returnErrorValue("Error occurred while initializing the Queue Client");
        }

        QueueClient receiveClient = (QueueClient) listenerBObject.getNativeData(AsbConstants.CONNECTION_NATIVE_OBJECT);

        if (service == null) {
            return null;
        }
        if (isStarted()) {
            services = (ArrayList<BObject>) listenerBObject.getNativeData(AsbConstants.CONSUMER_SERVICES);
            startReceivingMessages(service,  listenerBObject, receiveClient);
        }
        services.add(service);
        return null;
    }

    private static boolean isStarted() {
        return started;
    }

    private static void startReceivingMessages(BObject service, BObject listener, QueueClient iMessageReceiver) {
        MessageDispatcher messageDispatcher =
                new MessageDispatcher(service, runtime, iMessageReceiver);
        messageDispatcher.receiveMessages(listener);

    }

    public static Object start(BObject listenerBObject) {
        runtime = BRuntime.getCurrentRuntime();
        QueueClient iMessageReceiver = (QueueClient) listenerBObject.getNativeData(AsbConstants.CONNECTION_NATIVE_OBJECT);
        @SuppressWarnings(AsbConstants.UNCHECKED)
        ArrayList<BObject> services =
                (ArrayList<BObject>) listenerBObject.getNativeData(AsbConstants.CONSUMER_SERVICES);
        @SuppressWarnings(AsbConstants.UNCHECKED)
        ArrayList<BObject> startedServices =
                (ArrayList<BObject>) listenerBObject.getNativeData(AsbConstants.STARTED_SERVICES);
        if (services == null || services.isEmpty()) {
            return null;
        }
        for (BObject service : services) {
            if (startedServices == null || !startedServices.contains(service)) {
                MessageDispatcher messageDispatcher =
                        new MessageDispatcher(service, runtime, iMessageReceiver);
                messageDispatcher.receiveMessages(listenerBObject);
            }
        }
        started = true;
        return null;
    }

    public static Object detach(BObject listenerBObject, BObject service) {
        QueueClient iMessageReceiver = (QueueClient) listenerBObject.getNativeData(AsbConstants.CONNECTION_NATIVE_OBJECT);
        @SuppressWarnings(AsbConstants.UNCHECKED)
        ArrayList<BObject> startedServices =
                (ArrayList<BObject>) listenerBObject.getNativeData(AsbConstants.STARTED_SERVICES);
        @SuppressWarnings(AsbConstants.UNCHECKED)
        ArrayList<BObject> services =
                (ArrayList<BObject>) listenerBObject.getNativeData(AsbConstants.CONSUMER_SERVICES);
        String serviceName = service.getType().getName();
        String queueName = (String) service.getNativeData(AsbConstants.QUEUE_NAME.getValue());

        try {
            iMessageReceiver.close();
            System.out.println("[ballerina/rabbitmq] Consumer service unsubscribed from the queue " + queueName);
        } catch (Exception e) {
            return AsbUtils.returnErrorValue("Error occurred while detaching the service");
        }

        listenerBObject.addNativeData(AsbConstants.CONSUMER_SERVICES,
                removeFromList(services, service));
        listenerBObject.addNativeData(AsbConstants.STARTED_SERVICES,
                removeFromList(startedServices, service));
        return null;
    }

    public static Object stop(BObject listenerBObject) {
        IMessageReceiver iMessageReceiver = (IMessageReceiver) listenerBObject.getNativeData(AsbConstants.CONNECTION_NATIVE_OBJECT);
        if(iMessageReceiver == null) {
            return AsbUtils.returnErrorValue("IMessageReceiver is not properly initialised.");
        } else {
            try {
                iMessageReceiver.close();
                System.out.println("[ballerina/rabbitmq] Consumer service stopped");
            } catch (Exception e) {
                return AsbUtils.returnErrorValue("Error occurred while stopping the service");
            }
        }
        return null;
    }

    public static Object abortConnection(BObject listenerBObject) {
        IMessageReceiver iMessageReceiver = (IMessageReceiver) listenerBObject.getNativeData(AsbConstants.CONNECTION_NATIVE_OBJECT);
        if(iMessageReceiver == null) {
            return AsbUtils.returnErrorValue("IMessageReceiver is not properly initialised.");
        } else {
            try {
                iMessageReceiver.close();
                System.out.println("[ballerina/rabbitmq] Consumer service stopped");
            } catch (Exception e) {
                return AsbUtils.returnErrorValue("Error occurred while stopping the service");
            }
        }
        return null;
    }

    /**
     * Removes a given element from the provided array list and returns the resulting list.
     *
     * @param arrayList   The original list
     * @param objectValue Element to be removed
     * @return Resulting list after removing the element
     */
    public static ArrayList<BObject> removeFromList(ArrayList<BObject> arrayList, BObject objectValue) {
        if (arrayList != null) {
            arrayList.remove(objectValue);
        }
        return arrayList;
    }
}

