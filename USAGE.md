#### Author: A.Formica
# Table of Contents
1. [Introduction](#introduction)
2. [Tagging](#tagging)
3. [IoV](#iov)
4. [Payload](#payload)
5. [Global Tag](#global-tag)
6. [Global Tag Map](#global-tag-map)
7. [Examples](#examples)

## Introduction
The main concepts behind *CREST* are the following:
 * Tags: defined by a name, a payload type and time type. The time type indicates what is used in IoV.
 * IoV: defined by a tag, a start time and payload hash (sha256 in general). The start time indicates the beginning of the validity of the payload.
 * Payload: defined by a hash (sha256 in general) and a content. The content is a binary blob.
 * Global Tag: a container of Tags, defined by a name.
 * Global Tag Map: a collection of Tags to Global Tag mappings. The mapping is defined by a tag name, a record and a label.

## Tagging
 * create a tag: `POST /tags`
 * get a tag: `GET /tags/{name}`
 * get all tags: `GET /tags` (paginated), use several parameters to filter the results.

## IoV
 * create an iov: `POST /iovs`, a list of iovs can be created at once.
 * get all iovs: `GET /iovs` (paginated), use several parameters to filter the results.

## Payload
 * create a payload: `POST /payloads`
 * get a payload: `GET /payloads/{hash}`

## Global Tag
 * create a global tag: `POST /globaltags`
 * get a global tag: `GET /globaltags/{name}`
 * get all global tags: `GET /globaltags` (paginated), use several parameters to filter the results.

## Global Tag Map
 * create a global tag map: `POST /globaltagmaps`

## Examples
A user needs to create a tag for a payload of type `mytype` and time type `run-lumi`. 
The user creates the tag with the following command:
```
curl -X POST "http://localhost:8080/tags" -H "accept: application/json" -H "Content-Type: application/json" -d "{ \"name\": \"mytag\", \"payloadType\": \"mytype\", \"timeType\": \"run-lumi\"}"
```
The user creates a list of payloads inside a tag, providing the iov associated with the following command:
```
curl -X POST "http://localhost:8080/payloads" -H "accept: application/json" -H "Content-Type: application/json" -d ""
```
The user creates a global tag with the following command:
```
curl -X POST "http://localhost:8080/globaltags" -H "accept: application/json" -H "Content-Type: application/json" -d "{ \"name\": \"myglobaltag\", \"tags\": [ { \"name\": \"mytag\", \"record\": \"myrecord\", \"label\": \"mylabel\" } ]}"
```
The user creates a global tag map with the following command:
```
curl -X POST "http://localhost:8080/globaltagmaps" -H "accept: application/json" -H "Content-Type: application/json" -d "{ \"globalTagName\": \"myglobaltag\", \"tagName\": \"mytag\", \"record\": \"myrecord\", \"label\": \"mylabel\"}"
```
The user gets the global tag with the following command:
```
curl -X GET "http://localhost:8080/globaltags/myglobaltag" -H "accept: application/json"
```
A user can copy a payload from another tag to a new tag with the following command:
1. get the iov corresponding to the payload from the old tag.
2. insert the iov in the new tag, after changing eventually the start time (since).
