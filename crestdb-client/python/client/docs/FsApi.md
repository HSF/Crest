# crestapi.FsApi

All URIs are relative to *http://localhost:8080/crest*

Method | HTTP request | Description
------------- | ------------- | -------------
[**build_tar**](FsApi.md#build_tar) | **POST** /fs/tar | Dump a tag into filesystem and retrieve the tar file asynchronously.


# **build_tar**
> str build_tar(tagname, snapshot)

Dump a tag into filesystem and retrieve the tar file asynchronously.

This method allows to request a tar file from the server using a tag specified in input.

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.FsApi()
tagname = 'none' # str | tagname: the tag name {none} (default to none)
snapshot = 0 # int | snapshot: the snapshot time {0} (default to 0)

try:
    # Dump a tag into filesystem and retrieve the tar file asynchronously.
    api_response = api_instance.build_tar(tagname, snapshot)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling FsApi->build_tar: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **tagname** | **str**| tagname: the tag name {none} | [default to none]
 **snapshot** | **int**| snapshot: the snapshot time {0} | [default to 0]

### Return type

**str**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json, text/plain

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

