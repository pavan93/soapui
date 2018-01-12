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

package com.eviware.soapui.support.editor;

import com.eviware.soapui.support.PropertyChangeNotifier;

import javax.swing.*;

/**
 * Views available for the XmlDocument of a XmlEditor
 *
 * @author ole.matzura
 */

public interface EditorView<T extends EditorDocument> extends PropertyChangeNotifier, EditorLocationListener<T> {
    String TITLE_PROPERTY = EditorView.class.getName() + "@title";

    Editor<T> getEditor();

    String getTitle();

    JComponent getComponent();

    boolean deactivate();

    boolean activate(EditorLocation<T> location);

    EditorLocation<T> getEditorLocation();

    void setLocation(EditorLocation<T> location);

    T getDocument();

    void setDocument(T document);

    void addLocationListener(EditorLocationListener<T> listener);

    void removeLocationListener(EditorLocationListener<T> listener);

    void release();

    void setEditable(boolean enabled);

    String getViewId();

    void requestFocus();
}
