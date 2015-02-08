package se.dset.android.wallenberg.requests;

import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import junit.framework.TestCase;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.dset.android.wallenberg.callbacks.IsPurchasedCallback;
import se.dset.android.wallenberg.data.PurchaseData;
import se.dset.android.wallenberg.util.Constants;
import se.dset.android.wallenberg.util.PurchaseFilterer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IsPurchasedRequestTest extends TestCase {
    private PurchaseFilterer filterer;
    private IInAppBillingService service;
    private IsPurchasedCallback callback;

    @Override
    protected void setUp() throws Exception {
        filterer = mock(PurchaseFilterer.class);
        service = mock(IInAppBillingService.class);
        callback = mock(IsPurchasedCallback.class);
    }

    public void testApiUsage() throws RemoteException, JSONException {
        when(filterer.getAllProductIdPurchases(service, "packageName", "productId")).thenReturn(new ArrayList<PurchaseData>());

        IsPurchasedRequest request = new IsPurchasedRequest(filterer, "packageName", "productId1", callback);
        request.runBackground(service);

        verify(filterer).getAllProductIdPurchases(service, "packageName", "productId1");
    }

    public void testNullFiltererResult() throws RemoteException, JSONException {
        when(filterer.getAllProductIdPurchases(service, "packageName", "productId1")).thenReturn(null);

        IsPurchasedRequest request = new IsPurchasedRequest(filterer, "packageName", "productId1", callback);
        int result = request.runBackground(service);
        assertEquals(Constants.BILLING_RESPONSE_RESULT_ERROR, result);
    }

    public void testEmptyFiltererResult() throws RemoteException, JSONException {
        when(filterer.getAllProductIdPurchases(service, "packageName", "productId1")).thenReturn(new ArrayList<PurchaseData>());

        IsPurchasedRequest request = new IsPurchasedRequest(filterer, "packageName", "productId1", callback);
        int result = request.runBackground(service);
        assertEquals(Constants.BILLING_RESPONSE_RESULT_OK, result);

        request.onSuccess();
        verify(callback).onIsPurchased("productId1", false);
    }

    public void testNotOwnedPurchaseState() throws RemoteException, JSONException {
        PurchaseData data1 = new PurchaseData("orderId1", "packageName", "productId1", 0, 1, "", "token1");
        PurchaseData data2 = new PurchaseData("orderId2", "packageName", "productId1", 1000, 1, "", "token2");
        List<PurchaseData> dataList = Arrays.asList(data1, data2);
        when(filterer.getAllProductIdPurchases(service, "packageName", "productId1")).thenReturn(dataList);

        IsPurchasedRequest request = new IsPurchasedRequest(filterer, "packageName", "productId1", callback);
        int result = request.runBackground(service);
        assertEquals(Constants.BILLING_RESPONSE_RESULT_OK, result);

        request.onSuccess();
        verify(callback).onIsPurchased("productId1", false);
    }

    public void testOwnedPurchaseState() throws RemoteException, JSONException {
        PurchaseData data1 = new PurchaseData("orderId1", "packageName", "productId1", 0, 1, "", "token1");
        PurchaseData data2 = new PurchaseData("orderId2", "packageName", "productId1", 1000, 0, "", "token2");
        List<PurchaseData> dataList = Arrays.asList(data1, data2);
        when(filterer.getAllProductIdPurchases(service, "packageName", "productId1")).thenReturn(dataList);

        IsPurchasedRequest request = new IsPurchasedRequest(filterer, "packageName", "productId1", callback);
        int result = request.runBackground(service);
        assertEquals(Constants.BILLING_RESPONSE_RESULT_OK, result);

        request.onSuccess();
        verify(callback).onIsPurchased("productId1", true);
    }

    public void testFailedCallback() {
        IsPurchasedRequest request = new IsPurchasedRequest(filterer, "packageName", "productId1", callback);
        request.onFailure(Constants.BILLING_RESPONSE_RESULT_ERROR);
        verify(callback).onIsPurchasedFailed("productId1", Constants.BILLING_RESPONSE_RESULT_ERROR);
    }
}
