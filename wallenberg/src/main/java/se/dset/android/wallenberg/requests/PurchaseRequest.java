package se.dset.android.wallenberg.requests;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;

import java.lang.ref.WeakReference;

import se.dset.android.wallenberg.Request;
import se.dset.android.wallenberg.callbacks.PurchaseCallback;
import se.dset.android.wallenberg.util.Constants;

public class PurchaseRequest extends Request {
    private WeakReference<Activity> activity;
    private String packageName;
    private String productId;
    private int requestCode;
    private WeakReference<PurchaseCallback> callback;

    private PendingIntent intent;

    public PurchaseRequest(Activity activity, String packageName, String productId, int requestCode, PurchaseCallback callback) {
        this.activity = new WeakReference<>(activity);
        this.packageName = packageName;
        this.productId = productId;
        this.requestCode = requestCode;
        this.callback = new WeakReference<>(callback);
        this.intent = null;
    }

    @Override
    protected int runBackground(IInAppBillingService service) throws RemoteException, JSONException {
        Bundle response = service.getBuyIntent(Constants.API_VERSION, packageName, productId, Constants.TYPE_INAPP, "");
        int responseCode = response.getInt(Constants.RESPONSE_CODE);
        if(responseCode != Constants.BILLING_RESPONSE_RESULT_OK) {
            return responseCode;
        }

        intent = response.getParcelable(Constants.BUY_INTENT);
        return Constants.BILLING_RESPONSE_RESULT_OK;
    }

    @Override
    protected void onSuccess() {
        Activity a = activity.get();
        if(a != null && !a.isFinishing()) {
            try {
                a.startIntentSenderForResult(intent.getIntentSender(), requestCode, new Intent(), 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                onFailure(Constants.BILLING_RESPONSE_RESULT_ERROR);
            }
        }
    }

    @Override
    protected void onFailure(int errorCode) {
        PurchaseCallback c = callback.get();
        if(c != null) {
            c.onPurchaseFailure(productId, errorCode);
        }
    }
}
