package se.dset.android.biller.callbacks;

import se.dset.android.biller.data.PurchaseData;

public interface PurchaseCallback {
    public void onPurchase(String productId, PurchaseData data);
    public void onPurchaseFailure(String productId, int errorCode);
}
