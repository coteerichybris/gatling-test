package defaultsimulation

import io.gatling.core.session.Expression
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.util.concurrent.atomic._
import io.gatling.core.structure.ChainBuilder
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseSimulation extends Simulation {
  case class Scenario(name:String, beforeTest:ChainBuilder, test:ChainBuilder, afterTest:ChainBuilder)
 
  val NOP = exec { session =>
    session
  }

  val continue = new AtomicBoolean(true)

  def getScenarios : List[Scenario]
 
  val httpConf = http
    .baseURL(sys.env("BASE_URL"))
    .acceptHeader("text/html,application/xhtml+xml,application/xml,application/json;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val scenarios = scenario("Performance-Test").exec {
    randomSwitch(
      getScenarios.map( scenario => (100.0/ getScenarios.size) ->
        group(scenario.name) {
          exec { session => 
            session.set("continue", true)
          }.exec {
            scenario.beforeTest.exitHereIfFailed.asLongAs("${continue}") {
              scenario.test.exec { session => 
                session.set("continue", continue.get()) 
              }.pace(1 seconds)
            }.exec {
              scenario.afterTest
            }
          }
        }
      ) : _*
    )
  } 

  setUp(
    scenarios.inject(rampUsers(900) over (15 minutes)).protocols(httpConf),
    
    scenario("Stop-Performance-Test").exec {
      pause(30 minutes).exec { session =>
        continue.set(false)
        session
      }
    }

    .inject(atOnceUsers(1))
  )
}
