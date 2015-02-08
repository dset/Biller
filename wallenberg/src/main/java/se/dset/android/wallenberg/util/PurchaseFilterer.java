package se.dset.android.wallenberg.util;

import android.os.Bundle;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.dset.android.wallenberg.data.PurchaseData;
import se.dset.android.wallenberg.security.SignatureVerifier;

public class PurchaseFilterer {
    private SignatureVerifier signatureVerifier;

    /**
     * Constructs a new PurchaseFilterer.
     * @param signatureVerifier The signature verifier used to verify the signatures of the
     *                          retrieved data.
     */
    public PurchaseFilterer(SignatureVerifier signatureVerifier) {
        this.signatureVerifier = signatureVerifier;
    }

    /**
     * Returns all of the purchases made by the user as returned by the
     * {@link com.android.vending.billing.IInAppBillingService#getPurchases(int, String, String, String)}
     * method. The purchases are returned sorted on purchase time, oldest first.
     * @param service Service object to query.
     * @param packageName The package name of the application.
     * @return All of the purchases made by the user. Sorted on purchase time, oldest first.
     * Returns null on error or if some data has an incorrect signature.
     */
    public List<PurchaseData> getAllPurchases(IInAppBillingService service, String packageName) throws RemoteException, JSONException {
        List<String> dataList = new ArrayList<>();
        List<String> signatureList = new ArrayList<>();

        String continuationToken = null;
        do {
            Bundle response = service.getPurchases(Constants.API_VERSION, packageName, Constants.TYPE_INAPP, continuationToken);
            int responseCode = response.getInt(Constants.RESPONSE_CODE);
            if(responseCode != Constants.BILLING_RESPONSE_RESULT_OK) {
                return null;
            }

            dataList.addAll(response.getStringArrayList(Constants.INAPP_PURCHASE_DATA_LIST));
            signatureList.addAll(response.getStringArrayList(Constants.INAPP_DATA_SIGNATURE_LIST));
            continuationToken = response.getString(Constants.INAPP_CONTINUATION_TOKEN);
        } while (continuationToken != null);

        for(int i = 0; i < dataList.size(); i++) {
            if(!signatureVerifier.verify(dataList.get(i), signatureList.get(i))) {
                return null;
            }
        }

        List<PurchaseData> purchaseDataList = new ArrayList<>();
        for(String data : dataList) {
            purchaseDataList.add(new PurchaseData(data));
        }

        Collections.sort(purchaseDataList, new Comparator<PurchaseData>() {
            @Override
            public int compare(PurchaseData lhs, PurchaseData rhs) {
                return lhs.getPurchaseTime().compareTo(rhs.getPurchaseTime());
            }
        });

        return purchaseDataList;
    }

    /**
     * Filters the result of the {@link #getAllPurchases(com.android.vending.billing.IInAppBillingService, String)}
     * method to only return purchase data with the given product id.
     * @param service Service object to query.
     * @param packageName The package name of the application.
     * @param productId The product id to filter by.
     * @return All of the purchases of the given product made by the user. Sorted on purchase time,
     * oldest first. Returns null on error or if some data has an incorrect signature.
     */
    public List<PurchaseData> getAllProductIdPurchases(IInAppBillingService service, String packageName, String productId)
            throws RemoteException, JSONException {
        List<PurchaseData> dataList = getAllPurchases(service, packageName);
        if(dataList == null) {
            return null;
        }

        List<PurchaseData> res = new ArrayList<>();
        for(PurchaseData data : dataList) {
            if(data.getProductId().equals(productId)) {
                res.add(data);
            }
        }

        return res;
    }
}
