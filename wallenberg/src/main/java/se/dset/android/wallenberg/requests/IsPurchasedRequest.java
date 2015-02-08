package se.dset.android.wallenberg.requests;

import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.List;

import se.dset.android.wallenberg.Request;
import se.dset.android.wallenberg.callbacks.IsPurchasedCallback;
import se.dset.android.wallenberg.data.PurchaseData;
import se.dset.android.wallenberg.util.Constants;
import se.dset.android.wallenberg.util.PurchaseFilterer;

public class IsPurchasedRequest extends Request {
    private PurchaseFilterer filterer;
    private String packageName;
    private String productId;
    private WeakReference<IsPurchasedCallback> callback;

    private boolean isPurchased;

    public IsPurchasedRequest(PurchaseFilterer filterer, String packageName, String productId, IsPurchasedCallback callback) {
        this.filterer = filterer;
        this.packageName = packageName;
        this.productId = productId;
        this.callback = new WeakReference<>(callback);
        this.isPurchased = false;
    }

    @Override
    protected int runBackground(IInAppBillingService service) throws RemoteException, JSONException {
        List<PurchaseData> dataList = filterer.getAllProductIdPurchases(service, packageName, productId);

        if(dataList == null) {
            return Constants.BILLING_RESPONSE_RESULT_ERROR;
        }

        if(dataList.isEmpty()) {
            isPurchased = false;
            return Constants.BILLING_RESPONSE_RESULT_OK;
        }

        PurchaseData data = dataList.get(dataList.size() - 1);
        isPurchased = data.getPurchaseState() == Constants.PURCHASE_STATE_PURCHASED;
        return Constants.BILLING_RESPONSE_RESULT_OK;
    }

    @Override
    protected void onSuccess() {
        IsPurchasedCallback c = callback.get();
        if(c != null) {
            c.onIsPurchased(productId, isPurchased);
        }
    }

    @Override
    protected void onFailure(int errorCode) {
        IsPurchasedCallback c = callback.get();
        if(c != null) {
            c.onIsPurchasedFailed(productId, errorCode);
        }
    }
}
