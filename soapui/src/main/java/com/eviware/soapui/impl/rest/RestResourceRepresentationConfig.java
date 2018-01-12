package com.eviware.soapui.impl.rest;

public interface RestResourceRepresentationConfig {
    String getId();

    boolean isSetType();

    Object getType();

    boolean getParams();

    void addNewParams();
}
