/*
 * SoapUI, Copyright (C) 2004-2017 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.model.testsuite;

import com.eviware.soapui.support.action.swing.ActionList;

import java.io.PrintWriter;

/**
 * A TestStep result
 *
 * @author Ole.Matzura
 */

public interface TestStepResult {
    TestStepStatus getStatus();

    TestStep getTestStep();

    /**
     * Returns a list of actions that can be applied to this result
     */

    ActionList getActions();

    String[] getMessages();

    Throwable getError();

    long getTimeTaken();

    long getTimeStamp();

    /**
     * Used for calculating throughput
     *
     * @return the number of bytes in this result
     */

    long getSize();

    /**
     * Writes this result to the specified writer, used for logging.
     */

    void writeTo(PrintWriter writer);

    /**
     * Can discard any result data that may be taking up memory. Timing-values
     * must not be discarded.
     */

    void discard();

    boolean isDiscarded();

    enum TestStepStatus {
        UNKNOWN, OK, FAILED, CANCELED
    }
}
