package se.dset.android.biller.data;

import com.google.common.base.Objects;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductDetails {
    private String productId;
    private String type;
    private String price;

    /* The following two names are not camel cased. This is to be consistent with the JSON
    returned from Google. By using these names objects of this class can easily be turned
    into JSON that is compatible with the JSON from Google. */
    private long price_amount_micros;
    private String price_currency_code;

    private String title;
    private String description;

    public ProductDetails(String json) throws JSONException {
        JSONObject j = new JSONObject(json);
        this.productId = j.getString("productId");
        this.type = j.getString("type");
        this.price = j.getString("price");
        this.price_amount_micros = j.getLong("price_amount_micros");
        this.price_currency_code = j.getString("price_currency_code");
        this.title = j.getString("title");
        this.description = j.getString("description");
    }

    public ProductDetails(String productId, String type, String price, long price_amount_micros,
                          String price_currency_code, String title, String description) {
        this.productId = productId;
        this.type = type;
        this.price = price;
        this.price_amount_micros = price_amount_micros;
        this.price_currency_code = price_currency_code;
        this.title = title;
        this.description = description;
    }

    public String getProductId() {
        return productId;
    }

    public String getType() {
        return type;
    }

    public String getPrice() {
        return price;
    }

    public long getPriceAmountMicros() {
        return price_amount_micros;
    }

    public String getPriceCurrencyCode() {
        return price_currency_code;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ProductDetails)) {
            return false;
        }

        ProductDetails o = (ProductDetails) obj;

        return Objects.equal(productId, o.productId)
                && Objects.equal(type, o.type)
                && Objects.equal(price, o.price)
                && price_amount_micros == o.price_amount_micros
                && Objects.equal(price_currency_code, o.price_currency_code)
                && Objects.equal(title, o.title)
                && Objects.equal(description, o.description);
    }
}
