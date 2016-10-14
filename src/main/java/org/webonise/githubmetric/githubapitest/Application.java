package org.webonise.githubmetric.githubapitest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Application {
    private OkHttpClient client;
    private final String oauthToken = "a64950f07938ba9aff59b9a4efcb42e828e591fe";
    private final String organization = "GitHub-metrics";
    private final String rootEndPoint = "https://api.github.com";

    public void configureTimeouts(int connectTimeout, int writeTimeout, int readTimeout) throws Exception {
        client = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();
    }

    public void run() throws Exception {
        //get user details
        Request userRequest = new Request.Builder()
                .url(rootEndPoint + "/user")
                .header("Authorization", "token " + oauthToken)
                .build();

        //check
        Response userResponse = client.newCall(userRequest).execute();
        if (!userResponse.isSuccessful()) {
            throw new IOException("Unexpected code" + userResponse);
        }

        String userResponseBody = userResponse.body().string();
        JSONObject userJsonObject = new JSONObject(userResponseBody);

        String loginKey = "login";
        String userHandler = userJsonObject.getString(loginKey);
        System.out.println(userHandler);

        //=====================================================================================
        //Check memberShip of user
        String expectedRole = "admin";
        String expectedState = "active";
        String roleKey = "role";
        String stateKey = "state";

        Request checkMembershipRequest = new Request.Builder()
                .url(rootEndPoint + "/orgs/" + organization + "/memberships/" + userHandler)
                .header("Authorization", "token " + oauthToken)
                .build();

        //checking membership response if not found will throw IOException
        Response checkMembershipResponse = client.newCall(checkMembershipRequest).execute();
        if (!checkMembershipResponse.isSuccessful()) {
            throw new IOException("Unexpected code" + checkMembershipResponse);
        }

        String membershipResponseBody = checkMembershipResponse.body().string();
        JSONObject membershipJsonObject = new JSONObject(membershipResponseBody);

        String userRole = membershipJsonObject.getString(roleKey);
        String userState = membershipJsonObject.getString(stateKey);
        //======================================================================================
        //check role of user
        if (!(userRole.equals(expectedRole) && userState.equals(expectedState))) {
            throw new IOException("User is " + userRole + " and " + "state is " + userState);
        } else {
            System.out.println(userHandler + " is valid member of " + organization + " with role " + userRole + " and state " + userState);
        }

        //=======================================================================================
        //get repos of organization

        Request reposRequest = new Request.Builder()
                .url(rootEndPoint + "/orgs/" + organization + "/repos")
                .header("Authorization", "token " + oauthToken)
                .build();

        Response reposResponse = client.newCall(reposRequest).execute();
        if (!reposResponse.isSuccessful()) {
            throw new IOException("Unexpected code" + checkMembershipResponse);
        }

        JSONArray repos = new JSONArray(reposResponse.body().string());
        int NumberOfRepos = repos.length();
        for (int index = 0; index < NumberOfRepos; index++) {
            JSONObject repo = repos.getJSONObject(index);
        }
    }

    public static void main(String[] args) {
        Application application = new Application();
        try {
            application.configureTimeouts(30, 30, 30);
            application.run();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
