
<a name="paths"></a>
## Resources

<a name="admin_resource"></a>
### Admin

<a name="updateglobaltag"></a>
#### Update a GlobalTag in the database.
```
PUT /admin/globaltags/{name}
```


##### Description
This method allows to update a GlobalTag.Arguments: the name has to uniquely identify a global tag.


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Path**|**name**  <br>*required*||string|
|**Body**|**body**  <br>*required*|A json string that is used to construct a GlobalTagDto object: { name: xxx, ... }|[GlobalTagDto](definitions.md#globaltagdto)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[GlobalTagDto](definitions.md#globaltagdto)|


##### Consumes

* `application/json`


##### Produces

* `application/json`


<a name="removeglobaltag"></a>
#### Remove a GlobalTag from the database.
```
DELETE /admin/globaltags/{name}
```


##### Description
This method allows to remove a GlobalTag.Arguments: the name has to uniquely identify a global tag.


##### Parameters

|Type|Name|Schema|
|---|---|---|
|**Path**|**name**  <br>*required*|string|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**default**|successful operation|No Content|


##### Produces

* `application/json`


<a name="removetag"></a>
#### Remove a Tag from the database.
```
DELETE /admin/tags/{name}
```


##### Description
This method allows to remove a Tag.Arguments: the name has to uniquely identify a tag.


##### Parameters

|Type|Name|Schema|
|---|---|---|
|**Path**|**name**  <br>*required*|string|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**default**|successful operation|No Content|


##### Produces

* `application/json`


<a name="folders_resource"></a>
### Folders

<a name="createfolder"></a>
#### Create an entry for folder information.
```
POST /folders
```


##### Description
Folder informations go into a dedicated table.


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**body**  <br>*required*|A json string that is used to construct a folderdto object: { node: xxx, ... }|[FolderDto](definitions.md#folderdto)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|string|


##### Consumes

* `application/json`


##### Produces

* `application/json`
* `text/plain`


<a name="listfolders"></a>
#### Finds a FolderDto list.
```
GET /folders
```


##### Description
This method allows to perform search and sorting.Arguments: by=<pattern>, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**by**  <br>*optional*|by: the search pattern {none}|string|`"none"`|
|**Query**|**sort**  <br>*optional*|sort: the sort pattern {nodeFullpath:ASC}|string|`"nodeFullpath:ASC"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[FolderSetDto](definitions.md#foldersetdto)|


##### Produces

* `application/json`
* `application/xml`


<a name="fs_resource"></a>
### Fs

<a name="buildtar"></a>
#### Dump a tag into filesystem and retrieve the tar file asynchronously.
```
POST /fs/tar
```


##### Description
This method allows to request a tar file from the server using a tag specified in input.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**snapshot**  <br>*required*|snapshot: the snapshot time {0}|integer (int64)|`0`|
|**Query**|**tagname**  <br>*required*|tagname: the tag name {none}|string|`"none"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|string|


##### Consumes

* `application/json`


##### Produces

* `application/json`
* `text/plain`


<a name="globaltagmaps_resource"></a>
### Globaltagmaps

<a name="createglobaltagmap"></a>
#### Create a GlobalTagMap in the database.
```
POST /globaltagmaps
```


##### Description
This method allows to insert a GlobalTag.Arguments: GlobalTagMapDto should be provided in the body as a JSON file.


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**body**  <br>*required*|A json string that is used to construct a globaltagmapdto object: { globaltagname: xxx, ... }|[GlobalTagMapDto](definitions.md#globaltagmapdto)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**201**|successful operation|[GlobalTagMapDto](definitions.md#globaltagmapdto)|


##### Consumes

* `application/json`


##### Produces

* `application/json`


<a name="findglobaltagmap"></a>
#### Find GlobalTagMapDto lists.
```
GET /globaltagmaps/{name}
```


##### Description
This method search for mappings using the global tag name.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Header**|**X-Crest-MapMode**  <br>*optional*|If the mode is BackTrace then it will search for global tags containing the tag <name>|string|`"Trace"`|
|**Path**|**name**  <br>*required*||string||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[GlobalTagMapSetDto](definitions.md#globaltagmapsetdto)|


##### Produces

* `application/json`
* `application/xml`


<a name="deleteglobaltagmap"></a>
#### Delete GlobalTagMapDto lists.
```
DELETE /globaltagmaps/{name}
```


##### Description
This method search for mappings using the global tag name and deletes all mappings.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**name**  <br>*required*|the global tag name|string||
|**Query**|**label**  <br>*required*|label: the generic name labelling all tags of a certain kind.|string|`"none"`|
|**Query**|**record**  <br>*optional*|record: the record.|string||
|**Query**|**tagname**  <br>*required*|tagname: the name of the tag associated.|string|`"none"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[GlobalTagMapSetDto](definitions.md#globaltagmapsetdto)|


##### Produces

* `application/json`
* `application/xml`


<a name="globaltags_resource"></a>
### Globaltags

<a name="createglobaltag"></a>
#### Create a GlobalTag in the database.
```
POST /globaltags
```


##### Description
This method allows to insert a GlobalTag.Arguments: GlobalTagDto should be provided in the body as a JSON file.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**force**  <br>*optional*|force: tell the server if it should use or not the insertion time provided {default: false}|string|`"false"`|
|**Body**|**body**  <br>*required*|A json string that is used to construct a globaltagdto object: { name: xxx, ... }|[GlobalTagDto](definitions.md#globaltagdto)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**201**|successful operation|[GlobalTagDto](definitions.md#globaltagdto)|


##### Consumes

* `application/json`


##### Produces

* `application/json`


<a name="listglobaltags"></a>
#### Finds a GlobalTagDtos lists.
```
GET /globaltags
```


##### Description
This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**by**  <br>*optional*|by: the search pattern {none}|string|`"none"`|
|**Query**|**page**  <br>*optional*|page: the page number {0}|integer (int32)|`0`|
|**Query**|**size**  <br>*optional*|size: the page size {1000}|integer (int32)|`1000`|
|**Query**|**sort**  <br>*optional*|sort: the sort pattern {name:ASC}|string|`"name:ASC"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[GlobalTagSetDto](definitions.md#globaltagsetdto)|


##### Produces

* `application/json`
* `application/xml`


<a name="findglobaltag"></a>
#### Finds a GlobalTagDto by name
```
GET /globaltags/{name}
```


##### Description
This method will search for a global tag with the given name. Only one global tag should be returned.


##### Parameters

|Type|Name|Schema|
|---|---|---|
|**Path**|**name**  <br>*required*|string|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[GlobalTagSetDto](definitions.md#globaltagsetdto)|


##### Produces

* `application/json`
* `application/xml`


<a name="findglobaltagfetchtags"></a>
#### Finds a TagDtos lists associated to the global tag name in input.
```
GET /globaltags/{name}/tags
```


##### Description
This method allows to trace a global tag.Arguments: record=<record> filter output by record, label=<label> filter output by label


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**name**  <br>*required*||string||
|**Query**|**label**  <br>*optional*|label:  the label string {}|string|`"none"`|
|**Query**|**record**  <br>*optional*|record:  the record string {}|string|`"none"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[TagSetDto](definitions.md#tagsetdto)|


##### Produces

* `application/json`
* `application/xml`


<a name="iovs_resource"></a>
### Iovs

<a name="createiov"></a>
#### Create a Iov in the database.
```
POST /iovs
```


##### Description
This method allows to insert a Iov.Arguments: IovDto should be provided in the body as a JSON file.


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**body**  <br>*required*|A json string that is used to construct a iovdto object: { name: xxx, ... }|[IovDto](definitions.md#iovdto)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**201**|successful operation|[IovDto](definitions.md#iovdto)|


##### Consumes

* `application/json`


##### Produces

* `application/json`


<a name="findalliovs"></a>
#### Finds a IovDtos lists.
```
GET /iovs
```


##### Description
This method allows to perform search by tagname and sorting.Arguments: tagname={a tag name}, page={ipage}, size={isize},      sort=<pattern>, where pattern is <field>:[DESC|ASC]


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Header**|**dateformat**  <br>*optional*|The format of the input time fields: {yyyyMMdd'T'HHmmssX \| ms} DEFAULT: ms (so it is a long). Used for insertionTime comparaison.|string|`"ms"`|
|**Query**|**by**  <br>*required*|you need a mandatory tagname:xxxx. Additional field can be since or insertionTime rules.|string|`"none"`|
|**Query**|**page**  <br>*optional*|page: the page number {0}|integer (int32)|`0`|
|**Query**|**size**  <br>*optional*|size: the page size {10000}|integer (int32)|`10000`|
|**Query**|**sort**  <br>*optional*|sort: the sort pattern {id.since:ASC}|string|`"id.since:ASC"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[IovSetDto](definitions.md#iovsetdto)|


##### Produces

* `application/json`


<a name="getsize"></a>
#### Get the number o iovs for the given tag.
```
GET /iovs/getSize
```


##### Description
This method allows to select the count of iovs in a tag. Also possible to get the size of snapshot, if the time added.Arguments: tagname={a tag name}, snapshotTime={snapshot time in milliseconds (Long) from epoch}


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**snapshot**  <br>*optional*|snapshot: the snapshot time {0}|integer (int64)|`0`|
|**Query**|**tagname**  <br>*required*|tagname: the tag name {none}|string|`"none"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[CrestBaseResponse](definitions.md#crestbaseresponse)|


##### Produces

* `application/json`


<a name="getsizebytag"></a>
#### Get the number o iovs for tags matching pattern.
```
GET /iovs/getSizeByTag
```


##### Description
This method allows to select the count of iovs in a tag. Also possible to get the size of snapshot, if the time is added. Arguments: tagname={a tag name}


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**tagname**  <br>*required*|tagname: the tag name {none}|string|`"none"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[TagSummarySetDto](definitions.md#tagsummarysetdto)|


##### Produces

* `application/json`


<a name="lastiov"></a>
#### Select last iov for a given tagname and before a given since.
```
GET /iovs/lastIov
```


##### Description
This method allows to select the last iov in a tag, before a given time and (optionally) for a given snapshot time.Arguments: tagname={a tag name}, since={since time as string}, snapshot={snapshot time as long}


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Header**|**dateformat**  <br>*optional*|The format of the input time fields: {yyyyMMdd'T'HHmmssX \| ms} DEFAULT: ms (so it is a long). Used for insertionTime comparaison.|string|`"ms"`|
|**Query**|**since**  <br>*optional*|since: the since time|string|`"now"`|
|**Query**|**snapshot**  <br>*optional*|snapshot: the snapshot time {0}|integer (int64)|`0`|
|**Query**|**tagname**  <br>*optional*|tagname: the tag name {none}|string|`"none"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[IovSetDto](definitions.md#iovsetdto)|


##### Produces

* `application/json`


<a name="selectgroups"></a>
#### Select groups for a given tagname.
```
GET /iovs/selectGroups
```


##### Description
This method allows to select a list of groups.Arguments: tagname={a tag name}, snapshot={snapshot time as long}


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**snapshot**  <br>*optional*|snapshot: the snapshot time {0}|integer (int64)|`0`|
|**Query**|**tagname**  <br>*required*|tagname: the tag name {none}|string|`"none"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[IovSetDto](definitions.md#iovsetdto)|


##### Produces

* `application/json`


<a name="selectiovpayloads"></a>
#### Select iovs and payload meta info for a given tagname and in a given range.
```
GET /iovs/selectIovPayloads
```


##### Description
This method allows to select a list of iovs+payload meta in a tag, using a given range in time and (optionally) for a given snapshot time.Arguments: tagname={a tag name}, since={since time as string}, until={until time as string}, snapshot={snapshot time as long}


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Header**|**X-Crest-Query**  <br>*optional*|The query type. The header parameter X-Crest-Query can be : groups (default) or ranges (include previous since).|string|`"groups"`|
|**Query**|**since**  <br>*optional*|since: the since time as a string {0}|string|`"0"`|
|**Query**|**snapshot**  <br>*optional*|snapshot: the snapshot time {0}|integer (int64)|`0`|
|**Query**|**tagname**  <br>*optional*|tagname: the tag name {none}|string|`"none"`|
|**Query**|**until**  <br>*optional*|until: the until time as a string {INF}|string|`"INF"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[IovPayloadSetDto](definitions.md#iovpayloadsetdto)|


##### Produces

* `application/json`


<a name="selectiovs"></a>
#### Select iovs for a given tagname and in a given range.
```
GET /iovs/selectIovs
```


##### Description
This method allows to select a list of iovs in a tag, using a given range in time and (optionally) for a given snapshot time.Arguments: tagname={a tag name}, since={since time as string}, until={until time as string}, snapshot={snapshot time as long}


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Header**|**X-Crest-Query**  <br>*optional*|The query type. The header parameter X-Crest-Query can be : groups (default) or ranges (include previous since).|string|`"groups"`|
|**Query**|**since**  <br>*optional*|since: the since time as a string {0}|string|`"0"`|
|**Query**|**snapshot**  <br>*optional*|snapshot: the snapshot time {0}|integer (int64)|`0`|
|**Query**|**tagname**  <br>*optional*|tagname: the tag name {none}|string|`"none"`|
|**Query**|**until**  <br>*optional*|until: the until time as a string {INF}|string|`"INF"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[IovSetDto](definitions.md#iovsetdto)|


##### Produces

* `application/json`


<a name="selectsnapshot"></a>
#### Select snapshot for a given tagname and insertion time.
```
GET /iovs/selectSnapshot
```


##### Description
This method allows to select a list of all iovs in a tag, using (optionally) a given snapshot time.Arguments: tagname={a tag name}, snapshot={snapshot time as long}


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**snapshot**  <br>*required*|snapshot: the snapshot time {0}|integer (int64)|`0`|
|**Query**|**tagname**  <br>*required*|tagname: the tag name {none}|string|`"none"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[IovSetDto](definitions.md#iovsetdto)|


##### Produces

* `application/json`


<a name="storebatchiovmultiform"></a>
#### Create many IOVs in the database, associated to a tag name.
```
POST /iovs/storebatch
```


##### Description
This method allows to insert multiple IOVs. Arguments: tagname,end time.


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**body**  <br>*required*|A json string that is used to construct a IovSetDto object.|[IovSetDto](definitions.md#iovsetdto)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**201**|successful operation|[IovSetDto](definitions.md#iovsetdto)|


##### Consumes

* `application/json`


##### Produces

* `application/json`


<a name="monitoring_resource"></a>
### Monitoring

<a name="listpayloadtaginfo"></a>
#### Retrieves monitoring information on payload as a list of PayloadTagInfoDtos.
```
GET /monitoring/payloads
```


##### Description
This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**tagname**  <br>*optional*|tagname: the search pattern {none}|string|`"none"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[PayloadTagInfoSetDto](definitions.md#payloadtaginfosetdto)|


##### Produces

* `application/json`
* `application/xml`


<a name="payloads_resource"></a>
### Payloads

<a name="createpayload"></a>
#### Create a Payload in the database.
```
POST /payloads
```


##### Description
This method allows to insert a Payload.Arguments: PayloadDto should be provided in the body as a JSON file.


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**body**  <br>*required*|A json string that is used to construct a iovdto object: { name: xxx, ... }|[PayloadDto](definitions.md#payloaddto)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[PayloadDto](definitions.md#payloaddto)|


##### Consumes

* `application/json`


##### Produces

* `application/json`


<a name="storepayloadwithiovmultiform"></a>
#### Create a Payload in the database, associated to a given iov since and tag name.
```
POST /payloads/store
```


##### Description
This method allows to insert a Payload and an IOV. Arguments: since,tagname,stream,end time. The header parameter X-Crest-PayloadFormat can be : JSON (default) or TXT or BLOB


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Header**|**X-Crest-PayloadFormat**  <br>*optional*|The format of the input data|string|`"JSON"`|
|**FormData**|**endtime**  <br>*optional*|The end time to be used for protection at tag level|number||
|**FormData**|**file**  <br>*required*|The payload file as a stream|file||
|**FormData**|**objectType**  <br>*optional*|The payload objectType|string||
|**FormData**|**since**  <br>*required*|The since time|number||
|**FormData**|**tag**  <br>*required*|The tag name|string||
|**FormData**|**version**  <br>*optional*|The version|string||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[HTTPResponse](definitions.md#httpresponse)|


##### Consumes

* `multipart/form-data`


##### Produces

* `application/json`


<a name="storepayloadbatchwithiovmultiform"></a>
#### Create many Payloads in the database, associated to a given iov since list and tag name.
```
POST /payloads/storebatch
```


##### Description
This method allows to insert a Payload and an IOV. Arguments: tagname,stream,end time. The header parameter X-Crest-PayloadFormat can be : JSON (default) or TXT or BLOB


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Header**|**X-Crest-PayloadFormat**  <br>*optional*|The format of the input data|string|`"JSON"`|
|**FormData**|**endtime**  <br>*optional*|The end time to be used for protection at tag level|number||
|**FormData**|**iovsetupload**  <br>*required*||string||
|**FormData**|**tag**  <br>*required*|The tag name|string||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**201**|successful operation|[IovSetDto](definitions.md#iovsetdto)|


##### Consumes

* `multipart/form-data`


##### Produces

* `application/json`


<a name="createpayloadmultiform"></a>
#### Create a Payload in the database.
```
POST /payloads/upload
```


##### Description
This method allows to insert a Payload.Arguments: PayloadDto should be provided in the body as a JSON file.


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**FormData**|**file**  <br>*required*|The file|file|
|**FormData**|**payload**  <br>*required*|Json body for payloaddto|string|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[PayloadDto](definitions.md#payloaddto)|


##### Consumes

* `multipart/form-data`


##### Produces

* `application/json`


<a name="uploadpayloadbatchwithiovmultiform"></a>
#### Create many Payloads in the database, associated to a given iov since list and tag name.
```
POST /payloads/uploadbatch
```


##### Description
This method allows to insert a Payload and an IOV. Arguments: tagname,stream,end time. The header parameter X-Crest-PayloadFormat can be : JSON (default) or TXT or BLOB


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Header**|**X-Crest-PayloadFormat**  <br>*optional*|The format of the input data|string|`"FILE"`|
|**FormData**|**endtime**  <br>*optional*|The end time to be used for protection at tag level|number||
|**FormData**|**files**  <br>*required*|The files to upload|file||
|**FormData**|**iovsetupload**  <br>*required*||string||
|**FormData**|**tag**  <br>*required*|The tag name|string||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**201**|successful operation|[IovSetDto](definitions.md#iovsetdto)|


##### Consumes

* `multipart/form-data`


##### Produces

* `application/json`


<a name="getpayload"></a>
#### Finds a payload resource associated to the hash.
```
GET /payloads/{hash}
```


##### Description
This method retrieves a payload resource.Arguments: hash=<hash> the hash of the payload


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Header**|**X-Crest-PayloadFormat**  <br>*optional*|The format of the output data. The header parameter X-Crest-PayloadFormat can be : BLOB (default) or DTO (in JSON format).|string|`"BLOB"`|
|**Path**|**hash**  <br>*required*|hash:  the hash of the payload|string||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|string|


##### Produces

* `application/octet-stream`
* `application/json`
* `application/xml`


<a name="getpayloadmetainfo"></a>
#### Finds a payload resource associated to the hash.
```
GET /payloads/{hash}/meta
```


##### Description
This method retrieves metadata of the payload resource.Arguments: hash=<hash> the hash of the payload


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Path**|**hash**  <br>*required*|hash:  the hash of the payload|string|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[PayloadSetDto](definitions.md#payloadsetdto)|


##### Produces

* `application/json`
* `application/xml`


<a name="runinfo_resource"></a>
### Runinfo

<a name="createruninfo"></a>
#### Create an entry for run information.
```
POST /runinfo
```


##### Description
Run informations go into a separate table.


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**body**  <br>*required*|A json string that is used to construct one or more runinfodto object: { run: xxx, ... }|[RunInfoSetDto](definitions.md#runinfosetdto)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|string|


##### Consumes

* `application/json`


##### Produces

* `application/json`
* `text/plain`


<a name="listruninfo"></a>
#### Finds a RunLumiInfoDto lists.
```
GET /runinfo
```


##### Description
This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**by**  <br>*optional*|by: the search pattern {none}|string|`"none"`|
|**Query**|**page**  <br>*optional*|page: the page number {0}|integer (int32)|`0`|
|**Query**|**size**  <br>*optional*|size: the page size {1000}|integer (int32)|`1000`|
|**Query**|**sort**  <br>*optional*|sort: the sort pattern {runNumber:ASC}|string|`"runNumber:ASC"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[RunInfoSetDto](definitions.md#runinfosetdto)|


##### Produces

* `application/json`
* `application/xml`


<a name="selectruninfo"></a>
#### Finds a RunLumiInfoDto lists using parameters.
```
GET /runinfo/select
```


##### Description
This method allows to perform search.Arguments: from=<someformat>,to=<someformat>, format=<describe previous types>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**format**  <br>*optional*|format: the format to digest previous arguments [iso], [number]. Time(iso) = yyyymmddhhmiss, Run(number) = runnumber, Time(number) = milliseconds|string|`"number"`|
|**Query**|**from**  <br>*optional*|from: the starting time or run|string|`"none"`|
|**Query**|**mode**  <br>*optional*|mode: the mode for the request : [daterange] or [runrange]. Interprets|string|`"runrange"`|
|**Query**|**to**  <br>*optional*|to: the ending time or run|string|`"none"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[RunInfoSetDto](definitions.md#runinfosetdto)|


##### Produces

* `application/json`
* `application/xml`


<a name="tags_resource"></a>
### Tags

<a name="createtag"></a>
#### Create a Tag in the database.
```
POST /tags
```


##### Description
This method allows to insert a Tag.Arguments: TagDto should be provided in the body as a JSON file.


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**body**  <br>*required*|A json string that is used to construct a tagdto object: { name: xxx, ... }|[TagDto](definitions.md#tagdto)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[TagDto](definitions.md#tagdto)|


##### Consumes

* `application/json`


##### Produces

* `application/json`


<a name="listtags"></a>
#### Finds a TagDtos lists.
```
GET /tags
```


##### Description
This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**by**  <br>*optional*|by: the search pattern {none}|string|`"none"`|
|**Query**|**page**  <br>*optional*|page: the page number {0}|integer (int32)|`0`|
|**Query**|**size**  <br>*optional*|size: the page size {1000}|integer (int32)|`1000`|
|**Query**|**sort**  <br>*optional*|sort: the sort pattern {name:ASC}|string|`"name:ASC"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[TagSetDto](definitions.md#tagsetdto)|


##### Produces

* `application/json`
* `application/xml`


<a name="findtag"></a>
#### Finds a TagDto by name
```
GET /tags/{name}
```


##### Description
This method will search for a tag with the given name. Only one tag should be returned.


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Path**|**name**  <br>*required*|name: the tag name|string|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[TagSetDto](definitions.md#tagsetdto)|


##### Produces

* `application/json`
* `application/xml`


<a name="updatetag"></a>
#### Update a TagDto by name
```
PUT /tags/{name}
```


##### Description
This method will search for a tag with the given name, and update its content for the provided body fields. Only the following fields can be updated: description, timeType, objectTime, endOfValidity, lastValidatedTime.


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Path**|**name**  <br>*required*|name: the tag name|string|
|**Body**|**body**  <br>*required*|A json string that is used to construct a map of updatable fields: { description: xxx, ... }|[GenericMap](definitions.md#genericmap)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[TagDto](definitions.md#tagdto)|


##### Produces

* `application/json`
* `application/xml`



