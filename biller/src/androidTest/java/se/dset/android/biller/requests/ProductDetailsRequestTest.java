package se.dset.android.biller.requests;

import android.os.Bundle;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;

import junit.framework.TestCase;

import org.json.JSONException;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.dset.android.biller.callbacks.ProductDetailsCallback;
import se.dset.android.biller.data.ProductDetails;
import se.dset.android.biller.util.Constants;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProductDetailsRequestTest extends TestCase {
    private IInAppBillingService service;
    private ProductDetailsCallback callback;

    @Override
    protected void setUp() throws Exception {
        service = mock(IInAppBillingService.class);
        callback = mock(ProductDetailsCallback.class);
    }

    public void testApiUsage() throws RemoteException, JSONException {
        Bundle failedBundle = new Bundle();
        failedBundle.putInt(Constants.RESPONSE_CODE, Constants.BILLING_RESPONSE_RESULT_ERROR);
        when(service.getSkuDetails(anyInt(), anyString(), anyString(), any(Bundle.class))).thenReturn(failedBundle);

        ArrayList<String> idList = new ArrayList<>(Arrays.asList("productId1"));
        ProductDetailsRequest request = new ProductDetailsRequest("packageName", idList, callback);
        request.runBackground(service);

        ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(service).getSkuDetails(eq(Constants.API_VERSION), eq("packageName"), eq(Constants.TYPE_INAPP), captor.capture());
        assertEquals(idList, captor.getValue().getStringArrayList(Constants.ITEM_ID_LIST));
    }

    public void testGetSkuDetailsFailure() throws RemoteException, JSONException {
        Bundle failedBundle = new Bundle();
        failedBundle.putInt(Constants.RESPONSE_CODE, Constants.BILLING_RESPONSE_RESULT_ERROR);
        when(service.getSkuDetails(anyInt(), anyString(), anyString(), any(Bundle.class))).thenReturn(failedBundle);

        ProductDetailsRequest request = new ProductDetailsRequest("packageName", Arrays.asList("productId1"), callback);
        int result = request.runBackground(service);
        assertEquals(Constants.BILLING_RESPONSE_RESULT_ERROR, result);
    }

    public void testProductDetailsOrdering() throws RemoteException, JSONException {
        Gson gson = new Gson();
        ProductDetails details1 = new ProductDetails("productId1", Constants.TYPE_INAPP, "$7.99", 7990000, "USD", "", "");
        ProductDetails details2 = new ProductDetails("productId2", Constants.TYPE_INAPP, "$7.99", 7990000, "USD", "", "");
        ProductDetails details3 = new ProductDetails("productId3", Constants.TYPE_INAPP, "$7.99", 7990000, "USD", "", "");
        List<String> detailsJsonList = Arrays.asList(gson.toJson(details1), gson.toJson(details2), gson.toJson(details3));

        Bundle successBundle = new Bundle();
        successBundle.putInt(Constants.RESPONSE_CODE, Constants.BILLING_RESPONSE_RESULT_OK);
        successBundle.putStringArrayList(Constants.DETAILS_LIST, new ArrayList<>(detailsJsonList));
        when(service.getSkuDetails(anyInt(), anyString(), anyString(), any(Bundle.class))).thenReturn(successBundle);

        List<String> idList = Arrays.asList("productId3", "productId2", "productId1");
        ProductDetailsRequest request = new ProductDetailsRequest("packageName", idList, callback);
        int result = request.runBackground(service);
        assertEquals(Constants.BILLING_RESPONSE_RESULT_OK, result);

        List<ProductDetails> detailsList = Arrays.asList(details3, details2, details1);
        request.onSuccess();
        verify(callback).onProductDetails(idList, detailsList);
    }

    public void testFailedCallback() {
        ProductDetailsRequest request = new ProductDetailsRequest("packageName", Arrays.asList("productId"), callback);
        request.onFailure(Constants.BILLING_RESPONSE_RESULT_ERROR);
        verify(callback).onProductDetailsFailed(Arrays.asList("productId"), Constants.BILLING_RESPONSE_RESULT_ERROR);
    }
}
