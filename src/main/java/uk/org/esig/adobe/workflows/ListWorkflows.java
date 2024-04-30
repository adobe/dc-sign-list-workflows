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

package uk.org.esig.adobe.workflows;

import io.swagger.client.api.BaseUrisApi;
import io.swagger.client.api.GroupsApi;
import io.swagger.client.api.OriginatorWorkflowsApi;
import io.swagger.client.api.UsersApi;
import io.swagger.client.model.ApiClient;
import io.swagger.client.model.ApiException;
import io.swagger.client.model.baseUris.BaseUriInfo;
import io.swagger.client.model.groups.GroupInfo;
import io.swagger.client.model.groups.GroupsInfo;
import io.swagger.client.model.users.DetailedUserInfo;
import io.swagger.client.model.users.UserInfo;
import io.swagger.client.model.users.UsersInfo;
import io.swagger.client.model.workflows.OriginatorWorkflow;
import io.swagger.client.model.workflows.OriginatorWorkflows;
import io.swagger.client.model.workflows.UserWorkflow;
import org.apache.commons.csv.CSVFormat;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListWorkflows {
    private static final String API_PATH = "api/rest/v6";
    private static final String API_URL = "https://api.adobesign.com/";
    private static final String API_USER_PREFIX = "email:";
    private static final CharsetEncoder ASCII_ENCODER = StandardCharsets.US_ASCII.newEncoder();
    private static final String BEARER = "Bearer ";
    private static final int CAPACITY = 20000;
    private static final String NO_ORIGINATOR = "<No Originator found for Workflow>";
    private static final String NO_SUCH_ACTIVE_USER = "<No User found for Workflow Originator>";
    private static final int PAGE_SIZE = 1000;
    private static final String SANDBOX = "--sandbox";
    private static final String SANDBOX_API_URL = "https://api.adobesignsandbox.com/";
    private static final int TIMEOUT = 300000;
    private static final String USAGE = "Usage: java -jar dc-sign-list-workflows-<version>.jar <integrationKey> [--sandbox]";

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.out.println(getUsage());
        }
        else {
            String accessToken = BEARER + args[0];
            ListWorkflows list = new ListWorkflows();
            try {
                if (args.length == 2 && args[1] != null && args[1].equalsIgnoreCase(SANDBOX)) {
                    list.execute(SANDBOX_API_URL, accessToken);
                }
                else {
                    list.execute(API_URL, accessToken);
                }
            }
            catch (ApiException ae) {
                System.out.println(getExceptionDetails(ae));
                ae.printStackTrace();
            }
        }
    }

    public void execute(String apiUrl, String accessToken) throws ApiException {
        /*
         *  Establish connection to Adobe Sign API, and obtain the correct API Access Point for the account
         */
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(apiUrl + API_PATH);
        apiClient.setConnectTimeout(TIMEOUT).setReadTimeout(TIMEOUT);
        BaseUrisApi baseUrisApi = new BaseUrisApi(apiClient);
        BaseUriInfo baseUriInfo = baseUrisApi.getBaseUris(accessToken);
        apiClient.setBasePath(baseUriInfo.getApiAccessPoint() + API_PATH);

        /*
         *  Instantiate APIs for account
         */
        GroupsApi groupsApi = new GroupsApi(apiClient);
        UsersApi usersApi = new UsersApi(apiClient);
        OriginatorWorkflowsApi workflowsApi = new OriginatorWorkflowsApi(apiClient);

        Map<String, String> foundGroups = new HashMap<>(CAPACITY);
        Map<String, OriginatorWorkflow> foundWorkflows = new HashMap<>(CAPACITY);
        Map<String, UserInfo> foundUsers = new HashMap<>(CAPACITY);

        /*
         *  Populate the list of groups
         */
        GroupsInfo groupsInfo = groupsApi.getGroups(accessToken, null, null, PAGE_SIZE);
        List<GroupInfo> groupInfoList = groupsInfo.getGroupInfoList();
        while (groupInfoList != null && !groupInfoList.isEmpty()) {
            for (GroupInfo groupInfo: groupInfoList) {
                foundGroups.put(groupInfo.getGroupId(), groupInfo.getGroupName());
            }
            String groupCursor = groupsInfo.getPage().getNextCursor();
            if (groupCursor != null && !groupCursor.isEmpty()) {
                groupsInfo = groupsApi.getGroups(accessToken, null, groupCursor, PAGE_SIZE);
                groupInfoList = groupsInfo.getGroupInfoList();
            }
            else {
                groupInfoList = null;
            }
        }

        /*
         *  Obtain the first page of users
         */
        UsersInfo usersInfo = usersApi.getUsers(accessToken, null, null, PAGE_SIZE);
        List<UserInfo> userInfoList = usersInfo.getUserInfoList();
        System.out.println(format("workflow_id", "workflow_name", "owner_email", "sharing_mode", "group_name"));
        while (userInfoList != null && !userInfoList.isEmpty()) {
            /*
             *  For each user:
             *  (a) Make sure that they are ACTIVE
             *  (b) Get the list of workflows they have access to
             *  (c) Check which of these the user owns
             *  (d) Output details if, and only if, they are the owner
             */
            for (UserInfo userInfo: userInfoList) {
                foundUsers.put(userInfo.getId(), userInfo);
                DetailedUserInfo detail = usersApi.getUserDetail(accessToken, userInfo.getId(), null);
                if (detail != null && detail.getStatus().equals(DetailedUserInfo.StatusEnum.ACTIVE)) {
                    String email = userInfo.getEmail();
                    if (ASCII_ENCODER.canEncode(email)) {
                        String apiUser = API_USER_PREFIX + email;
                        OriginatorWorkflows workflows = workflowsApi.getWorkflows(accessToken,
                                                                                  apiUser,
                                                                                  Boolean.FALSE,
                                                                                  Boolean.FALSE,
                                                                                  null);
                        List<OriginatorWorkflow> workflowList = workflows.getOriginatorWorkflowList();
                        if (workflowList != null) {
                            for (OriginatorWorkflow workflow : workflowList) {
                                foundWorkflows.put(workflow.getId(), workflow);
                            }
                        }
                    }
                }
            }
            String userCursor = usersInfo.getPage().getNextCursor();
            if (userCursor != null && !userCursor.isEmpty()) {
                usersInfo = usersApi.getUsers(accessToken, null, userCursor, PAGE_SIZE);
                userInfoList = usersInfo.getUserInfoList();
            }
            else {
                userInfoList = null;
            }
        }
        for (OriginatorWorkflow workflow: foundWorkflows.values()) {
            String groupName = null;
            if (workflow.getScope() != null && workflow.getScope().equals(UserWorkflow.ScopeEnum.GROUP)) {
                groupName = foundGroups.get(workflow.getScopeId());
            }
            String scope = (workflow.getScope() != null) ? workflow.getScope().name() : "USER";
            String originatorEmail = NO_ORIGINATOR;
            if (workflow.getOriginatorId() != null) {
                UserInfo userInfo = foundUsers.get(workflow.getOriginatorId());
                if (userInfo != null) {
                    originatorEmail = userInfo.getEmail();
                }
                else {
                    originatorEmail = NO_SUCH_ACTIVE_USER;
                }
            }
            System.out.println(format(workflow.getId(),
                                      workflow.getDisplayName(),
                                      originatorEmail,
                                      scope,
                                      groupName));
        }
    }

    private String format(String id, String name, String email, String sharingMode, String groupName) {
        return CSVFormat.EXCEL.format(id, name, email, sharingMode, groupName);
    }

    private static String getExceptionDetails(ApiException e) {
        StringBuilder sb = new StringBuilder();
        if (e != null) {
            sb.append("Message: ");
            sb.append(e.getMessage());
            sb.append("\n");
            sb.append("Code: ");
            sb.append(e.getCode());
            sb.append("\n");
            sb.append("Response Headers: ");
            sb.append(e.getResponseHeaders());
            sb.append("\n");
            sb.append("Response Body: ");
            sb.append(e.getResponseBody());
        }
        return sb.toString();
    }

    private static String getUsage() {
        return USAGE;
    }
}
