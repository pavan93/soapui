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

import com.eviware.soapui.model.iface.SoapUIListener;

/**
 * Listener for LoadTest run events
 *
 * @author Ole.Matzura
 */

public interface LoadTestRunListener extends SoapUIListener {
    /**
     * Called before a load-test is about to be run
     *
     * @param loadTestRunner
     * @param context
     */

    void beforeLoadTest(LoadTestRunner loadTestRunner, LoadTestRunContext context);

    /**
     * Called after all initial loadtest threads have been started
     *
     * @param loadTestRunner
     * @param context
     */

    void loadTestStarted(LoadTestRunner loadTestRunner, LoadTestRunContext context);

    /**
     * Called before the execution of a testcase
     *
     * @param loadTestRunner
     * @param context
     * @param testRunner
     * @param runContext
     */

    void beforeTestCase(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                        TestCaseRunContext runContext);

    /**
     * Called before the execution of a teststep
     *
     * @param loadTestRunner
     * @param context
     * @param testRunner
     * @param runContext
     * @param testStep
     */

    void beforeTestStep(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                        TestCaseRunContext runContext, TestStep testStep);

    /**
     * Called after the execution of a teststep
     *
     * @param loadTestRunner
     * @param context
     * @param testRunner
     * @param runContext
     * @param testStepResult
     */

    void afterTestStep(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                       TestCaseRunContext runContext, TestStepResult testStepResult);

    /**
     * Called after the execution of a testcase
     *
     * @param loadTestRunner
     * @param context
     * @param testRunner
     * @param runContext
     */

    void afterTestCase(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                       TestCaseRunContext runContext);

    /**
     * Called when a loadtest has been stopped for some reason
     *
     * @param loadTestRunner
     * @param context
     */

    void loadTestStopped(LoadTestRunner loadTestRunner, LoadTestRunContext context);

    /**
     * Called after the execution of a loadtest when all threads have terminated
     *
     * @param loadTestRunner
     * @param context
     */
    void afterLoadTest(LoadTestRunner loadTestRunner, LoadTestRunContext context);
}
