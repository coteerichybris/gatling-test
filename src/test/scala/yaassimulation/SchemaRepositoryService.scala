package defaultsimulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class SchemaRepositoryService extends BaseSimulation {  
  def getScenarios : List[Scenario] = {
    List(
      Scenario(
        // NAME
        "add single new schema and fetch multiple times",

        // BEFORE TEST
        exec {
          new OAuth2Authentication(sys.env("OAUTH2AUTHENTICATION_TOKEN_URL")).fetchOauth2AccessToken(sys.env("CLIENT_ID"), sys.env("CLIENT_SECRET"), 
            List("FULL_ACCESS")
          )
        }. exec {
          http("add new schema")
            .post("/schema-repository/v1/"+sys.env("TENANT")+"/performanceTestSchema.json")
            .header("Authorization", "Bearer ${accessToken}")
            .header(HttpHeaderNames.ContentType, "application/json")
            .header(HttpHeaderNames.ContentLength, "0")
            .body(StringBody(
              """{
                "$schema": "http://json-schema.org/draft-04/schema#",
                "title": "Product",
                "description": "A product from hybris' catalog",
                "type": "object"
              }"""
            ))
            .check(status.in(200, 409))
            .silent
        },
       
        // TEST
        exec { 
          http("get schema")
            .get("/schema-repository/v1/"+sys.env("TENANT")+"/performanceTestSchema.json")
            .header("Authorization", "Bearer ${accessToken}")
            .check(status.is(200))
        },
     
        // AFTER TEST
        NOP
      )
    )
  }
}

