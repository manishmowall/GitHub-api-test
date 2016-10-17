package org.webonise.githubmetric.githubapitest;


public class Repo {
   private final String name;
   private final String url;

   public Repo(final String name, final String url) {
      this.name = name;
      this.url = url;
   }

   public String getName() {
      return name;
   }

   public String getUrl() {
      return url;
   }
}
