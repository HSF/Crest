# crestapi.IovsApi

All URIs are relative to *http://localhost:8080/crest*

Method | HTTP request | Description
------------- | ------------- | -------------
[**create_iov**](IovsApi.md#create_iov) | **POST** /iovs | Create a Iov in the database.
[**find_all_iovs**](IovsApi.md#find_all_iovs) | **GET** /iovs | Finds a IovDtos lists.
[**get_size**](IovsApi.md#get_size) | **GET** /iovs/getSize | Get the number o iovs for the given tag.
[**get_size_by_tag**](IovsApi.md#get_size_by_tag) | **GET** /iovs/getSizeByTag | Get the number o iovs for tags matching pattern.
[**select_groups**](IovsApi.md#select_groups) | **GET** /iovs/selectGroups | Select groups for a given tagname.
[**select_iovs**](IovsApi.md#select_iovs) | **GET** /iovs/selectIovs | Select iovs for a given tagname and in a given range.
[**select_snapshot**](IovsApi.md#select_snapshot) | **GET** /iovs/selectSnapshot | Select snapshot for a given tagname and insertion time.


# **create_iov**
> IovDto create_iov(body)

Create a Iov in the database.

This method allows to insert a Iov.Arguments: IovDto should be provided in the body as a JSON file.

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.IovsApi()
body = crestapi.IovDto() # IovDto | A json string that is used to construct a iovdto object: { name: xxx, ... }

try: 
    # Create a Iov in the database.
    api_response = api_instance.create_iov(body)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling IovsApi->create_iov: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**IovDto**](IovDto.md)| A json string that is used to construct a iovdto object: { name: xxx, ... } | 

### Return type

[**IovDto**](IovDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **find_all_iovs**
> list[IovDto] find_all_iovs(tagname=tagname, page=page, size=size, sort=sort)

Finds a IovDtos lists.

This method allows to perform search by tagname and sorting.Arguments: tagname={a tag name}, page={ipage}, size={isize},      sort=<pattern>, where pattern is <field>:[DESC|ASC]

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.IovsApi()
tagname = 'none' # str | tagname: the tag name {none} (optional) (default to none)
page = 0 # int | page: the page number {0} (optional) (default to 0)
size = 10000 # int | size: the page size {10000} (optional) (default to 10000)
sort = 'id.since:ASC' # str | sort: the sort pattern {id.since:ASC} (optional) (default to id.since:ASC)

try: 
    # Finds a IovDtos lists.
    api_response = api_instance.find_all_iovs(tagname=tagname, page=page, size=size, sort=sort)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling IovsApi->find_all_iovs: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **tagname** | **str**| tagname: the tag name {none} | [optional] [default to none]
 **page** | **int**| page: the page number {0} | [optional] [default to 0]
 **size** | **int**| size: the page size {10000} | [optional] [default to 10000]
 **sort** | **str**| sort: the sort pattern {id.since:ASC} | [optional] [default to id.since:ASC]

### Return type

[**list[IovDto]**](IovDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **get_size**
> int get_size(tagname, snapshot=snapshot)

Get the number o iovs for the given tag.

This method allows to select the count of iovs in a tag. Also possible to get the size of snapshot, if the time added.Arguments: tagname={a tag name}, snapshotTime={snapshot time in milliseconds (Long) from epoch}

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.IovsApi()
tagname = 'none' # str | tagname: the tag name {none} (default to none)
snapshot = 0 # int | snapshot: the snapshot time {0} (optional) (default to 0)

try: 
    # Get the number o iovs for the given tag.
    api_response = api_instance.get_size(tagname, snapshot=snapshot)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling IovsApi->get_size: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **tagname** | **str**| tagname: the tag name {none} | [default to none]
 **snapshot** | **int**| snapshot: the snapshot time {0} | [optional] [default to 0]

### Return type

**int**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **get_size_by_tag**
> list[TagSummaryDto] get_size_by_tag(tagname)

Get the number o iovs for tags matching pattern.

This method allows to select the count of iovs in a tag. Also possible to get the size of snapshot, if the time added.Arguments: tagname={a tag name}

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.IovsApi()
tagname = 'none' # str | tagname: the tag name {none} (default to none)

try: 
    # Get the number o iovs for tags matching pattern.
    api_response = api_instance.get_size_by_tag(tagname)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling IovsApi->get_size_by_tag: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **tagname** | **str**| tagname: the tag name {none} | [default to none]

### Return type

[**list[TagSummaryDto]**](TagSummaryDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **select_groups**
> GroupDto select_groups(tagname, snapshot=snapshot)

Select groups for a given tagname.

This method allows to select a list of groups.Arguments: tagname={a tag name}, snapshot={snapshot time as long}

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.IovsApi()
tagname = 'none' # str | tagname: the tag name {none} (default to none)
snapshot = 0 # int | snapshot: the snapshot time {0} (optional) (default to 0)

try: 
    # Select groups for a given tagname.
    api_response = api_instance.select_groups(tagname, snapshot=snapshot)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling IovsApi->select_groups: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **tagname** | **str**| tagname: the tag name {none} | [default to none]
 **snapshot** | **int**| snapshot: the snapshot time {0} | [optional] [default to 0]

### Return type

[**GroupDto**](GroupDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **select_iovs**
> list[IovDto] select_iovs(tagname=tagname, since=since, until=until, snapshot=snapshot)

Select iovs for a given tagname and in a given range.

This method allows to select a list of iovs in a tag, using a given range in time and (optionally) for a given snapshot time.Arguments: tagname={a tag name}, since={since time as string}, until={until time as string}, snapshot={snapshot time as long}

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.IovsApi()
tagname = 'none' # str | tagname: the tag name {none} (optional) (default to none)
since = '0' # str | since: the since time as a string {0} (optional) (default to 0)
until = 'INF' # str | until: the until time as a string {INF} (optional) (default to INF)
snapshot = 0 # int | snapshot: the snapshot time {0} (optional) (default to 0)

try: 
    # Select iovs for a given tagname and in a given range.
    api_response = api_instance.select_iovs(tagname=tagname, since=since, until=until, snapshot=snapshot)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling IovsApi->select_iovs: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **tagname** | **str**| tagname: the tag name {none} | [optional] [default to none]
 **since** | **str**| since: the since time as a string {0} | [optional] [default to 0]
 **until** | **str**| until: the until time as a string {INF} | [optional] [default to INF]
 **snapshot** | **int**| snapshot: the snapshot time {0} | [optional] [default to 0]

### Return type

[**list[IovDto]**](IovDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **select_snapshot**
> list[IovDto] select_snapshot(tagname, snapshot)

Select snapshot for a given tagname and insertion time.

This method allows to select a list of all iovs in a tag, using (optionally) a given snapshot time.Arguments: tagname={a tag name}, snapshot={snapshot time as long}

### Example 
```python
from __future__ import print_statement
import time
import crestapi
from crestapi.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = crestapi.IovsApi()
tagname = 'none' # str | tagname: the tag name {none} (default to none)
snapshot = 0 # int | snapshot: the snapshot time {0} (default to 0)

try: 
    # Select snapshot for a given tagname and insertion time.
    api_response = api_instance.select_snapshot(tagname, snapshot)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling IovsApi->select_snapshot: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **tagname** | **str**| tagname: the tag name {none} | [default to none]
 **snapshot** | **int**| snapshot: the snapshot time {0} | [default to 0]

### Return type

[**list[IovDto]**](IovDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

