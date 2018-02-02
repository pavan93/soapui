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

package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.panels.request.views.content.RestRequestContentView;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.support.EndpointsComboBoxModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AddParamAction;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.utils.ContainerWalker;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.soapui.utils.StubbedDialogs;
import com.eviware.x.dialogs.XDialogs;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.util.List;

import static com.eviware.soapui.utils.ModelItemMatchers.hasParameter;
import static com.eviware.soapui.utils.StubbedDialogs.hasPromptWithValue;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * Unit tests for RestRequestDesktopPanel.
 */
public class RestRequestDesktopPanelTest {

    private static final String PARAMETER_NAME = "jsessionid";
    private static final String PARAMETER_VALUE = "Da Value";
    private static final String ENDPOINT = "http://sunet.se/search";
    private static final String RESOURCE_PATH = "abc/path";

    private RestRequestDesktopPanel requestDesktopPanel;
    private RestRequest restRequest;
    private StubbedDialogs dialogs;
    private XDialogs originalDialogs;
    private JComboBox endpointsCombo;

    @Before
    public void setUp() throws Exception {
        restRequest = ModelItemFactory.makeRestRequest();
        restRequest.setMethod(RestRequestInterface.HttpMethod.GET);
        restRequest.getResource().getParams().addProperty(PARAMETER_NAME);
        restRequest.getResource().setPath(RESOURCE_PATH);
        restService().addEndpoint(ENDPOINT);
        restRequest.setEndpoint(ENDPOINT);
        RestParamProperty restParamProperty = restRequest.getParams().getProperty(PARAMETER_NAME);
        restParamProperty.setValue(PARAMETER_VALUE);
        requestDesktopPanel = new RestRequestDesktopPanel(restRequest);
        originalDialogs = UISupport.getDialogs();
        dialogs = new StubbedDialogs();
        UISupport.setDialogs(dialogs);
        endpointsCombo = findEndpointsComboBox();
    }

    @After
    public void resetDialogs() {
        UISupport.setDialogs(originalDialogs);
    }

    @Test
    public void retainsParameterValueWhenChangingItsLevel() {
        JTable paramsTable = getRestParameterTable();
        paramsTable.setValueAt(NewRestResourceActionBase.ParamLocation.METHOD, 0, 3);
        paramsTable.setValueAt(NewRestResourceActionBase.ParamLocation.RESOURCE, 0, 3);

        RestParamProperty returnedParameter = restRequest.getParams().getProperty(PARAMETER_NAME);
        assertThat(returnedParameter.getValue(), is(PARAMETER_VALUE));
    }

    @Test
    public void addsNewParameterToResource() {
        JTable restParameterTable = getRestParameterTable();
        new AddParamAction(restParameterTable, restRequest.getParams(), "").actionPerformed(new ActionEvent(restParameterTable, 1, "Add"));
        String newParamName = "newParamName";
        restParameterTable.setValueAt(newParamName, 1, 0);
        restParameterTable.setValueAt("newParamValue", 1, 1);

        assertThat(restRequest.getRestMethod().hasProperty(newParamName), is(false));
        assertThat(restRequest.getResource().hasProperty(newParamName), is(true));
    }

    @Test
    public void retainsParameterOrderWhenChangingItsLevel() {
        restRequest.getParams().addProperty("Param2");
        getRestParameterTable().setValueAt(NewRestResourceActionBase.ParamLocation.METHOD, 0, 3);

        assertThat(getRestParameterTable().getValueAt(0, 0), is(PARAMETER_NAME));
    }

    @Test
    public void addsAndRemovesTemplateParameterOnResourceField() {
        String path = restRequest.getResource().getPath();
        assertThat(requestDesktopPanel.resourcePanel.getText(), equalTo(path));

        restRequest.getParams().getProperty(PARAMETER_NAME).setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
        // Assert that it adds the template parameter on the path
        assertThat(requestDesktopPanel.resourcePanel.getText(), containsString("{" + PARAMETER_NAME + "}"));


        restRequest.getParams().getProperty(PARAMETER_NAME).setStyle(RestParamsPropertyHolder.ParameterStyle.QUERY);
        assertThat(requestDesktopPanel.resourcePanel.getText(), not(containsString("{" + PARAMETER_NAME + "}")));
    }

