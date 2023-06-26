package io.swagger.client.model.workflows;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

public class OriginatorWorkflow extends UserWorkflow {
    @SerializedName("originatorId")
    private String originatorId = null;

    public OriginatorWorkflow originatorId(String originatorId) {
        this.originatorId = originatorId;
        return this;
    }

    @ApiModelProperty(value = "The originator ID of the workflow.")
    public String getOriginatorId() {
        return originatorId;
    }

    public void setOriginatorId(String originatorId) {
        this.originatorId = originatorId;
    }
}
