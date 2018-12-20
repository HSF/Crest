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


class GlobalTagDto(object):
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
        'name': 'str',
        'validity': 'float',
        'description': 'str',
        'release': 'str',
        'insertion_time': 'datetime',
        'snapshot_time': 'datetime',
        'scenario': 'str',
        'workflow': 'str',
        'type': 'str',
        'snapshot_time_milli': 'int',
        'insertion_time_milli': 'int'
    }

    attribute_map = {
        'name': 'name',
        'validity': 'validity',
        'description': 'description',
        'release': 'release',
        'insertion_time': 'insertionTime',
        'snapshot_time': 'snapshotTime',
        'scenario': 'scenario',
        'workflow': 'workflow',
        'type': 'type',
        'snapshot_time_milli': 'snapshotTimeMilli',
        'insertion_time_milli': 'insertionTimeMilli'
    }

    def __init__(self, name=None, validity=None, description=None, release=None, insertion_time=None, snapshot_time=None, scenario=None, workflow=None, type=None, snapshot_time_milli=None, insertion_time_milli=None):  # noqa: E501
        """GlobalTagDto - a model defined in Swagger"""  # noqa: E501

        self._name = None
        self._validity = None
        self._description = None
        self._release = None
        self._insertion_time = None
        self._snapshot_time = None
        self._scenario = None
        self._workflow = None
        self._type = None
        self._snapshot_time_milli = None
        self._insertion_time_milli = None
        self.discriminator = None

        if name is not None:
            self.name = name
        if validity is not None:
            self.validity = validity
        if description is not None:
            self.description = description
        if release is not None:
            self.release = release
        if insertion_time is not None:
            self.insertion_time = insertion_time
        if snapshot_time is not None:
            self.snapshot_time = snapshot_time
        if scenario is not None:
            self.scenario = scenario
        if workflow is not None:
            self.workflow = workflow
        if type is not None:
            self.type = type
        if snapshot_time_milli is not None:
            self.snapshot_time_milli = snapshot_time_milli
        if insertion_time_milli is not None:
            self.insertion_time_milli = insertion_time_milli

    @property
    def name(self):
        """Gets the name of this GlobalTagDto.  # noqa: E501


        :return: The name of this GlobalTagDto.  # noqa: E501
        :rtype: str
        """
        return self._name

    @name.setter
    def name(self, name):
        """Sets the name of this GlobalTagDto.


        :param name: The name of this GlobalTagDto.  # noqa: E501
        :type: str
        """

        self._name = name

    @property
    def validity(self):
        """Gets the validity of this GlobalTagDto.  # noqa: E501


        :return: The validity of this GlobalTagDto.  # noqa: E501
        :rtype: float
        """
        return self._validity

    @validity.setter
    def validity(self, validity):
        """Sets the validity of this GlobalTagDto.


        :param validity: The validity of this GlobalTagDto.  # noqa: E501
        :type: float
        """

        self._validity = validity

    @property
    def description(self):
        """Gets the description of this GlobalTagDto.  # noqa: E501


        :return: The description of this GlobalTagDto.  # noqa: E501
        :rtype: str
        """
        return self._description

    @description.setter
    def description(self, description):
        """Sets the description of this GlobalTagDto.


        :param description: The description of this GlobalTagDto.  # noqa: E501
        :type: str
        """

        self._description = description

    @property
    def release(self):
        """Gets the release of this GlobalTagDto.  # noqa: E501


        :return: The release of this GlobalTagDto.  # noqa: E501
        :rtype: str
        """
        return self._release

    @release.setter
    def release(self, release):
        """Sets the release of this GlobalTagDto.


        :param release: The release of this GlobalTagDto.  # noqa: E501
        :type: str
        """

        self._release = release

    @property
    def insertion_time(self):
        """Gets the insertion_time of this GlobalTagDto.  # noqa: E501


        :return: The insertion_time of this GlobalTagDto.  # noqa: E501
        :rtype: datetime
        """
        return self._insertion_time

    @insertion_time.setter
    def insertion_time(self, insertion_time):
        """Sets the insertion_time of this GlobalTagDto.


        :param insertion_time: The insertion_time of this GlobalTagDto.  # noqa: E501
        :type: datetime
        """

        self._insertion_time = insertion_time

    @property
    def snapshot_time(self):
        """Gets the snapshot_time of this GlobalTagDto.  # noqa: E501


        :return: The snapshot_time of this GlobalTagDto.  # noqa: E501
        :rtype: datetime
        """
        return self._snapshot_time

    @snapshot_time.setter
    def snapshot_time(self, snapshot_time):
        """Sets the snapshot_time of this GlobalTagDto.


        :param snapshot_time: The snapshot_time of this GlobalTagDto.  # noqa: E501
        :type: datetime
        """

        self._snapshot_time = snapshot_time

    @property
    def scenario(self):
        """Gets the scenario of this GlobalTagDto.  # noqa: E501


        :return: The scenario of this GlobalTagDto.  # noqa: E501
        :rtype: str
        """
        return self._scenario

    @scenario.setter
    def scenario(self, scenario):
        """Sets the scenario of this GlobalTagDto.


        :param scenario: The scenario of this GlobalTagDto.  # noqa: E501
        :type: str
        """

        self._scenario = scenario

    @property
    def workflow(self):
        """Gets the workflow of this GlobalTagDto.  # noqa: E501


        :return: The workflow of this GlobalTagDto.  # noqa: E501
        :rtype: str
        """
        return self._workflow

    @workflow.setter
    def workflow(self, workflow):
        """Sets the workflow of this GlobalTagDto.


        :param workflow: The workflow of this GlobalTagDto.  # noqa: E501
        :type: str
        """

        self._workflow = workflow

    @property
    def type(self):
        """Gets the type of this GlobalTagDto.  # noqa: E501


        :return: The type of this GlobalTagDto.  # noqa: E501
        :rtype: str
        """
        return self._type

    @type.setter
    def type(self, type):
        """Sets the type of this GlobalTagDto.


        :param type: The type of this GlobalTagDto.  # noqa: E501
        :type: str
        """

        self._type = type

    @property
    def snapshot_time_milli(self):
        """Gets the snapshot_time_milli of this GlobalTagDto.  # noqa: E501


        :return: The snapshot_time_milli of this GlobalTagDto.  # noqa: E501
        :rtype: int
        """
        return self._snapshot_time_milli

    @snapshot_time_milli.setter
    def snapshot_time_milli(self, snapshot_time_milli):
        """Sets the snapshot_time_milli of this GlobalTagDto.


        :param snapshot_time_milli: The snapshot_time_milli of this GlobalTagDto.  # noqa: E501
        :type: int
        """

        self._snapshot_time_milli = snapshot_time_milli

    @property
    def insertion_time_milli(self):
        """Gets the insertion_time_milli of this GlobalTagDto.  # noqa: E501


        :return: The insertion_time_milli of this GlobalTagDto.  # noqa: E501
        :rtype: int
        """
        return self._insertion_time_milli

    @insertion_time_milli.setter
    def insertion_time_milli(self, insertion_time_milli):
        """Sets the insertion_time_milli of this GlobalTagDto.


        :param insertion_time_milli: The insertion_time_milli of this GlobalTagDto.  # noqa: E501
        :type: int
        """

        self._insertion_time_milli = insertion_time_milli

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
        if issubclass(GlobalTagDto, dict):
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
        if not isinstance(other, GlobalTagDto):
            return False

        return self.__dict__ == other.__dict__

    def __ne__(self, other):
        """Returns true if both objects are not equal"""
        return not self == other
