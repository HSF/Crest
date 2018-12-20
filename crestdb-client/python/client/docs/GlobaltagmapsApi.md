# crestapi.GlobaltagmapsApi

All URIs are relative to *http://localhost:8080/crest*

Method | HTTP request | Description
------------- | ------------- | -------------
[**create_global_tag_map**](GlobaltagmapsApi.md#create_global_tag_map) | **POST** /globaltagmaps | Create a GlobalTagMap in the database.
[**find_global_tag_map**](GlobaltagmapsApi.md#find_global_tag_map) | **GET** /globaltagmaps/{name} | Find GlobalTagMapDto lists.


# **create_global_tag_map**
> GlobalTagMapDto create_global_tag_map(body)

Create a GlobalTagMap in the database.

This method allows to insert a GlobalTag.Arguments: GlobalTagMapDto should be provided in the body as a JSON file.

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.GlobaltagmapsApi()
body = crestapi.GlobalTagMapDto() # GlobalTagMapDto | A json string that is used to construct a globaltagmapdto object: { globaltagname: xxx, ... }

try:
    # Create a GlobalTagMap in the database.
    api_response = api_instance.create_global_tag_map(body)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling GlobaltagmapsApi->create_global_tag_map: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**GlobalTagMapDto**](GlobalTagMapDto.md)| A json string that is used to construct a globaltagmapdto object: { globaltagname: xxx, ... } | 

### Return type

[**GlobalTagMapDto**](GlobalTagMapDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **find_global_tag_map**
> list[GlobalTagMapDto] find_global_tag_map(name)

Find GlobalTagMapDto lists.

This method search for mappings using the global tag name.

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.GlobaltagmapsApi()
name = 'name_example' # str | 

try:
    # Find GlobalTagMapDto lists.
    api_response = api_instance.find_global_tag_map(name)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling GlobaltagmapsApi->find_global_tag_map: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**|  | 

### Return type

[**list[GlobalTagMapDto]**](GlobalTagMapDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

