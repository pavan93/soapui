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

package com.eviware.soapui.impl.wsdl.panels.project;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.project.AddNewTestSuiteAction;
import com.eviware.soapui.impl.wsdl.panels.support.MockProjectRunner;
import com.eviware.soapui.impl.wsdl.panels.testcase.JTestRunLog;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlProjectRunner;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.model.testsuite.TestSuite.TestSuiteRunType;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.*;
import com.eviware.soapui.support.types.StringToObjectMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WsdlProjectTestSuitesTabPanel extends JPanel {
    private final WsdlProject project;
    private JProgressBar progressBar;
    private JProjectTestSuiteList testSuiteList;
    private RunAction runAction = new RunAction();
    private CancelAction cancelAction = new CancelAction();
    private JToggleButton sequentialButton;
    private JToggleButton parallellButton;
    private final InternalProjectListener testSuiteListener = new InternalProjectListener();
    private final InternalTestSuiteRunListener testSuiteRunListener = new InternalTestSuiteRunListener();
    private JTestRunLog testRunLog;
    private GroovyEditorComponent tearDownGroovyEditor;
    private GroovyEditorComponent setupGroovyEditor;
    private JInspectorPanel testSuiteListInspectorPanel;
    private JInspectorPanel inspectorPanel;
    private WsdlProjectRunner projectRunner;

    public WsdlProjectTestSuitesTabPanel(WsdlProject project) {
        super(new BorderLayout());
        this.project = project;

        buildUI();
        project.addProjectRunListener(testSuiteRunListener);
        project.addProjectListener(testSuiteListener);
    }

    public WsdlProject getProject() {
        return project;
    }

    private void buildUI() {
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        setPreferredSize(new Dimension(500, 500));
    }

    private JComponent buildContent() {
        inspectorPanel = JInspectorPanelFactory.build(buildTabs());
        addInspectors(inspectorPanel);

        return inspectorPanel.getComponent();
    }

    private void addInspectors(JInspectorPanel inspectorPanel) {
        inspectorPanel.addInspector(new JComponentInspector<JComponent>(buildRunLog(), "TestSuite Log",
                "Log of executed TestSuites, TestCases and TestSteps", true));
    }

    private JComponent buildRunLog() {
        testRunLog = new JTestRunLog(project.getSettings());
        return testRunLog;
    }

    protected JProjectTestSuiteList getTestSuiteList() {
        return testSuiteList;
    }

    private JComponent buildToolbar() {
        cancelAction.setEnabled(false);
        runAction.setEnabled(project.getTestSuiteCount() > 0);

        JXToolBar toolbar = UISupport.createToolbar();

        addToolbarActions(toolbar);
        toolbar.addGlue();
        toolbar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.TESTSUITE_HELP_URL)));

        progressBar = new JProgressBar(0, project.getTestSuiteCount());
        JPanel progressPanel = UISupport.createProgressBarPanel(progressBar, 10, false);

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(toolbar, BorderLayout.PAGE_START);
        panel.add(progressPanel, BorderLayout.CENTER);

        return panel;
    }

    private void addToolbarActions(JXToolBar toolbar) {
        toolbar.add(UISupport.createToolbarButton(runAction));
        toolbar.add(UISupport.createToolbarButton(cancelAction));

        toolbar.addRelatedGap();

        ButtonGroup buttonGroup = new ButtonGroup();

        sequentialButton = new JToggleButton(UISupport.createImageIcon("/sequential.gif"), true);
        sequentialButton.setToolTipText("The selected TestSuites are run in sequence");
        sequentialButton.setPreferredSize(UISupport.getPreferredButtonSize());
        sequentialButton.setSelected(project.getRunType() == TestSuiteRunType.SEQUENTIAL);
        sequentialButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                project.setRunType(TestSuiteRunType.SEQUENTIAL);
            }
        });

        buttonGroup.add(sequentialButton);

        parallellButton = new JToggleButton(UISupport.createImageIcon("/parallell.gif"));
        parallellButton.setToolTipText("The selected TestSuites are run in parallel");
        parallellButton.setPreferredSize(UISupport.getPreferredButtonSize());
        parallellButton.setSelected(project.getRunType() == TestSuiteRunType.PARALLEL);
        parallellButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                project.setRunType(TestSuiteRunType.PARALLEL);
            }
        });

        buttonGroup.add(parallellButton);

        toolbar.addUnrelatedGap();
        toolbar.add(sequentialButton);
        toolbar.addRelatedGap();
        toolbar.add(parallellButton);
    }

    private JComponent buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        testSuiteListInspectorPanel = JInspectorPanelFactory.build(buildTestSuiteList(project));

        tabs.addTab("TestSuites", testSuiteListInspectorPanel.getComponent());

        addTabs(tabs, testSuiteListInspectorPanel);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return UISupport.createTabPanel(tabs, true);
    }

    private void addTabs(JTabbedPane tabs, JInspectorPanel inspectorPanel) {
        inspectorPanel.addInspector(new GroovyEditorInspector(buildSetupScriptPanel(), "Setup Script",
                "Script to run before running TestSuites"));
        inspectorPanel.addInspector(new GroovyEditorInspector(buildTearDownScriptPanel(), "TearDown Script",
                "Script to run after running TestSuites"));
    }

    private GroovyEditorComponent buildTearDownScriptPanel() {
        tearDownGroovyEditor = new GroovyEditorComponent(new TearDownScriptGroovyEditorModel(), HelpUrls.FUNCTIONAL_TESTING_TEARDOWN_SCRIPT);
        return tearDownGroovyEditor;
    }

    private GroovyEditorComponent buildSetupScriptPanel() {
        setupGroovyEditor = new GroovyEditorComponent(new SetupScriptGroovyEditorModel(), HelpUrls.FUNCTIONAL_TESTING_SETUP_SCRIPT);
        return setupGroovyEditor;
    }

    private JComponent buildTestSuiteList(WsdlProject testSuite) {
        testSuiteList = new JProjectTestSuiteList(testSuite);

        JPanel p = new JPanel(new BorderLayout());

        p.add(buildTestCaseListToolbar(), BorderLayout.NORTH);
        p.add(new JScrollPane(testSuiteList), BorderLayout.CENTER);

        return p;
    }

    private Component buildTestCaseListToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();
        SwingActionDelegate addTestSuiteDelegate = SwingActionDelegate.createDelegate(
                AddNewTestSuiteAction.SOAPUI_ACTION_ID, project, null, "/test_suite.png");
        addTestSuiteDelegate.getMapping().setParam(SoapUIActions.CREATE_TEST_SUITE_FROM_PROJECT_PANEL);

        toolbar.add(UISupport.createToolbarButton(addTestSuiteDelegate));

        toolbar.addGlue();
        toolbar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.TESTSUITELIST_HELP_URL)));
        return toolbar;
    }

    public void release() {
        inspectorPanel.release();
        testSuiteListInspectorPanel.release();

        setupGroovyEditor.release();
        tearDownGroovyEditor.release();

        testRunLog.release();
        project.removeProjectRunListener(testSuiteRunListener);
        project.removeProjectListener(testSuiteListener);
    }

    private void runProject() {
        projectRunner = project.run(new StringToObjectMap(), true);
    }

    private void beforeRun() {
        runAction.setEnabled(false);
        cancelAction.setEnabled(true);
        testSuiteList.setEnabled(false);
        progressBar.setForeground(Color.GREEN.darker());
    }

    private void afterRun() {
        runAction.setEnabled(true);
        cancelAction.setEnabled(false);
        testSuiteList.setEnabled(true);

        progressBar.setString(projectRunner.getStatus().toString());
        progressBar.setForeground(projectRunner.isFailed() ? Color.RED : Color.GREEN.darker());
    }

    private final class InternalProjectListener extends ProjectListenerAdapter {
        public void testSuiteAdded(TestSuite testSuite) {
            runAction.setEnabled(project.getTestSuiteCount() > 0);
        }

        public void testSuiteRemoved(TestSuite testSuite) {
            runAction.setEnabled(project.getTestSuiteCount() > 0);
        }
    }

    private class RunAction extends AbstractAction {
        RunAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run.png"));
            putValue(Action.SHORT_DESCRIPTION, "Runs the selected TestSuites");
        }

        public void actionPerformed(ActionEvent e) {
            runProject();
            Analytics.trackAction(SoapUIActions.RUN_TEST_SUITE_FROM_TOOLBAR);
        }
    }

    private class CancelAction extends AbstractAction {
        CancelAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/stop.png"));
            putValue(Action.SHORT_DESCRIPTION, "Cancels ongoing TestSuite runs");
        }

        public void actionPerformed(ActionEvent e) {
            projectRunner.cancel("Cancelled from UI");
        }
    }

    private class SetupScriptGroovyEditorModel extends AbstractGroovyEditorModel {
        SetupScriptGroovyEditorModel() {
            super(new String[]{"log", "runner", "context", "project"}, project, "Setup");
        }

        public String getScript() {
            return project.getBeforeRunScript();
        }

        public void setScript(String text) {
            project.setBeforeRunScript(text);
        }

        @Override
        public Action createRunAction() {
            return new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        MockProjectRunner runner = new MockProjectRunner(project);
                        project.runBeforeRunScript((ProjectRunContext) runner.getRunContext(), runner);
                    } catch (Exception e1) {
                        UISupport.showErrorMessage(e1);
                    }
                }
            };
        }
    }

    private class TearDownScriptGroovyEditorModel extends AbstractGroovyEditorModel {
        TearDownScriptGroovyEditorModel() {
            super(new String[]{"log", "runner", "context", "project"}, project, "TearDown");
        }

        public String getScript() {
            return project.getAfterRunScript();
        }

        public void setScript(String text) {
            project.setAfterRunScript(text);
        }

        @Override
        public Action createRunAction() {
            return new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        MockProjectRunner runner = new MockProjectRunner(project);
                        project.runAfterRunScript((ProjectRunContext) runner.getRunContext(), runner);
                    } catch (Exception e1) {
                        UISupport.showErrorMessage(e1);
                    }
                }
            };
        }
    }

    private class InternalTestSuiteRunListener implements ProjectRunListener {
        private TestRunLogTestSuiteRunListener runLogListener;
        private int finishCount;

        public void afterRun(ProjectRunner testScenarioRunner, ProjectRunContext runContext) {
            if (testScenarioRunner != projectRunner) {
                return;
            }

            WsdlProjectTestSuitesTabPanel.this.afterRun();
        }

        public void afterTestSuite(ProjectRunner testScenarioRunner, ProjectRunContext runContext,
                                   TestSuiteRunner testRunner) {
            if (testScenarioRunner != projectRunner) {
                return;
            }

            progressBar.setValue(++finishCount);

            if (project.getRunType() == TestSuiteRunType.SEQUENTIAL) {
                testRunner.getTestSuite().removeTestSuiteRunListener(runLogListener);
            }
        }

        public void beforeRun(ProjectRunner testScenarioRunner, ProjectRunContext runContext) {
            if (testScenarioRunner != projectRunner) {
                return;
            }

            WsdlProjectTestSuitesTabPanel.this.beforeRun();

            testSuiteList.reset();

            progressBar.setMaximum(project.getTestSuiteCount());
            progressBar.setValue(0);
            progressBar.setString("");
            finishCount = 0;

            if (runLogListener == null) {
                runLogListener = new TestRunLogTestSuiteRunListener(testRunLog, false);
            }

            testRunLog.clear();

            if (project.getRunType() == TestSuiteRunType.PARALLEL) {
                testRunLog.addText("<log disabled during parallel execution>");
            }
        }

        public void beforeTestSuite(ProjectRunner testScenarioRunner, ProjectRunContext runContext,
                                    TestSuite testRunnable) {
            if (testScenarioRunner != projectRunner) {
                return;
            }

            progressBar.setString("Running " + testRunnable.getName());

            if (project.getRunType() == TestSuiteRunType.SEQUENTIAL) {
                testRunnable.addTestSuiteRunListener(runLogListener);
            }
        }
    }

    public WsdlProjectRunner getProjectRunner() {
        return projectRunner;
    }
}
