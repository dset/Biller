package se.dset.android.wallenberg.util;

import android.os.Bundle;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;

import junit.framework.TestCase;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.dset.android.wallenberg.data.PurchaseData;
import se.dset.android.wallenberg.security.SignatureVerifier;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PurchaseFiltererTest extends TestCase {
    private SignatureVerifier signatureVerifier;
    private PurchaseFilterer filterer;
    private IInAppBillingService service;

    private Bundle failedBundle;
    private Bundle emptyBundle;
    private Bundle successBundle1;
    private Bundle successBundle2;

    PurchaseData data1;
    PurchaseData data2;
    PurchaseData data3;

    @Override
    protected void setUp() throws Exception {
        signatureVerifier = mock(SignatureVerifier.class);
        when(signatureVerifier.verify(anyString(), anyString())).thenReturn(true);
        filterer = new PurchaseFilterer(signatureVerifier);
        service = mock(IInAppBillingService.class);

        failedBundle = new Bundle();
        failedBundle.putInt(Constants.RESPONSE_CODE, Constants.BILLING_RESPONSE_RESULT_ERROR);

        emptyBundle = new Bundle();
        emptyBundle.putInt(Constants.RESPONSE_CODE, Constants.BILLING_RESPONSE_RESULT_OK);
        emptyBundle.putStringArrayList(Constants.INAPP_PURCHASE_ITEM_LIST, new ArrayList<String>());
        emptyBundle.putStringArrayList(Constants.INAPP_PURCHASE_DATA_LIST, new ArrayList<String>());
        emptyBundle.putStringArrayList(Constants.INAPP_DATA_SIGNATURE_LIST, new ArrayList<String>());

        data1 = new PurchaseData("orderId1", "packageName", "productId1", 2000, 0, "", "");
        data2 = new PurchaseData("orderId2", "packageName", "productId2", 1000, 0, "", "");
        data3 = new PurchaseData("orderId3", "packageName", "productId1", 0, 1, "", "");

        Gson gson = new Gson();

        successBundle1 = new Bundle();
        successBundle1.putInt(Constants.RESPONSE_CODE, Constants.BILLING_RESPONSE_RESULT_OK);

        ArrayList<String> itemList1 = new ArrayList<>(Arrays.asList(data1.getProductId(), data2.getProductId()));
        successBundle1.putStringArrayList(Constants.INAPP_PURCHASE_ITEM_LIST, itemList1);

        ArrayList<String> dataList1 = new ArrayList<>(Arrays.asList(gson.toJson(data1), gson.toJson(data2)));
        successBundle1.putStringArrayList(Constants.INAPP_PURCHASE_DATA_LIST, dataList1);

        ArrayList<String> signatureList1 = new ArrayList<>(Arrays.asList("", ""));
        successBundle1.putStringArrayList(Constants.INAPP_DATA_SIGNATURE_LIST, signatureList1);

        successBundle2 = new Bundle();
        successBundle2.putInt(Constants.RESPONSE_CODE, Constants.BILLING_RESPONSE_RESULT_OK);

        ArrayList<String> itemList2 = new ArrayList<>(Arrays.asList(data3.getProductId()));
        successBundle2.putStringArrayList(Constants.INAPP_PURCHASE_ITEM_LIST, itemList2);

        ArrayList<String> dataList2 = new ArrayList<>(Arrays.asList(gson.toJson(data3)));
        successBundle2.putStringArrayList(Constants.INAPP_PURCHASE_DATA_LIST, dataList2);

        ArrayList<String> signatureList2 = new ArrayList<>(Arrays.asList(""));
        successBundle2.putStringArrayList(Constants.INAPP_DATA_SIGNATURE_LIST, signatureList2);
    }

    public void testApiUsage() throws RemoteException, JSONException {
        when(service.getPurchases(anyInt(), anyString(), anyString(), anyString())).thenReturn(failedBundle);
        filterer.getAllPurchases(service, "packageName");
        verify(service).getPurchases(Constants.API_VERSION, "packageName", Constants.TYPE_INAPP, null);
    }

    public void testGetAllPurchasesApiFail() throws RemoteException, JSONException {
        when(service.getPurchases(anyInt(), anyString(), anyString(), anyString())).thenReturn(failedBundle);
        assertNull(filterer.getAllPurchases(service, "packageName"));
    }

    public void testGetAllPurchasesEmptyBundle() throws RemoteException, JSONException {
        when(service.getPurchases(anyInt(), anyString(), anyString(), anyString())).thenReturn(emptyBundle);
        assertTrue(filterer.getAllPurchases(service, "packageName").isEmpty());
    }

    public void testGetAllPurchasesSortOrder() throws RemoteException, JSONException {
        when(service.getPurchases(anyInt(), anyString(), anyString(), anyString())).thenReturn(successBundle1);
        List<PurchaseData> res = filterer.getAllPurchases(service, "packageName");
        assertEquals(2, res.size());
        assertEquals(data2, res.get(0));
        assertEquals(data1, res.get(1));
    }

    public void testGetAllPurchasesContinuedBundle() throws RemoteException, JSONException {
        successBundle1.putString(Constants.INAPP_CONTINUATION_TOKEN, "cont");
        when(service.getPurchases(anyInt(), anyString(), anyString(), anyString())).thenReturn(successBundle1, successBundle2);
        List<PurchaseData> res = filterer.getAllPurchases(service, "packageName");
        assertEquals(3, res.size());
        assertEquals(data3, res.get(0));
        assertEquals(data2, res.get(1));
        assertEquals(data1, res.get(2));
    }

    public void testGetAllPurchasesContinuedFailedBundle() throws RemoteException, JSONException {
        successBundle1.putString(Constants.INAPP_CONTINUATION_TOKEN, "cont");
        when(service.getPurchases(anyInt(), anyString(), anyString(), anyString())).thenReturn(successBundle1, failedBundle);
        assertNull(filterer.getAllPurchases(service, "packageName"));
    }

    public void testGetAllProductIdPurchasesFiltering() throws RemoteException, JSONException {
        successBundle1.putString(Constants.INAPP_CONTINUATION_TOKEN, "cont");
        when(service.getPurchases(anyInt(), anyString(), anyString(), anyString())).thenReturn(successBundle1, successBundle2);
        List<PurchaseData> res = filterer.getAllProductIdPurchases(service, "packageName", "productId1");
        assertEquals(2, res.size());
        assertEquals(data3, res.get(0));
        assertEquals(data1, res.get(1));
    }

    public void testGetAllPurchasesFailedSignature() throws RemoteException, JSONException {
        when(signatureVerifier.verify(anyString(), anyString())).thenReturn(true, false);
        when(service.getPurchases(anyInt(), anyString(), anyString(), anyString())).thenReturn(successBundle1);
        assertNull(filterer.getAllPurchases(service, "packageName"));
    }
}
