# coding: utf-8

"""
    CrestDB REST API

    Crest Rest Api to manage data for calibration files.  # noqa: E501

    OpenAPI spec version: 2.0
    Contact: andrea.formica@cern.ch
    Generated by: https://github.com/swagger-api/swagger-codegen.git
"""


from __future__ import absolute_import

import unittest

import crestapi
from crestapi.api.payloads_api import PayloadsApi  # noqa: E501
from crestapi.rest import ApiException


class TestPayloadsApi(unittest.TestCase):
    """PayloadsApi unit test stubs"""

    def setUp(self):
        self.api = crestapi.api.payloads_api.PayloadsApi()  # noqa: E501

    def tearDown(self):
        pass

    def test_create_payload(self):
        """Test case for create_payload

        Create a Payload in the database.  # noqa: E501
        """
        pass

    def test_create_payload_multi_form(self):
        """Test case for create_payload_multi_form

        Create a Payload in the database.  # noqa: E501
        """
        pass

    def test_get_blob(self):
        """Test case for get_blob

        Finds payload data by hash; the payload object contains the real BLOB.  # noqa: E501
        """
        pass

    def test_get_payload(self):
        """Test case for get_payload

        Finds a payload resource associated to the hash.  # noqa: E501
        """
        pass

    def test_get_payload_meta_info(self):
        """Test case for get_payload_meta_info

        Finds a payload resource associated to the hash.  # noqa: E501
        """
        pass

    def test_store_payload_with_iov_multi_form(self):
        """Test case for store_payload_with_iov_multi_form

        Create a Payload in the database, associated to a given iov since and tag name.  # noqa: E501
        """
        pass


if __name__ == '__main__':
    unittest.main()
