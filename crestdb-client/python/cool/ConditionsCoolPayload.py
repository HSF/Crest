from pprint import pformat
from six import iteritems
import re
import json


class ConditionsCoolPayload(object):
    """
    Test class for Cool Blob generic reader and writer
    """
    def __init__(self, coolblob=None):
        """
        ConditionsCoolPayload - a model defined to contain a typical COOL resultset

        :param dict obj_types: The key is attribute name
                                  and the value is attribute type.
        :param dict attribute_map: The key is attribute name
                                  and the value is json key in definition.
        """
        #print 'Build ConditionsCoolPayload object...use argument ',coolblob

        self.obj_types = {
            'folder_payloadspec' : 'str',
            'node_description' : 'str',
            'modified_channels' : 'list',
            'nchans': 'int',
            'types': 'dict',
            'stime': 'int',
            'tag_name': 'str',
            'iov_base' : 'str',
            'data_array' : 'list'
        }

        self.attribute_map = {
            'folder_payloadspec' : 'FOLDER_PAYLOADSPEC',
            'node_description' : 'NODE_DESCRIPTION',
            'nchans': 'NCHANS',
            'modified_channels' : 'MOD_CHANNELS',
            'types': 'TYPES',
            'stime': 'STIME',
            'tag_name': 'TAG_NAME',
            'iov_base': 'IOV_BASE',
            'data_array' : 'DATA'
        }

        if coolblob is None:
            self._info = { 'FOLDER_PAYLOADSPEC' : '', 'NODE_DESCRIPTION' : '' }
            self._header = { 'MOD_CHANNELS': [], 'NCHANS': 0, 'TAG_NAME': 'Default', 'IOV_BASE': 'time', 'STIME': 0, 'TYPES': {} }
            self._data_array = []

        else:
            self._info = coolblob['COOL_NODE']
            self._header = coolblob['HEADER']
        
            self._data_array = coolblob[self.attribute_map['data_array']]
        #print 'Initialise structure for COOL JSON payload ',self._info

        self._folder_payloadspec = self._info[self.attribute_map['folder_payloadspec']]
        self._node_description = self._info[self.attribute_map['node_description']]
        self._nchans = self._header[self.attribute_map['nchans']]
        if self.attribute_map['modified_channels'] in self._header:
            self._modified_channels = self._header[self.attribute_map['modified_channels']]
        else:
            self._modified_channels = []
        self._types = self._header[self.attribute_map['types']]
        self._stime = self._header[self.attribute_map['stime']]
        self._tag_name = self._header[self.attribute_map['tag_name']]
        self._iov_base = self._header[self.attribute_map['iov_base']]
    
        self._cooltypes = [ 'Blob', 'UInt', 'Float', 'String', 'Int', 'Bool' ]
        self._oratypes = { 'Blob' : "<type 'cx_Oracle.LOB'>", 'UInt' : "<type 'int'>", 'Int' : "<type 'int'>", 'Float' : "<type 'float'>" , 'String' : "<type 'str'>", 'Bool' : "<type 'int'>"}
        
    @property
    def node_description(self):
        """
        Gets the node_description.


        :return: The node_description from COOL.
        :rtype: str
        """
        return self._node_description

    @node_description.setter
    def node_description(self, node_description):
        """
        Sets the node_description.

        :param node_description: The node_description of this Dto.
        :type: str
        """

        self._node_description = node_description

    @property
    def folder_payloadspec(self):
        """
        Gets the folder_payloaddesc.


        :return: The folder_payloaddesc from COOL.
        :rtype: str
        """
        return self._folder_payloadspec

    @folder_payloadspec.setter
    def folder_payloadspec(self, folder_payloadspec):
        """
        Sets the folder_payloadspec.

        :param folder_payloadspec: The folder_payloadspec of this Dto.
        :type: str
        """

        self._folder_payloadspec = folder_payloadspec

    @property
    def tag_name(self):
        """
        Gets the tag_name.


        :return: The tag_name from COOL.
        :rtype: str
        """
        return self._tag_name

    @tag_name.setter
    def tag_name(self, tag_name):
        """
        Sets the tag_name.

        :param tag_name: The tag_name of this Dto.
        :type: str
        """

        self._tag_name = tag_name

    @property
    def iov_base(self):
        """
        Gets the iov_base.


        :return: The iov_base from COOL.
        :rtype: str
        """
        return self._iov_base

    @iov_base.setter
    def iov_base(self, iov_base):
        """
        Sets the iov_base.

        :param iov_base: The iov_base of this Dto.
        :type: str
        """
        self._iov_base = iov_base

    @property
    def types(self):
        """
        Gets the types.


        :return: The types from COOL.
        :rtype: list
        """
        return self._types

    @types.setter
    def types(self, types):
        """
        Sets the types.

        :param types: The types of this Dto.
        :type: list
        """

        self._types = types

    @property
    def nchans(self):
        """
        Gets the nchans.


        :return: The nchans from COOL.
        :rtype: int
        """
        return self._nchans

    @nchans.setter
    def nchans(self, nchans):
        """
        Sets the nchans.

        :param nchans: The nchans of this Dto.
        :type: int
        """

        self._nchans = nchans

    @property
    def modified_channels(self):
        """
            Gets the modified_channels.
            
            
            :return: The modified_channels in this IOV.
            :rtype: list
            """
        return self._modified_channels
    
    @modified_channels.setter
    def modified_channels(self, modchannels):
        """
            Sets the modified_channels.
            
            :param modified_channels: The modified_channels list.
            :type: list
            """
        
        self._modified_channels = modchannels

    @property
    def stime(self):
        """
        Gets the stime.


        :return: The stime from COOL.
        :rtype: int
        """
        return self._stime

    @stime.setter
    def stime(self, stime):
        """
        Sets the stime.

        :param stime: The stime of this Dto.
        :type: int
        """

        self._stime = stime

    @property
    def data_array(self):
        """
        Gets the data_array.


        :return: The data_array from COOL.
        :rtype: list
        """
        return self._data_array

    @data_array.setter
    def data_array(self, data_array):
        """
        Sets the data_array.

        :param data_array: The data_array of this Dto.
        :type: list
        """

        self._data_array = data_array

    def oratype(self,atype):
        for k,v in self._oratypes.items():
            if atype == v:
                print ('type %s already exists..' % atype)
                return True
        return False

    def extend(self,colname,coltype):
        print ('Extend method is adding column %s of type %s' % (colname,coltype))

        itype = [ x for x in self._cooltypes if coltype[:3] in x ]
        #print 'Found type ',itype
        if itype[0] not in self._cooltypes:
            raise Exception('Cannot add unknown type {0}'.format(coltype))
        cooldbtype = itype[0]
        #print 'Selected cool type is ',cooldbtype
        if colname not in self._types:
            #print 'Add new column...',colname
            self._folder_payloadspec = ('{0},{1}:{2}'.format(self._folder_payloadspec,colname,coltype)).strip(',')
            #print 'Created folder spec ',self._folder_payloadspec
            self._types[colname] = self._oratypes[cooldbtype]

    def gettype(self,colname):
        cooltypes = self._folder_payloadspec.split(',')
        for acol in cooltypes:
            if colname in acol:
                coltype = acol.split(':')[1]
                return coltype
        return None

    def listChannels(self):
    #    for x in self._data_array:
    #        print x['CHANNEL_ID'],' has name ',x['CHANNEL_NAME']
        channelidlist = [ x['CHANNEL_ID'] for x in self._data_array ]
        return channelidlist
    
    def getChannelData(self,chanid):
        xdata = [ x for x in self._data_array if str(x['CHANNEL_ID']) == str(chanid)]
        columns = self.folder_payloadspec.split(',')
        columnnames = [ name.split(':') for i,name in enumerate(columns) ]
        ##print 'Retrieved payload row for channel ',self._cpchan,chdata
        datadict = {}        
        for (colname,ptype) in columnnames:
            columndata = xdata[0][colname]
