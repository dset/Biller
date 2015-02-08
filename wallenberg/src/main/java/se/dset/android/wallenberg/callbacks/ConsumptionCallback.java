package se.dset.android.wallenberg.callbacks;

public interface ConsumptionCallback {
    public void onConsumptionSuccessful(String productId);
    public void onConsumptionFailed(String productId, int errorCode);
}
