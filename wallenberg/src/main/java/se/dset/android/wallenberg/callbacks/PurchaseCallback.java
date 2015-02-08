package se.dset.android.wallenberg.callbacks;

import se.dset.android.wallenberg.data.PurchaseData;

public interface PurchaseCallback {
    public void onPurchase(String productId, PurchaseData data);
    public void onPurchaseFailure(String productId, int errorCode);
}
