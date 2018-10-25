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
            'nchans': 'int',
            'modified_channels' : 'list',
            'stime': 'int',
            'tag_name': 'str',
            'data_array' : 'list'
        }

        self.attribute_map = {
            'folder_payloadspec' : 'FOLDER_PAYLOADSPEC',
            'node_description' : 'NODE_DESCRIPTION',
            'nchans': 'NCHANS',
            'modified_channels' : 'MOD_CHANNELS',
            'stime': 'STIME',
            'tag_name': 'TAG_NAME',
            'data_array' : 'DATA'
        }
        self._cb = {}

        if coolblob is None:
            self._cb[self.attribute_map['folder_payloadspec']] = ''
            self._cb[self.attribute_map['node_description']] = '' 
            self._cb[self.attribute_map['nchans']] = '' 
            self._cb[self.attribute_map['modified_channels']] = []
            self._cb[self.attribute_map['tag_name']] = 'Default' 
            self._cb[self.attribute_map['stime']] = 0
            self._data_array = []
            self._cb[self.attribute_map['data_array']] = self._data_array

        else:
            self._cb = coolblob        
            self._data_array = coolblob[self.attribute_map['data_array']]
        #print 'Initialise structure for COOL JSON payload ',self._info

        self._folder_payloadspec = self._cb[self.attribute_map['folder_payloadspec']]
        self._node_description = self._cb[self.attribute_map['node_description']]
        self._nchans = self._cb[self.attribute_map['nchans']]
        if self.attribute_map['modified_channels'] in self._cb:
            self._modified_channels = self._cb[self.attribute_map['modified_channels']]
        else:
            self._modified_channels = []
        self._stime = self._cb[self.attribute_map['stime']]
        self._tag_name = self._cb[self.attribute_map['tag_name']]
    
        self._types = {'Blob' : 'str', 'UInt' : 'int', 'Int' : 'int', 'Float' : 'float' , 'String' : 'str', 'Bool' : 'int'}
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
        print ('Found type %s' % itype)
        if itype[0] not in self._cooltypes:
            raise Exception('Cannot add unknown type {0}'.format(coltype))
        cooldbtype = itype[0]
        print ('Selected cool type is %s' % cooldbtype)
        if colname not in self._types:
            #print 'Add new column...',colname
            self._folder_payloadspec = ('{0},{1}:{2}'.format(self._folder_payloadspec,colname,coltype)).strip(',')

    def getColumns(self):
        colnames = [ x.split(':')[0] for x in self._folder_payloadspec.split(',') ]
        return colnames
            

    def gettype(self,colname):
        cooltypes = self._folder_payloadspec.split(',')
        for acol in cooltypes:
            if colname in acol:
                coltype = acol.split(':')[1]
                return coltype
        return None
    
    def gettypeat(self,idx):
        cooltypes = self._folder_payloadspec.split(',')
        #print ('Search for type in %s at index %s' %(cooltypes,idx))
        coltype = cooltypes[idx].split(':')[1]
        #print ('Retrieved type is %s' % coltype)
        for (k,v) in self._types.items():
            if k in coltype:
                #print ('Return python type %s' % v)
                return v
        return None

    def listChannels(self):
    #    for x in self._data_array:
    #        print x['CHANNEL_ID'],' has name ',x['CHANNEL_NAME']
        channelidlist = [ next(iter(x)) for x in self._data_array ]
        return channelidlist
    
    def __getval__(self, v, type, modtype='str'):
        val = v
        try:
            import codecs
        except Exception as e:
            print ('Exception in importing codecs %s' % e)
        if 'Blob' in type:
            if 'str' in modtype:
                #v = v.decode('base64')
                print ('Dump Blob as string')
                val = codecs.decode(v.encode(),'base64')
                #print('Reading data %s ' % (val))
                v = val
            elif 'zip' in modtype:
                #v = self.unzipcolumn(v.decode('base64'))
                print ('Dump Blob as zip')
                val = codecs.decode(v.encode(),'base64')
                v = self.unzipcolumn(val)
        elif 'String64k' in type:
            print ('Dump String64k as string after decoding...')
            val = codecs.decode(v.encode(),'base64')
            v = val
        elif 'String16M' in type:
            print ('Dump String16M as string after decoding...')
            val = codecs.decode(v.encode(),'base64')
            v = val
        val = v
        return val
        
    def getChannelData(self,chanid):
        xdata = [ x for x in self._data_array if str(next(iter(x))) == str(chanid)]
        #print('Use xdata = %s' % xdata)
        columns = self.folder_payloadspec.split(',')
        #columnnames = [ name.split(':') for i,name in enumerate(columns) ]
        ##print 'Retrieved payload row for channel ',self._cpchan,chdata
        datadict = {}        
        for idx,name in enumerate(columns):
            colname = name.split(':')[0]
            columndata = xdata[0][str(chanid)][idx]
            datadict[colname] = columndata
        
        return datadict
    
    def getChannelDataColumn(self,chanid,name):
        chandata = self.getChannelData(chanid)
        for columns in self._folder_payloadspec.split(','):
            (colname,ptype) = columns.split(':')
            if colname == name:
                return chandata[name]
