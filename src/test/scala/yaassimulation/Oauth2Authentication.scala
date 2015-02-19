package defaultsimulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.net.URL

class OAuth2Authentication(tokenUrl:String) {
  def fetchOauth2AccessToken(clientId:String, clientSecret:String, scopes:List[String]) = {
    http("login")
      .post(tokenUrl)
      .header(HttpHeaderNames.ContentType, "application/x-www-form-urlencoded")
      .header(HttpHeaderNames.ContentLength, "0")
      .queryParam("client_id", clientId)
      .queryParam("client_secret", clientSecret)
      .queryParam("grant_type", "client_credentials")
      .queryParam("scope", scopes.mkString(","))
      .silent
      .ignoreDefaultChecks
      .disableFollowRedirect
      .check(status.is(200))
      .check(jsonPath("$.access_token").find.saveAs("accessToken"))
  } 
}
