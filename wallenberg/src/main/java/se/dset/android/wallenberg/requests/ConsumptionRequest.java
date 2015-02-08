package se.dset.android.wallenberg.requests;

import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.List;

import se.dset.android.wallenberg.Request;
import se.dset.android.wallenberg.callbacks.ConsumptionCallback;
import se.dset.android.wallenberg.data.PurchaseData;
import se.dset.android.wallenberg.util.Constants;
import se.dset.android.wallenberg.util.PurchaseFilterer;

public class ConsumptionRequest extends Request {
    private PurchaseFilterer filterer;
    private String packageName;
    private String productId;
    private WeakReference<ConsumptionCallback> callback;

    public ConsumptionRequest(PurchaseFilterer filterer, String packageName, String productId, ConsumptionCallback callback) {
        this.filterer = filterer;
        this.packageName = packageName;
        this.productId = productId;
        this.callback = new WeakReference<>(callback);
    }

    @Override
    protected int runBackground(IInAppBillingService service) throws RemoteException, JSONException {
        List<PurchaseData> dataList = filterer.getAllProductIdPurchases(service, packageName, productId);

        if(dataList == null) {
            return Constants.BILLING_RESPONSE_RESULT_ERROR;
        }

        if(dataList.isEmpty()) {
            return Constants.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED;
        }

        return service.consumePurchase(Constants.API_VERSION, packageName, dataList.get(dataList.size() - 1).getPurchaseToken());
    }

    @Override
    protected void onSuccess() {
        ConsumptionCallback c = callback.get();
        if(c != null) {
            c.onConsumptionSuccessful(productId);
        }
    }

    @Override
    protected void onFailure(int errorCode) {
        ConsumptionCallback c = callback.get();
        if(c != null) {
            c.onConsumptionFailed(productId, errorCode);
        }
    }
}
