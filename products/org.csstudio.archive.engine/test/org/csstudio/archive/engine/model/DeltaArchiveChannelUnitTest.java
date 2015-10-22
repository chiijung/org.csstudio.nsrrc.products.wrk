/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.archive.engine.model;

import static org.epics.pvmanager.ExpressionLanguage.channel;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVWriter;
import org.epics.pvmanager.loc.LocalDataSource;
import org.junit.Test;

/** JUnit test of the DeltaArchiveChannel
 * 
 *  <p>Depending on timing, this test does not always pass...
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DeltaArchiveChannelUnitTest
{
    private static final String PV_NAME = "loc://demo";

    @Test(timeout=20000)
    public void testHandleNewValue() throws Exception
    {
    	PVManager.setDefaultDataSource(new LocalDataSource());
    	
    	final PVWriter<Object> pv = PVManager.write(channel(PV_NAME)).sync();

        final DeltaArchiveChannel channel = new DeltaArchiveChannel(PV_NAME, Enablement.Passive, 100, null, 1.01, 0.1);
        final SampleBuffer samples = channel.getSampleBuffer();
        channel.start();
        SECONDS.sleep(2);

        pv.write(1.0);
        SECONDS.sleep(2);
        System.out.println("Initial sample(s):");
        int initial = TestHelper.dump(samples);
        assertTrue(initial == 1  ||  initial == 2);

        // Big Change
        pv.write(2.0);
        SECONDS.sleep(2);
        System.out.println("Big change:");
        assertEquals(1, TestHelper.dump(samples));

        // Small Change
        pv.write(2.05);
        SECONDS.sleep(2);
        System.out.println("Small change:");
        assertEquals(0, TestHelper.dump(samples));

        // Big Change
        pv.write(2.5);
        SECONDS.sleep(2);
        System.out.println("Big change:");
        assertEquals(1, TestHelper.dump(samples));

        channel.stop();
        pv.close();
    }
}