#            if 'Blob' in ptype:
#                columndata = xdata[0][colname].decode('base64')
#            if 'String16M' in ptype:
#                columndata = xdata[0][colname].decode('base64')

            datadict[colname] = columndata
        
        return datadict
    
    def getChannelDataColumn(self,chanid,name):
        chandata = self.getChannelData(chanid)
        for columns in self._folder_payloadspec.split(','):
            (colname,ptype) = columns.split(':')
            if colname == name:
                if 'Blob' in ptype:
                    return chandata[name].decode('base64')
                return chandata[name]

    def getChannelDataColumnIndex(self,chanid,index):
        chandata = self.getChannelData(chanid)
        columns = self._folder_payloadspec.split(',')
        for i,name in enumerate(columns):
            if i == index:
                (colname,ptype) = name.split(':')
                if 'Blob' in ptype:
                    return chandata[colname].decode('base64')
                return chandata[colname]

    def addColumn(self, colname, coltype):
        self._types[colname] = coltype
   
    def addRecord(self, chanid, channame, iov_since, row):
        if len([ x for x in self._data_array if x['CHANNEL_ID'] == chanid]) > 0:
            raise Exception('Cannot add record for an existing channeld id : ',chanid)
        for key,val in row.items():
            #print 'Inserting key and value ',key,val
            if key not in self._folder_payloadspec:
                raise Exception('Cannot find column with name {0}'.format(key))
        row['CHANNEL_ID'] = chanid
        row['CHANNEL_NAME'] = channame
        row['IOV_SINCE'] = int(iov_since)
        self._data_array.append(row)
        self._nchans = len(self._data_array)
        self._modified_channels.append(chanid)

    def deleteRecord(self, chanid):
        removable = {}
        if len([ x for x in self._data_array if x['CHANNEL_ID'] == chanid]) > 0:
            for x in self._data_array:
                if x['CHANNEL_ID'] == chanid:
                    print ('Found element %s' % x)
                    removable = x
        if removable is not None:
            self._data_array.remove(removable)
            self._nchans = len(self._data_array)
            self._modified_channels.remove(chanid)

    def readPayloadFromFile(self,filename):
        with open(filename) as json_data:
            d = json.load(json_data)
            print(d)
            ddata=d['DATA']
            for row in ddata:
                print ('Retrieved data row  %s' % row)
            print ('length of data array: %d' % len(ddata))


    def clean(self):
        self._data_array = []
        self._modified_channels = []
        self._nchans = 0

    def pack(self):
        
        self._info[self.attribute_map['folder_payloadspec']] = self._folder_payloadspec
        self._info[self.attribute_map['node_description']] = self._node_description
        self._header[self.attribute_map['nchans']] = self._nchans
        self._header[self.attribute_map['modified_channels']] = self._modified_channels
        self._header[self.attribute_map['types']] = self._types
        self._header[self.attribute_map['stime']] = self._stime
        self._header[self.attribute_map['tag_name']] = self._tag_name
        self._header[self.attribute_map['iov_base']] = self._iov_base
        coolblob = {}
        coolblob['COOL_NODE'] = self._info
        coolblob['HEADER'] = self._header
        coolblob[self.attribute_map['data_array']] = self._data_array
        return coolblob

    def to_dict(self):
        """
        Returns the model properties as a dict
        """
        result = {}
        result_info = {}
        result_header = {}

        for attr, _ in self.obj_types.items():
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
                
        return result

    def to_str(self):
        """
        Returns the string representation of the model
        """
        return pformat(self.to_dict())

    def __repr__(self):
        """
        For `print` and `pprint`
        """
        return self.to_str()

    def __eq__(self, other):
        """
        Returns true if both objects are equal
        """
        return self.__dict__ == other.__dict__

    def __ne__(self, other):
        """
        Returns true if both objects are not equal
        """
        return not self == other
    
    @property
    def info(self):
        return self._info

    @info.setter
    def info(self, info):
        self._info = info

    
if __name__ == '__main__':
    cp = ConditionsCoolPayload()
    cp.extend('AFIELD','UInt32')
    cp.extend('ANOTHER','String255')
   # print cp.types, ' and payload spec ', cp.folder_payloadspec,' !!'
   # print 'Adding row....'
    cp.addRecord(3, 'test',100,{'AFIELD' : 30, 'ANOTHER' : 'pippo'})
   # print cp.to_str()
   # print 'Delete record for channel 3'
    cp.deleteRecord(3)
   # print cp.to_str()
   # print 'Re-add row....same channel id'
    cp.addRecord(3,'test',101,{'AFIELD' : 40, 'ANOTHER' : 'pluto'})
   # print cp.to_str()
    cp.addRecord(4,'test',101,{'AFIELD' : 50, 'ANOTHER' : 'pippo'})
   # print cp.to_str()
    cp.tag_name = 'LAT_TAG'
    cp.node_description = 'some description'
    cp.iov_base = 'run-lumi'
    #print cp.to_str()
    mb = cp.pack()
#    print 'The payload has been packed to a cool blob ',mb
