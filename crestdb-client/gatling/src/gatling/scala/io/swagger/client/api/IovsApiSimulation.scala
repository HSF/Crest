package io.swagger.client.api

import io.swagger.client.model._
import com.typesafe.config.ConfigFactory

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.structure.PopulationBuilder

import java.io.File

import scala.collection.mutable

class IovsApiSimulation extends Simulation {

    def getCurrentDirectory = new File("").getAbsolutePath
    def userDataDirectory = getCurrentDirectory + "/src/gatling/resources/data"

    // basic test setup
    val configName = System.getProperty("testConfig", "baseline")

    val config = ConfigFactory.load(configName).withFallback(ConfigFactory.load("default"))
    val hostName = config.getString("performance.hostName")
    val durationSeconds = config.getInt("performance.durationSeconds")
    val rampUpSeconds = config.getInt("performance.rampUpSeconds")
    val rampDownSeconds = config.getInt("performance.rampDownSeconds")
    val authentication = config.getString("performance.authorizationHeader")
    val acceptHeader = config.getString("performance.acceptType")
    val contentTypeHeader = config.getString("performance.contentType")
    val rateMultiplier = config.getDouble("performance.rateMultiplier")
    val instanceMultiplier = config.getDouble("performance.instanceMultiplier")

    // global assertion data
    val globalResponseTimeMinLTE = config.getInt("performance.global.assertions.responseTime.min.lte")
    val globalResponseTimeMinGTE = config.getInt("performance.global.assertions.responseTime.min.gte")
    val globalResponseTimeMaxLTE = config.getInt("performance.global.assertions.responseTime.max.lte")
    val globalResponseTimeMaxGTE = config.getInt("performance.global.assertions.responseTime.max.gte")
    val globalResponseTimeMeanLTE = config.getInt("performance.global.assertions.responseTime.mean.lte")
    val globalResponseTimeMeanGTE = config.getInt("performance.global.assertions.responseTime.mean.gte")
    val globalResponseTimeFailedRequestsPercentLTE = config.getDouble("performance.global.assertions.failedRequests.percent.lte")
    val globalResponseTimeFailedRequestsPercentGTE = config.getDouble("performance.global.assertions.failedRequests.percent.gte")
    val globalResponseTimeSuccessfulRequestsPercentLTE = config.getDouble("performance.global.assertions.successfulRequests.percent.lte")
    val globalResponseTimeSuccessfulRequestsPercentGTE = config.getDouble("performance.global.assertions.successfulRequests.percent.gte")

// Setup http protocol configuration
    val httpConf = http
        .baseURL(hostName+"/crestapi")
        .doNotTrackHeader("1")
        .acceptLanguageHeader("en-US,en;q=0.5")
        .acceptEncodingHeader("gzip, deflate")
        .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")
        .acceptHeader(acceptHeader)
        .contentTypeHeader(contentTypeHeader)

    // set authorization header if it has been modified from config
    if(!authentication.equals("~MANUAL_ENTRY")){
        httpConf.authorizationHeader(authentication)
    }

    // Setup all the operations per second for the test to ultimately be generated from configs
    val createIovPerSecond = config.getDouble("performance.operationsPerSecond.createIov") * rateMultiplier * instanceMultiplier
    val findAllIovsPerSecond = config.getDouble("performance.operationsPerSecond.findAllIovs") * rateMultiplier * instanceMultiplier
    val selectGroupsPerSecond = config.getDouble("performance.operationsPerSecond.selectGroups") * rateMultiplier * instanceMultiplier
    val selectIovsPerSecond = config.getDouble("performance.operationsPerSecond.selectIovs") * rateMultiplier * instanceMultiplier
    val selectSnapshotPerSecond = config.getDouble("performance.operationsPerSecond.selectSnapshot") * rateMultiplier * instanceMultiplier

    val scenarioBuilders: mutable.MutableList[PopulationBuilder] = new mutable.MutableList[PopulationBuilder]()

    // Set up CSV feeders
//    val createIovBodyFeeder = csv(userDataDirectory + File.separator + "createIov-bodyParams.csv", escapeChar = '\\').random
//    val findAllIovsQUERYFeeder = csv(userDataDirectory + File.separator + "findAllIovs-queryParams.csv").random
    val selectGroupsQUERYFeeder = csv(userDataDirectory + File.separator + "selectGroups-queryParams.csv").random
    val selectIovsQUERYFeeder = csv(userDataDirectory + File.separator + "selectIovs-queryParams.csv").random
    val selectSnapshotQUERYFeeder = csv(userDataDirectory + File.separator + "selectSnapshot-queryParams.csv").random

