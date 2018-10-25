package io.swagger.client.api

import io.swagger.client.model._
import com.typesafe.config.ConfigFactory

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.structure.PopulationBuilder

import java.io.File
import java.util.concurrent.TimeUnit

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

class TagsApiSimulation extends Simulation {

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
//        .baseURL("http://localhost:8080/crest")
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
    val createTagPerSecond = config.getDouble("performance.operationsPerSecond.createTag") * rateMultiplier * instanceMultiplier
    val findTagPerSecond = config.getDouble("performance.operationsPerSecond.findTag") * rateMultiplier * instanceMultiplier
    val listTagsPerSecond = config.getDouble("performance.operationsPerSecond.listTags") * rateMultiplier * instanceMultiplier
    val updateTagPerSecond = config.getDouble("performance.operationsPerSecond.updateTag") * rateMultiplier * instanceMultiplier

    val scenarioBuilders: mutable.MutableList[PopulationBuilder] = new mutable.MutableList[PopulationBuilder]()

    // Set up CSV feeders
    //val createTagBodyFeeder = csv(userDataDirectory + File.separator + "createTag-bodyParams.csv", escapeChar = '\\').random
    val findTagPATHFeeder = csv(userDataDirectory + File.separator + "findTag-pathParams.csv").random
    val listTagsQUERYFeeder = csv(userDataDirectory + File.separator + "listTags-queryParams.csv").random
    //val updateTagPATHFeeder = csv(userDataDirectory + File.separator + "updateTag-pathParams.csv").random
    //val updateTagBodyFeeder = csv(userDataDirectory + File.separator + "updateTag-bodyParams.csv", escapeChar = '\\').random

    // Setup all scenarios


    // val scncreateTag = scenario("createTagSimulation")
    //     .feed(createTagBodyFeeder)
    //     .exec(http("createTag")
    //     .httpRequest("POST","/tags")
    //     .body(StringBody(TagDto.toStringBody("${endOfValidity}","${insertionTime}","${timeType}","${description}","${name}","${objectType}","${modificationTime}","${lastValidatedTime}","${synchronization}")))
    //     )
    //
    // // Run scncreateTag with warm up and reach a constant rate for entire duration
    // scenarioBuilders += scncreateTag.inject(
    //     rampUsersPerSec(1) to(createTagPerSecond) during(rampUpSeconds),
    //     constantUsersPerSec(createTagPerSecond) during(durationSeconds),
    //     rampUsersPerSec(createTagPerSecond) to(1) during(rampDownSeconds)
    // )


    val scnfindTag = scenario("findTagSimulation")
        .feed(findTagPATHFeeder)
        .exec(http("findTag")
        .httpRequest("GET","/tags/${name}")
)

    // Run scnfindTag with warm up and reach a constant rate for entire duration
    scenarioBuilders += scnfindTag.inject(
        rampUsersPerSec(1) to(findTagPerSecond) during(rampUpSeconds),
        constantUsersPerSec(findTagPerSecond) during(durationSeconds),
        rampUsersPerSec(findTagPerSecond) to(1) during(rampDownSeconds)
    )


    val scnlistTags = scenario("listTagsSimulation")
        .feed(listTagsQUERYFeeder)
        .exec(http("listTags")
        .httpRequest("GET","/tags")
        .queryParam("page","${page}")
        .queryParam("size","${size}")
        .queryParam("by","${by}")
        .queryParam("sort","${sort}")
)

    // Run scnlistTags with warm up and reach a constant rate for entire duration
    scenarioBuilders += scnlistTags.inject(
        rampUsersPerSec(1) to(listTagsPerSecond) during(rampUpSeconds),
        constantUsersPerSec(listTagsPerSecond) during(durationSeconds),
        rampUsersPerSec(listTagsPerSecond) to(1) during(rampDownSeconds)
    )


    // val scnupdateTag = scenario("updateTagSimulation")
    //     .feed(updateTagBodyFeeder)
    //     .feed(updateTagPATHFeeder)
    //     .exec(http("updateTag")
    //     .httpRequest("POST","/tags/${name}")
    //     .body(StringBody(GenericMap.toStringBody("${name}")))
    //     )
    //
    // // Run scnupdateTag with warm up and reach a constant rate for entire duration
    // scenarioBuilders += scnupdateTag.inject(
    //     rampUsersPerSec(1) to(updateTagPerSecond) during(rampUpSeconds),
    //     constantUsersPerSec(updateTagPerSecond) during(durationSeconds),
    //     rampUsersPerSec(updateTagPerSecond) to(1) during(rampDownSeconds)
    // )

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
