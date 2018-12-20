# crestapi.FoldersApi

All URIs are relative to *http://localhost:8080/crest*

Method | HTTP request | Description
------------- | ------------- | -------------
[**create_folder**](FoldersApi.md#create_folder) | **POST** /folders | Create an entry for folder information.
[**list_folders**](FoldersApi.md#list_folders) | **GET** /folders | Finds a FolderDto list.


# **create_folder**
> str create_folder(body)

Create an entry for folder information.

Folder informations go into a dedicated table.

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.FoldersApi()
body = crestapi.FolderDto() # FolderDto | A json string that is used to construct a folderdto object: { node: xxx, ... }

try:
    # Create an entry for folder information.
    api_response = api_instance.create_folder(body)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling FoldersApi->create_folder: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**FolderDto**](FolderDto.md)| A json string that is used to construct a folderdto object: { node: xxx, ... } | 

### Return type

**str**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json, text/plain

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **list_folders**
> list[FolderDto] list_folders(by=by, sort=sort)

Finds a FolderDto list.

This method allows to perform search and sorting.Arguments: by=<pattern>, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.FoldersApi()
by = 'none' # str | by: the search pattern {none} (optional) (default to none)
sort = 'since:ASC' # str | sort: the sort pattern {nodeFullpath:ASC} (optional) (default to since:ASC)

try:
    # Finds a FolderDto list.
    api_response = api_instance.list_folders(by=by, sort=sort)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling FoldersApi->list_folders: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **by** | **str**| by: the search pattern {none} | [optional] [default to none]
 **sort** | **str**| sort: the sort pattern {nodeFullpath:ASC} | [optional] [default to since:ASC]

### Return type

[**list[FolderDto]**](FolderDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

