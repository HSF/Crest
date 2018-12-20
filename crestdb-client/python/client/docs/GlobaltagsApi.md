# crestapi.GlobaltagsApi

All URIs are relative to *http://localhost:8080/crest*

Method | HTTP request | Description
------------- | ------------- | -------------
[**create_global_tag**](GlobaltagsApi.md#create_global_tag) | **POST** /globaltags | Create a GlobalTag in the database.
[**find_global_tag**](GlobaltagsApi.md#find_global_tag) | **GET** /globaltags/{name} | Finds a GlobalTagDto by name
[**find_global_tag_fetch_tags**](GlobaltagsApi.md#find_global_tag_fetch_tags) | **GET** /globaltags/{name}/tags | Finds a TagDtos lists associated to the global tag name in input.
[**list_global_tags**](GlobaltagsApi.md#list_global_tags) | **GET** /globaltags | Finds a GlobalTagDtos lists.


# **create_global_tag**
> GlobalTagDto create_global_tag(body, force=force)

Create a GlobalTag in the database.

This method allows to insert a GlobalTag.Arguments: GlobalTagDto should be provided in the body as a JSON file.

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.GlobaltagsApi()
body = crestapi.GlobalTagDto() # GlobalTagDto | A json string that is used to construct a globaltagdto object: { name: xxx, ... }
force = 'false' # str | force: tell the server if it should use or not the insertion time provided {default: false} (optional) (default to false)

try:
    # Create a GlobalTag in the database.
    api_response = api_instance.create_global_tag(body, force=force)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling GlobaltagsApi->create_global_tag: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**GlobalTagDto**](GlobalTagDto.md)| A json string that is used to construct a globaltagdto object: { name: xxx, ... } | 
 **force** | **str**| force: tell the server if it should use or not the insertion time provided {default: false} | [optional] [default to false]

### Return type

[**GlobalTagDto**](GlobalTagDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **find_global_tag**
> GlobalTagDto find_global_tag(name)

Finds a GlobalTagDto by name

This method will search for a global tag with the given name. Only one global tag should be returned.

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.GlobaltagsApi()
name = 'name_example' # str | 

try:
    # Finds a GlobalTagDto by name
    api_response = api_instance.find_global_tag(name)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling GlobaltagsApi->find_global_tag: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**|  | 

### Return type

[**GlobalTagDto**](GlobalTagDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **find_global_tag_fetch_tags**
> list[TagDto] find_global_tag_fetch_tags(name, record=record, label=label)

Finds a TagDtos lists associated to the global tag name in input.

This method allows to trace a global tag.Arguments: record=<record> filter output by record, label=<label> filter output by label

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.GlobaltagsApi()
name = 'name_example' # str | 
record = 'record_example' # str | record:  the record string {} (optional)
label = 'label_example' # str | label:  the label string {} (optional)

try:
    # Finds a TagDtos lists associated to the global tag name in input.
    api_response = api_instance.find_global_tag_fetch_tags(name, record=record, label=label)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling GlobaltagsApi->find_global_tag_fetch_tags: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**|  | 
 **record** | **str**| record:  the record string {} | [optional] 
 **label** | **str**| label:  the label string {} | [optional] 

### Return type

[**list[TagDto]**](TagDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **list_global_tags**
> list[GlobalTagDto] list_global_tags(by=by, page=page, size=size, sort=sort)

Finds a GlobalTagDtos lists.

This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.GlobaltagsApi()
by = 'none' # str | by: the search pattern {none} (optional) (default to none)
page = 0 # int | page: the page number {0} (optional) (default to 0)
size = 1000 # int | size: the page size {1000} (optional) (default to 1000)
sort = 'name:ASC' # str | sort: the sort pattern {name:ASC} (optional) (default to name:ASC)

try:
    # Finds a GlobalTagDtos lists.
    api_response = api_instance.list_global_tags(by=by, page=page, size=size, sort=sort)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling GlobaltagsApi->list_global_tags: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **by** | **str**| by: the search pattern {none} | [optional] [default to none]
 **page** | **int**| page: the page number {0} | [optional] [default to 0]
 **size** | **int**| size: the page size {1000} | [optional] [default to 1000]
 **sort** | **str**| sort: the sort pattern {name:ASC} | [optional] [default to name:ASC]

### Return type

[**list[GlobalTagDto]**](GlobalTagDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

