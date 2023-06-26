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

import java.util.List;

public class ListWorkflows {
    private static final String API_HOST = "https://api.adobesign.com/";
    private static final String API_PATH = "api/rest/v6";
    private static final String API_USER_PREFIX = "email:";
    private static final String BEARER = "Bearer ";
    private static final int PAGE_SIZE = 1000;
    private static final int TIMEOUT = 300000;
    private static final String USAGE = "Usage: java -jar aas-list-workflows-<version>.jar <integrationKey>";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(getUsage());
        }
        else {
            String accessToken = BEARER + args[0];
            ListWorkflows list = new ListWorkflows();
            try {
                list.execute(accessToken);
            }
            catch (ApiException ae) {
                System.out.println(getExceptionDetails(ae));
                ae.printStackTrace();
            }
        }
    }

    public void execute(String accessToken) throws ApiException {
        /*
         *  Establish connection to Adobe Sign API, and obtain the correct API Access Point for the account
         */
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(API_HOST + API_PATH);
        apiClient.setConnectTimeout(TIMEOUT).setReadTimeout(TIMEOUT);
        BaseUrisApi baseUrisApi = new BaseUrisApi(apiClient);
        BaseUriInfo baseUriInfo = baseUrisApi.getBaseUris(accessToken);
        apiClient.setBasePath(baseUriInfo.getApiAccessPoint() + API_PATH);

        /*
         *  Instantiate APIs for account
         */
        UsersApi usersApi = new UsersApi(apiClient);
        OriginatorWorkflowsApi workflowsApi = new OriginatorWorkflowsApi(apiClient);

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
                DetailedUserInfo detail = usersApi.getUserDetail(accessToken, userInfo.getId(), null);
                if (detail != null && detail.getStatus().equals(DetailedUserInfo.StatusEnum.ACTIVE)) {
                    String userId = userInfo.getId();
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
                            String originatorId = workflow.getOriginatorId();
                            if (userId != null && userId.equals(originatorId)) {
                                System.out.println(format(workflow.getId(),
                                                          workflow.getDisplayName(),
                                                          email,
                                                          workflow.getScope().name()));
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
