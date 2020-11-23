package com.roland.asb;

import org.ballerinalang.jvm.types.BPackage;

public class AsbConstants {
    // RabbitMQ package name constant fields
    public static final String ORG_NAME = "ballerinax";
    static final String ASB = "asb";
    static final String ASB_VERSION = "1.2.8";
    public static final BPackage PACKAGE_ID_ASB =
            new BPackage(ORG_NAME, "asb", ASB_VERSION);

    // Error constant fields
    static final String ASB_ERROR = "AsbError";
}
