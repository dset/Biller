package se.dset.android.wallenberg;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.android.vending.billing.IInAppBillingService;

public class BillingServiceConnection implements ServiceConnection {
    public interface Callback {
        public void onServiceConnected(BillingServiceConnection connection, IInAppBillingService service);
    }

    private Callback callback;
    private boolean hasFired;

    public BillingServiceConnection() {
        this.callback = null;
        this.hasFired = false;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        IInAppBillingService billingService = IInAppBillingService.Stub.asInterface(service);

        if(callback != null && !hasFired) {
            callback.onServiceConnected(this, billingService);
        }

        hasFired = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
