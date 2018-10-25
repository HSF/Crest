# crestapi.TagsApi

All URIs are relative to *http://localhost:8080/crest*

Method | HTTP request | Description
------------- | ------------- | -------------
[**create_tag**](TagsApi.md#create_tag) | **POST** /tags | Create a Tag in the database.
[**find_tag**](TagsApi.md#find_tag) | **GET** /tags/{name} | Finds a TagDto by name
[**list_tags**](TagsApi.md#list_tags) | **GET** /tags | Finds a TagDtos lists.
[**update_tag**](TagsApi.md#update_tag) | **POST** /tags/{name} | Update a TagDto by name


# **create_tag**
> TagDto create_tag(body)

Create a Tag in the database.

This method allows to insert a Tag.Arguments: TagDto should be provided in the body as a JSON file.

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.TagsApi()
body = crestapi.TagDto() # TagDto | A json string that is used to construct a tagdto object: { name: xxx, ... }

try: 
    # Create a Tag in the database.
    api_response = api_instance.create_tag(body)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling TagsApi->create_tag: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**TagDto**](TagDto.md)| A json string that is used to construct a tagdto object: { name: xxx, ... } | 

### Return type

[**TagDto**](TagDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **find_tag**
> TagDto find_tag(name)

Finds a TagDto by name

This method will search for a tag with the given name. Only one tag should be returned.

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.TagsApi()
name = 'name_example' # str | name: the tag name

try: 
    # Finds a TagDto by name
    api_response = api_instance.find_tag(name)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling TagsApi->find_tag: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| name: the tag name | 

### Return type

[**TagDto**](TagDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **list_tags**
> list[TagDto] list_tags(by=by, page=page, size=size, sort=sort)

Finds a TagDtos lists.

This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.TagsApi()
by = 'none' # str | by: the search pattern {none} (optional) (default to none)
page = 0 # int | page: the page number {0} (optional) (default to 0)
size = 1000 # int | size: the page size {1000} (optional) (default to 1000)
sort = 'name:ASC' # str | sort: the sort pattern {name:ASC} (optional) (default to name:ASC)

try: 
    # Finds a TagDtos lists.
    api_response = api_instance.list_tags(by=by, page=page, size=size, sort=sort)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling TagsApi->list_tags: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **by** | **str**| by: the search pattern {none} | [optional] [default to none]
 **page** | **int**| page: the page number {0} | [optional] [default to 0]
 **size** | **int**| size: the page size {1000} | [optional] [default to 1000]
 **sort** | **str**| sort: the sort pattern {name:ASC} | [optional] [default to name:ASC]

### Return type

[**list[TagDto]**](TagDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **update_tag**
> TagDto update_tag(name, body)

Update a TagDto by name

This method will search for a tag with the given name, and update its content for the provided body fields. Only the following fields can be updated: description, timeType, objectTime, endOfValidity, lastValidatedTime.

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.TagsApi()
name = 'name_example' # str | name: the tag name
body = crestapi.GenericMap() # GenericMap | A json string that is used to construct a map of updatable fields: { description: xxx, ... }

try: 
    # Update a TagDto by name
    api_response = api_instance.update_tag(name, body)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling TagsApi->update_tag: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**| name: the tag name | 
 **body** | [**GenericMap**](GenericMap.md)| A json string that is used to construct a map of updatable fields: { description: xxx, ... } | 

### Return type

[**TagDto**](TagDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

