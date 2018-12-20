# coding: utf-8

"""
    CrestDB REST API

    Crest Rest Api to manage data for calibration files.  # noqa: E501

    OpenAPI spec version: 2.0
    Contact: andrea.formica@cern.ch
    Generated by: https://github.com/swagger-api/swagger-codegen.git
"""


import pprint
import re  # noqa: F401

import six


class HTTPResponse(object):
    """NOTE: This class is auto generated by the swagger code generator program.

    Do not edit the class manually.
    """

    """
    Attributes:
      swagger_types (dict): The key is attribute name
                            and the value is attribute type.
      attribute_map (dict): The key is attribute name
                            and the value is json key in definition.
    """
    swagger_types = {
        'message': 'str',
        'action': 'str',
        'code': 'int',
        'id': 'str'
    }

    attribute_map = {
        'message': 'message',
        'action': 'action',
        'code': 'code',
        'id': 'id'
    }

    def __init__(self, message=None, action=None, code=None, id=None):  # noqa: E501
        """HTTPResponse - a model defined in Swagger"""  # noqa: E501

        self._message = None
        self._action = None
        self._code = None
        self._id = None
        self.discriminator = None

        self.message = message
        self.action = action
        self.code = code
        if id is not None:
            self.id = id

    @property
    def message(self):
        """Gets the message of this HTTPResponse.  # noqa: E501


        :return: The message of this HTTPResponse.  # noqa: E501
        :rtype: str
        """
        return self._message

    @message.setter
    def message(self, message):
        """Sets the message of this HTTPResponse.


        :param message: The message of this HTTPResponse.  # noqa: E501
        :type: str
        """
        if message is None:
            raise ValueError("Invalid value for `message`, must not be `None`")  # noqa: E501

        self._message = message

    @property
    def action(self):
        """Gets the action of this HTTPResponse.  # noqa: E501

        Action performed by the server  # noqa: E501

        :return: The action of this HTTPResponse.  # noqa: E501
        :rtype: str
        """
        return self._action

    @action.setter
    def action(self, action):
        """Sets the action of this HTTPResponse.

        Action performed by the server  # noqa: E501

        :param action: The action of this HTTPResponse.  # noqa: E501
        :type: str
        """
        if action is None:
            raise ValueError("Invalid value for `action`, must not be `None`")  # noqa: E501

        self._action = action

    @property
    def code(self):
        """Gets the code of this HTTPResponse.  # noqa: E501

        Code of the response  # noqa: E501

        :return: The code of this HTTPResponse.  # noqa: E501
        :rtype: int
        """
        return self._code

    @code.setter
    def code(self, code):
        """Sets the code of this HTTPResponse.

        Code of the response  # noqa: E501

        :param code: The code of this HTTPResponse.  # noqa: E501
        :type: int
        """
        if code is None:
            raise ValueError("Invalid value for `code`, must not be `None`")  # noqa: E501

        self._code = code

    @property
    def id(self):
        """Gets the id of this HTTPResponse.  # noqa: E501

        Can be used to store the ID of the generated object  # noqa: E501

        :return: The id of this HTTPResponse.  # noqa: E501
        :rtype: str
        """
        return self._id

    @id.setter
    def id(self, id):
        """Sets the id of this HTTPResponse.

        Can be used to store the ID of the generated object  # noqa: E501

        :param id: The id of this HTTPResponse.  # noqa: E501
        :type: str
        """

        self._id = id

    def to_dict(self):
        """Returns the model properties as a dict"""
        result = {}

        for attr, _ in six.iteritems(self.swagger_types):
            value = getattr(self, attr)
            if isinstance(value, list):
                result[attr] = list(map(
                    lambda x: x.to_dict() if hasattr(x, "to_dict") else x,
                    value
                ))
            elif hasattr(value, "to_dict"):
                result[attr] = value.to_dict()
            elif isinstance(value, dict):
                result[attr] = dict(map(
                    lambda item: (item[0], item[1].to_dict())
                    if hasattr(item[1], "to_dict") else item,
                    value.items()
                ))
            else:
                result[attr] = value
        if issubclass(HTTPResponse, dict):
            for key, value in self.items():
                result[key] = value

        return result

    def to_str(self):
        """Returns the string representation of the model"""
        return pprint.pformat(self.to_dict())

    def __repr__(self):
        """For `print` and `pprint`"""
        return self.to_str()

    def __eq__(self, other):
        """Returns true if both objects are equal"""
        if not isinstance(other, HTTPResponse):
            return False

        return self.__dict__ == other.__dict__

    def __ne__(self, other):
        """Returns true if both objects are not equal"""
        return not self == other
