/**
 * Copyright 2011-2017 GatlingCorp (http://gatling.io)
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
import scala.collection.mutable._
import scala.util.Random
import java.io._

class CrestBasicSimulation01 extends Simulation {

  val rampUpTimeSecs = 25
  val testTimeSecs   = 200
  val noOfUsers      = 200
  val minWaitMs      = 400 milliseconds
  val maxWaitMs      = 800 milliseconds
  val tagName        = "none"

  val purl = "http://localhost:8180/crest/crestapi"
  //val purl = "http://localhost:8080/crestapi"
  //val snapshot = java.util.Calendar.getInstance().getTimeInMillis()
  val snapshot : Long = 1507715309078L;
  val httpConf = http
    .baseURL(purl) // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml,application/json;q=0.9,*/*;q=0.8") // Here are the common headers
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val headers_10 = Map("Content-Type" -> "application/json") // Note the headers specific to a given request

  val feeder = csv("tags.csv").random  // Prepare a feeder
  val feedergroups = csv("taggroups.csv").random
  
    // object are native Scala singletons
  object Search {
    println ("Found snapshot time "+snapshot)
//    val searchiovs =  during(testTimeSecs) {
    val searchiovgroups = during (testTimeSecs) {
    feed(feeder) // 3
    .exec(
         http("TagIovGroups")
        .get("/iovs/selectGroups?tagname=${tagname}&snapshot="+snapshot) // 6
//        .check(status.is(200),
//               jsonPath("$.groups").ofType[scala.collection.Seq[Any]].findAll.saveAs("groups"))
         )
    .pause(minWaitMs,maxWaitMs)
    }
  }
  
  object Browse {     

      val searchiovs = during (testTimeSecs) {
        feed(feedergroups)
        .exec(
           http("TagIovs")
           .get("/iovs/selectIovs?tagname=${tagname}&since=${MySince}&until=${MyUntil}") // 6
        )
        .pause(minWaitMs,maxWaitMs)
      }
   }

  val scn = scenario("Crest select iovs groups").exec(Search.searchiovgroups,Browse.searchiovs)

  setUp(scn.inject((rampUsers(noOfUsers) over (rampUpTimeSecs seconds))).protocols(httpConf))
  //setUp(scn.inject(constantUsersPerSec(10) during(1 minutes))).throttle(
  //reachRps(20) in (10 seconds),
  //holdFor(1 minute),
  //jumpToRps(5),
  //holdFor(1 minute)).protocols(httpConf)

}
