# crestapi.AdminApi

All URIs are relative to *http://localhost:8080/crest*

Method | HTTP request | Description
------------- | ------------- | -------------
[**remove_global_tag**](AdminApi.md#remove_global_tag) | **DELETE** /admin/globaltags/{name} | Remove a GlobalTag from the database.
[**remove_tag**](AdminApi.md#remove_tag) | **DELETE** /admin/tags/{name} | Remove a Tag from the database.
[**update_global_tag**](AdminApi.md#update_global_tag) | **PUT** /admin/globaltags/{name} | Update a GlobalTag in the database.


# **remove_global_tag**
> remove_global_tag(name)

Remove a GlobalTag from the database.

This method allows to remove a GlobalTag.Arguments: the name has to uniquely identify a global tag.

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.AdminApi()
name = 'name_example' # str | 

try: 
    # Remove a GlobalTag from the database.
    api_instance.remove_global_tag(name)
except ApiException as e:
    print("Exception when calling AdminApi->remove_global_tag: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**|  | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **remove_tag**
> remove_tag(name)

Remove a Tag from the database.

This method allows to remove a Tag.Arguments: the name has to uniquely identify a tag.

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.AdminApi()
name = 'name_example' # str | 

try: 
    # Remove a Tag from the database.
    api_instance.remove_tag(name)
except ApiException as e:
    print("Exception when calling AdminApi->remove_tag: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**|  | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **update_global_tag**
> GlobalTagDto update_global_tag(name, body)

Update a GlobalTag in the database.

This method allows to update a GlobalTag.Arguments: the name has to uniquely identify a global tag.

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.AdminApi()
name = 'name_example' # str | 
body = crestapi.GlobalTagDto() # GlobalTagDto | A json string that is used to construct a GlobalTagDto object: { name: xxx, ... }

try: 
    # Update a GlobalTag in the database.
    api_response = api_instance.update_global_tag(name, body)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling AdminApi->update_global_tag: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **str**|  | 
 **body** | [**GlobalTagDto**](GlobalTagDto.md)| A json string that is used to construct a GlobalTagDto object: { name: xxx, ... } | 

### Return type

[**GlobalTagDto**](GlobalTagDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

