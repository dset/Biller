package se.dset.android.wallenberg;

import android.os.Bundle;
import android.test.InstrumentationTestRunner;

public class BillerTestRunner extends InstrumentationTestRunner {
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }
}
