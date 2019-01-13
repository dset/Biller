package se.dset.android.biller;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import com.android.vending.billing.IInAppBillingService;

import junit.framework.TestCase;

import org.mockito.ArgumentCaptor;

import se.dset.android.biller.util.Constants;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestManagerTest extends TestCase {
    private Context context;
    private Request request;
    private RequestManager manager;

    @Override
    protected void setUp() throws Exception {
        context = mock(Context.class);
        request = mock(Request.class);
        manager = new RequestManager(context);
    }

    public void testApiUsage() {
        manager.runRequest(request);
        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(context).bindService(captor.capture(), any(BillingServiceConnection.class), eq(Context.BIND_AUTO_CREATE));
        assertEquals(Constants.IAB_SERVICE_INTENT_ACTION, captor.getValue().getAction());
        assertEquals(Constants.IAB_SERVICE_INTENT_PACKAGE, captor.getValue().getPackage());
    }

    public void testFailedBindService() {
        when(context.bindService(any(Intent.class), any(ServiceConnection.class), anyInt())).thenReturn(false);
        assertFalse(manager.runRequest(request));
    }

    public void testSuccessfulBindService() {
        when(context.bindService(any(Intent.class), any(ServiceConnection.class), anyInt())).thenReturn(true);
        assertTrue(manager.runRequest(request));
    }

    public void testCompleteRequest() {
        IInAppBillingService service = mock(IInAppBillingService.class);
        ArgumentCaptor<BillingServiceConnection> captor = ArgumentCaptor.forClass(BillingServiceConnection.class);
        when(context.bindService(any(Intent.class), captor.capture(), anyInt())).thenReturn(true);
        manager.runRequest(request);
        manager.onServiceConnected(captor.getValue(), service);
        manager.onRequestDone(request);

        verify(request).run(service);
        verify(context).unbindService(captor.getValue());
    }
}
