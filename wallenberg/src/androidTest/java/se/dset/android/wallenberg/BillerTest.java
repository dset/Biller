package se.dset.android.wallenberg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import junit.framework.TestCase;

import java.util.Arrays;

import se.dset.android.wallenberg.callbacks.ConsumptionCallback;
import se.dset.android.wallenberg.callbacks.IsPurchasedCallback;
import se.dset.android.wallenberg.callbacks.ProductDetailsCallback;
import se.dset.android.wallenberg.callbacks.PurchaseCallback;
import se.dset.android.wallenberg.data.PurchaseData;
import se.dset.android.wallenberg.security.SignatureVerifier;
import se.dset.android.wallenberg.util.Constants;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BillerTest extends TestCase {
    private Context context;
    private RequestManager manager;
    private SignatureVerifier signatureVerifier;
    private Biller biller;
    private Gson gson;

    @Override
    protected void setUp() throws Exception {
        Context applicationContext = mock(Context.class);
        when(applicationContext.getPackageName()).thenReturn("com.example.app");
        context = mock(Context.class);
        when(context.getApplicationContext()).thenReturn(applicationContext);

        manager = mock(RequestManager.class);
        this.signatureVerifier = mock(SignatureVerifier.class);
        biller = new Biller(context, manager, signatureVerifier, Constants.DEFAULT_REQUEST_CODE);
        when(manager.runRequest(any(Request.class))).thenReturn(false);
        when(signatureVerifier.verify(anyString(), anyString())).thenReturn(true);

        gson = new Gson();
    }

    public void testFailedBuy() {
        PurchaseCallback callback = mock(PurchaseCallback.class);
        biller.buy(mock(Activity.class), "productId", callback);
        verify(callback).onPurchaseFailure("productId", Constants.BILLING_RESPONSE_RESULT_ERROR);
    }

    public void testFailedConsume() {
        ConsumptionCallback callback = mock(ConsumptionCallback.class);
        biller.consume("productId", callback);
        verify(callback).onConsumptionFailed("productId", Constants.BILLING_RESPONSE_RESULT_ERROR);
    }

    public void testFailedIsPurchased() {
        IsPurchasedCallback callback = mock(IsPurchasedCallback.class);
        biller.isPurchased("productId", callback);
        verify(callback).onIsPurchasedFailed("productId", Constants.BILLING_RESPONSE_RESULT_ERROR);
    }

    public void testFailedGetProductDetails() {
        ProductDetailsCallback callback = mock(ProductDetailsCallback.class);
        biller.getProductDetails(Arrays.asList("productId1", "productId2"), callback);
        verify(callback).onProductDetailsFailed(Arrays.asList("productId1", "productId2"), Constants.BILLING_RESPONSE_RESULT_ERROR);
    }

    public void testHandleActivityResultWrongRequestCode() {
        assertFalse(biller.handleActivityResult(Constants.DEFAULT_REQUEST_CODE + 1, Activity.RESULT_OK, new Intent(), mock(PurchaseCallback.class)));
    }

    public void testHandleActivityResultCanceled() {
        PurchaseData data = new PurchaseData("orderId", "packageName", "productId", 0, Constants.PURCHASE_STATE_CANCELED, "", "token");
        Intent intent = new Intent();
        intent.putExtra(Constants.RESPONSE_CODE, Constants.BILLING_RESPONSE_RESULT_USER_CANCELED);
        intent.putExtra(Constants.INAPP_PURCHASE_DATA, gson.toJson(data));
        intent.putExtra(Constants.INAPP_DATA_SIGNATURE, "");

        PurchaseCallback callback = mock(PurchaseCallback.class);
        assertTrue(biller.handleActivityResult(Constants.DEFAULT_REQUEST_CODE, Activity.RESULT_CANCELED, intent, callback));
        verify(callback).onPurchaseFailure("productId", Constants.BILLING_RESPONSE_RESULT_USER_CANCELED);
    }

    public void testHandleActivityResultBadSignature() {
        PurchaseData data = new PurchaseData("orderId", "packageName", "productId", 0, Constants.PURCHASE_STATE_PURCHASED, "", "token");
        Intent intent = new Intent();
        intent.putExtra(Constants.RESPONSE_CODE, Constants.BILLING_RESPONSE_RESULT_OK);
        intent.putExtra(Constants.INAPP_PURCHASE_DATA, gson.toJson(data));
        intent.putExtra(Constants.INAPP_DATA_SIGNATURE, "");

        when(signatureVerifier.verify(anyString(), anyString())).thenReturn(false);

        PurchaseCallback callback = mock(PurchaseCallback.class);
        assertTrue(biller.handleActivityResult(Constants.DEFAULT_REQUEST_CODE, Activity.RESULT_OK, intent, callback));
        verify(callback).onPurchaseFailure("productId", Constants.BILLING_RESPONSE_RESULT_ERROR);
        verify(callback, never()).onPurchase(anyString(), any(PurchaseData.class));
    }

    public void testHandleActivityResultSuccessful() {
        PurchaseData data = new PurchaseData("orderId", "packageName", "productId", 0, Constants.PURCHASE_STATE_PURCHASED, "", "token");
        Intent intent = new Intent();
        intent.putExtra(Constants.RESPONSE_CODE, Constants.BILLING_RESPONSE_RESULT_OK);
        intent.putExtra(Constants.INAPP_PURCHASE_DATA, gson.toJson(data));
        intent.putExtra(Constants.INAPP_DATA_SIGNATURE, "");

        PurchaseCallback callback = mock(PurchaseCallback.class);
        assertTrue(biller.handleActivityResult(Constants.DEFAULT_REQUEST_CODE, Activity.RESULT_OK, intent, callback));
        verify(callback).onPurchase("productId", data);
    }
}
