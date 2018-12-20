# crestapi.MonitoringApi

All URIs are relative to *http://localhost:8080/crest*

Method | HTTP request | Description
------------- | ------------- | -------------
[**list_payload_tag_info**](MonitoringApi.md#list_payload_tag_info) | **GET** /monitoring/payloads | Retrieves monitoring information on payload as a list of PayloadTagInfoDtos.


# **list_payload_tag_info**
> list[PayloadTagInfoDto] list_payload_tag_info(tagname=tagname)

Retrieves monitoring information on payload as a list of PayloadTagInfoDtos.

This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]

### Example
```python
from __future__ import print_function
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.MonitoringApi()
tagname = 'none' # str | tagname: the search pattern {none} (optional) (default to none)

try:
    # Retrieves monitoring information on payload as a list of PayloadTagInfoDtos.
    api_response = api_instance.list_payload_tag_info(tagname=tagname)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling MonitoringApi->list_payload_tag_info: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **tagname** | **str**| tagname: the search pattern {none} | [optional] [default to none]

### Return type

[**list[PayloadTagInfoDto]**](PayloadTagInfoDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

