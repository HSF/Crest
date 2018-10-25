/**
 * Copyright 2011-2016 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package crest

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class CrestSimulationTagIovSelection extends Simulation {

  // Let's split this big scenario into composable business processes, like one would do with PageObject pattern with Selenium
  val rampUpTimeSecs = 60
  val testTimeSecs   = 600
  val noOfUsers      = 1000
  val minWaitMs      = 400 milliseconds
  val maxWaitMs      = 800 milliseconds
  val tagName        = "none"

  val purl = "http://localhost:8180/crest/crestapi"

  val http_headers = Map(
    "Content-Type" -> "text/json;charset=UTF-8",
    "Keep-Alive" -> "120")

  val feeder = csv("tags.csv").random  // Prepare a feeder

  // object are native Scala singletons
  object Search {

    val searchiovs =  during(testTimeSecs) {
    feed(feeder) // 3
    .exec(http("TagIovs")
    .get("/iovs/selectGroups?tagname=${tagname}")) // 6
    .pause(minWaitMs,maxWaitMs)
    }
      
    val searchpayloads =  during(testTimeSecs) {feed(feederhash) // 3
    .exec(http("Hash")
    .get("/payloads/${hash}")) // 6
    .pause(1)
    }
  }

  val httpConf = http
    .baseURL(purl)
    .acceptHeader("application/json;q=0.9,*/*;q=0.8")
//    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  // Now, we can write the scenario as a composition
  val scn = scenario("Cms select iovs ranges and payloads").exec(Search.searchiovs)

  setUp(scn.inject((rampUsers(noOfUsers) over (rampUpTimeSecs seconds))).protocols(httpConf))
}