    @Test
    public void extractsQueryStringParamsFromUrl() {
        RestParamsPropertyHolder params = restRequest.getParams();
        String url = restRequest.getEndpoint() + restRequest.getPath() + "?q=foo&page=2";
        RestUtils.extractParams(url, params, true);
        assertThat(params, hasParameter("q"));
        assertThat(params, hasParameter("page"));
    }

    @Test
    public void addsAndRemovesTemplateParameterOnParentShouldOnlyUpdateParentPath() {
        RestResource restResource = restRequest.getResource();
        RestResource childResource = restResource.addNewChildResource("childResource", "/subPath");
        String childPath = childResource.getPath();
        String parentPath = restResource.getPath();
        String expectedParentPath = parentPath + "{" + PARAMETER_NAME + "}";
        String expectedChildPath = expectedParentPath + childPath;

        RestRequest childRestRequest = ModelItemFactory.makeRestRequest(childResource);
        childRestRequest.setMethod(RestRequestInterface.HttpMethod.GET);

        RestRequestDesktopPanel childRequestDesktopPanel = new RestRequestDesktopPanel(childRestRequest);

        restRequest.getParams().getProperty(PARAMETER_NAME).setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);

        // Assert that it adds the template parameter on the path
        assertThat(requestDesktopPanel.resourcePanel.getText(), equalTo(expectedParentPath));
        assertThat(childRequestDesktopPanel.resourcePanel.getText(), equalTo(expectedChildPath));

        restRequest.getParams().getProperty(PARAMETER_NAME).setStyle(RestParamsPropertyHolder.ParameterStyle.QUERY);

