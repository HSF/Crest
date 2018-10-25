# crestapi.RuninfoApi

All URIs are relative to *http://localhost:8080/crest*

Method | HTTP request | Description
------------- | ------------- | -------------
[**create_run_lumi_info**](RuninfoApi.md#create_run_lumi_info) | **POST** /runinfo | Create an entry for run information.
[**find_run_lumi_info**](RuninfoApi.md#find_run_lumi_info) | **GET** /runinfo/list | Finds a RunLumiInfoDto lists using parameters.
[**list_run_lumi_info**](RuninfoApi.md#list_run_lumi_info) | **GET** /runinfo | Finds a RunLumiInfoDto lists.


# **create_run_lumi_info**
> str create_run_lumi_info(body)

Create an entry for run information.

Run informations go into a separate table.

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.RuninfoApi()
body = crestapi.RunLumiInfoDto() # RunLumiInfoDto | A json string that is used to construct a runlumiinfodto object: { run: xxx, ... }

try: 
    # Create an entry for run information.
    api_response = api_instance.create_run_lumi_info(body)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling RuninfoApi->create_run_lumi_info: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**RunLumiInfoDto**](RunLumiInfoDto.md)| A json string that is used to construct a runlumiinfodto object: { run: xxx, ... } | 

### Return type

**str**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json, text/plain

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **find_run_lumi_info**
> list[RunLumiInfoDto] find_run_lumi_info(_from=_from, to=to, format=format, page=page, size=size, sort=sort)

Finds a RunLumiInfoDto lists using parameters.

This method allows to perform search.Arguments: from=<someformat>,to=<someformat>, format=<describe previous types>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.RuninfoApi()
_from = 'none' # str | from: the starting time or run-lumi (optional) (default to none)
to = 'none' # str | to: the ending time or run-lumi (optional) (default to none)
format = 'time' # str | format: the format to digest previous arguments [time] or [run-lumi]. Time = yyyymmddhhmiss, Run-lumi = run-lumi (optional) (default to time)
page = 0 # int | page: the page number {0} (optional) (default to 0)
size = 1000 # int | size: the page size {1000} (optional) (default to 1000)
sort = 'since:ASC' # str | sort: the sort pattern {since:ASC} (optional) (default to since:ASC)

try: 
    # Finds a RunLumiInfoDto lists using parameters.
    api_response = api_instance.find_run_lumi_info(_from=_from, to=to, format=format, page=page, size=size, sort=sort)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling RuninfoApi->find_run_lumi_info: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **_from** | **str**| from: the starting time or run-lumi | [optional] [default to none]
 **to** | **str**| to: the ending time or run-lumi | [optional] [default to none]
 **format** | **str**| format: the format to digest previous arguments [time] or [run-lumi]. Time &#x3D; yyyymmddhhmiss, Run-lumi &#x3D; run-lumi | [optional] [default to time]
 **page** | **int**| page: the page number {0} | [optional] [default to 0]
 **size** | **int**| size: the page size {1000} | [optional] [default to 1000]
 **sort** | **str**| sort: the sort pattern {since:ASC} | [optional] [default to since:ASC]

### Return type

[**list[RunLumiInfoDto]**](RunLumiInfoDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **list_run_lumi_info**
> list[RunLumiInfoDto] list_run_lumi_info(by=by, page=page, size=size, sort=sort)

Finds a RunLumiInfoDto lists.

This method allows to perform search and sorting.Arguments: by=<pattern>, page={ipage}, size={isize}, sort=<sortpattern>. The pattern <pattern> is in the form <param-name><operation><param-value>       <param-name> is the name of one of the fields in the dto       <operation> can be [< : >] ; for string use only [:]        <param-value> depends on the chosen parameter. A list of this criteria can be provided       using comma separated strings for <pattern>.      The pattern <sortpattern> is <field>:[DESC|ASC]

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.RuninfoApi()
by = 'none' # str | by: the search pattern {none} (optional) (default to none)
page = 0 # int | page: the page number {0} (optional) (default to 0)
size = 1000 # int | size: the page size {1000} (optional) (default to 1000)
sort = 'since:ASC' # str | sort: the sort pattern {since:ASC} (optional) (default to since:ASC)

try: 
    # Finds a RunLumiInfoDto lists.
    api_response = api_instance.list_run_lumi_info(by=by, page=page, size=size, sort=sort)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling RuninfoApi->list_run_lumi_info: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **by** | **str**| by: the search pattern {none} | [optional] [default to none]
 **page** | **int**| page: the page number {0} | [optional] [default to 0]
 **size** | **int**| size: the page size {1000} | [optional] [default to 1000]
 **sort** | **str**| sort: the sort pattern {since:ASC} | [optional] [default to since:ASC]

### Return type

[**list[RunLumiInfoDto]**](RunLumiInfoDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

