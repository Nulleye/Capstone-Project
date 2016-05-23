package com.nulleye.yaaa.test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.AlarmDbHelper;
import com.nulleye.yaaa.data.YaaaContract;
import com.nulleye.yaaa.data.YaaaProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ControllerTest extends ProviderTestCase2<YaaaProvider> {


    public ControllerTest() {
        super(YaaaProvider.class, YaaaContract.CONTENT_AUTHORITY);
    }


    @Before
    @Override
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
    }


    @Test
    public void testAddAndGetAlarm() {
        Context context = getContext();

        Alarm alarm = new Alarm();

        boolean result = AlarmDbHelper.addAlarm(context, alarm);
        assertEquals("Unable to add default alarm", true, result);

        Alarm newAlarm = AlarmDbHelper.getAlarm(context, alarm.getId());
        assertEquals("Unable to get default alarm", alarm, newAlarm);
    }



    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
