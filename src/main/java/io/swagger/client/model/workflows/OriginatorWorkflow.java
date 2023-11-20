/************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2023 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.

 *************************************************************************
 */

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
