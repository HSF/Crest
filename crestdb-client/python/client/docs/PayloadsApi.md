# crestapi.PayloadsApi

All URIs are relative to *http://localhost:8080/crest*

Method | HTTP request | Description
------------- | ------------- | -------------
[**create_payload**](PayloadsApi.md#create_payload) | **POST** /payloads | Create a Payload in the database.
[**create_payload_multi_form**](PayloadsApi.md#create_payload_multi_form) | **POST** /payloads/upload | Create a Payload in the database.
[**get_blob**](PayloadsApi.md#get_blob) | **GET** /payloads/{hash}/data | Finds payload data by hash; the payload object contains the real BLOB.
[**get_payload**](PayloadsApi.md#get_payload) | **GET** /payloads/{hash} | Finds a payload resource associated to the hash.
[**get_payload_meta_info**](PayloadsApi.md#get_payload_meta_info) | **GET** /payloads/{hash}/meta | Finds a payload resource associated to the hash.
[**store_payload_with_iov_multi_form**](PayloadsApi.md#store_payload_with_iov_multi_form) | **POST** /payloads/store | Create a Payload in the database, associated to a given iov since and tag name.


# **create_payload**
> PayloadDto create_payload(body)

Create a Payload in the database.

This method allows to insert a Payload.Arguments: PayloadDto should be provided in the body as a JSON file.

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.PayloadsApi()
body = crestapi.PayloadDto() # PayloadDto | A json string that is used to construct a iovdto object: { name: xxx, ... }

try:
    # Create a Payload in the database.
    api_response = api_instance.create_payload(body)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling PayloadsApi->create_payload: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**PayloadDto**](PayloadDto.md)| A json string that is used to construct a iovdto object: { name: xxx, ... } | 

### Return type

[**PayloadDto**](PayloadDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **create_payload_multi_form**
> PayloadDto create_payload_multi_form(file, payload)

Create a Payload in the database.

This method allows to insert a Payload.Arguments: PayloadDto should be provided in the body as a JSON file.

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.PayloadsApi()
file = '/path/to/file.txt' # file | The file
payload = 'payload_example' # str | Json body for payloaddto

try:
    # Create a Payload in the database.
    api_response = api_instance.create_payload_multi_form(file, payload)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling PayloadsApi->create_payload_multi_form: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **file** | **file**| The file | 
 **payload** | **str**| Json body for payloaddto | 

### Return type

[**PayloadDto**](PayloadDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **get_blob**
> str get_blob(hash)

Finds payload data by hash; the payload object contains the real BLOB.

Select one payload at the time, no regexp searches allowed here

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.PayloadsApi()
hash = 'hash_example' # str | hash of the payload

try:
    # Finds payload data by hash; the payload object contains the real BLOB.
    api_response = api_instance.get_blob(hash)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling PayloadsApi->get_blob: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **hash** | **str**| hash of the payload | 

### Return type

**str**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/octet-stream

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **get_payload**
> str get_payload(hash, format=format)

Finds a payload resource associated to the hash.

This method retrieves a payload resource.Arguments: hash=<hash> the hash of the payload

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.PayloadsApi()
hash = 'hash_example' # str | hash:  the hash of the payload
format = 'BLOB' # str | The format of the output data: BLOB or DTO in JSON format (optional) (default to BLOB)

try:
    # Finds a payload resource associated to the hash.
    api_response = api_instance.get_payload(hash, format=format)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling PayloadsApi->get_payload: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **hash** | **str**| hash:  the hash of the payload | 
 **format** | **str**| The format of the output data: BLOB or DTO in JSON format | [optional] [default to BLOB]

### Return type

**str**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/octet-stream, application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **get_payload_meta_info**
> PayloadDto get_payload_meta_info(hash)

Finds a payload resource associated to the hash.

This method retrieves metadata of the payload resource.Arguments: hash=<hash> the hash of the payload

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.PayloadsApi()
hash = 'hash_example' # str | hash:  the hash of the payload

try:
    # Finds a payload resource associated to the hash.
    api_response = api_instance.get_payload_meta_info(hash)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling PayloadsApi->get_payload_meta_info: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **hash** | **str**| hash:  the hash of the payload | 

### Return type

[**PayloadDto**](PayloadDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **store_payload_with_iov_multi_form**
> HTTPResponse store_payload_with_iov_multi_form(file, tag, since, format=format, endtime=endtime)

Create a Payload in the database, associated to a given iov since and tag name.

This method allows to insert a Payload and an IOV. Arguments: since,tagname,stream,end time.

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.PayloadsApi()
file = '/path/to/file.txt' # file | The payload file as a stream
tag = 'tag_example' # str | The tag name
since = 8.14 # float | The since time
format = 'JSON' # str | The format of the input data (optional) (default to JSON)
endtime = 8.14 # float | The end time to be used for protection at tag level (optional)

try:
    # Create a Payload in the database, associated to a given iov since and tag name.
    api_response = api_instance.store_payload_with_iov_multi_form(file, tag, since, format=format, endtime=endtime)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling PayloadsApi->store_payload_with_iov_multi_form: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **file** | **file**| The payload file as a stream | 
 **tag** | **str**| The tag name | 
 **since** | **float**| The since time | 
 **format** | **str**| The format of the input data | [optional] [default to JSON]
 **endtime** | **float**| The end time to be used for protection at tag level | [optional] 

### Return type

[**HTTPResponse**](HTTPResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

