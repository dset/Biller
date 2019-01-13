package se.dset.android.biller.callbacks;

import java.util.List;

import se.dset.android.biller.data.ProductDetails;

public interface ProductDetailsCallback {
    public void onProductDetails(List<String> productIds, List<ProductDetails> productDetails);
    public void onProductDetailsFailed(List<String> productIds, int errorCode);
}
