package se.dset.android.biller.callbacks;

public interface IsPurchasedCallback {
    public void onIsPurchased(String productId, boolean isPurchased);
    public void onIsPurchasedFailed(String productId, int errorCode);
}
