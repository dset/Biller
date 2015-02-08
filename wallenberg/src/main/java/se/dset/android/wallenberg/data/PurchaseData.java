package se.dset.android.wallenberg.data;

import com.google.common.base.Objects;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class PurchaseData {
    private String orderId;
    private String packageName;
    private String productId;
    private long purchaseTime;
    private int purchaseState;
    private String developerPayload;
    private String purchaseToken;

    public PurchaseData(String json) throws JSONException {
        JSONObject data = new JSONObject(json);
        this.orderId = data.getString("orderId");
        this.packageName = data.getString("packageName");
        this.productId = data.getString("productId");
        this.purchaseTime = data.getLong("purchaseTime");
        this.purchaseState = data.getInt("purchaseState");
        this.developerPayload = data.getString("developerPayload");
        this.purchaseToken = data.getString("purchaseToken");
    }

    public PurchaseData(String orderId, String packageName, String productId, long purchaseTime,
                        int purchaseState, String developerPayload, String purchaseToken) {
        this.orderId = orderId;
        this.packageName = packageName;
        this.productId = productId;
        this.purchaseTime = purchaseTime;
        this.purchaseState = purchaseState;
        this.developerPayload = developerPayload;
        this.purchaseToken = purchaseToken;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getProductId() {
        return productId;
    }

    public Date getPurchaseTime() {
        return new Date(purchaseTime);
    }

    public int getPurchaseState() {
        return purchaseState;
    }

    public String getDeveloperPayload() {
        return developerPayload;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PurchaseData)) {
            return false;
        }

        PurchaseData o = (PurchaseData) obj;

        return Objects.equal(orderId, o.orderId)
                && Objects.equal(packageName, o.packageName)
                && Objects.equal(productId, o.productId)
                && purchaseTime == o.purchaseTime
                && purchaseState == o.purchaseState
                && Objects.equal(developerPayload, o.developerPayload)
                && Objects.equal(purchaseToken, o.purchaseToken);
    }

    @Override
    public int hashCode() {
        return (int) purchaseTime;
    }
}
