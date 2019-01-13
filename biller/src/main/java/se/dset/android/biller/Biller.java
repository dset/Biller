package se.dset.android.biller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;

import java.util.List;

import se.dset.android.biller.callbacks.ConsumptionCallback;
import se.dset.android.biller.callbacks.IsPurchasedCallback;
import se.dset.android.biller.callbacks.ProductDetailsCallback;
import se.dset.android.biller.callbacks.PurchaseCallback;
import se.dset.android.biller.data.PurchaseData;
import se.dset.android.biller.requests.ConsumptionRequest;
import se.dset.android.biller.requests.IsPurchasedRequest;
import se.dset.android.biller.requests.ProductDetailsRequest;
import se.dset.android.biller.requests.PurchaseRequest;
import se.dset.android.biller.security.BillingSignatureVerifier;
import se.dset.android.biller.security.SignatureVerifier;
import se.dset.android.biller.util.Constants;
import se.dset.android.biller.util.PurchaseFilterer;

public class Biller {
    private Context context;
    private RequestManager requestManager;
    private SignatureVerifier signatureVerifier;
    private int requestCode;

    public Biller(Context context, String base64PublicKey) {
        this(context, base64PublicKey, Constants.DEFAULT_REQUEST_CODE);
    }

    public Biller(Context context, String base64PublicKey, int requestCode) {
        this(context, new RequestManager(context.getApplicationContext()), new BillingSignatureVerifier(base64PublicKey), requestCode);
    }

    /* package-private */ Biller(Context context, RequestManager requestManager, SignatureVerifier signatureVerifier, int requestCode) {
        this.context = context.getApplicationContext();
        this.requestManager = requestManager;
        this.signatureVerifier = signatureVerifier;
        this.requestCode = requestCode;
    }

    public void buy(Activity activity, String productId, PurchaseCallback callback) {
        PurchaseRequest request = new PurchaseRequest(activity, context.getPackageName(), productId, requestCode, callback);
        if(!requestManager.runRequest(request)) {
            callback.onPurchaseFailure(productId, Constants.BILLING_RESPONSE_RESULT_ERROR);
        }
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data, PurchaseCallback callback) {
        if(requestCode != this.requestCode) {
            return false;
        }

        int responseCode = data.getIntExtra(Constants.RESPONSE_CODE, Constants.BILLING_RESPONSE_RESULT_ERROR);
        String jsonPurchaseData = data.getStringExtra(Constants.INAPP_PURCHASE_DATA);
        String signature = data.getStringExtra(Constants.INAPP_DATA_SIGNATURE);

        PurchaseData purchaseData;
        try {
            purchaseData = new PurchaseData(jsonPurchaseData);
        } catch (JSONException e) {
            callback.onPurchaseFailure("", Constants.BILLING_RESPONSE_RESULT_ERROR);
            return true;
        }

        if(!signatureVerifier.verify(jsonPurchaseData, signature)) {
            callback.onPurchaseFailure(purchaseData.getProductId(), Constants.BILLING_RESPONSE_RESULT_ERROR);
            return true;
        }

        if(responseCode == Constants.BILLING_RESPONSE_RESULT_OK) {
            callback.onPurchase(purchaseData.getProductId(), purchaseData);
        } else {
            callback.onPurchaseFailure(purchaseData.getProductId(), responseCode);
        }

        return true;
    }

    public void consume(String productId, ConsumptionCallback callback) {
        PurchaseFilterer filterer = new PurchaseFilterer(signatureVerifier);
        ConsumptionRequest request = new ConsumptionRequest(filterer, context.getPackageName(), productId, callback);
        if(!requestManager.runRequest(request)) {
            callback.onConsumptionFailed(productId, Constants.BILLING_RESPONSE_RESULT_ERROR);
        }
    }

    public void isPurchased(String productId, IsPurchasedCallback callback) {
        PurchaseFilterer filterer = new PurchaseFilterer(signatureVerifier);
        IsPurchasedRequest request = new IsPurchasedRequest(filterer, context.getPackageName(), productId, callback);
        if(!requestManager.runRequest(request)) {
            callback.onIsPurchasedFailed(productId, Constants.BILLING_RESPONSE_RESULT_ERROR);
        }
    }

    public void getProductDetails(List<String> productIds, ProductDetailsCallback callback) {
        ProductDetailsRequest request = new ProductDetailsRequest(context.getPackageName(), productIds, callback);
        if(!requestManager.runRequest(request)) {
            callback.onProductDetailsFailed(productIds, Constants.BILLING_RESPONSE_RESULT_ERROR);
        }
    }
}
