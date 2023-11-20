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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OriginatorWorkflows {
    @SerializedName("userWorkflowList")
    private List<OriginatorWorkflow> originatorWorkflowList = null;

    public OriginatorWorkflows originatorWorkflowList(List<OriginatorWorkflow> originatorWorkflowList) {
        this.originatorWorkflowList = originatorWorkflowList;
        return this;
    }

    public OriginatorWorkflows addUserWorkflowListItem(OriginatorWorkflow originatorWorkflowListItem) {
        if (this.originatorWorkflowList == null) {
            this.originatorWorkflowList = new ArrayList<OriginatorWorkflow>();
        }
        this.originatorWorkflowList.add(originatorWorkflowListItem);
        return this;
    }

    /**
     * An array of workflows
     * @return userWorkflowList
     **/
    @ApiModelProperty(value = "An array of workflows")
    public List<OriginatorWorkflow> getOriginatorWorkflowList() {
        return originatorWorkflowList;
    }

    public void setOriginatorWorkflowList(List<OriginatorWorkflow> originatorWorkflowList) {
        this.originatorWorkflowList = originatorWorkflowList;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OriginatorWorkflows originatorWorkflows = (OriginatorWorkflows) o;
        return Objects.equals(this.originatorWorkflowList, originatorWorkflows.originatorWorkflowList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originatorWorkflowList);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OriginatorWorkflows {\n");
        sb.append("    userWorkflowList: ").append(toIndentedString(originatorWorkflowList)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}