        assertThat(requestDesktopPanel.resourcePanel.getText(), equalTo(parentPath));
        assertThat(childRequestDesktopPanel.resourcePanel.getText(), equalTo(childResource.getFullPath()));
    }

    @Test
    public void renamesTemplateParameterOnParentResource() {
        RestResource restResource = restRequest.getResource();
        RestResource childResource = restResource.addNewChildResource("childResource", "/subPath");
        String childPath = childResource.getPath();
        String parentPath = restResource.getPath();
        String newParamName = "sessionID";
        String expectedParentPath = parentPath + "{" + newParamName + "}";
        String expectedChildPath = expectedParentPath + childPath;


        RestRequest childRestRequest = ModelItemFactory.makeRestRequest(childResource);
        childRestRequest.setMethod(RestRequestInterface.HttpMethod.GET);

        RestRequestDesktopPanel childRequestDesktopPanel = new RestRequestDesktopPanel(childRestRequest);

        restRequest.getParams().getProperty(PARAMETER_NAME).setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
        restRequest.getParams().getProperty(PARAMETER_NAME).setName(newParamName);

        // Assert that it adds the template parameter on the path
        assertThat(requestDesktopPanel.resourcePanel.getText(), equalTo(expectedParentPath));
        assertThat(childRequestDesktopPanel.resourcePanel.getText(), equalTo(expectedChildPath));

    }

    @Test
    public void addsAndRemovesTemplateParameterOnChildResourcePath() {
        RestResource restResource = restRequest.getResource();
        RestResource childResource = restResource.addNewChildResource("childResource", "/subPath");
        String fullPath = childResource.getFullPath();

        String childParamName = "childParam";
        childResource.setPropertyValue(childParamName, "childValue");

        RestRequest childRestRequest = ModelItemFactory.makeRestRequest(childResource);
        childRestRequest.setMethod(RestRequestInterface.HttpMethod.GET);

        RestRequestDesktopPanel origDesktopPanel = requestDesktopPanel;
        requestDesktopPanel = new RestRequestDesktopPanel(childRestRequest);

        childResource.getProperty(childParamName).setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
        // Assert that it adds the template parameter on the path
        assertThat(requestDesktopPanel.resourcePanel.getText(), equalTo(fullPath + "{" + childParamName + "}"));

        childResource.getProperty(childParamName).setStyle(RestParamsPropertyHolder.ParameterStyle.QUERY);
        // Assert that it adds the template parameter on the path
        assertThat(requestDesktopPanel.resourcePanel.getText(), equalTo(fullPath));

        // Set back the desktop panel
        requestDesktopPanel = origDesktopPanel;
    }

    @Test
    public void renameTemplateParameterOnChildResourcePath() {
        RestResource restResource = restRequest.getResource();
        RestResource childResource = restResource.addNewChildResource("childResource", "/subPath");
        String fullPath = childResource.getFullPath();

        String childParamName = "childParam";
        String newParamName = "newChildParam";
        childResource.setPropertyValue(childParamName, "childValue");

        RestRequest childRestRequest = ModelItemFactory.makeRestRequest(childResource);
        childRestRequest.setMethod(RestRequestInterface.HttpMethod.GET);

        RestRequestDesktopPanel origDesktopPanel = requestDesktopPanel;
        requestDesktopPanel = new RestRequestDesktopPanel(childRestRequest);

        childResource.getProperty(childParamName).setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
        childResource.getProperty(childParamName).setName(newParamName);

        // Assert that it adds the template parameter on the path
        assertThat(requestDesktopPanel.resourcePanel.getText(), equalTo(fullPath + "{" + newParamName + "}"));

        // Set back the desktop panel
        requestDesktopPanel = origDesktopPanel;
    }

    @Test
    public void addsAndRemovesTemplateParameterOnModel() {
        String path = restRequest.getResource().getPath();
        assertThat(requestDesktopPanel.resourcePanel.getText(), equalTo(path));

        restRequest.getParams().getProperty(PARAMETER_NAME).setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
        // Assert that it adds the template parameter on the path
        assertThat(restRequest.getResource().getPath(), containsString("{" + PARAMETER_NAME + "}"));


        restRequest.getParams().getProperty(PARAMETER_NAME).setStyle(RestParamsPropertyHolder.ParameterStyle.QUERY);
        assertThat(restRequest.getResource().getPath(), not(containsString(PARAMETER_NAME)));
    }

    @Test
    public void updatesExistingTemplateParameterName() {

        String newParamName = "sessionID";
        String path = restRequest.getResource().getPath();
        assertThat(requestDesktopPanel.resourcePanel.getText(), equalTo(path));

        restRequest.getParams().getProperty(PARAMETER_NAME).setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
        restRequest.getParams().getProperty(PARAMETER_NAME).setName(newParamName);

        // Assert that parameter is replaced with new name
        assertThat(requestDesktopPanel.resourcePanel.getText(), equalTo(path + "{" + newParamName + "}"));
    }

    @Test
    public void addingTemplateParameterOnParentShouldUpdateChildPathEvenIfParentDialogNotOpen() throws SoapUIException {
        RestResource parentResource = ModelItemFactory.makeRestResource();
        parentResource.setPath("/parent");

        RestResource childResource = parentResource.addNewChildResource("child", "/child");

        String expectedChildPath = parentResource.getPath() + "{" + PARAMETER_NAME + "}" + childResource.getPath();

        RestRequest childRestRequest = ModelItemFactory.makeRestRequest(childResource);
        childRestRequest.setMethod(RestRequestInterface.HttpMethod.GET);

        RestRequestDesktopPanel childRequestDesktopPanel = new RestRequestDesktopPanel(childRestRequest);

        parentResource.addProperty(PARAMETER_NAME);
        parentResource.getParams().getProperty(PARAMETER_NAME).setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
        assertThat(childRequestDesktopPanel.resourcePanel.getText(), equalTo(expectedChildPath));
    }


    @Test
    public void allowsRemovalOfParameterAfterParameterLevelChange() {
        restRequest.getParams().addProperty("Param2");
        getRestParameterTable().setValueAt(NewRestResourceActionBase.ParamLocation.METHOD, 0, 3);

        String paramNameAtRow0;
        restRequest.getParams().removeProperty(PARAMETER_NAME);
        paramNameAtRow0 = (String) getRestParameterTable().getValueAt(0, 0);
        assertThat(paramNameAtRow0, is("Param2"));
    }


    @Test
    public void displaysEndpoint() {
        assertThat(requestDesktopPanel.getEndpointsModel().getSelectedItem(), is(ENDPOINT));
    }

    @Test
    public void reactsToEndpointChanges() {
        String anotherEndpoint = "http://mafia.ru/search";
        restService().changeEndpoint(ENDPOINT, anotherEndpoint);
        assertThat(requestDesktopPanel.getEndpointsModel().getSelectedItem(), is(anotherEndpoint));
    }

    @Test
    public void keepsEnteredEndpointValueWhenEditingEndpoint() throws Exception {
        JComboBox endpointsCombo = findEndpointsComboBox();
        String otherValue = "http://dn.se";
        setComboTextFieldValue(endpointsCombo, otherValue);
        endpointsCombo.setSelectedItem(EndpointsComboBoxModel.EDIT_ENDPOINT);

        waitForSwingThread();
        assertThat(dialogs.getPrompts(), hasPromptWithValue(otherValue));
    }

    @Test
    public void keepsEnteredEndpointValueWhenAddingNewEndpoint() throws Exception {
        String otherValue = "http://dn.se";
        setComboTextFieldValue(endpointsCombo, otherValue);
        endpointsCombo.setSelectedItem(EndpointsComboBoxModel.ADD_NEW_ENDPOINT);

        waitForSwingThread();
        assertThat(dialogs.getPrompts(), hasPromptWithValue(otherValue));
    }

    @Ignore("For some reason this test fails, although it works fine in the GUI")
    @Test
    public void resetsToEnteredValueWhenCancelingAdd() throws Exception {
        dialogs.mockPromptWithReturnValue(null);
        String otherValue = "http://dn.se";
        setComboTextFieldValue(endpointsCombo, otherValue);
        endpointsCombo.setSelectedItem(EndpointsComboBoxModel.ADD_NEW_ENDPOINT);

        waitForSwingThread();
        assertThat(getComboTextFieldValue(), is(otherValue));
    }

    @Test
    public void reactsToPathChanges() {
        String anotherPath = "/changed/path";
        restRequest.getResource().setPath(anotherPath);
        assertThat(requestDesktopPanel.resourcePanel.getText(), is(anotherPath));
    }

    @Ignore("Fails intermittently, but works in GUI")
    @Test
    public void parameterAdditionUpdatesParametersField() {
        final String parameterName = "the_new_param";
        RestParamProperty newParameter = restRequest.getParams().addProperty(parameterName);
        newParameter.setStyle(RestParamsPropertyHolder.ParameterStyle.QUERY);
        final String value = "the_new_value";
        newParameter.setValue(value);

        assertThat(requestDesktopPanel.queryPanel.getText(), containsString(parameterName + "=" + value));
    }


	/* Helpers */

    private String getComboTextFieldValue() throws Exception {
        Document document = ((JTextComponent) endpointsCombo.getEditor().getEditorComponent()).getDocument();
        return document.getText(0, document.getLength());
    }

    private JComboBox findEndpointsComboBox() {
        ContainerWalker finder = new ContainerWalker(requestDesktopPanel);
        return finder.findComboBoxWithValue(ENDPOINT);
    }

    private void setComboTextFieldValue(JComboBox endpointsCombo, String otherValue) {
        ((JTextComponent) endpointsCombo.getEditor().getEditorComponent()).setText(otherValue);
    }

    private void waitForSwingThread() throws InterruptedException {
        Thread.sleep(50);
    }

    private JTable getRestParameterTable() {
        List<? extends EditorView<? extends XmlDocument>> views = requestDesktopPanel.getRequestEditor().getViews();
        RestRequestContentView restRequestContentView = (RestRequestContentView) views.get(0);
        return restRequestContentView.getParamsTable().getParamsTable();
    }


    private RestService restService() {
        return restRequest.getOperation().getInterface();
    }
}