    // Setup all scenarios


//     val scncreateIov = scenario("createIovSimulation")
//         .feed(createIovBodyFeeder)
//         .exec(http("createIov")
//         .httpRequest("POST","/iovs")
//         .body(StringBody(IovDto.toStringBody("${insertionTime}","${since}","${tagName}","${payloadHash}")))
//         )
//
//     // Run scncreateIov with warm up and reach a constant rate for entire duration
//     scenarioBuilders += scncreateIov.inject(
//         rampUsersPerSec(1) to(createIovPerSecond) during(rampUpSeconds),
//         constantUsersPerSec(createIovPerSecond) during(durationSeconds),
//         rampUsersPerSec(createIovPerSecond) to(1) during(rampDownSeconds)
//     )
//
//
//     val scnfindAllIovs = scenario("findAllIovsSimulation")
//         .feed(findAllIovsQUERYFeeder)
//         .exec(http("findAllIovs")
//         .httpRequest("GET","/iovs")
//         .queryParam("page","${page}")
//         .queryParam("size","${size}")
//         .queryParam("sort","${sort}")
//         .queryParam("tagname","${tagname}")
// )
//
//     // Run scnfindAllIovs with warm up and reach a constant rate for entire duration
//     scenarioBuilders += scnfindAllIovs.inject(
//         rampUsersPerSec(1) to(findAllIovsPerSecond) during(rampUpSeconds),
//         constantUsersPerSec(findAllIovsPerSecond) during(durationSeconds),
//         rampUsersPerSec(findAllIovsPerSecond) to(1) during(rampDownSeconds)
//     )


    val scnselectGroups = scenario("selectGroupsSimulation")
        .feed(selectGroupsQUERYFeeder)
        .exec(http("selectGroups")
        .httpRequest("GET","/iovs/selectGroups")
        .queryParam("tagname","${tagname}")
        .queryParam("snapshot","${snapshot}")
)

    // Run scnselectGroups with warm up and reach a constant rate for entire duration
    scenarioBuilders += scnselectGroups.inject(
        rampUsersPerSec(1) to(selectGroupsPerSecond) during(rampUpSeconds),
        constantUsersPerSec(selectGroupsPerSecond) during(durationSeconds),
        rampUsersPerSec(selectGroupsPerSecond) to(1) during(rampDownSeconds)
    )


    val scnselectIovs = scenario("selectIovsSimulation")
        .feed(selectIovsQUERYFeeder)
        .exec(http("selectIovs")
        .httpRequest("GET","/iovs/selectIovs")
        .queryParam("snapshot","${snapshot}")
        .queryParam("since","${since}")
        .queryParam("tagname","${tagname}")
        .queryParam("until","${until}")
)

    // Run scnselectIovs with warm up and reach a constant rate for entire duration
    scenarioBuilders += scnselectIovs.inject(
        rampUsersPerSec(1) to(selectIovsPerSecond) during(rampUpSeconds),
        constantUsersPerSec(selectIovsPerSecond) during(durationSeconds),
        rampUsersPerSec(selectIovsPerSecond) to(1) during(rampDownSeconds)
    )


    val scnselectSnapshot = scenario("selectSnapshotSimulation")
        .feed(selectSnapshotQUERYFeeder)
        .exec(http("selectSnapshot")
        .httpRequest("GET","/iovs/selectSnapshot")
        .queryParam("tagname","${tagname}")
        .queryParam("snapshot","${snapshot}")
)

    // Run scnselectSnapshot with warm up and reach a constant rate for entire duration
    scenarioBuilders += scnselectSnapshot.inject(
        rampUsersPerSec(1) to(selectSnapshotPerSecond) during(rampUpSeconds),
        constantUsersPerSec(selectSnapshotPerSecond) during(durationSeconds),
        rampUsersPerSec(selectSnapshotPerSecond) to(1) during(rampDownSeconds)
    )

    setUp(
        scenarioBuilders.toList
    ).protocols(httpConf).assertions(
        global.responseTime.min.lte(globalResponseTimeMinLTE),
        global.responseTime.min.gte(globalResponseTimeMinGTE),
        global.responseTime.max.lte(globalResponseTimeMaxLTE),
        global.responseTime.max.gte(globalResponseTimeMaxGTE),
        global.responseTime.mean.lte(globalResponseTimeMeanLTE),
        global.responseTime.mean.gte(globalResponseTimeMeanGTE),
        global.failedRequests.percent.lte(globalResponseTimeFailedRequestsPercentLTE),
        global.failedRequests.percent.gte(globalResponseTimeFailedRequestsPercentGTE),
        global.successfulRequests.percent.lte(globalResponseTimeSuccessfulRequestsPercentLTE),
        global.successfulRequests.percent.gte(globalResponseTimeSuccessfulRequestsPercentGTE)
    )
}
