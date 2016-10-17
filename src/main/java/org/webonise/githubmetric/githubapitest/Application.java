package org.webonise.githubmetric.githubapitest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Application {
   private OkHttpClient client;
   private final String oauthToken = "a062a00ca6d9cf24391f6850f2485389ff53f578";
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
         throw new IOException("Unexpected code" + reposResponse);
      }
      String reposResponseBody = reposResponse.body().string();
      JSONArray repos = new JSONArray(reposResponseBody);

      List<Repo> repoList = new ArrayList();

      int NumberOfRepos = repos.length();
      for (int index = 0; index < NumberOfRepos; index++) {
         JSONObject repoObject = repos.getJSONObject(index);
         String repoName = repoObject.getString("name");
         String repoUrl = repoObject.getString("url");
         Repo currentRepo = new Repo(repoName, repoUrl);
         repoList.add(currentRepo);
      }

      for (Repo r : repoList) {
         System.out.println(r.getName() + " : " + r.getUrl());
      }

      //======================================================================
      //get PRs of speicific repo
      Repo repo = repoList.get(0);
      String repoUrl = repo.getUrl();
      Request repoRequest = new Request.Builder()
         .url(repoUrl)
         .header("Authorization", "token " + oauthToken)
         .build();

      Response repoResponse = client.newCall(repoRequest).execute();
      if (!repoResponse.isSuccessful()) {
         throw new IOException("Unexpected code" + repoResponse);
      }
      String repoResponseBody = repoResponse.body().string();
      JSONObject repoJsonObject = new JSONObject(repoResponseBody);
      String pullsUrl = repoJsonObject.getString("pulls_url");
      System.out.println(pullsUrl);

      String pullRequestsUrl = pullsUrl.substring(0, pullsUrl.length() - 9);

      Request pullsRequest = new Request.Builder()
         .url(pullRequestsUrl)
         .header("Authorization", "token " + oauthToken)
         .build();

      Response pullsResponse = client.newCall(pullsRequest).execute();
      if (!pullsResponse.isSuccessful()) {
         throw new IOException("Unexpected code" + pullsResponse);
      }
      String pullsResponseBody = pullsResponse.body().string();
      JSONArray pullsJsonArray = new JSONArray(pullsResponseBody);

      //======================================================================


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