#                val = self.__getval__(chandata[name], ptype)
#                return val
        return None
#                if 'Blob' in ptype:
#                    return chandata[name].decode('base64')
#                return chandata[name]

    def getChannelDataColumnIndex(self,chanid,index):
        chandata = self.getChannelData(chanid)
        columns = self._folder_payloadspec.split(',')
        for i,name in enumerate(columns):
            if i == index:
                (colname,ptype) = name.split(':')
#                if 'Blob' in ptype:
#                    return chandata[colname].decode('base64')
                return chandata[colname]

    def addColumn(self, colname, coltype):
        self._types[colname] = coltype
   
    def addRecord(self, chanid, iov_since, row):
        if len([ x for x in self._data_array if str(next(iter(x))) == str(chanid)]) > 0:
            raise Exception('Cannot add record for an existing channeld id : ',chanid)
        for idx,val in enumerate(row):
            #print ('Inserting value %s of type %s' % (val, type(val).__name__))
            tv = type(val).__name__
            if val is None:
                tv = self.gettypeat(idx)
            if tv != self.gettypeat(idx):
                print('Error in row : %s'%row)
                raise Exception('Cannot find column with name {0} {1} {2} {3}'.format(type(val).__name__,val,idx,chanid))
        datarow = {}
        datarow[str(chanid)] = row
        if self._stime == 0:
            self._stime = iov_since
        self._data_array.append(datarow)
        self._nchans = len(self._data_array)
        #print ('Add channel to list of modified...%s' % type(self._modified_channels))
        self._modified_channels.append(str(chanid))

    def deleteRecord(self, chanid):
        removable = {}
        if len([ x for x in self._data_array if str(next(iter(x))) == str(chanid)]) > 0:
            for x in self._data_array:
                if str(next(iter(x))) == str(chanid):
                    print ('Found element %s' % x)
                    removable = x
        if removable is not None:
            self._data_array.remove(removable)
            self._nchans = len(self._data_array)
            self._modified_channels.remove(str(chanid))

    def readPayloadFromFile(self,filename):
        with open(filename) as json_data:
            d = json.load(json_data)
            print(d)
            ddata=d[self.attribute_map['data_array']]
#            for row in ddata:
#                print ('Retrieved data row  %s' % row)
            print ('length of data array: %d' % len(ddata)) 
        
    def clean(self):
        self._data_array = []
        self._modified_channels = []
        self._nchans = 0

    def pack(self):
        
        self._cb[self.attribute_map['folder_payloadspec']] = self._folder_payloadspec
        self._cb[self.attribute_map['node_description']] = self._node_description
        self._cb[self.attribute_map['nchans']] = self._nchans
        self._cb[self.attribute_map['modified_channels']] = self._modified_channels
        self._cb[self.attribute_map['stime']] = self._stime
        self._cb[self.attribute_map['tag_name']] = self._tag_name
        self._cb[self.attribute_map['data_array']] = self._data_array
        return self._cb

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
    cp.addRecord(3, 100,[30, 'pluto'])
   # print cp.to_str()
   # print 'Delete record for channel 3'
    cp.deleteRecord(3)
   # print cp.to_str()
   # print 'Re-add row....same channel id'
    cp.addRecord(3,101,[40, 'pluto'])
   # print cp.to_str()
    cp.addRecord(4,101,[50, 'pippo'])
   # print cp.to_str()
    cp.tag_name = 'LAT_TAG'
    cp.node_description = 'some description'
    #print cp.to_str()
    mb = cp.pack()
#    print 'The payload has been packed to a cool blob ',mb
    print (mb)