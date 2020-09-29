
<a name="definitions"></a>
## Definitions

<a name="crestbaseresponse"></a>
### CrestBaseResponse

|Name|Schema|
|---|---|
|**datatype**  <br>*optional*|string|
|**filter**  <br>*optional*|[GenericMap](definitions.md#genericmap)|
|**format**  <br>*required*|string|
|**size**  <br>*optional*|integer (int64)|


<a name="folderdto"></a>
### FolderDto

|Name|Schema|
|---|---|
|**groupRole**  <br>*optional*|string|
|**nodeDescription**  <br>*optional*|string|
|**nodeFullpath**  <br>*optional*|string|
|**nodeName**  <br>*optional*|string|
|**schemaName**  <br>*optional*|string|
|**tagPattern**  <br>*optional*|string|


<a name="foldersetdto"></a>
### FolderSetDto
An FolderSet containing FolderDto objects.

*Polymorphism* : Inheritance  
*Discriminator* : format


|Name|Schema|
|---|---|
|**datatype**  <br>*optional*|string|
|**filter**  <br>*optional*|[GenericMap](definitions.md#genericmap)|
|**format**  <br>*required*|string|
|**resources**  <br>*optional*|< [FolderDto](definitions.md#folderdto) > array|
|**size**  <br>*optional*|integer (int64)|


<a name="genericmap"></a>
### GenericMap
*Type* : < string, string > map


<a name="globaltagdto"></a>
### GlobalTagDto

|Name|Schema|
|---|---|
|**description**  <br>*optional*|string|
|**insertionTime**  <br>*optional*|string (date-time)|
|**insertionTimeMilli**  <br>*optional*|integer (int64)|
|**name**  <br>*optional*|string|
|**release**  <br>*optional*|string|
|**scenario**  <br>*optional*|string|
|**snapshotTime**  <br>*optional*|string (date-time)|
|**snapshotTimeMilli**  <br>*optional*|integer (int64)|
|**type**  <br>*optional*|string|
|**validity**  <br>*optional*|number|
|**workflow**  <br>*optional*|string|


<a name="globaltagmapdto"></a>
### GlobalTagMapDto

|Name|Schema|
|---|---|
|**globalTagName**  <br>*optional*|string|
|**label**  <br>*optional*|string|
|**record**  <br>*optional*|string|
|**tagName**  <br>*optional*|string|


<a name="globaltagmapsetdto"></a>
### GlobalTagMapSetDto
An GlobalTagMapSet containing GlobalTagMapDto objects.

*Polymorphism* : Inheritance  
*Discriminator* : format


|Name|Description|Schema|
|---|---|---|
|**datatype**  <br>*optional*||string|
|**filter**  <br>*optional*||[GenericMap](definitions.md#genericmap)|
|**format**  <br>*optional*|**Default** : `"GlobalTagMapSetDto"`|string|
|**resources**  <br>*optional*||< [GlobalTagMapDto](definitions.md#globaltagmapdto) > array|
|**size**  <br>*optional*||integer (int64)|


<a name="globaltagsetdto"></a>
### GlobalTagSetDto
An GlobalTagSet containing GlobalTagDto objects.

*Polymorphism* : Inheritance  
*Discriminator* : format


|Name|Description|Schema|
|---|---|---|
|**datatype**  <br>*optional*||string|
|**filter**  <br>*optional*||[GenericMap](definitions.md#genericmap)|
|**format**  <br>*optional*|**Default** : `"GlobalTagSetDto"`|string|
|**resources**  <br>*optional*||< [GlobalTagDto](definitions.md#globaltagdto) > array|
|**size**  <br>*optional*||integer (int64)|


<a name="groupdto"></a>
### GroupDto

|Name|Schema|
|---|---|
|**groups**  <br>*optional*|< number > array|


<a name="httpresponse"></a>
### HTTPResponse
general response object that can be used for POST and PUT methods


|Name|Description|Schema|
|---|---|---|
|**action**  <br>*required*|Action performed by the server|string|
|**code**  <br>*required*|Code of the response|integer|
|**id**  <br>*optional*|Can be used to store the ID of the generated object|string|
|**message**  <br>*required*||string|


<a name="iovdto"></a>
### IovDto

|Name|Schema|
|---|---|
|**insertionTime**  <br>*optional*|string (date-time)|
|**payloadHash**  <br>*required*|string|
|**since**  <br>*required*|number|
|**tagName**  <br>*optional*|string|


<a name="iovpayloaddto"></a>
### IovPayloadDto

|Name|Schema|
|---|---|
|**objectType**  <br>*optional*|string|
|**payloadHash**  <br>*optional*|string|
|**since**  <br>*optional*|number|
|**size**  <br>*optional*|integer (int32)|
|**streamerInfo**  <br>*optional*|string|
|**version**  <br>*optional*|string|


<a name="iovpayloadsetdto"></a>
### IovPayloadSetDto
An Set containing IovPayloadDto objects.

*Polymorphism* : Inheritance  
*Discriminator* : format


|Name|Description|Schema|
|---|---|---|
|**datatype**  <br>*optional*||string|
|**filter**  <br>*optional*||[GenericMap](definitions.md#genericmap)|
|**format**  <br>*optional*|**Default** : `"IovPayloadSetDto"`|string|
|**resources**  <br>*optional*||< [IovPayloadDto](definitions.md#iovpayloaddto) > array|
|**size**  <br>*optional*||integer (int64)|


<a name="iovsetdto"></a>
### IovSetDto
An Set containing IovDto objects.

*Polymorphism* : Inheritance  
*Discriminator* : format


|Name|Description|Schema|
|---|---|---|
|**datatype**  <br>*optional*||string|
|**filter**  <br>*optional*||[GenericMap](definitions.md#genericmap)|
|**format**  <br>*optional*|**Default** : `"IovSetDto"`|string|
|**resources**  <br>*optional*||< [IovDto](definitions.md#iovdto) > array|
|**size**  <br>*optional*||integer (int64)|


<a name="payloaddto"></a>
### PayloadDto

|Name|Description|Schema|
|---|---|---|
|**data**  <br>*optional*|**Pattern** : `"^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==\|[A-Za-z0-9+/]{3}=)?$"`|string (byte)|
|**hash**  <br>*optional*||string|
|**insertionTime**  <br>*optional*||string (date-time)|
|**objectType**  <br>*optional*||string|
|**size**  <br>*optional*||integer (int32)|
|**streamerInfo**  <br>*optional*|**Pattern** : `"^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==\|[A-Za-z0-9+/]{3}=)?$"`|string (byte)|
|**version**  <br>*optional*||string|


<a name="payloadsetdto"></a>
### PayloadSetDto
An Set containing PayloadDto objects.

*Polymorphism* : Inheritance  
*Discriminator* : format


|Name|Description|Schema|
|---|---|---|
|**datatype**  <br>*optional*||string|
|**filter**  <br>*optional*||[GenericMap](definitions.md#genericmap)|
|**format**  <br>*optional*|**Default** : `"PayloadSetDto"`|string|
|**resources**  <br>*optional*||< [PayloadDto](definitions.md#payloaddto) > array|
|**size**  <br>*optional*||integer (int64)|


<a name="payloadtaginfodto"></a>
### PayloadTagInfoDto

|Name|Schema|
|---|---|
|**avgvolume**  <br>*optional*|number (float)|
|**niovs**  <br>*optional*|integer|
|**tagname**  <br>*optional*|string|
|**totvolume**  <br>*optional*|number (float)|


<a name="payloadtaginfosetdto"></a>
### PayloadTagInfoSetDto
An PayloadTagInfoSet containing PayloadTagInfoDto objects.

*Polymorphism* : Inheritance  
*Discriminator* : format


|Name|Schema|
|---|---|
|**datatype**  <br>*optional*|string|
|**filter**  <br>*optional*|[GenericMap](definitions.md#genericmap)|
|**format**  <br>*required*|string|
|**resources**  <br>*optional*|< [PayloadTagInfoDto](definitions.md#payloadtaginfodto) > array|
|**size**  <br>*optional*|integer (int64)|


<a name="runinfodto"></a>
### RunInfoDto

|Name|Schema|
|---|---|
|**endTime**  <br>*optional*|string (date-time)|
|**runNumber**  <br>*optional*|number|
|**startTime**  <br>*optional*|string (date-time)|


<a name="runinfosetdto"></a>
### RunInfoSetDto
An RunInfoSet containing RunInfoDto objects.

*Polymorphism* : Inheritance  
*Discriminator* : format


|Name|Description|Schema|
|---|---|---|
|**datatype**  <br>*optional*||string|
|**filter**  <br>*optional*||[GenericMap](definitions.md#genericmap)|
|**format**  <br>*optional*|**Default** : `"RunInfoSetDto"`|string|
|**resources**  <br>*optional*||< [RunInfoDto](definitions.md#runinfodto) > array|
|**size**  <br>*optional*||integer (int64)|


<a name="runlumiinfodto"></a>
### RunLumiInfoDto

|Name|Schema|
|---|---|
|**endtime**  <br>*optional*|number|
|**lb**  <br>*optional*|number|
|**run**  <br>*optional*|number|
|**since**  <br>*optional*|number|
|**starttime**  <br>*optional*|number|


<a name="runlumisetdto"></a>
### RunLumiSetDto
An RunLumiSet containing RunLumiInfoDto objects.

*Polymorphism* : Inheritance  
*Discriminator* : format


|Name|Schema|
|---|---|
|**datatype**  <br>*optional*|string|
|**filter**  <br>*optional*|[GenericMap](definitions.md#genericmap)|
|**format**  <br>*required*|string|
|**resources**  <br>*optional*|< [RunLumiInfoDto](definitions.md#runlumiinfodto) > array|
|**size**  <br>*optional*|integer (int64)|


<a name="tagdto"></a>
### TagDto

|Name|Schema|
|---|---|
|**description**  <br>*optional*|string|
|**endOfValidity**  <br>*optional*|number|
|**insertionTime**  <br>*optional*|string (date-time)|
|**lastValidatedTime**  <br>*optional*|number|
|**modificationTime**  <br>*optional*|string (date-time)|
|**name**  <br>*optional*|string|
|**payloadSpec**  <br>*optional*|string|
|**synchronization**  <br>*optional*|string|
|**timeType**  <br>*optional*|string|


<a name="tagsetdto"></a>
### TagSetDto
An Set containing TagDto objects.

*Polymorphism* : Inheritance  
*Discriminator* : format


|Name|Description|Schema|
|---|---|---|
|**datatype**  <br>*optional*||string|
|**filter**  <br>*optional*||[GenericMap](definitions.md#genericmap)|
|**format**  <br>*optional*|**Default** : `"TagSetDto"`|string|
|**resources**  <br>*optional*||< [TagDto](definitions.md#tagdto) > array|
|**size**  <br>*optional*||integer (int64)|


<a name="tagsummarydto"></a>
### TagSummaryDto

|Name|Schema|
|---|---|
|**niovs**  <br>*optional*|integer (int64)|
|**tagname**  <br>*optional*|string|


<a name="tagsummarysetdto"></a>
### TagSummarySetDto
An Set containing TagSummaryDto objects.

*Polymorphism* : Inheritance  
*Discriminator* : format


|Name|Description|Schema|
|---|---|---|
|**datatype**  <br>*optional*||string|
|**filter**  <br>*optional*||[GenericMap](definitions.md#genericmap)|
|**format**  <br>*optional*|**Default** : `"TagSummarySetDto"`|string|
|**resources**  <br>*optional*||< [TagSummaryDto](definitions.md#tagsummarydto) > array|
|**size**  <br>*optional*||integer (int64)|



