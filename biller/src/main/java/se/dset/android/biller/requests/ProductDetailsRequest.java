package se.dset.android.biller.requests;

import android.os.Bundle;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import se.dset.android.biller.Request;
import se.dset.android.biller.callbacks.ProductDetailsCallback;
import se.dset.android.biller.data.ProductDetails;
import se.dset.android.biller.util.Constants;

public class ProductDetailsRequest extends Request {
    private String packageName;
    private List<String> productIds;
    private WeakReference<ProductDetailsCallback> callback;

    private List<ProductDetails> sortedProductDetails;

    public ProductDetailsRequest(String packageName, List<String> productIds, ProductDetailsCallback callback) {
        this.packageName = packageName;
        this.productIds = productIds;
        this.callback = new WeakReference<>(callback);
    }

    @Override
    protected int runBackground(IInAppBillingService service) throws RemoteException, JSONException {
        Bundle queryBundle = new Bundle();
        queryBundle.putStringArrayList(Constants.ITEM_ID_LIST, new ArrayList<String>(productIds));

        Bundle detailsBundle = service.getSkuDetails(Constants.API_VERSION, packageName, Constants.TYPE_INAPP, queryBundle);
        int responseCode = detailsBundle.getInt(Constants.RESPONSE_CODE);
        if(responseCode != Constants.BILLING_RESPONSE_RESULT_OK) {
            return responseCode;
        }

        ArrayList<String> jsonProductDetails = detailsBundle.getStringArrayList(Constants.DETAILS_LIST);
        List<ProductDetails> productDetails = new ArrayList<>();
        for(String json : jsonProductDetails) {
            productDetails.add(new ProductDetails(json));
        }

        sortedProductDetails = new ArrayList<>();
        for(String productId : productIds) {
            for(ProductDetails details : productDetails) {
                if(productId.equals(details.getProductId())) {
                    sortedProductDetails.add(details);
                    break;
                }
            }
        }

        return Constants.BILLING_RESPONSE_RESULT_OK;
    }

    @Override
    protected void onSuccess() {
        ProductDetailsCallback c = callback.get();
        if(c != null) {
            c.onProductDetails(productIds, sortedProductDetails);
        }
    }

    @Override
    protected void onFailure(int errorCode) {
        ProductDetailsCallback c = callback.get();
        if(c != null) {
            c.onProductDetailsFailed(productIds, errorCode);
        }
    }
}
