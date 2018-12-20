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


class PayloadTagInfoDto(object):
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
        'tagname': 'str',
        'niovs': 'int',
        'totvolume': 'float',
        'avgvolume': 'float'
    }

    attribute_map = {
        'tagname': 'tagname',
        'niovs': 'niovs',
        'totvolume': 'totvolume',
        'avgvolume': 'avgvolume'
    }

    def __init__(self, tagname=None, niovs=None, totvolume=None, avgvolume=None):  # noqa: E501
        """PayloadTagInfoDto - a model defined in Swagger"""  # noqa: E501

        self._tagname = None
        self._niovs = None
        self._totvolume = None
        self._avgvolume = None
        self.discriminator = None

        if tagname is not None:
            self.tagname = tagname
        if niovs is not None:
            self.niovs = niovs
        if totvolume is not None:
            self.totvolume = totvolume
        if avgvolume is not None:
            self.avgvolume = avgvolume

    @property
    def tagname(self):
        """Gets the tagname of this PayloadTagInfoDto.  # noqa: E501


        :return: The tagname of this PayloadTagInfoDto.  # noqa: E501
        :rtype: str
        """
        return self._tagname

    @tagname.setter
    def tagname(self, tagname):
        """Sets the tagname of this PayloadTagInfoDto.


        :param tagname: The tagname of this PayloadTagInfoDto.  # noqa: E501
        :type: str
        """

        self._tagname = tagname

    @property
    def niovs(self):
        """Gets the niovs of this PayloadTagInfoDto.  # noqa: E501


        :return: The niovs of this PayloadTagInfoDto.  # noqa: E501
        :rtype: int
        """
        return self._niovs

    @niovs.setter
    def niovs(self, niovs):
        """Sets the niovs of this PayloadTagInfoDto.


        :param niovs: The niovs of this PayloadTagInfoDto.  # noqa: E501
        :type: int
        """

        self._niovs = niovs

    @property
    def totvolume(self):
        """Gets the totvolume of this PayloadTagInfoDto.  # noqa: E501


        :return: The totvolume of this PayloadTagInfoDto.  # noqa: E501
        :rtype: float
        """
        return self._totvolume

    @totvolume.setter
    def totvolume(self, totvolume):
        """Sets the totvolume of this PayloadTagInfoDto.


        :param totvolume: The totvolume of this PayloadTagInfoDto.  # noqa: E501
        :type: float
        """

        self._totvolume = totvolume

    @property
    def avgvolume(self):
        """Gets the avgvolume of this PayloadTagInfoDto.  # noqa: E501


        :return: The avgvolume of this PayloadTagInfoDto.  # noqa: E501
        :rtype: float
        """
        return self._avgvolume

    @avgvolume.setter
    def avgvolume(self, avgvolume):
        """Sets the avgvolume of this PayloadTagInfoDto.


        :param avgvolume: The avgvolume of this PayloadTagInfoDto.  # noqa: E501
        :type: float
        """

        self._avgvolume = avgvolume

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
        if issubclass(PayloadTagInfoDto, dict):
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
        if not isinstance(other, PayloadTagInfoDto):
            return False

        return self.__dict__ == other.__dict__

    def __ne__(self, other):
        """Returns true if both objects are not equal"""
        return not self == other
