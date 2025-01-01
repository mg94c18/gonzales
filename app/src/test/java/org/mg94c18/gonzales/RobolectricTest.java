package org.mg94c18.gonzales;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import org.robolectric.RobolectricTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricTestRunner.class)
public class RobolectricTest {
    @Test
    public void testContext() {
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
    }
}
