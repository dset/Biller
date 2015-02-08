package se.dset.android.wallenberg;

import android.content.Context;
import android.content.Intent;

import com.android.vending.billing.IInAppBillingService;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import se.dset.android.wallenberg.util.Constants;

public class RequestManager implements BillingServiceConnection.Callback, Request.Callback {
    private Context context;
    private BiMap<BillingServiceConnection, Request> connectionRequestMap;

    public RequestManager(Context context) {
        this.context = context;
        this.connectionRequestMap = HashBiMap.create();
    }

    public boolean runRequest(Request request) {
        BillingServiceConnection connection = new BillingServiceConnection();
        connection.setCallback(this);

        connectionRequestMap.put(connection, request);

        Intent serviceIntent = new Intent(Constants.IAB_SERVICE_INTENT);
        return context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(BillingServiceConnection connection, IInAppBillingService service) {
        Request request = connectionRequestMap.get(connection);
        request.setCallback(this);
        request.run(service);
    }

    @Override
    public void onRequestDone(Request request) {
        BillingServiceConnection connection = connectionRequestMap.inverse().get(request);
        connectionRequestMap.remove(connection);
        context.unbindService(connection);
    }
}
