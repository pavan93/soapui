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

package com.eviware.soapui.support.components;

import com.eviware.soapui.support.action.swing.ActionList;

import java.util.Map;

/**
 * Behavior of a configuration dialog
 *
 * @author Ole.Matzura
 */

public interface ConfigurationDialog {
    boolean show(Map<String, String> values);

    void hide();

    void addTextField(String name, String tooltip);

    void addTextField(String name, String tooltip, FieldType type);

    void addCheckBox(String caption, String label, boolean selected);

    void addComboBox(String label, Object[] objects, String tooltip);

    void setValues(String id, String[] values);

    void addComboBox(String label, String tooltip);

    ActionList getActions();

    void getValues(Map<String, String> values);

    enum FieldType {
        TEXT, DIRECTORY, FILE, URL, JAVA_PACKAGE, JAVA_CLASS
    }
}
