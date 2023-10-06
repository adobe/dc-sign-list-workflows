package uk.org.esig.adobe.workflows;

import io.swagger.client.api.BaseUrisApi;
import io.swagger.client.api.OriginatorWorkflowsApi;
import io.swagger.client.api.UsersApi;
import io.swagger.client.model.ApiClient;
import io.swagger.client.model.ApiException;
import io.swagger.client.model.baseUris.BaseUriInfo;
import io.swagger.client.model.users.DetailedUserInfo;
import io.swagger.client.model.users.UserInfo;
import io.swagger.client.model.users.UsersInfo;
import io.swagger.client.model.workflows.OriginatorWorkflow;
import io.swagger.client.model.workflows.OriginatorWorkflows;
import org.apache.commons.csv.CSVFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListWorkflows {
    private static final String API_PATH = "api/rest/v6";
    private static final String API_URL = "https://api.adobesign.com/";
    private static final String API_USER_PREFIX = "email:";
    private static final String BEARER = "Bearer ";
    private static final int CAPACITY = 20000;
    private static final int PAGE_SIZE = 1000;
    private static final String SANDBOX = "--sandbox";
    private static final String SANDBOX_API_URL = "https://api.adobesignsandbox.com/";
    private static final int TIMEOUT = 300000;
    private static final String USAGE = "Usage: java -jar aas-list-workflows-<version>.jar <integrationKey> [--sandbox]";

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
        UsersApi usersApi = new UsersApi(apiClient);
        OriginatorWorkflowsApi workflowsApi = new OriginatorWorkflowsApi(apiClient);

        Map<String, OriginatorWorkflow> foundWorkflows = new HashMap<>(CAPACITY);
        Map<String, UserInfo> foundUsers = new HashMap<>(CAPACITY);
        /*
         *  Obtain the first page of users
         */
        UsersInfo usersInfo = usersApi.getUsers(accessToken, null, null, PAGE_SIZE);
        List<UserInfo> userInfoList = usersInfo.getUserInfoList();
        System.out.println(format("workflow_id", "workflow_name", "owner_email", "sharing_mode"));
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
            String scope = (workflow.getScope() != null) ? workflow.getScope().name() : "USER";
            UserInfo userInfo = foundUsers.get(workflow.getOriginatorId());
            System.out.println(format(workflow.getId(),
                                      workflow.getDisplayName(),
                                      userInfo.getEmail(),
                                      scope));
        }
    }

    private String format(String id, String name, String email, String sharingMode) {
        return CSVFormat.EXCEL.format(id, name, email, sharingMode);
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
