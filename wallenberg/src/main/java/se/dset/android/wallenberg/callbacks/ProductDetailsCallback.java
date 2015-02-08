package se.dset.android.wallenberg.callbacks;

import java.util.List;

import se.dset.android.wallenberg.data.ProductDetails;

public interface ProductDetailsCallback {
    public void onProductDetails(List<String> productIds, List<ProductDetails> productDetails);
    public void onProductDetailsFailed(List<String> productIds, int errorCode);
}
