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


class PayloadsApi(object):
    """NOTE: This class is auto generated by the swagger code generator program.

    Do not edit the class manually.
    Ref: https://github.com/swagger-api/swagger-codegen
    """

    def __init__(self, api_client=None):
        if api_client is None:
            api_client = ApiClient()
        self.api_client = api_client

    def create_payload(self, body, **kwargs):  # noqa: E501
        """Create a Payload in the database.  # noqa: E501

        This method allows to insert a Payload.Arguments: PayloadDto should be provided in the body as a JSON file.  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.create_payload(body, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param PayloadDto body: A json string that is used to construct a iovdto object: { name: xxx, ... } (required)
        :return: PayloadDto
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.create_payload_with_http_info(body, **kwargs)  # noqa: E501
        else:
            (data) = self.create_payload_with_http_info(body, **kwargs)  # noqa: E501
            return data

    def create_payload_with_http_info(self, body, **kwargs):  # noqa: E501
        """Create a Payload in the database.  # noqa: E501

        This method allows to insert a Payload.Arguments: PayloadDto should be provided in the body as a JSON file.  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.create_payload_with_http_info(body, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param PayloadDto body: A json string that is used to construct a iovdto object: { name: xxx, ... } (required)
        :return: PayloadDto
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
                    " to method create_payload" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'body' is set
        if ('body' not in params or
                params['body'] is None):
            raise ValueError("Missing the required parameter `body` when calling `create_payload`")  # noqa: E501

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
            '/payloads', 'POST',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='PayloadDto',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)

    def create_payload_multi_form(self, file, payload, **kwargs):  # noqa: E501
        """Create a Payload in the database.  # noqa: E501

        This method allows to insert a Payload.Arguments: PayloadDto should be provided in the body as a JSON file.  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.create_payload_multi_form(file, payload, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param file file: The file (required)
        :param str payload: Json body for payloaddto (required)
        :return: PayloadDto
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.create_payload_multi_form_with_http_info(file, payload, **kwargs)  # noqa: E501
        else:
            (data) = self.create_payload_multi_form_with_http_info(file, payload, **kwargs)  # noqa: E501
            return data

    def create_payload_multi_form_with_http_info(self, file, payload, **kwargs):  # noqa: E501
        """Create a Payload in the database.  # noqa: E501

        This method allows to insert a Payload.Arguments: PayloadDto should be provided in the body as a JSON file.  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.create_payload_multi_form_with_http_info(file, payload, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param file file: The file (required)
        :param str payload: Json body for payloaddto (required)
        :return: PayloadDto
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['file', 'payload']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method create_payload_multi_form" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'file' is set
        if ('file' not in params or
                params['file'] is None):
            raise ValueError("Missing the required parameter `file` when calling `create_payload_multi_form`")  # noqa: E501
        # verify the required parameter 'payload' is set
        if ('payload' not in params or
                params['payload'] is None):
            raise ValueError("Missing the required parameter `payload` when calling `create_payload_multi_form`")  # noqa: E501

        collection_formats = {}

        path_params = {}

        query_params = []

        header_params = {}

        form_params = []
        local_var_files = {}
        if 'file' in params:
            local_var_files['file'] = params['file']  # noqa: E501
        if 'payload' in params:
            form_params.append(('payload', params['payload']))  # noqa: E501

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/json'])  # noqa: E501

        # HTTP header `Content-Type`
        header_params['Content-Type'] = self.api_client.select_header_content_type(  # noqa: E501
            ['multipart/form-data'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/payloads/upload', 'POST',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='PayloadDto',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)

    def get_blob(self, hash, **kwargs):  # noqa: E501
        """Finds payload data by hash; the payload object contains the real BLOB.  # noqa: E501

        Select one payload at the time, no regexp searches allowed here  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.get_blob(hash, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str hash: hash of the payload (required)
        :return: str
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.get_blob_with_http_info(hash, **kwargs)  # noqa: E501
        else:
            (data) = self.get_blob_with_http_info(hash, **kwargs)  # noqa: E501
            return data

    def get_blob_with_http_info(self, hash, **kwargs):  # noqa: E501
        """Finds payload data by hash; the payload object contains the real BLOB.  # noqa: E501

        Select one payload at the time, no regexp searches allowed here  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.get_blob_with_http_info(hash, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str hash: hash of the payload (required)
        :return: str
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['hash']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method get_blob" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'hash' is set
        if ('hash' not in params or
                params['hash'] is None):
            raise ValueError("Missing the required parameter `hash` when calling `get_blob`")  # noqa: E501

        collection_formats = {}

        path_params = {}
        if 'hash' in params:
            path_params['hash'] = params['hash']  # noqa: E501

        query_params = []

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/octet-stream'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/payloads/{hash}/data', 'GET',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='str',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)

    def get_payload(self, hash, **kwargs):  # noqa: E501
        """Finds a payload resource associated to the hash.  # noqa: E501

        This method retrieves a payload resource.Arguments: hash=<hash> the hash of the payload  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.get_payload(hash, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str hash: hash:  the hash of the payload (required)
        :param str x_crest_payload_format: The format of the output data. The header parameter X-Crest-PayloadFormat can be : BLOB (default) or DTO (in JSON format).
        :return: str
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.get_payload_with_http_info(hash, **kwargs)  # noqa: E501
        else:
            (data) = self.get_payload_with_http_info(hash, **kwargs)  # noqa: E501
            return data

    def get_payload_with_http_info(self, hash, **kwargs):  # noqa: E501
        """Finds a payload resource associated to the hash.  # noqa: E501

        This method retrieves a payload resource.Arguments: hash=<hash> the hash of the payload  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.get_payload_with_http_info(hash, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str hash: hash:  the hash of the payload (required)
        :param str x_crest_payload_format: The format of the output data. The header parameter X-Crest-PayloadFormat can be : BLOB (default) or DTO (in JSON format).
        :return: str
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['hash', 'x_crest_payload_format']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method get_payload" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'hash' is set
        if ('hash' not in params or
                params['hash'] is None):
            raise ValueError("Missing the required parameter `hash` when calling `get_payload`")  # noqa: E501

        collection_formats = {}

        path_params = {}
        if 'hash' in params:
            path_params['hash'] = params['hash']  # noqa: E501

        query_params = []

        header_params = {}
        if 'x_crest_payload_format' in params:
            header_params['X-Crest-PayloadFormat'] = params['x_crest_payload_format']  # noqa: E501

        form_params = []
        local_var_files = {}

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/octet-stream', 'application/json', 'application/xml'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/payloads/{hash}', 'GET',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='str',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)

    def get_payload_meta_info(self, hash, **kwargs):  # noqa: E501
        """Finds a payload resource associated to the hash.  # noqa: E501

        This method retrieves metadata of the payload resource.Arguments: hash=<hash> the hash of the payload  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.get_payload_meta_info(hash, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str hash: hash:  the hash of the payload (required)
        :return: PayloadDto
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.get_payload_meta_info_with_http_info(hash, **kwargs)  # noqa: E501
        else:
            (data) = self.get_payload_meta_info_with_http_info(hash, **kwargs)  # noqa: E501
            return data

    def get_payload_meta_info_with_http_info(self, hash, **kwargs):  # noqa: E501
        """Finds a payload resource associated to the hash.  # noqa: E501

        This method retrieves metadata of the payload resource.Arguments: hash=<hash> the hash of the payload  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.get_payload_meta_info_with_http_info(hash, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str hash: hash:  the hash of the payload (required)
        :return: PayloadDto
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['hash']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method get_payload_meta_info" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'hash' is set
        if ('hash' not in params or
                params['hash'] is None):
            raise ValueError("Missing the required parameter `hash` when calling `get_payload_meta_info`")  # noqa: E501

        collection_formats = {}

        path_params = {}
        if 'hash' in params:
            path_params['hash'] = params['hash']  # noqa: E501

        query_params = []

        header_params = {}

        form_params = []
        local_var_files = {}

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/json', 'application/xml'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/payloads/{hash}/meta', 'GET',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='PayloadDto',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)

    def store_payload_batch_with_iov_multi_form(self, tag, iovsetupload, **kwargs):  # noqa: E501
        """Create many Payloads in the database, associated to a given iov since list and tag name.  # noqa: E501

        This method allows to insert a Payload and an IOV. Arguments: tagname,stream,end time. The header parameter X-Crest-PayloadFormat can be : JSON (default) or TXT or BLOB  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.store_payload_batch_with_iov_multi_form(tag, iovsetupload, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tag: The tag name (required)
        :param str iovsetupload: (required)
        :param str x_crest_payload_format: The format of the input data
        :param float endtime: The end time to be used for protection at tag level
        :return: IovSetDto
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.store_payload_batch_with_iov_multi_form_with_http_info(tag, iovsetupload, **kwargs)  # noqa: E501
        else:
            (data) = self.store_payload_batch_with_iov_multi_form_with_http_info(tag, iovsetupload, **kwargs)  # noqa: E501
            return data

    def store_payload_batch_with_iov_multi_form_with_http_info(self, tag, iovsetupload, **kwargs):  # noqa: E501
        """Create many Payloads in the database, associated to a given iov since list and tag name.  # noqa: E501

        This method allows to insert a Payload and an IOV. Arguments: tagname,stream,end time. The header parameter X-Crest-PayloadFormat can be : JSON (default) or TXT or BLOB  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.store_payload_batch_with_iov_multi_form_with_http_info(tag, iovsetupload, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str tag: The tag name (required)
        :param str iovsetupload: (required)
        :param str x_crest_payload_format: The format of the input data
        :param float endtime: The end time to be used for protection at tag level
        :return: IovSetDto
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['tag', 'iovsetupload', 'x_crest_payload_format', 'endtime']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method store_payload_batch_with_iov_multi_form" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'tag' is set
        if ('tag' not in params or
                params['tag'] is None):
            raise ValueError("Missing the required parameter `tag` when calling `store_payload_batch_with_iov_multi_form`")  # noqa: E501
        # verify the required parameter 'iovsetupload' is set
        if ('iovsetupload' not in params or
                params['iovsetupload'] is None):
            raise ValueError("Missing the required parameter `iovsetupload` when calling `store_payload_batch_with_iov_multi_form`")  # noqa: E501

        collection_formats = {}

        path_params = {}

        query_params = []

        header_params = {}
        if 'x_crest_payload_format' in params:
            header_params['X-Crest-PayloadFormat'] = params['x_crest_payload_format']  # noqa: E501

        form_params = []
        local_var_files = {}
        if 'tag' in params:
            form_params.append(('tag', params['tag']))  # noqa: E501
        if 'iovsetupload' in params:
            form_params.append(('iovsetupload', params['iovsetupload']))  # noqa: E501
        if 'endtime' in params:
            form_params.append(('endtime', params['endtime']))  # noqa: E501

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/json'])  # noqa: E501

        # HTTP header `Content-Type`
        header_params['Content-Type'] = self.api_client.select_header_content_type(  # noqa: E501
            ['multipart/form-data'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/payloads/storebatch', 'POST',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='IovSetDto',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)

    def store_payload_with_iov_multi_form(self, file, tag, since, **kwargs):  # noqa: E501
        """Create a Payload in the database, associated to a given iov since and tag name.  # noqa: E501

        This method allows to insert a Payload and an IOV. Arguments: since,tagname,stream,end time. The header parameter X-Crest-PayloadFormat can be : JSON (default) or TXT or BLOB  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.store_payload_with_iov_multi_form(file, tag, since, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param file file: The payload file as a stream (required)
        :param str tag: The tag name (required)
        :param float since: The since time (required)
        :param str x_crest_payload_format: The format of the input data
        :param float endtime: The end time to be used for protection at tag level
        :return: HTTPResponse
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.store_payload_with_iov_multi_form_with_http_info(file, tag, since, **kwargs)  # noqa: E501
        else:
            (data) = self.store_payload_with_iov_multi_form_with_http_info(file, tag, since, **kwargs)  # noqa: E501
            return data

    def store_payload_with_iov_multi_form_with_http_info(self, file, tag, since, **kwargs):  # noqa: E501
        """Create a Payload in the database, associated to a given iov since and tag name.  # noqa: E501

        This method allows to insert a Payload and an IOV. Arguments: since,tagname,stream,end time. The header parameter X-Crest-PayloadFormat can be : JSON (default) or TXT or BLOB  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.store_payload_with_iov_multi_form_with_http_info(file, tag, since, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param file file: The payload file as a stream (required)
        :param str tag: The tag name (required)
        :param float since: The since time (required)
        :param str x_crest_payload_format: The format of the input data
        :param float endtime: The end time to be used for protection at tag level
        :return: HTTPResponse
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['file', 'tag', 'since', 'x_crest_payload_format', 'endtime']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method store_payload_with_iov_multi_form" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'file' is set
        if ('file' not in params or
                params['file'] is None):
            raise ValueError("Missing the required parameter `file` when calling `store_payload_with_iov_multi_form`")  # noqa: E501
        # verify the required parameter 'tag' is set
        if ('tag' not in params or
                params['tag'] is None):
            raise ValueError("Missing the required parameter `tag` when calling `store_payload_with_iov_multi_form`")  # noqa: E501
        # verify the required parameter 'since' is set
        if ('since' not in params or
                params['since'] is None):
            raise ValueError("Missing the required parameter `since` when calling `store_payload_with_iov_multi_form`")  # noqa: E501

        collection_formats = {}

        path_params = {}

        query_params = []

        header_params = {}
        if 'x_crest_payload_format' in params:
            header_params['X-Crest-PayloadFormat'] = params['x_crest_payload_format']  # noqa: E501

        form_params = []
        local_var_files = {}
        if 'file' in params:
            local_var_files['file'] = params['file']  # noqa: E501
        if 'tag' in params:
            form_params.append(('tag', params['tag']))  # noqa: E501
        if 'since' in params:
            form_params.append(('since', params['since']))  # noqa: E501
        if 'endtime' in params:
            form_params.append(('endtime', params['endtime']))  # noqa: E501

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/json'])  # noqa: E501

        # HTTP header `Content-Type`
        header_params['Content-Type'] = self.api_client.select_header_content_type(  # noqa: E501
            ['multipart/form-data'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/payloads/store', 'POST',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='HTTPResponse',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)

    def upload_payload_batch_with_iov_multi_form(self, files, tag, iovsetupload, **kwargs):  # noqa: E501
        """Create many Payloads in the database, associated to a given iov since list and tag name.  # noqa: E501

        This method allows to insert a Payload and an IOV. Arguments: tagname,stream,end time. The header parameter X-Crest-PayloadFormat can be : JSON (default) or TXT or BLOB  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.upload_payload_batch_with_iov_multi_form(files, tag, iovsetupload, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param file files: The files to upload (required)
        :param str tag: The tag name (required)
        :param str iovsetupload: (required)
        :param str x_crest_payload_format: The format of the input data
        :param float endtime: The end time to be used for protection at tag level
        :return: IovSetDto
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.upload_payload_batch_with_iov_multi_form_with_http_info(files, tag, iovsetupload, **kwargs)  # noqa: E501
        else:
            (data) = self.upload_payload_batch_with_iov_multi_form_with_http_info(files, tag, iovsetupload, **kwargs)  # noqa: E501
            return data

    def upload_payload_batch_with_iov_multi_form_with_http_info(self, files, tag, iovsetupload, **kwargs):  # noqa: E501
        """Create many Payloads in the database, associated to a given iov since list and tag name.  # noqa: E501

        This method allows to insert a Payload and an IOV. Arguments: tagname,stream,end time. The header parameter X-Crest-PayloadFormat can be : JSON (default) or TXT or BLOB  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.upload_payload_batch_with_iov_multi_form_with_http_info(files, tag, iovsetupload, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param file files: The files to upload (required)
        :param str tag: The tag name (required)
        :param str iovsetupload: (required)
        :param str x_crest_payload_format: The format of the input data
        :param float endtime: The end time to be used for protection at tag level
        :return: IovSetDto
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['files', 'tag', 'iovsetupload', 'x_crest_payload_format', 'endtime']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method upload_payload_batch_with_iov_multi_form" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'files' is set
        if ('files' not in params or
                params['files'] is None):
            raise ValueError("Missing the required parameter `files` when calling `upload_payload_batch_with_iov_multi_form`")  # noqa: E501
        # verify the required parameter 'tag' is set
        if ('tag' not in params or
                params['tag'] is None):
            raise ValueError("Missing the required parameter `tag` when calling `upload_payload_batch_with_iov_multi_form`")  # noqa: E501
        # verify the required parameter 'iovsetupload' is set
        if ('iovsetupload' not in params or
                params['iovsetupload'] is None):
            raise ValueError("Missing the required parameter `iovsetupload` when calling `upload_payload_batch_with_iov_multi_form`")  # noqa: E501

        collection_formats = {}

        path_params = {}

        query_params = []

        header_params = {}
        if 'x_crest_payload_format' in params:
            header_params['X-Crest-PayloadFormat'] = params['x_crest_payload_format']  # noqa: E501

        form_params = []
        local_var_files = {}
        if 'files' in params:
            local_var_files['files'] = params['files']  # noqa: E501
        if 'tag' in params:
            form_params.append(('tag', params['tag']))  # noqa: E501
        if 'iovsetupload' in params:
            form_params.append(('iovsetupload', params['iovsetupload']))  # noqa: E501
        if 'endtime' in params:
            form_params.append(('endtime', params['endtime']))  # noqa: E501

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/json'])  # noqa: E501

        # HTTP header `Content-Type`
        header_params['Content-Type'] = self.api_client.select_header_content_type(  # noqa: E501
            ['multipart/form-data'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/payloads/uploadbatch', 'POST',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type='IovSetDto',  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)
