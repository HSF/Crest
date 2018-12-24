# coding: utf-8

"""
    CrestDB REST API

    Crest Rest Api to manage data for calibration files.  # noqa: E501

    OpenAPI spec version: 2.0
    Contact: andrea.formica@cern.ch
    Generated by: https://github.com/swagger-api/swagger-codegen.git
"""


from __future__ import absolute_import

import re  # noqa: F401

# python 2 and python 3 compatibility library
import six

from crestapi.api_client import ApiClient


class IovsApi(object):
    """NOTE: This class is auto generated by the swagger code generator program.

    Do not edit the class manually.
    Ref: https://github.com/swagger-api/swagger-codegen
    """

    def __init__(self, api_client=None):
        if api_client is None:
            api_client = ApiClient()
        self.api_client = api_client

    def create_iov(self, body, **kwargs):  # noqa: E501
        """Create a Iov in the database.  # noqa: E501

        This method allows to insert a Iov.Arguments: IovDto should be provided in the body as a JSON file.  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.create_iov(body, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param IovDto body: A json string that is used to construct a iovdto object: { name: xxx, ... } (required)
        :return: IovDto
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.create_iov_with_http_info(body, **kwargs)  # noqa: E501
        else:
            (data) = self.create_iov_with_http_info(body, **kwargs)  # noqa: E501
            return data

    def create_iov_with_http_info(self, body, **kwargs):  # noqa: E501
        """Create a Iov in the database.  # noqa: E501

        This method allows to insert a Iov.Arguments: IovDto should be provided in the body as a JSON file.  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.create_iov_with_http_info(body, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param IovDto body: A json string that is used to construct a iovdto object: { name: xxx, ... } (required)
        :return: IovDto
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['body']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method create_iov" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'body' is set
        if ('body' not in params or
                params['body'] is None):
            raise ValueError("Missing the required parameter `body` when calling `create_iov`")  # noqa: E501

        collection_formats = {}

        path_params = {}

        query_params = []

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        if 'body' in params:
            body_params = params['body']
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/json'])  # noqa: E501

        # HTTP header `Content-Type`
        header_params['Content-Type'] = self.api_client.select_header_content_type(  # noqa: E501
            ['application/json'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/iovs', 'POST',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='IovDto',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)

    def find_all_iovs(self, **kwargs):  # noqa: E501
        """Finds a IovDtos lists.  # noqa: E501

        This method allows to perform search by tagname and sorting.Arguments: tagname={a tag name}, page={ipage}, size={isize},      sort=<pattern>, where pattern is <field>:[DESC|ASC]  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.find_all_iovs(async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tagname: tagname: the tag name {none}
        :param int page: page: the page number {0}
        :param int size: size: the page size {10000}
        :param str sort: sort: the sort pattern {id.since:ASC}
        :return: list[IovDto]
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.find_all_iovs_with_http_info(**kwargs)  # noqa: E501
        else:
            (data) = self.find_all_iovs_with_http_info(**kwargs)  # noqa: E501
            return data

    def find_all_iovs_with_http_info(self, **kwargs):  # noqa: E501
        """Finds a IovDtos lists.  # noqa: E501

        This method allows to perform search by tagname and sorting.Arguments: tagname={a tag name}, page={ipage}, size={isize},      sort=<pattern>, where pattern is <field>:[DESC|ASC]  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.find_all_iovs_with_http_info(async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tagname: tagname: the tag name {none}
        :param int page: page: the page number {0}
        :param int size: size: the page size {10000}
        :param str sort: sort: the sort pattern {id.since:ASC}
        :return: list[IovDto]
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['tagname', 'page', 'size', 'sort']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method find_all_iovs" % key
                )
            params[key] = val
        del params['kwargs']

        collection_formats = {}

        path_params = {}

        query_params = []
        if 'tagname' in params:
            query_params.append(('tagname', params['tagname']))  # noqa: E501
        if 'page' in params:
            query_params.append(('page', params['page']))  # noqa: E501
        if 'size' in params:
            query_params.append(('size', params['size']))  # noqa: E501
        if 'sort' in params:
            query_params.append(('sort', params['sort']))  # noqa: E501

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/json'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/iovs', 'GET',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='list[IovDto]',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)

    def get_size(self, tagname, **kwargs):  # noqa: E501
        """Get the number o iovs for the given tag.  # noqa: E501

        This method allows to select the count of iovs in a tag. Also possible to get the size of snapshot, if the time added.Arguments: tagname={a tag name}, snapshotTime={snapshot time in milliseconds (Long) from epoch}  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.get_size(tagname, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tagname: tagname: the tag name {none} (required)
        :param int snapshot: snapshot: the snapshot time {0}
        :return: int
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.get_size_with_http_info(tagname, **kwargs)  # noqa: E501
        else:
            (data) = self.get_size_with_http_info(tagname, **kwargs)  # noqa: E501
            return data

    def get_size_with_http_info(self, tagname, **kwargs):  # noqa: E501
        """Get the number o iovs for the given tag.  # noqa: E501

        This method allows to select the count of iovs in a tag. Also possible to get the size of snapshot, if the time added.Arguments: tagname={a tag name}, snapshotTime={snapshot time in milliseconds (Long) from epoch}  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.get_size_with_http_info(tagname, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tagname: tagname: the tag name {none} (required)
        :param int snapshot: snapshot: the snapshot time {0}
        :return: int
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['tagname', 'snapshot']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method get_size" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'tagname' is set
        if ('tagname' not in params or
                params['tagname'] is None):
            raise ValueError("Missing the required parameter `tagname` when calling `get_size`")  # noqa: E501

        collection_formats = {}

        path_params = {}

        query_params = []
        if 'tagname' in params:
            query_params.append(('tagname', params['tagname']))  # noqa: E501
        if 'snapshot' in params:
            query_params.append(('snapshot', params['snapshot']))  # noqa: E501

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/json'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/iovs/getSize', 'GET',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='int',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)

    def get_size_by_tag(self, tagname, **kwargs):  # noqa: E501
        """Get the number o iovs for tags matching pattern.  # noqa: E501

        This method allows to select the count of iovs in a tag. Also possible to get the size of snapshot, if the time added.Arguments: tagname={a tag name}  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.get_size_by_tag(tagname, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tagname: tagname: the tag name {none} (required)
        :return: list[TagSummaryDto]
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.get_size_by_tag_with_http_info(tagname, **kwargs)  # noqa: E501
        else:
            (data) = self.get_size_by_tag_with_http_info(tagname, **kwargs)  # noqa: E501
            return data

    def get_size_by_tag_with_http_info(self, tagname, **kwargs):  # noqa: E501
        """Get the number o iovs for tags matching pattern.  # noqa: E501

        This method allows to select the count of iovs in a tag. Also possible to get the size of snapshot, if the time added.Arguments: tagname={a tag name}  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.get_size_by_tag_with_http_info(tagname, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tagname: tagname: the tag name {none} (required)
        :return: list[TagSummaryDto]
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['tagname']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method get_size_by_tag" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'tagname' is set
        if ('tagname' not in params or
                params['tagname'] is None):
            raise ValueError("Missing the required parameter `tagname` when calling `get_size_by_tag`")  # noqa: E501

        collection_formats = {}

        path_params = {}

        query_params = []
        if 'tagname' in params:
            query_params.append(('tagname', params['tagname']))  # noqa: E501

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/json'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/iovs/getSizeByTag', 'GET',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='list[TagSummaryDto]',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)

    def select_groups(self, tagname, **kwargs):  # noqa: E501
        """Select groups for a given tagname.  # noqa: E501

        This method allows to select a list of groups.Arguments: tagname={a tag name}, snapshot={snapshot time as long}  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.select_groups(tagname, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tagname: tagname: the tag name {none} (required)
        :param int snapshot: snapshot: the snapshot time {0}
        :return: GroupDto
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.select_groups_with_http_info(tagname, **kwargs)  # noqa: E501
        else:
            (data) = self.select_groups_with_http_info(tagname, **kwargs)  # noqa: E501
            return data

    def select_groups_with_http_info(self, tagname, **kwargs):  # noqa: E501
        """Select groups for a given tagname.  # noqa: E501

        This method allows to select a list of groups.Arguments: tagname={a tag name}, snapshot={snapshot time as long}  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.select_groups_with_http_info(tagname, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tagname: tagname: the tag name {none} (required)
        :param int snapshot: snapshot: the snapshot time {0}
        :return: GroupDto
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['tagname', 'snapshot']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method select_groups" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'tagname' is set
        if ('tagname' not in params or
                params['tagname'] is None):
            raise ValueError("Missing the required parameter `tagname` when calling `select_groups`")  # noqa: E501

        collection_formats = {}

        path_params = {}

        query_params = []
        if 'tagname' in params:
            query_params.append(('tagname', params['tagname']))  # noqa: E501
        if 'snapshot' in params:
            query_params.append(('snapshot', params['snapshot']))  # noqa: E501

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/json'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/iovs/selectGroups', 'GET',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='GroupDto',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)

    def select_iovs(self, **kwargs):  # noqa: E501
        """Select iovs for a given tagname and in a given range.  # noqa: E501

        This method allows to select a list of iovs in a tag, using a given range in time and (optionally) for a given snapshot time.Arguments: tagname={a tag name}, since={since time as string}, until={until time as string}, snapshot={snapshot time as long}  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.select_iovs(async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tagname: tagname: the tag name {none}
        :param str since: since: the since time as a string {0}
        :param str until: until: the until time as a string {INF}
        :param int snapshot: snapshot: the snapshot time {0}
        :return: list[IovDto]
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.select_iovs_with_http_info(**kwargs)  # noqa: E501
        else:
            (data) = self.select_iovs_with_http_info(**kwargs)  # noqa: E501
            return data

    def select_iovs_with_http_info(self, **kwargs):  # noqa: E501
        """Select iovs for a given tagname and in a given range.  # noqa: E501

        This method allows to select a list of iovs in a tag, using a given range in time and (optionally) for a given snapshot time.Arguments: tagname={a tag name}, since={since time as string}, until={until time as string}, snapshot={snapshot time as long}  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.select_iovs_with_http_info(async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tagname: tagname: the tag name {none}
        :param str since: since: the since time as a string {0}
        :param str until: until: the until time as a string {INF}
        :param int snapshot: snapshot: the snapshot time {0}
        :return: list[IovDto]
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['tagname', 'since', 'until', 'snapshot']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method select_iovs" % key
                )
            params[key] = val
        del params['kwargs']

        collection_formats = {}

        path_params = {}

        query_params = []
        if 'tagname' in params:
            query_params.append(('tagname', params['tagname']))  # noqa: E501
        if 'since' in params:
            query_params.append(('since', params['since']))  # noqa: E501
        if 'until' in params:
            query_params.append(('until', params['until']))  # noqa: E501
        if 'snapshot' in params:
            query_params.append(('snapshot', params['snapshot']))  # noqa: E501

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/json'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/iovs/selectIovs', 'GET',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='list[IovDto]',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)

    def select_snapshot(self, tagname, snapshot, **kwargs):  # noqa: E501
        """Select snapshot for a given tagname and insertion time.  # noqa: E501

        This method allows to select a list of all iovs in a tag, using (optionally) a given snapshot time.Arguments: tagname={a tag name}, snapshot={snapshot time as long}  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.select_snapshot(tagname, snapshot, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tagname: tagname: the tag name {none} (required)
        :param int snapshot: snapshot: the snapshot time {0} (required)
        :return: list[IovDto]
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.select_snapshot_with_http_info(tagname, snapshot, **kwargs)  # noqa: E501
        else:
            (data) = self.select_snapshot_with_http_info(tagname, snapshot, **kwargs)  # noqa: E501
            return data

    def select_snapshot_with_http_info(self, tagname, snapshot, **kwargs):  # noqa: E501
        """Select snapshot for a given tagname and insertion time.  # noqa: E501

        This method allows to select a list of all iovs in a tag, using (optionally) a given snapshot time.Arguments: tagname={a tag name}, snapshot={snapshot time as long}  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.select_snapshot_with_http_info(tagname, snapshot, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tagname: tagname: the tag name {none} (required)
        :param int snapshot: snapshot: the snapshot time {0} (required)
        :return: list[IovDto]
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['tagname', 'snapshot']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method select_snapshot" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'tagname' is set
        if ('tagname' not in params or
                params['tagname'] is None):
            raise ValueError("Missing the required parameter `tagname` when calling `select_snapshot`")  # noqa: E501
        # verify the required parameter 'snapshot' is set
        if ('snapshot' not in params or
                params['snapshot'] is None):
            raise ValueError("Missing the required parameter `snapshot` when calling `select_snapshot`")  # noqa: E501

        collection_formats = {}

        path_params = {}

        query_params = []
        if 'tagname' in params:
            query_params.append(('tagname', params['tagname']))  # noqa: E501
        if 'snapshot' in params:
            query_params.append(('snapshot', params['snapshot']))  # noqa: E501

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/json'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/iovs/selectSnapshot', 'GET',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='list[IovDto]',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)