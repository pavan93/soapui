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

package com.eviware.soapui.impl.wsdl.support.wss;

import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.registry.RegistryEntry;
import org.apache.ws.security.message.WSSecHeader;
import org.w3c.dom.Document;

import javax.swing.*;

public interface WssEntry extends RegistryEntry<WSSEntryConfig, OutgoingWss> {
    void process(WSSecHeader secHeader, Document doc, PropertyExpansionContext context);

    JComponent getConfigurationPanel();

    String getLabel();

    OutgoingWss getOutgoingWss();

    void updateEntryConfig(WSSEntryConfig config);

    void release();
}
