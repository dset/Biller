package se.dset.android.wallenberg.util;

import android.content.Intent;

public class Constants {
    public static final Intent IAB_SERVICE_INTENT;
    public static final String IAB_SERVICE_INTENT_ACTION = "com.android.vending.billing.InAppBillingService.BIND";
    public static final String IAB_SERVICE_INTENT_PACKAGE = "com.android.vending";

    public static final int API_VERSION = 3;

    public static final String TYPE_INAPP = "inapp";
    public static final String TYPE_SUBS = "subs";

    public static final int BILLING_RESPONSE_RESULT_OK = 0;
    public static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    public static final int BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2;
    public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    public static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
    public static final int BILLING_RESPONSE_RESULT_ERROR = 6;
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    public static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;

    public static final String RESPONSE_CODE = "RESPONSE_CODE";

    public static final String ITEM_ID_LIST = "ITEM_ID_LIST";
    public static final String DETAILS_LIST = "DETAILS_LIST";

    public static final String BUY_INTENT = "BUY_INTENT";

    public static final String INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    public static final String INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE";

    public static final String INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    public static final String INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    public static final String INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    public static final String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

    public static final int PURCHASE_STATE_PURCHASED = 0;
    public static final int PURCHASE_STATE_CANCELED = 1;
    public static final int PURCHASE_STATE_REFUNDED = 2;

    public static final int DEFAULT_REQUEST_CODE = 10000;

    static {
        IAB_SERVICE_INTENT = new Intent(IAB_SERVICE_INTENT_ACTION);
        IAB_SERVICE_INTENT.setPackage(IAB_SERVICE_INTENT_PACKAGE);
    }
}
