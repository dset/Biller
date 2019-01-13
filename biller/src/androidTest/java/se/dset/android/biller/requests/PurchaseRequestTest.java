package se.dset.android.biller.requests;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;
import android.test.AndroidTestCase;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;

import se.dset.android.biller.callbacks.PurchaseCallback;
import se.dset.android.biller.util.Constants;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PurchaseRequestTest extends AndroidTestCase {
    private Activity activity;
    private IInAppBillingService service;
    private PurchaseCallback callback;
    private PendingIntent pendingIntent;
    private Bundle successBundle;

    @Override
    protected void setUp() throws Exception {
        activity = mock(Activity.class);
        service = mock(IInAppBillingService.class);
        callback = mock(PurchaseCallback.class);

        pendingIntent = PendingIntent.getActivity(getContext(), 0, new Intent(), 0);
        successBundle = new Bundle();
        successBundle.putInt(Constants.RESPONSE_CODE, Constants.BILLING_RESPONSE_RESULT_OK);
        successBundle.putParcelable(Constants.BUY_INTENT, pendingIntent);
    }

    public void testApiUsage() throws RemoteException, JSONException, IntentSender.SendIntentException {
        when(service.getBuyIntent(anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(successBundle);

        PurchaseRequest request = new PurchaseRequest(activity, "packageName", "productId", 1001, callback);
        request.runBackground(service);
        verify(service).getBuyIntent(Constants.API_VERSION, "packageName", "productId", Constants.TYPE_INAPP, "");

        request.onSuccess();
        verify(activity).startIntentSenderForResult(eq(pendingIntent.getIntentSender()), eq(1001), any(Intent.class), eq(0), eq(0), eq(0));
    }

    public void testGetBuyIntentFailure() throws RemoteException, JSONException {
        Bundle failedBundle = new Bundle();
        failedBundle.putInt(Constants.RESPONSE_CODE, Constants.BILLING_RESPONSE_RESULT_ERROR);

        when(service.getBuyIntent(anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(failedBundle);

        PurchaseRequest request = new PurchaseRequest(activity, "packageName", "productId", 1001, callback);
        int result = request.runBackground(service);
        assertEquals(Constants.BILLING_RESPONSE_RESULT_ERROR, result);
    }

    public void testStartIntentSenderForResultFailure() throws RemoteException, JSONException, IntentSender.SendIntentException {
        when(service.getBuyIntent(anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(successBundle);
        doThrow(new IntentSender.SendIntentException()).when(activity)
                .startIntentSenderForResult(any(IntentSender.class), anyInt(), any(Intent.class), anyInt(), anyInt(), anyInt());

        PurchaseRequest request = new PurchaseRequest(activity, "packageName", "productId", 1001, callback);
        request.runBackground(service);
        request.onSuccess();

        verify(callback).onPurchaseFailure("productId", Constants.BILLING_RESPONSE_RESULT_ERROR);
    }

    public void testDoNotCallIfActivityFinishing() throws RemoteException, JSONException, IntentSender.SendIntentException {
        when(activity.isFinishing()).thenReturn(true);
        when(service.getBuyIntent(anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(successBundle);

        PurchaseRequest request = new PurchaseRequest(activity, "packageName", "productId", 1001, callback);
        request.runBackground(service);
        request.onSuccess();

        verify(activity, never()).startIntentSenderForResult(any(IntentSender.class), anyInt(), any(Intent.class), anyInt(), anyInt(), anyInt());
    }

    public void testFailedCallback() {
        PurchaseRequest request = new PurchaseRequest(activity, "packageName", "productId", 1001, callback);
        request.onFailure(Constants.BILLING_RESPONSE_RESULT_ERROR);
        verify(callback).onPurchaseFailure("productId", Constants.BILLING_RESPONSE_RESULT_ERROR);
    }
}
