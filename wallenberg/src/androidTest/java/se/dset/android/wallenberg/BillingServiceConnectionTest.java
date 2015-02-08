package se.dset.android.wallenberg;

import junit.framework.TestCase;

import static org.mockito.Mockito.*;

public class BillingServiceConnectionTest extends TestCase {
    private BillingServiceConnection connection;
    private BillingServiceConnection.Callback callback;

    @Override
    protected void setUp() throws Exception {
        connection = new BillingServiceConnection();
        callback = mock(BillingServiceConnection.Callback.class);
    }

    public void testNullCall() {
        connection.setCallback(null);
        connection.onServiceConnected(null, null);
    }

    public void testCallbackCall() {
        connection.setCallback(callback);
        connection.onServiceConnected(null, null);
        verify(callback, times(1)).onServiceConnected(connection, null);
    }

    public void testCallbackNotMultipleCalls() {
        connection.setCallback(callback);
        connection.onServiceConnected(null, null);
        connection.onServiceConnected(null, null);
        verify(callback, times(1)).onServiceConnected(connection, null);
    }
}
