package se.dset.android.biller.requests;

import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import junit.framework.TestCase;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.dset.android.biller.callbacks.ConsumptionCallback;
import se.dset.android.biller.data.PurchaseData;
import se.dset.android.biller.util.Constants;
import se.dset.android.biller.util.PurchaseFilterer;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConsumptionRequestTest extends TestCase {
    private PurchaseFilterer filterer;
    private IInAppBillingService service;
    private ConsumptionCallback callback;
    private List<PurchaseData> dataList;

    @Override
    protected void setUp() throws Exception {
        filterer = mock(PurchaseFilterer.class);
        service = mock(IInAppBillingService.class);
        callback = mock(ConsumptionCallback.class);

        PurchaseData data1 = new PurchaseData("orderId1", "packageName", "productId1", 0, 0, "", "token1");
        PurchaseData data2 = new PurchaseData("orderId2", "packageName", "productId1", 1000, 0, "", "token2");
        PurchaseData data3 = new PurchaseData("orderId3", "packageName", "productId1", 2000, 0, "", "token3");
        dataList = Arrays.asList(data1, data2, data3);
    }

    public void testApiUsage() throws RemoteException, JSONException {
        when(filterer.getAllProductIdPurchases(service, "packageName", "productId1")).thenReturn(dataList);
        when(service.consumePurchase(anyInt(), anyString(), anyString())).thenReturn(Constants.BILLING_RESPONSE_RESULT_OK);

        ConsumptionRequest request = new ConsumptionRequest(filterer, "packageName", "productId1", callback);
        request.runBackground(service);

        verify(filterer).getAllProductIdPurchases(service, "packageName", "productId1");
        verify(service).consumePurchase(Constants.API_VERSION, "packageName", "token3");
    }

    public void testNullFiltererResult() throws RemoteException, JSONException {
        when(filterer.getAllProductIdPurchases(service, "packageName", "productId1")).thenReturn(null);

        ConsumptionRequest request = new ConsumptionRequest(filterer, "packageName", "productId1", callback);
        int result = request.runBackground(service);
        assertEquals(Constants.BILLING_RESPONSE_RESULT_ERROR, result);
    }

    public void testEmptyFiltererResult() throws RemoteException, JSONException {
        when(filterer.getAllProductIdPurchases(service, "packageName", "productId1")).thenReturn(new ArrayList<PurchaseData>());

        ConsumptionRequest request = new ConsumptionRequest(filterer, "packageName", "productId", callback);
        int result = request.runBackground(service);
        assertEquals(Constants.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED, result);
    }

    public void testFailedConsumePurchase() throws RemoteException, JSONException {
        when(filterer.getAllProductIdPurchases(service, "packageName", "productId1")).thenReturn(dataList);
        when(service.consumePurchase(anyInt(), anyString(), anyString())).thenReturn(Constants.BILLING_RESPONSE_RESULT_ERROR);

        ConsumptionRequest request = new ConsumptionRequest(filterer, "packageName", "productId1", callback);
        int result = request.runBackground(service);
        assertEquals(Constants.BILLING_RESPONSE_RESULT_ERROR, result);
    }

    public void testConsumeOwnedProduct() throws RemoteException, JSONException {
        when(filterer.getAllProductIdPurchases(service, "packageName", "productId1")).thenReturn(dataList);
        when(service.consumePurchase(anyInt(), anyString(), anyString())).thenReturn(Constants.BILLING_RESPONSE_RESULT_OK);

        ConsumptionRequest request = new ConsumptionRequest(filterer, "packageName", "productId1", callback);
        int result = request.runBackground(service);
        assertEquals(Constants.BILLING_RESPONSE_RESULT_OK, result);
    }

    public void testFailedCallback() {
        ConsumptionRequest request = new ConsumptionRequest(filterer, "packageName", "productId1", callback);
        request.onFailure(Constants.BILLING_RESPONSE_RESULT_ERROR);
        verify(callback).onConsumptionFailed("productId1", Constants.BILLING_RESPONSE_RESULT_ERROR);
    }

    public void testSuccessCallback() {
        ConsumptionRequest request = new ConsumptionRequest(filterer, "packageName", "productId1", callback);
        request.onSuccess();
        verify(callback).onConsumptionSuccessful("productId1");
    }
}
