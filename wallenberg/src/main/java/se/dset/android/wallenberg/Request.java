package se.dset.android.wallenberg;

import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;

import se.dset.android.wallenberg.util.Constants;

/**
 * A general IAB request. Create a subclass to provide support for specific IAB procedures. There
 * are three methods that need to be implemented. The {@link #runBackground(com.android.vending.billing.IInAppBillingService)}
 * method is where you interact with the IAB service. Use the provided service object to access
 * IAB functionality. Since the methods of the service object can cause network requests the
 * {@link #runBackground(com.android.vending.billing.IInAppBillingService)} method runs in a
 * background thread. The {@link #onSuccess()} method is run if the
 * {@link #runBackground(com.android.vending.billing.IInAppBillingService)} method returns
 * {@link se.dset.android.wallenberg.util.Constants#BILLING_RESPONSE_RESULT_OK}. The {@link #onFailure(int)}
 * method is run if the {@link #runBackground(com.android.vending.billing.IInAppBillingService)}
 * method returns anything else. Both {@link #onSuccess()} and {@link #onFailure(int)} run on the main thread.
 * Use these methods to notify interested entities of the result of the request.
 */
public abstract class Request {
    /* package-private */ interface Callback {
        public void onRequestDone(Request request);
    }

    private Callback callback;
    private Request that;

    public Request() {
        this.callback = null;
        this.that = this;
    }

    /* package-private */ void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void run(IInAppBillingService service) {
        Thread t = new Thread(new BackgroundRunnable(service));
        t.start();
    }

    /**
     * Called on a background thread. Override this and use the provided service object to fulfill
     * the IAB request. Return {@link se.dset.android.wallenberg.util.Constants#BILLING_RESPONSE_RESULT_OK}
     * on success and anything else to signal failure.
     * @param service Service object to be used to fulfill the request.
     * @return {@link se.dset.android.wallenberg.util.Constants#BILLING_RESPONSE_RESULT_OK} on success.
     * Anything else to signal failure.
     */
    protected abstract int runBackground(IInAppBillingService service) throws RemoteException, JSONException;

    /**
     * Called on the main thread if {@link #runBackground(com.android.vending.billing.IInAppBillingService)}
     * returns {@link se.dset.android.wallenberg.util.Constants#BILLING_RESPONSE_RESULT_OK}.
     */
    protected abstract void onSuccess();

    /**
     * Called on the main thread if {@link #runBackground(com.android.vending.billing.IInAppBillingService)}
     * returns something other than {@link se.dset.android.wallenberg.util.Constants#BILLING_RESPONSE_RESULT_OK}.
     * @param errorCode The value returned by {@link #runBackground(com.android.vending.billing.IInAppBillingService)}.
     */
    protected abstract void onFailure(int errorCode);

    private class BackgroundRunnable implements Runnable {
        private IInAppBillingService service;

        public BackgroundRunnable(IInAppBillingService service) {
            this.service = service;
        }

        @Override
        public void run() {
            int result;
            try {
                result = runBackground(service);
            } catch (RemoteException e) {
                result = Constants.BILLING_RESPONSE_RESULT_ERROR;
            } catch (JSONException e) {
                result = Constants.BILLING_RESPONSE_RESULT_ERROR;
            }

            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(new ResultRunnable(result));
        }
    }

    private class ResultRunnable implements Runnable {
        private int result;

        public ResultRunnable(int result) {
            this.result = result;
        }

        @Override
        public void run() {
            if(result == Constants.BILLING_RESPONSE_RESULT_OK) {
                onSuccess();
            } else {
                onFailure(result);
            }

            if(callback != null) {
                callback.onRequestDone(that);
            }
        }
    }
}
