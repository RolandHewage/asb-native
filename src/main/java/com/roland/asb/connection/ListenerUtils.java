package com.roland.asb.connection;

import com.roland.asb.AsbConstants;
import com.roland.asb.MessageDispatcher;
import org.ballerinalang.jvm.api.BRuntime;
import org.ballerinalang.jvm.api.values.BObject;

import java.util.ArrayList;

public class ListenerUtils {
    private static BRuntime runtime;

    private static boolean started = false;
    private static ArrayList<BObject> services = new ArrayList<>();
    private static ArrayList<BObject> startedServices = new ArrayList<>();

    public static void init(BObject listenerBObject) {
        listenerBObject.addNativeData(AsbConstants.CONSUMER_SERVICES, services);
        listenerBObject.addNativeData(AsbConstants.STARTED_SERVICES, startedServices);
    }

    public static Object registerListener(BObject listenerBObject, BObject service) {
        runtime = BRuntime.getCurrentRuntime();
        if (service == null) {
            return null;
        }
        if (isStarted()) {
            services = (ArrayList<BObject>) listenerBObject.getNativeData(AsbConstants.CONSUMER_SERVICES);
            startReceivingMessages(service,  listenerBObject);
        }
        services.add(service);
        return null;
    }

    private static boolean isStarted() {
        return started;
    }

    private static void startReceivingMessages(BObject service, BObject listener) {
        MessageDispatcher messageDispatcher =
                new MessageDispatcher(service, runtime);
        messageDispatcher.receiveMessages(listener);

    }

    public static Object start(BObject listenerBObject) {
        runtime = BRuntime.getCurrentRuntime();
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
                        new MessageDispatcher(service, runtime);
                messageDispatcher.receiveMessages(listenerBObject);
            }
        }
        started = true;
        return null;
    }

    public static Object detach(BObject listenerBObject, BObject service) {
        @SuppressWarnings(AsbConstants.UNCHECKED)
        ArrayList<BObject> startedServices =
                (ArrayList<BObject>) listenerBObject.getNativeData(AsbConstants.STARTED_SERVICES);
        @SuppressWarnings(AsbConstants.UNCHECKED)
        ArrayList<BObject> services =
                (ArrayList<BObject>) listenerBObject.getNativeData(AsbConstants.CONSUMER_SERVICES);
        String serviceName = service.getType().getName();
        String queueName = (String) service.getNativeData(AsbConstants.QUEUE_NAME.getValue());

        System.out.println("[ballerina/rabbitmq] Consumer service unsubscribed from the queue " + queueName);

        listenerBObject.addNativeData(AsbConstants.CONSUMER_SERVICES,
                removeFromList(services, service));
        listenerBObject.addNativeData(AsbConstants.STARTED_SERVICES,
                removeFromList(startedServices, service));
        return null;
    }

    public static Object stop(BObject listenerBObject) {
        return null;
    }

    public static Object abortConnection(BObject listenerBObject) {
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

