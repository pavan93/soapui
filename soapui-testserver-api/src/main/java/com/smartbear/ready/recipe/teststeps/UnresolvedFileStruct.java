package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "UnresolvedFile")
class UnresolvedFileStruct {

    private String fileName;

    @JsonCreator
    public UnresolvedFileStruct(@JsonProperty("fileName") String fileName) {
        this.fileName = fileName;
    }
}
