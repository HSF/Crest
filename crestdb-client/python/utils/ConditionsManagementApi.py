
import sys
import os
import re
import time

# python 2 and python 3 compatibility library
from six import iteritems
from bisect import bisect_left

import json
import logging
import datetime

sys.path.append(os.path.join(sys.path[0],'..'))

from crestapi.apis import GlobaltagsApi, TagsApi, GlobaltagmapsApi, IovsApi, PayloadsApi, AdminApi
from crestapi.models import GlobalTagDto, TagDto, GlobalTagMapDto, IovDto,PayloadDto
from crestapi import ApiClient
from crestapi.rest import ApiException

import cdms_profile

MAX_OBJTYPE_LENGTH=1000

DefaultSnapshot = 2500000000

class ConditionManagementAbstract(object):

    '''Abstract class providing list of methods for conditions data management'''
    def createTag(self):
        pass
        raise NotImplementedError('Method not implemented here')
    def storeObject(self):
        pass
        raise NotImplementedError('Method not implemented here')
    def getTag(self):
        pass
        raise NotImplementedError('Method not implemented here')
    def listTags(self):
        pass
        raise NotImplementedError('Method not implemented here')
    def selectSnapshot(self):
        '''
            This method allows to query ALL IOVs for a given tag. The parameters allowed are:
            - snapshot : a long representing a time in seconds, which can be used to query a given
            version of the IOV. This is what can be used in core services via a locked global tag.
            '''
        pass
        raise NotImplementedError('Method not implemented here')
    def selectGroups(self):
        '''
            This method allows to retrieve a list of IOV pages (group size is determined at server level). The parameters allowed are:
            - snapshot : a long representing a time in seconds, which can be used to query a given
            version of the IOV. This is what can be used in core services via a locked global tag.
            '''
        pass
        raise NotImplementedError('Method not implemented here')
    def listIovs(self):
        '''
            This method allows to query IOVs for a given tag and in a range in time. The parameters allowed are:
            all_params = [ 'since', 'until', 'snapshot', 'type' ]
            1) tagname : required, a string containing a valid tag name
            2) since : optional, a string containing a since time. Default is 0.
            3) until : optional, a string containing an until time. Default is INF.
            4) snapshot : optional, a string containing the snapshot time in millisec.
            5) type : optional, can be "time" or "run-lumi". If "run-lumi" then it supposes
                      that the strings for since and until are expressed like : <arun>,<alumi block> 
        '''
        pass
        raise NotImplementedError('Method not implemented here')
    def listLastNIovs(self):
        pass
        raise NotImplementedError('Method not implemented here')
    def getPayload(self):
        pass
        raise NotImplementedError('Method not implemented here')
        
    def resolveTag(self):
        pass
        raise NotImplementedError('Method not implemented here')

    def runlumiToStime(self,run,lumi):
        stime = (int(run) << 32) | int(lumi)
        return stime
    
    def stimeToRunlumi(self,stime):
        run = int(stime) >> 32
        lumi = int(stime) & int('0xffffffff',16)
        return (run,lumi)
    

    def getGlobalTagMaps(self):
        '''
        Take a global tag and search the tag mapped
        '''
        pass
        raise NotImplementedError('Method not implemented here')
    

    def takeClosest(self, iovlist, stime):
        """
        Assumes iovlist is sorted. Returns closest value to stime.
        
        If two numbers are equally close, return the smallest number.
        """
        self._logger.debug('Received iov list of length %d and search for time %s' %  (len(iovlist),stime))
        pos = bisect_left(iovlist, stime)
        self._logger.debug('takeClosest : %d %s' % (pos,type(pos)))
        if pos == 0:
            return (iovlist[0], iovlist[1])
        if pos == len(iovlist):
            self._logger.debug('takeClosest: found as position the last element in iov list...')
        before = iovlist[pos - 1]
        after = iovlist[pos]
#        if after - myNumber < myNumber - before:
#           return after
#        else:
        return before,after
    
    def gethash(self, data):
        try: 
            import hashlib
        # Select iovs for a given tagname and in a given range.
        #print 'Compute payload hash from binary ',data
        
### orig...this one was working?
###            bd = bytes(data.encode('utf-8'))
            bd = bytes(data)
            mh=hashlib.sha256(bd).hexdigest()
        #pprint(mh)
            return mh
        except Exception as e:
            print ("Exception when calling gethash: %s\n" % e)
        return "none"
    
    def getCoolJson(self,payload):
        strinfo = ""
        datastr = ""
        #print ('Transforming payload as cool json format')
        try:
            import codecs
            strinfo = (codecs.decode(payload.streamer_info.encode(),'base64')).decode("utf-8")
            datastr = (codecs.decode(payload.data.encode(),'base64')).decode("utf-8")
            #print('Reading streamer %s and blob size %d' % (strinfo, len(datastr)))
        except Exception as e:
            print ('Exception in decoding in getCoolJson %s' % e)
        #strinfo = (payload.streamer_info).decode('base64')
        #datastr= (payload.data).decode('base64')
        if strinfo == 'COOL_JSON':
            #print ('old json format')
            try:
                from cool.ConditionsCoolPayload import ConditionsCoolPayload
            except Exception as e:
                print(e)
                raise
            self._logger.debug('Read data as json')
            coolpylddict = json.loads(datastr)
            self._logger.debug('Created dictionary from data %s' %  (coolpylddict))
            condcoolpyld = ConditionsCoolPayload(coolblob=coolpylddict)
            return condcoolpyld
        elif strinfo == 'SHAUNROE_JSON':
            #print ('Shaun json format')
            try:
                from cool.JsonCoolPayload import ConditionsCoolPayload
            except Exception as e:
                print(e)
                raise
            self._logger.debug('Read data as json')
            coolpylddict = json.loads(datastr)
            self._logger.debug('Created dictionary from data %s' %  (coolpylddict))
            condcoolpyld = ConditionsCoolPayload(coolblob=coolpylddict)
            return condcoolpyld
        else:
            print ('Cannot identify this as a COOL payload')
            return None
                
    def getChannelDataColumn(self,channelId,column,cooljson):
        return cooljson.getChannelDataColumn(channelId,column)
    
    def getChannelDataColumnIndex(self,channelId,column,cooljson):
        return cooljson.getChannelDataColumnIndex(channelId,column)
    
    def getChannelData(self,channelId,cooljson):
        return cooljson.getChannelData(channelId)
    
    def activatesocks(self):
        SOCKS5_PROXY_HOST = os.getenv('CDMS_SOCKS_HOST', 'localhost')
        SOCKS5_PROXY_PORT = 3129
        try:
            import socket
            import socks # you need to install pysocks (use the command: pip install pysocks)
# Configuration

# Remove this if you don't plan to "deactivate" the proxy later
#        default_socket = socket.socket
# Set up a proxy
#            if self.useSocks:
            socks.set_default_proxy(socks.SOCKS5, SOCKS5_PROXY_HOST, SOCKS5_PROXY_PORT)
            socket.socket = socks.socksocket
            print ('Activated socks proxy on %s:%s' % (SOCKS5_PROXY_HOST,SOCKS5_PROXY_PORT))
        except:
            print ('Error activating socks...%s %s' % (SOCKS5_PROXY_HOST,SOCKS5_PROXY_PORT))
        

class CMFileApi(ConditionManagementAbstract):

    def __init__(self):
        print ('Initialise CMFileApi...')
        self._basedir='/tmp/cdms'
        self._payloaddir = "data"
        self.__folder = None
        self.__iovlist = []
        self.__tag = None
        self.__coolpayload = None
        self.__payload = { 'hash' : None, 'payload' : None}
        
        logging.basicConfig(format='%(asctime)s %(message)s')
        self._logger = logging.getLogger('CMApi')
        self._logger.setLevel(logging.DEBUG)

    def sanitizeForSerialization(self,obj):    
        api_client = ApiClient()
        obj_dict = api_client.sanitize_for_serialization(obj)
#        obj_dict = {obj.attribute_map[attr]: getattr(obj, attr)
#                    for attr, _ in iteritems(obj.swagger_types) if getattr(obj, attr) is not None}
        return obj_dict

    def sanitizeForDeserialization(self,data, klass):    
        api_client = ApiClient()
        instance = api_client._ApiClient__deserialize(data, klass)
        return instance

    
    def checkdir(self,directory):
        if not os.path.exists(directory):
            os.makedirs(directory)
        return True
      
    def checkpayloaddir(self,tag,blobpath):
        directory = "%s/%s/%s" % (self._basedir,tag,self._payloaddir)
        blobdir =  "%s/%s" % (directory,blobpath)
        pdatadir = None
        if not os.path.exists(directory):
            # create data dir at basedir and create link to it in tag dir
            pdatadir = "%s/%s" % (self._basedir,self._payloaddir)
            os.makedirs(pdatadir)
            # create link
            os.symlink(pdatadir, directory)

        if not os.path.exists(blobdir):
            os.makedirs(blobdir)
            
        return blobdir
      
    def findblobdir(self, blobpath):
        self._logger.debug("Search for payload %s" % blobpath)
        for root, dirs, files in os.walk(self._basedir):
            self._logger.debug("findblobdir navigating in %s %s %s" % (root,dirs,files))
            if blobpath in files:
                return os.path.join(root, blobpath)        
        return None
      
    def dtoTojson(self,obj):
#        if isinstance(obj,(TagDto,IovDto,PayloadDto)):
            #dictobj = obj.to_dict()
        dictobj = self.sanitizeForSerialization(obj)
        jsonobj = json.dumps(dictobj, sort_keys=True,indent=4, separators=(',', ': '))
        return jsonobj
                
    def dumpToFile(self,jsonstr,filenamepath):
        with open(filenamepath,"w") as files:
            files.write(jsonstr)
            files.close()
                    
    def setlevel(self,loglevel):
        if 'DEBUG' == loglevel:
            self._logger.setLevel(logging.DEBUG)
        elif 'INFO' == loglevel:
            self._logger.setLevel(logging.INFO)
        elif 'WARN' == loglevel:
            self._logger.setLevel(logging.WARN)
        else:
            raise Exception("Cannot use log level %s " % loglevel)

    def createTag(self,tagname,**kwargs):
        all_params = [ 'time_type', 'object_type', 'synchronization', 'description', 'last_validated_time', 'end_of_validity' ]

        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method select_iovs" % key
                )
            params[key] = val
        del params['kwargs']
        tag_params = { 'time_type' : 'time', 'object_type' : 'json', 'synchronization' : 'none', 'description' : 'none', 'last_validated_time' : 0, 'end_of_validity' : 0 }
       
        if 'time_type' in params:
            tag_params['time_type'] = params['time_type']
        if 'object_type' in params:
            tag_params['object_type'] = params['object_type']
        if 'synchronization' in params:
            tag_params['synchronization'] = params['synchronization']
        if 'description' in params:
            tag_params['description'] = params['description']
        if 'last_validated_time' in params:
            tag_params['last_validated_time'] = params['last_validated_time']
        if 'end_of_validity' in params:
            tag_params['end_of_validity'] = params['end_of_validity']
   
        # check types
        if len(tag_params['object_type']) > MAX_OBJTYPE_LENGTH:
            raise ApiException('Cannot create tag with object_type length > %s' % MAX_OBJTYPE_LENGTH)         
        
        self._logger.debug('Creating tag dto %s %s ' % (tagname,tag_params))
        tagpath = "%s/%s" % (self._basedir,tagname)
        # If directory does not exists then it creates it
        self.checkdir(tagpath)
        
        try:
            dto = TagDto(tagname,time_type=tag_params['time_type'], object_type=tag_params['object_type'], synchronization=tag_params['synchronization'], description=tag_params['description'], last_validated_time=tag_params['last_validated_time'], end_of_validity=tag_params['end_of_validity']) 
            msg = ('Created dto resource %s ' ) % (dto.to_dict())
            self._logger.debug("createTag: %s" % msg)
            # HERE the low level API method
            tagfilename = ("%s/%s") % (tagpath,'tag.json')
            jsondto = self.dtoTojson(dto)
            self.dumpToFile(jsondto, tagfilename)
            return True
        except Exception as e:
            print ("Exception when calling FileApi->create_tag: %s\n" % e)
            raise
    
    def getTag(self,tagname):
        try:
            tagpath = "%s/%s" % (self._basedir,tagname)
            if not os.path.exists(tagpath):
                return False
            tagfilename = ("%s/%s") % (tagpath,'tag.json')
            jsonobj = None
            self._logger.debug("Loading data from json file %s" % tagfilename)
            with open(tagfilename) as json_data:
                jsonobj = json.load(json_data)
                json_data.close()

            self._logger.debug("Loaded json from file %s %s" % (tagfilename,jsonobj))
#            dtodict = jsonobj
#            dto = TagDto(dtodict['name'],time_type=dtodict['time_type'], object_type=dtodict['object_type'], synchronization=dtodict['synchronization'], description=dtodict['description'], last_validated_time=dtodict['last_validated_time'], end_of_validity=dtodict['end_of_validity']) 
            dto = self.sanitizeForDeserialization(jsonobj,'TagDto')                       
            return dto
       
        except Exception as e:
            print(e)
        return None

    def listTags(self,tagnamepattern):
        try:
            tagpath = "%s" % (self._basedir)
            taglist = [x[0] for x in os.walk(tagpath) if tagnamepattern in x[0]]

            print ('List of filtered tags %s' % taglist)
            return taglist
        except Exception as e:
            print ("Exception when calling TagsApi->find_tag: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None
        
            
    def listIovs(self,tagname,**kwargs):
        all_params = [ 'since', 'until', 'snapshot', 'type' ]

        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method select_iovs" % key
                )
            params[key] = val
        del params['kwargs']
        search_params = { 'tagname' : tagname, 'since' : '0', 'until' : 'INF', 'snapshot' : 0, 'type' : 'time' }
        try:
            self._logger.debug('List Iovs using arguments: %s ' % (str(search_params)))
            tagpath = "%s/%s" % (self._basedir,tagname)
            if not os.path.exists(tagpath):
                return False
            iovsfilename = ("%s/%s") % (tagpath,'iovs.json')
            jsonobj = None
            self._logger.debug("Loading data from json file %s" % iovsfilename)
            jsonobj = []
            if os.path.isfile(iovsfilename):
                with open(iovsfilename,"r") as json_data:
                    jsonobj = json.load(json_data)
                    json_data.close()
            dtolist =  self.sanitizeForDeserialization(jsonobj,'list[IovDto]')                       
            self._logger.debug("Return list of iovs...%d" % len(dtolist))
            return dtolist
        except Exception as e:
            print('listiovs got exception %s' % e)
        return None
    
   

    @cdms_profile.profile
    def storeObject(self,tagname,since,data,**kwargs):
        all_params = ['tag_name', 'since', 'insertion_time', 'payload_hash', 'streamer_info', 'object_type', 'version', 'dataflag' ]

        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method select_iovs" % key
                )
            params[key] = val
        del params['kwargs']
        instime = datetime.datetime.now() # time as string
        iov_params = { 'since' : since, 'tag_name' : tagname }
        self._logger.debug ('Creating iov dto  %s %s ' % (tagname,iov_params))
        p_params = { 'insertion_time' : instime, 'streamer_info' : 'none'.encode('base64'), 'version' : 'test', 'object_type' : 'json-file'}
        
        if 'streamer_info' in params:
            p_params['streamer_info'] = params['streamer_info'].encode('base64')
        if 'object_type' in params:
            p_params['object_type'] = params['object_type']
        if 'version' in params:
            p_params['version'] = params['version']
        try:
            self._logger.debug('store object using arguments: %s ' % (str(p_params)))
            tagpath = "%s/%s" % (self._basedir,tagname)
            if not os.path.exists(tagpath):
                return False
            
            iovlist = self.listIovs(tagname)
            self._logger.debug("Loaded list of iovs from json file...%s" % iovlist)
            
            if 'dataflag' not in params:
                params['dataflag'] = 'inmemory'
                
            datapayload = data
            if params['dataflag'] == 'fromfile':
                # compute hash from file
                mhash=None
    
                with open(data, mode='rb') as file: # b is important -> binary
                    fileContent = file.read()
                    datapayload = fileContent
                    mhash = self.gethash(fileContent)
                if mhash is None:
                    raise ValueError('Cannot store payload with null hash')
                p_params['hash'] = mhash
                p_params['data']  = None
                iov_params['payload_hash'] = mhash

            elif params['dataflag'] == 'inmemory':
                #print 'Use data as in memory object...'
            ## Consider default for dataflag to be "inmemory"
                phash = self.gethash(data)
                p_params['hash'] = phash
                iov_params['payload_hash'] = phash
                
            p_params['data'] = data.encode('base64')

            pdto = PayloadDto(hash=p_params['hash'], version=p_params['version'], object_type=p_params['object_type'], data=p_params['data'], streamer_info=p_params['streamer_info'], insertion_time=p_params['insertion_time'])
            iovdto = IovDto(tag_name=iov_params['tag_name'], since=iov_params['since'], insertion_time=p_params['insertion_time'], payload_hash=iov_params['payload_hash'])
                
            print ('store payload with hash: %s  and first 10 characters ' % (pdto.hash,pdto.hash[:10]))
            ##filename = "%s/%s/%s.blob" % (self._payloaddir,iov_params['payload_hash'])

            payloaddatadir = self.checkpayloaddir(tagname,pdto.hash[:10])

            #jsondict = pdto.to_dict()
            jsonobj = self.dtoTojson(pdto)
            ##jsonobj = json.dumps(jsondict)
            ###print '....Content to dump: ',data,jsonobj
            filename = "%s/%s.blob" % (payloaddatadir,pdto.hash)
            if os.path.isfile(filename):
                self._logger.info("File for payload %s already exists!" % filename)
            else:
                self.dumpToFile(jsonobj, filename)

#                with open(filename,"w") as pfile:
#                    pfile.write(jsonobj)
#                    pfile.close()
            # Store iov for the created payload
            
            print ("store iov with since %s and append to %s" % (iov_params['since'],iovlist))
            iovlist.append(iovdto)
            print ("Now try to serialize the list in iovs.json")
            iovjsondto = self.dtoTojson(iovlist)
            self._logger.debug("Json formatted list is %s" %iovjsondto)

            #iovlistjson = json.dumps(iovlist)
            #print 'Json serialization gives ', iovlistjson

            iovsfilename = ("%s/%s") % (tagpath,'iovs.json')
            if os.path.isfile(iovsfilename):
                os.rename(iovsfilename,"%s.old" % iovsfilename)
                
            self.dumpToFile(iovjsondto, iovsfilename)
    
#            with open(iovsfilename,"w") as iovfile:
#                iovfile.write(iovlistjson)
#                iovfile.close()
            
            print ('stored object using %s %d' % (iov_params,sys.getsizeof(p_params)))
        except Exception as e:
            print ("Exception when calling storeObject*: %s\n" % e )       

        return True
      
    @cdms_profile.profile
    def getPayload(self, phash):
        if self.__payload['hash'] == phash:
            return self.__payload['payload']
        #api_instance = PayloadsApi(self.api_client)
        try: 
            blobpath = self.findblobdir("%s.blob" % phash)
            self._logger.debug("Found blob %s" % blobpath)
            
            jsonobj = None
            self._logger.debug("Loading data from payload file %s" % blobpath)
            if os.path.isfile(blobpath):
                with open(blobpath,"r") as json_data:
                    jsonobj = json.load(json_data)
                    json_data.close()
            self._logger.debug("Deserialize json %s" % jsonobj)                    
            payloaddto =  self.sanitizeForDeserialization(jsonobj,'PayloadDto')                       
            return payloaddto
        except Exception as e:
            print ("Exception when calling getPayload: %s\n" % e)
        return None


class IovDtoWrap():
    
    def __init__(self, iovdto, tagtype):
        #print('Create iov wrapper using %s and tag %s' % (iovdto,tagtype))
        self._iovdto = iovdto
        self._tagtype = tagtype
        
    def stimeToRunlumi(self,stime):
        run = int(stime) >> 32
        lumi = int(stime) & int('0xffffffff',16)
        return (run,lumi)

    def to_str(self):
        #print('method to_str called...')
        strstime = None
        if 'header' in self._tagtype:
            msg = "%s [%s][%s]: %s" % ("since 63 bits", "since human readable", "insertion time", "hash")
            return msg
        if 'time' in self._tagtype:
            st = int(self._iovdto.since)/1000000000
            strstime = datetime.datetime.fromtimestamp(st).strftime('%Y-%m-%d %H:%M:%S')
        else:
            (run,lb) = self.stimeToRunlumi(self._iovdto.since)
            strstime = "%s - %s" % (run,lb)
        msg = "%d [%s][%s]: %s" % (self._iovdto.since, strstime, self._iovdto.insertion_time,self._iovdto.payload_hash)
        ##print('method to_str uses %s...' % msg)
        return msg

    def __repr__(self):
        """
        For `print` and `pprint`
        """
        return self.to_str()
    
class GlobalTagDtoWrap():
    
    def __init__(self, tdto):
        self._tdto = tdto

    def to_str(self):
        msg = "%s: %s; %s" % (self._tdto.name, self._tdto.description,self._tdto.workflow)
        return msg

    def __repr__(self):
        """
        For `print` and `pprint`
        """
        return self.to_str()

class GlobalTagMapDtoWrap():
    
    def __init__(self, tdto):
        self._tdto = tdto

    def to_str(self):
        msg = "%s -> %s %s %s" % (self._tdto._global_tag_name, self._tdto.tag_name,self._tdto.record,self._tdto.label)
        return msg

    def __repr__(self):
        """
        For `print` and `pprint`
        """
        return self.to_str()

class TagDtoWrap():
    
    def __init__(self, tdto):
        self._tdto = tdto

    def to_str(self):
        msglist=[]
        msg = "tag:  {:<70} [{:>10}] {:<80}".format(self._tdto.name, self._tdto.time_type,self._tdto.object_type)
        msglist.append(msg)
        msg = "\t \t \t insertion time={} \n\t \t \t synchro={:>20}".format(self._tdto.insertion_time,self._tdto.synchronization)
        msglist.append(msg)
        #msg = "tag: %s [%10s]: %100s \n\tinsertion time=%s\n\tsynchro=%s\n\tdescription=%s" % (self._tdto.name, self._tdto.time_type,self._tdto.object_type,self._tdto.insertion_time,self._tdto.synchronization,self._tdto.description)
        return "\n".join(msglist)

    def __repr__(self):
        """
        For `print` and `pprint`
        """
        return self.to_str()

class TagInfoWrap():
    
    def __init__(self, tdto,tsum):
        self._tdto = tdto
        self._tsum = tsum

    def to_str(self):
        msglist=[]
        msg = "tag:  {:<70} [{:>10}] {:<80}".format(self._tdto.name, self._tdto.time_type,self._tdto.object_type)
        msglist.append(msg)
        msg = "\t \t \t insertion time={} \n\t \t \t synchro={:>20}".format(self._tdto.insertion_time,self._tdto.synchronization)
        msglist.append(msg)
        msg = "\t \t \t description={} ".format(self._tdto.description)
        msglist.append(msg)
        msg = "\t \t \t niovs={} ".format(self._tsum.niovs)
        msglist.append(msg)
        #msg = "tag: %s [%10s]: %100s \n\tinsertion time=%s\n\tsynchro=%s\n\tdescription=%s" % (self._tdto.name, self._tdto.time_type,self._tdto.object_type,self._tdto.insertion_time,self._tdto.synchronization,self._tdto.description)
        return "\n".join(msglist)

    def __repr__(self):
        """
        For `print` and `pprint`
        """
        return self.to_str()

class PayloadDtoWrap():
    
    def __init__(self, pdto):
        self._pdto = pdto

    def to_str(self):
        stinfo = ""
        try:
            import codecs
            strinfo = codecs.decode(self._pdto.streamer_info.encode(),'base64')
            print('Reading streamer %s' % (strinfo))
        except Exception as e:
            print ('Exception in decoding while wrapping to payload dto %s' % e)

        ###stinfo = str(self._pdto.streamer_info.decode('base64'))
        msg = "%s %s %s %s; data size = %d" % (self._pdto.object_type, self._pdto.version, self._pdto.insertion_time, stinfo, len(self._pdto.data))
        return msg

    def __repr__(self):
        """
        For `print` and `pprint`
        """
        return self.to_str()


class NormalPayloadWrap():
    def __init__(self, pdto):
        self._pdto = pdto

    def to_str(self):
        datadict = self._pdto.to_dict()
        msglist = []
        for (k,v) in datadict.items():
            val = v
            if 'data' == k:
                try:
                    import codecs
                    val = codecs.decode(v.encode(),'base64')
                    print('Reading normal payload data %s ' % (val))
                    v = val
                except Exception as e:
                    print ('warning: exception in decoding normal payload dto %s' % e)
                #val = v.decode('base64')
            elif "streamer_info" == k:
                try:
                    import codecs
                    val = codecs.decode(v.encode(),'base64')
                    print('Reading normal streamer info %s ' % (val))
                    v = val
                except Exception as e:
                    print ('warning: exception in decoding %s' % e)
                     
            try:
                val = v.decode('utf-8','ignore') 
                    #val = v.decode('utf-8','ignore')
                msg = " - %s = %s " % (k,str(val))
            except Exception as e1:
                print ('warning: exception in decoding: %s ' % e1)
                msg = " - %s = %s " % (k,v)

            msglist.append(msg)
                 
            #msg = '%s = %s ' % (k,val)
            #msglist.append(msg)
        
        return "\n".join(msglist)

    def __repr__(self):
        """
        For `print` and `pprint`
        """
        return self.to_str()

class CoolPayloadWrap():
    
    def __init__(self, pdto, chanid, colsel):
        self._coolpdto = pdto
        self._chanid = chanid
        self._colselection = colsel
        self._wholepyld = ','.join(colsel)
        logging.basicConfig(format='%(asctime)s %(message)s')
        self._logger = logging.getLogger('CoolPayloadWrap')
        self._logger.setLevel(logging.INFO)

        
    def getcol(self,cn):
        crep = [ x.split(':')[1] for x in self._colselection if cn in x ]
        origrep = self._coolpdto.gettype(cn)
        return (crep,origrep)
    
    def unzipcolumn(self,data):
        import zlib
        unzipped = zlib.decompress(data)
        return unzipped

    def to_str(self):
        chdata = self._coolpdto.getChannelData(self._chanid)
        msglist = []
        msg = "channel: %s\n" % (self._chanid)
        msglist.append(msg)
        for (k,v) in chdata.items():
            if (k in self._wholepyld):
                (crep,origrep) = self.getcol(k)
                val = v
                #print ('Printing %s with types %s %s' % (val,crep,origrep))
                try:
                    import codecs
                except Exception as e:
                    print ('Exception in importing codecs %s' % e)
                if 'Blob' in origrep:
                    if 'str' in crep:
                        #v = v.decode('base64')
                        val = codecs.decode(v.encode(),'base64')
                        #print('Reading data %s ' % (val))
                        v = val
                    elif 'zip' in crep:
                        #v = self.unzipcolumn(v.decode('base64'))
                        val = codecs.decode(v.encode(),'base64')
                        v = self.unzipcolumn(val)
                elif 'String64k' in origrep:
                    val = codecs.decode(v.encode(),'base64')
                    v = val
                elif 'String16M' in origrep:
                    val = codecs.decode(v.encode(),'base64')
                    v = val
                    #v = v.decode('base64')

                try:
                    val = v.decode('utf-8','ignore') 
                    #val = v.decode('utf-8','ignore')
                    msg = " - %s = %s " % (k,str(val))
                except Exception as e1:
                    self._logger.debug("warn: coolpayload wrap is not encoded: %s " % e1)                    
                    msg = " - %s = %s " % (k,v)

                msglist.append(msg)
        
        return "\n".join(msglist)

    def __repr__(self):
        """
        For `print` and `pprint`
        """
        return self.to_str()

    

class CMApi(ConditionManagementAbstract):

    def __init__(self):
        print ('Initialise CMApi...')
        self._sockson=False
        self.urlsvc = os.getenv('CDMS_HOST', 'http://localhost:8080/crestapi')
        if os.getenv('CDMS_SOCKS', False) and not self._sockson:
            self.activatesocks()
            self._sockson = True
        self.api_client = ApiClient(host=self.urlsvc)
        self.__folder = None
        self.__iovlist = []
        self.__tag = None
        self.__coolpayload = None
        self.__payload = { 'hash' : None, 'payload' : None}
        
        logging.basicConfig(format='%(asctime)s %(message)s')
        self._logger = logging.getLogger('CMApi')
        self._logger.setLevel(logging.INFO)
    
    def setlevel(self,loglevel):
        if 'DEBUG' == loglevel:
            self._logger.setLevel(logging.DEBUG)
        elif 'INFO' == loglevel:
            self._logger.setLevel(logging.INFO)
        elif 'WARN' == loglevel:
            self._logger.setLevel(logging.WARN)
        else:
            raise Exception("Cannot use log level %s " % loglevel)

    def updateTag(self,tagname,body):
        
        api_instance = TagsApi(self.api_client)
        try:
            api_response = api_instance.update_tag(tagname, body)
            return api_response
        except ApiException as e:
            print ("Exception when calling TagsApi->update_tag: %s\n" % e)
            raise

    def createTag(self,tagname,**kwargs):
        all_params = [ 'time_type', 'object_type', 'synchronization', 'description', 'last_validated_time', 'end_of_validity' ]

        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method select_iovs" % key
                )
            params[key] = val
        del params['kwargs']
        tag_params = { 'time_type' : 'time', 'object_type' : 'json', 'synchronization' : 'none', 'description' : 'none', 'last_validated_time' : 0, 'end_of_validity' : 0 }
       
        if 'time_type' in params:
            tag_params['time_type'] = params['time_type']
        if 'object_type' in params:
            tag_params['object_type'] = params['object_type']
        if 'synchronization' in params:
            tag_params['synchronization'] = params['synchronization']
        if 'description' in params:
            tag_params['description'] = params['description']
        if 'last_validated_time' in params:
            tag_params['last_validated_time'] = params['last_validated_time']
        if 'end_of_validity' in params:
            tag_params['end_of_validity'] = params['end_of_validity']

        # check types
        if len(tag_params['object_type']) > MAX_OBJTYPE_LENGTH:
            raise ApiException('Cannot create tag with object_type length > %s' % MAX_OBJTYPE_LENGTH)         
        self._logger.debug('Creating tag dto %s %s ' % (tagname,tag_params))
        api_instance = TagsApi(self.api_client)
        try:
            dto = TagDto(tagname,time_type=tag_params['time_type'], object_type=tag_params['object_type'], synchronization=tag_params['synchronization'], description=tag_params['description'], last_validated_time=tag_params['last_validated_time'], end_of_validity=tag_params['end_of_validity']) 
            msg = ('Created dto resource %s ' ) % (dto.to_dict())
            api_response = api_instance.create_tag(dto)
            return api_response
        except ApiException as e:
            print ("Exception when calling TagsApi->create_tag: %s\n" % e)
            if e.status == 303: # the tag already exists....
                return e.body
            raise
    
    def getTag(self,tagname):
        api_instance = TagsApi(self.api_client)
        try:
            self._logger.debug ('Search for tag %s' % tagname)
            api_response = api_instance.find_tag(tagname)
            return api_response
        except ApiException as e:
            print ("Exception when calling TagsApi->find_tag: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None

    def listTags(self,tagnamepattern):
        api_instance = TagsApi(self.api_client)
        try:
            searchby='name:{0}'.format(tagnamepattern)
            api_response = api_instance.list_tags(by=searchby)
            return api_response
        except ApiException as e:
            print ("Exception when calling TagsApi->find_tag: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None

    def createGlobalTag(self,gtagname,**kwargs):
        all_params = [ 'validity', 'release', 'description', 'snapshot_time', 'scenario', 'workflow', 'type' ]

        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method select_iovs" % key
                )
            params[key] = val
        del params['kwargs']
        tag_params = { 'validity' : 0, 'release' : 'none', 'description' : 'none', 'snapshot_time' : 0, 'scenario' : 'none', 'workflow' : 'none', 'type' : 'none' }
       
        if 'validity' in params:
            tag_params['validity'] = params['validity']
        if 'release' in params:
            tag_params['release'] = params['release']
        if 'description' in params:
            tag_params['description'] = params['description']
        if 'snapshot_time' in params:
            tag_params['snapshot_time'] = params['snapshot_time']
        if 'scenario' in params:
            tag_params['scenario'] = params['scenario']
        if 'workflow' in params:
            tag_params['workflow'] = params['workflow']
        if 'type' in params:
            tag_params['type'] = params['type']
        
        self._logger.debug('Creating global tag dto %s %s ' % (gtagname,tag_params))
        try:
            gtag = self.getGlobalTag(gtagname)
            self._logger.debug('Global Tag found: %s ...' % (gtag.name))
            return gtag
        except Exception as e:
            self._logger.debug('Global Tag with name: %s does not exists...creating it now....' % gtagname)

        api_instance = GlobaltagsApi(self.api_client)
        try:
            dto = GlobalTagDto(gtagname,validity=tag_params['validity'], release=tag_params['release'], description=tag_params['description'], snapshot_time=tag_params['snapshot_time'], scenario=tag_params['scenario'], workflow=tag_params['workflow'], type=tag_params['type']) 
            msg = ('Created dto resource %s ' ) % (dto.to_dict())
            api_response = api_instance.create_global_tag(dto)
            return api_response
        except ApiException as e:
            print ("Exception when calling GlobaltagsApi->create_global_tag: %s\n" % e)
            if e.status == 303: # the global tag already exists....
                return e.body
            raise

    def createGlobalTagMap(self,gtagname,tagname,**kwargs):
        all_params = [ 'record', 'label' ]

        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method select_iovs" % key
                )
            params[key] = val
        del params['kwargs']
        tag_params = { 'record' : 'none', 'label' : 'none' }
       
        if 'record' in params:
            tag_params['record'] = params['record']
        if 'label' in params:
            tag_params['label'] = params['label']
      
        self._logger.debug('Creating global tag map dto %s %s %s ' % (gtagname, tagname, tag_params))
       
        api_instance = GlobaltagmapsApi(self.api_client)
        try:
            dto = GlobalTagMapDto(global_tag_name=gtagname, tag_name=tagname,record=tag_params['record'], label=tag_params['label']) 
            msg = ('Created dto resource %s ' ) % (dto.to_dict())
            api_response = api_instance.create_global_tag_map(dto)
            return api_response
        except ApiException as e:
            print ("Exception when calling GlobaltagmapsApi->create_global_tag_map: %s\n" % e)
            raise

    def getGlobalTag(self,gtname):
        api_instance = GlobaltagsApi(self.api_client)
        try:
            api_response = api_instance.find_global_tag(gtname)
            return api_response
        except ApiException as e:
            print ("Exception when calling GlobaltagsApi->find_global_tag: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None

    def listGlobalTags(self,tagnamepattern):
        api_instance = GlobaltagsApi(self.api_client)
        try:
            searchby='name:{0}'.format(tagnamepattern)
            api_response = api_instance.list_global_tags(by=searchby)
            return api_response
        except ApiException as e:
            print ("Exception when calling GlobaltagsApi->list_global_tags: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None

    def getGlobalTagMaps(self,globaltagname):
        api_instance = GlobaltagmapsApi(self.api_client)
        try:
            api_response = api_instance.find_global_tag_map(globaltagname)
            return api_response
        except ApiException as e:
            print ("Exception when calling GlobaltagmapsApi->find_global_tag_map: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None

    def resolveTag(self,globaltagname,**kwargs):
        all_params = [ 'folder' ]
        
        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                                "Got an unexpected keyword argument '%s'"
                                " to method resolveTag" % key
                                )
            params[key] = val
        del params['kwargs']
        search_params = { 'globaltag' : globaltagname, 'folder' : 'DEFAULT' }
        if 'folder' in params:
            search_params['folder'] = params['folder']

        maplist = self.getGlobalTagMaps(globaltagname)
        if search_params['folder'] is not None:
            tagname = [ x.tag_name for x in maplist if x.label == search_params['folder']]
            return tagname
            
        return None
        
            
    def listIovs(self,tagname,**kwargs):
        all_params = [ 'since', 'until', 'snapshot', 'type' ]

        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method select_iovs" % key
                )
            params[key] = val
        del params['kwargs']
        search_params = { 'tagname' : tagname, 'since' : '0', 'until' : 'INF', 'snapshot' : 0, 'type' : 'time' }
        timetypesearch = 'time'
        if 'type' in params:
            timetypesearch = params['type']
        if 'since' in params:
            search_params['since'] = params['since']
            if timetypesearch == 'run-lumi':
                run_lumi = [x.strip() for x in params['since'].split(',')]
                (run,lumi) = (run_lumi[0],run_lumi[1])
                self._logger.debug('Use since from run and lumi %s %s ' % (run,lumi))
                search_params['since'] = self.runlumiToStime(run,lumi)
                    
        if 'until' in params:
            search_params['until'] = params['until']
            if timetypesearch == 'run-lumi':
                run_lumi = [x.strip() for x in params['until'].split(',')]
                (run,lumi) = (run_lumi[0],run_lumi[1])
                self._logger.debug('Use until from run and lumi %s %s ' % (run,lumi))
                search_params['until'] = self.runlumiToStime(run,lumi)

        if 'snapshot' in params:
            search_params['snapshot'] = params['snapshot']
        
        api_instance = IovsApi(self.api_client)
        try:
            self._logger.debug('List Iovs using arguments: %s ' % (str(search_params)))
            api_response = api_instance.select_iovs(tagname=search_params['tagname'],
                    since=search_params['since'],until=search_params['until'],snapshot=search_params['snapshot'])
            self._logger.debug('listIovs: retrieved response of length %d ' % len(api_response))
            return api_response
        except ApiException as e:
            print ("Exception when calling IovsApi->select_iovs: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None
   
    def getSize(self,tagname,**kwargs):
        all_params = [ 'snapshot' ]
        
        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                                "Got an unexpected keyword argument '%s'"
                                " to method select_iovs" % key
                                )
            params[key] = val
        del params['kwargs']
        search_params = { 'tagname' : tagname, 'snapshot' : 0 }
        
        if 'snapshot' in params:
            search_params['snapshot'] = params['snapshot']
        
        api_instance = IovsApi(self.api_client)
        try:
            api_response = api_instance.get_size_by_tag(search_params['tagname'])
            return api_response
        except ApiException as e:
            print ("Exception when calling IovsApi->find_all_iovs: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None
    
    def selectGroups(self,tagname,**kwargs):
        all_params = [ 'snapshot' ]
        
        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                                "Got an unexpected keyword argument '%s'"
                                " to method select_iovs" % key
                                )
            params[key] = val
        del params['kwargs']
        search_params = { 'tagname' : tagname, 'snapshot' : 0 }
        
        if 'snapshot' in params:
            search_params['snapshot'] = params['snapshot']
        
        api_instance = IovsApi(self.api_client)
        try:
            api_response = api_instance.select_groups(tagname=search_params['tagname'],
                                                      snapshot=search_params['snapshot'])
            return api_response
        except ApiException as e:
            print ("Exception when calling IovsApi->find_all_iovs: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None
                
    def selectSnapshot(self,tagname,**kwargs):
        all_params = [ 'snapshot' ]
    
        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                                "Got an unexpected keyword argument '%s'"
                                " to method select_iovs" % key
                                )
            params[key] = val
    
        del params['kwargs']
        search_params = { 'tagname' : tagname, 'snapshot' : DefaultSnapshot*1000 }
        
        if 'snapshot' in params:
            search_params['snapshot'] = params['snapshot']
        
        api_instance = IovsApi(self.api_client)
        try:
            api_response = api_instance.select_snapshot(tagname=search_params['tagname'],
                                                            snapshot=search_params['snapshot'])
            ###print 'Retrieved list of iovs ',api_response
            return api_response
        except ApiException as e:
            print ("Exception when calling IovsApi->find_all_iovs: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None


    def listLastNIovs(self,tagname,niovs,**kwargs):
        all_params = [ 'by', 'sort', 'size' ]

        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method listLastNIovs" % key
                )
            params[key] = val
        del params['kwargs']
        search_params = { 'tagname' : tagname, 'page' : 0, 'size' : niovs, 'sort' : 'id.since:DESC' }
        if 'sort' in params:
            search_params['sort'] = params['sort']
            
        api_instance = IovsApi(self.api_client)
        try:
            api_response = api_instance.find_all_iovs(tagname=search_params['tagname'],
                    page=search_params['page'],size=search_params['size'],sort=search_params['sort'])
            ###print 'Retrieved list of iovs ',api_response 
            return api_response
        except ApiException as e:
            print ("Exception when calling IovsApi->find_all_iovs: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None
   
    def listAllIovs(self,tagname,**kwargs):
        all_params = [ 'page', 'sort', 'size' ]

        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method listLastNIovs" % key
                )
            params[key] = val
        del params['kwargs']
        search_params = { 'tagname' : tagname, 'page' : 0, 'size' : 100, 'sort' : 'id.since:ASC' }
        if 'sort' in params:
            search_params['sort'] = params['sort']
        if 'page' in params:
            search_params['page'] = params['page']
        if 'size' in params:
            search_params['size'] = params['size']
            
        api_instance = IovsApi(self.api_client)
        try:
            api_response = api_instance.find_all_iovs(tagname=search_params['tagname'],
                    page=search_params['page'],size=search_params['size'],sort=search_params['sort'])
            ###print 'Retrieved list of iovs ',api_response 
            return api_response
        except ApiException as e:
            print ("Exception when calling IovsApi->find_all_iovs: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None
    
    

    @cdms_profile.profile
    def storeObject(self,tagname,since,data,**kwargs):
        all_params = ['tag_name', 'since', 'insertion_time', 'payload_hash', 'streamer_info', 'object_type', 'version', 'dataflag' ]
        import base64
        self._logger.debug('Store payload in tag %s at time %s using %s ' %  (tagname,since,data))

        params = locals()
        for key, val in params['kwargs'].items():
            self._logger.debug('Analyse arg %s = %s' % (key,val))
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method select_iovs" % key
                )
            params[key] = val
            
        del params['kwargs']
        self._logger.debug('setting insertion time to %s' % (time.time()))
        instime = int(time.time()*1000) # time in ms
        iov_params = { 'since' : since, 'tag_name' : tagname }
        
        defstr = base64.b64encode(bytes('none'.encode('utf-8')))

        p_params = { 'insertion_time' : instime, 'streamer_info' : defstr.decode('utf-8'), 'version' : 'test', 'object_type' : 'json-file'}
        
        if 'streamer_info' in params:
            defstr = base64.b64encode(bytes(params['streamer_info'].encode('utf-8')))
            p_params['streamer_info'] = defstr.decode('utf-8')
        if 'object_type' in params:
            p_params['object_type'] = params['object_type']
        if 'version' in params:
            p_params['version'] = params['version']
            
        papi_instance = PayloadsApi(self.api_client)
        iapi_instance = IovsApi(self.api_client)    
        # Check if data is a filename or the real data object   
        try:
            if 'dataflag' not in params:
                params['dataflag'] = 'inmemory'
                
            if params['dataflag'] == 'fromfile':
                # compute hash from file
                mhash=None
    
                with open(data, mode='rb') as file: # b is important -> binary
                    fileContent = file.read()
                    mhash = self.gethash(fileContent)
                if mhash is None:
                    raise ValueError('Cannot store payload with null hash')
                p_params['hash'] = mhash
                p_params['data']  = None
                iov_params['payload_hash'] = mhash

            elif params['dataflag'] == 'inmemory':
                print ('Use data as in memory object...')
            ## Consider default for dataflag to be "inmemory"
                phash = self.gethash(data)
                p_params['hash'] = phash
                encdata = base64.b64encode(bytes(data.encode('utf-8')))

                #encdata = base64.b64encode(data)
                p_params['data'] = encdata.decode('utf-8')
                iov_params['payload_hash'] = phash

            pdto = PayloadDto(hash=p_params['hash'], version=p_params['version'], object_type=p_params['object_type'], data=p_params['data'], streamer_info=p_params['streamer_info'], insertion_time=p_params['insertion_time'])
                
            print ('store payload with hash: %s' % pdto.hash)
            papi_response = None
            if params['dataflag'] == 'fromfile':
                jsondict = { "hash" : pdto.hash, "objectType" : pdto.object_type, "streamerInfo" : pdto.streamer_info,
                           "version" : pdto.version, "insertionTime" : pdto.insertion_time}
                self._logger.debug('Dump dictionary to json %s' % (jsondict))
                
                jsonobj = json.dumps(jsondict)
                self._logger.debug('- use multiform and external file %s %s %s' % (data,jsondict,jsonobj))
                papi_response = papi_instance.create_payload_multi_form(data,jsonobj)
            else:
                self._logger.debug('- use in memory %s ' % (pdto.hash))
                papi_response = papi_instance.create_payload(pdto)

            # Store iov for the created payload
            iov = IovDto(tag_name=iov_params['tag_name'], since=iov_params['since'], insertion_time=None, payload_hash=iov_params['payload_hash'])
            self._logger.debug('Store iov with since %s ' % (iov_params['since']))
            iapi_response = iapi_instance.create_iov(iov)
            
#            print ('CMApi: stored object using %s and returning %s' % (iov_params,iapi_response))
            return iapi_response
        except ApiException as e:
            print ("Exception when calling PayloadsApi->create_payload*: %s\n" % e)
            print ("Exception when calling IovsApi->create_iov: %s\n" % e)
        except Exception as e:
            exc_type, exc_obj, exc_tb = sys.exc_info()
            fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
            print(exc_type, fname, exc_tb.tb_lineno)
            print (e)
        
    @cdms_profile.profile
    def getPayload(self, phash):
        if self.__payload['hash'] == phash:
            return self.__payload['payload']
        api_instance = PayloadsApi(self.api_client)
        try:
            #print 'Retrieving payload using hash ',phash
            api_response = api_instance.get_payload(phash)
            self.__payload['hash'] = phash
            self.__payload['payload'] = api_response
            return api_response
        except Exception as e:
            print ("Exception when calling getPayload: %s\n" % e)
        return None

    @cdms_profile.profile
    def getBlob(self, phash):
        if self.__payload['hash'] == phash:
            return self.__payload['payload']
        api_instance = PayloadsApi(self.api_client)
        try:
            #print 'Retrieving payload using hash ',phash
            api_response = api_instance.get_blob(phash)
            self.__payload['hash'] = phash
            self.__payload['payload'] = api_response
            return api_response
        except Exception as e:
            print ("Exception when calling getPayload: %s\n" % e)
        return None

    def readPayloadFromFile(self,filename):
        with open(filename) as json_data:
            d = json.load(json_data)
            print(d)
            ddata=d['DATA']
            for row in ddata:
                print ('Retrieved data row %s' % row)
            print ('length of data array: %d' % len(ddata))


class Info(dict):
    """
    This class extends a dictionary with a format string for its representation.
    It is used as the return value for the inspection commands, such that one
    can both query for the inspected object's information:
    
      res = tool.ls( '/a' )
      print res['name']
      
    as well as obtain a standard printout of the information:
    
      print res
    
    """
    def __init__( self, fmt = None ):
        super( Info, self ).__init__()
        self.format = fmt
    
    def __str__( self ):
        if self.format is None:
            return super( Info, self ).__str__()
        else:
            return self.format % self
        
    def setformat(self,fmt):
        self.format = fmt
        
    def format(self):
        return self.format

class InfoList(list):
    """
    This class extends a list with a custom string representation.
    """
    def __str__( self ):
        return '\n'.join( [ str(i) for i in self ] )
        
    def __repr__( self ):
        return str( self )




class CrestConsole(CMApi):
    def __init__(self):
        print ('Initialise CrestConsole and the management API...')
        CMApi.__init__(self)
        self._seltag = None
        self._start = '0'
        self._end = 'INF'
        self._chan = None
        self._lastpyld=None
        logging.basicConfig(format='%(asctime)s %(message)s')
        self._logger = logging.getLogger('CrestConsole')
        self._logger.setLevel(logging.INFO)
    
    def reset(self):
        self._start='0'
        self._end='INF'
        self._chan='all'
        self._lastpyld=None
        
    def lstags(self,pattern):
        res = self.listTags(pattern)
        wrapres = []
        for x in res:
            wrapit = TagDtoWrap(x)
            wrapres.append(wrapit)
        return InfoList(wrapres)

    def tracetags(self,gtname):
        res = self.getGlobalTagMaps(gtname)
        wrapres = []
        for x in res:
            wrapit = GlobalTagMapDtoWrap(x)
            wrapres.append(wrapit)
        return InfoList(wrapres)
        
    def savetag(self,arguments):
        args = arguments.split(',')
        tagdic = { 'name' : args[0].split('=')[1] }
        try:
            tag = self.getTag(tagdic['name'])
            if tag is not None:
                self._logger.debug('Tag with name: %s already exists...updating it...' % tagdic['name'])
                for ar in args:
                    (k,v) = (ar.split('=')[0],ar.split('=')[1])
                    tagdic[k] = v
                    self._logger.debug('Fill dictionary with key=%s and value=%s' % (k,v))
                self.updateTag(tagdic['name'], tagdic)
            else:
                self._logger.debug('Tag with name: %s does not exists...creating it now....' % tagdic['name'])
                tagdic = { 'timeType':'time', 'objectType':'none', 'synchronization':'all', 'description':'a new tag', 'lastValidatedTime':0, 'endOfValidity':0 }
                for ar in args:
                    (k,v) = (ar.split('=')[0],ar.split('=')[1])
                    tagdic[k] = v
                    self._logger.debug('Fill dictionary with key=%s and value=%s' % (k,v))
                self.createTag(tagdic['name'], time_type=tagdic['timeType'], object_type=tagdic['objectType'], synchronization=tagdic['synchronization'], description=tagdic['description'], last_validated_time=tagdic['lastValidatedTime'], end_of_validity=tagdic['endOfValidity'])
        except:
            print('Exception in creating or updating a tag')

        return self.lstags(tagdic['name'])

    def insertiov(self,arguments):
        args = arguments.split(',')
        #print ('Got arguments  %s' % args)
        iovdic = { }
        for ar in args:
            (k,v) = (ar.split('=')[0],ar.split('=')[1])
            iovdic[k] = v
            self._logger.debug('Fill dictionary with key=%s and value=%s' % (k,v))
        #print ('Creating dictionary for insertion %s' % iovdic)
        resiov = {}
        try:
            import base64

            tag = self.getTag(iovdic['name'])
            if tag is None:
                raise Exception('cannot insert iov in not existing tag')
            
            if iovdic['data'] is None:
                raise Exception('need an input file...')
            
            if 'streamer_info' not in iovdic or iovdic['streamer_info'] is None:
                iovdic['streamer_info'] = base64.b64encode(bytes('none'.encode('utf-8')))
            else:
                iovdic['streamer_info'] = base64.b64encode(bytes(iovdic['streamer_info'].encode('utf-8')))
            
            if 'version' not in iovdic or iovdic['version'] is None:
                iovdic['version'] = 'test'
            
            if 'object_type' not in iovdic or iovdic['object_type'] is None:
                iovdic['object_type'] = 'file'
                
            self._logger.debug ('Try to store iov...%s' % iovdic)

            resiov = self.storeObject(iovdic['name'], iovdic['since'], iovdic['data'], dataflag='fromfile')
            #print('After store object got iov %s' %resiov)
            idto = IovDtoWrap(resiov,tag.time_type)
            print('Inserted iov dto : %s' % idto.to_str())
            wrapres = []
            headeriov = IovDtoWrap(None,'header')
            wrapres.append(headeriov)
            if idto:
                wrapres.append(idto)
            return InfoList(wrapres)
            ##return Info(idto)
        except:
            self._logger.debug('Error inserting iov %s.....' % iovdic)
            
        return None

        
    def lsgtags(self,pattern):
        res = self.listGlobalTags(pattern)
        wrapres = []
        for x in res:
            wrapit = GlobalTagDtoWrap(x)
            wrapres.append(wrapit)
        return InfoList(wrapres)

    def lsiovs(self,page,size):
        tag = self.getTag(self._seltag)
        if tag is None:
            print ('Cannot list IOVs because tag %s was not found' % self._seltag)
            return InfoList([])
        res = self.listAllIovs(self._seltag,page=page,size=size)
        wrapres = []
        headeriov = IovDtoWrap(None,'header')
        wrapres.append(headeriov)
        for x in res:
            wrapit = IovDtoWrap(x,tag.time_type)
            wrapres.append(wrapit)
        return InfoList(wrapres)

    def tailiovs(self,size):
        tag = self.getTag(self._seltag)
        if tag is None:
            print ('Cannot list IOVs because tag %s was not found' % self._seltag)
            return InfoList([])
        res = self.listLastNIovs(self._seltag, size)
        wrapres = []
        headeriov = IovDtoWrap(None,'header')
        wrapres.append(headeriov)
        for x in res:
            wrapit = IovDtoWrap(x,tag.time_type)
            wrapres.append(wrapit)
        return InfoList(wrapres)

    def selectiovs(self):
        tag = self.getTag(self._seltag)
        if tag is None:
            print ('Cannot list IOVs because tag %s was not found' % self._seltag)
            return InfoList([])
        res = self.listIovs(self._seltag,since=self._start,until=self._end)
        wrapres = []
        headeriov = IovDtoWrap(None,'header')
        wrapres.append(headeriov)
        if res:
            for x in res:
                wrapit = IovDtoWrap(x,tag.time_type)
                wrapres.append(wrapit)
        return InfoList(wrapres)

    def listinfo(self):
        tag = self.getTag(self._seltag)
        if tag is None:
            print ('Cannot list information because tag %s was not found' % self._seltag)
            return InfoList([])
        res = self.getSize(self._seltag)
        wrapres = self.tagSummary(res[0])
        wrapreslist = []
        wrapreslist.append(wrapres)
        
        return InfoList(wrapreslist)

    def userunlumi(self, arguments):
        args = arguments.split()
        if len(args) == 1:
            args.append(0)
        self._start = self.runlumiToStime(args[0],args[1])
        self._end = 'INF'
        if len(args) > 2:
            self._end = self.runlumiToStime(args[2],args[3])            
            
    def usetimes(self, arguments):
        args = arguments.split()
        if len(args) == 1:
            args.append('INF')
            
        self._start = args[0]
        self._end = args[1]
              
    def usechan(self, arguments):
        args = arguments.split()
        if len(args) > 1:
            raise Exception('Cannot take more than one arguments for channel selection')
        if 'all' in args[0] or 'ALL' in args[0]:
            self._chan = None
        else:
            self._chan = int(args[0])
        res=InfoList()
        if (self._chan is not None):
            res.append('Changed current channel selection to %i' % self._chan)
        else:
            res.append('Changed current channel selection to all')
        return res

    def listchans(self):
        nchans = self._lastpyld.listChannels()
        return InfoList(nchans)
        
    def getpayload(self,thash):
        res = self.getPayload(thash)
        wrapres = self.payloadInfo(res)
        return InfoList(wrapres)
                     
    def dumppayload(self, mhash, col):

        tag = self.getTag(self._seltag)
        thash = mhash
        colsel = col.split(',')
        if 'all' in colsel[0]:
            colsel = tag.object_type.split(',')
        res = self.getPayload(thash)
        synchro = tag.synchronization
        plist = []
        if 'COPY_FROM_COOL' in synchro and 'COOL_JSON' in res.object_type:
            # This is a cool payload...add informations on columns and channels
            cp = self.getCoolJson(res)
            self._lastpyld=cp

            ###print ('Retrieved cool payload %s' % cp)
            nchans = cp.listChannels()
            print ('Oracle type are : %s ' % cp.types)
            print ('Dump using channel selection %s ' % self._chan )
            for ch in nchans:
                if self._chan is None or (isinstance(self._chan,int) and int(ch) == int(self._chan)):
                    wp = CoolPayloadWrap(cp,ch,colsel)
                    plist.append(wp)
        elif 'SHAUNROE_JSON' in res.object_type:
            # This is a cool payload...add informations on columns and channels
            cp = self.getCoolJson(res)
            self._lastpyld=cp

            ###print ('Retrieved cool payload %s' % cp)
            nchans = cp.listChannels()
            print ('Dump new format using channel selection %s ' % self._chan )
            for ch in nchans:
                if self._chan is None or (isinstance(self._chan,int) and int(ch) == int(self._chan)):
                    wp = CoolPayloadWrap(cp,ch,colsel)
                    plist.append(wp)
        else:
            decoded = res
            print ('Found payload  %s' % decoded.hash)
            wp = NormalPayloadWrap(decoded)
            plist.append(wp)
#                elif isinstance(self._chan,int):
#                    if int(ch) == int(self._chan):
#                        wp = CoolPayloadWrap(cp,ch)
#                        plist.append(wp)
                        
        return InfoList(plist)

    def dumppayloadtofile(self, mhash, outf, mode=None):
        tag = self.getTag(self._seltag)
        thash = mhash
        fname = outf
        self._logger.info('Dumping payload for hash %s into output file name %s' % (mhash,outf))
        if 'default' in outf:
            fname = '{0}_{1}.blob'.format(tag.name,mhash)
        res = self.getPayload(thash)
        synchro = tag.synchronization
        colsel = tag.object_type.split(',')

        plist=[]
        if 'COPY_FROM_COOL' in synchro and 'COOL_JSON' in res.object_type and mode is None:
            # This is a cool payload...add informations on columns and channels
            cp = self.getCoolJson(res)
            nchans = cp.listChannels()
            print ('Oracle type are : %s ' % cp.types)
            print ('Dump using channel selection %s ' % self._chan )
            for ch in nchans:
                if self._chan is None or (isinstance(self._chan,int) and int(ch) == int(self._chan)):
                    wp = CoolPayloadWrap(cp,ch,colsel)
                    plist.append(wp)
        else:
            decoded = res
            print ('Found payload  %s' % decoded.hash)
            wp = NormalPayloadWrap(decoded)
            plist.append(wp)
            
        with open(fname,'w') as f:
            for pld in plist:
                f.write(pld.to_str())
            f.close()
        return "payload stored in {0}".format(fname)
                
    def usetag(self,tag):
        self._seltag = tag
        return 

    def pwd(self):
        res=InfoList()
        res.append('Current tag: %s' % self._seltag)
        return res

    def pws(self):
        res=InfoList()
        res.append('Current tag: %s' % self._seltag)
        res.append('Current channel selection: %s' % self._chan)
        return res

    def tagSummary(self,pdto):
        tag = self.getTag(self._seltag)
        taginfo = TagInfoWrap(tag,pdto)
        return taginfo

    def payloadInfo(self,pdto):        
        pyldinfo = PayloadDtoWrap(pdto)
        wrapres = []
        wrapres.append(pyldinfo)
        return wrapres


## Cool wrapper for CDMS

class CoolCMApi(CMApi):
    def __init__(self):
        print ('Initialise CoolCMApi...')
        CMApi.__init__(self)
        self._logger.setLevel(logging.DEBUG)

    
    def browseObjects(self,since,until,tagname):
        snapshot=DefaultSnapshot*1000
        self.__iovlist = self.listIovs(tagname,since=since, until=until,snapshot=snapshot)
        return self.__iovlist
    
    def getObject(self,iov,channelSel=None):
        pyld = None
        if iov is not None:
            pyld = self.getPayload(iov.payload_hash)
        if pyld is not None:
            cooljson = self.getCoolJson(pyld)
            if channelSel is not None:
                return cooljson.getChannelData(channelSel)
            else:
                return cooljson
        return None
    
    
    def findObject(self,stime,tag=None):
        tagname = None
        timeformat = 'time'
        self._logger.debug('findObject: calling with arguments %s %s ' % (str(stime) , str(tag)))
        
        if tag is not None:
            self.__tag = self.getTag(tag)
        else:
            if self.__tag is None:
                raise Exception("Cannot load object from None folder or tag")
        
        tagname = self.__tag.name
        timeformat = self.__tag.time_type
        search_params = {}
        ##        snapshot=int(time.time())*1000
        snapshot=DefaultSnapshot*1000
        self.__iovlist = self.selectSnapshot(tagname,snapshot=snapshot)
        self._logger.debug('List of iovs has length : %d ' % (len(self.__iovlist)))
        
        search_params['tagname'] = tagname
        self._logger.debug('1 Search parameters are %s' % (search_params))
        if timeformat == 'run-lumi' and isinstance(stime,str):
            run_lumi = [x.strip() for x in stime.split(',')]
            (run,lumi) = (run_lumi[0],run_lumi[1])
            self._logger.debug('Use until from run and lumi %s %s ' % (run,lumi))
            search_params['since'] = self.runlumiToStime(run,lumi)
        else:
            search_params['since'] = int(stime)
        
        self._logger.debug('2 Search parameters are %s' % (search_params))

        stimelist = [ int(x.since) for x in self.__iovlist ]
        self._logger.debug('List of timestamp to search has length %d ' % (len(stimelist)))
        
        (before,after)=self.takeClosest(stimelist,search_params['since'])
        self._logger.debug('Range in time is %d %d ' % (before,after))
        selobjiov= self.listIovs(tagname,since=before, until=after)
        pyld=None
        if len(selobjiov) > 1:
            raise Exception("Too many iovs loaded by this function....",len(selobjiov))
        
        for aniov in selobjiov:
            self._logger.debug('Loop over selected object list %s ' % str(aniov))
            pyld = self.getPayload(aniov.payload_hash)
            self.__coolpayload = self.getCoolJson(pyld)
            self._logger.debug('Return cool payload object %s ' % self.__coolpayload)
            return self.__coolpayload
        
        return pyld



if __name__ == '__main__':
    
#     cmapi = CMFileApi()
#     cmapi.createTag('LOCAL_TEST',time_type='time',object_type='testobj',synchronization='online',description='file test tag') 
#     print 'Getting back the tag'
#     tag = cmapi.getTag('LOCAL_TEST')
#     print 'Found tag ',tag
#     taglist = cmapi.listTags('TEST')
#     print 'Retrieved list of tags ',taglist
#     cmapi.storeObject('LOCAL_TEST',0,'this is a test',version='10')
#     print 'stored object...'
#     cmapi.storeObject('LOCAL_TEST',100,'this is another test',version='10')
#     pld = cmapi.getPayload('f69bff44070ba35d7169196ba0095425979d96346a31486b507b4a3f77bd356d')
#     print 'Found payload ',pld
#     sys.exit()
    
    cm = CMApi()
    res = cm.getSize('MuonAlignMDTEndCapCAlign-RUN2-ECC_ROLLING_2015_03_01-UPD4-02')
    print (res)
    #sys.exit(0)
    
    cmapi = CoolCMApi()
    a = 944214200287232
    (run, lumi) = cmapi.stimeToRunlumi(a)
    print ('Found run / lumi %s %s ' % (run,lumi))
    #pass
    cmapi = CoolCMApi()

#### Example of retrieval for Antoine
    print ('=================== GET TAG ======================')        
    tag = cmapi.getTag('TRIGGER_LUMI_PerBcidDeadtime_SHAUNROE')
    pobj = cmapi.getPayload('5ca1b5fb59df861a0c0eae6433d9c86e8305f6f9d1fdb4e6c44c68b2d1953819')
    print ('Retrieved payload: %s ' % pobj)
    cp = cmapi.getCoolJson(pobj)
    print ('Interpret payload as cool json: %s ' % cp)
    print ('Check folder spec: %s ' % cp.folder_payloadspec)
    v = cp.getChannelDataColumn('0', 'CPLX0')
    print ('Check data for CPLX0: %s ' % v)
    try:
        val = cp.__getval__(v,'Blob','str')
        #val = v.decode('utf-8','ignore')
        msg = " - %s = %s " % ('CPLX0',str(val))
        print(msg)
    except Exception as e1:
        print("warn: coolpayload is not encoded: %s " % e1)                    


    sys.exit(0)
    pass
    try:
        cmapi.createTag('TEST',time_type='run-lumi')
    except Exception as e :
        print (e)
    print ('=================== STORE OBJECT ======================')
    try:     
        cmapi.storeObject('TEST',0,'this is a test',version='10')
    except Exception as e :
        print (e)
    print ('=================== GET TAG ======================')        
    cmapi.getTag('TEST')

    print ('=================== STORE OBJECT FROM FILE ======================')
    try:
        cmapi.storeObject('TEST',10,'test.txt',version='11',object_type='simple string',dataflag='fromfile')
    except Exception as e :
        print (e)

    print ('=================== LIST IOVS ======================')
    iovlist = cmapi.listIovs('TEST')
    for iov in iovlist:
        pyld = cmapi.getPayload(iov.payload_hash)
        print ('Retrieved payload %s' % pyld)
        
    print ('=================== LIST TAGS  ======================')
    taglist = cmapi.listTags('Til')
    for tag in taglist:
        name = tag.name
        print ('Found tag %s' % name)
        
    print ('=================== LIST LAST N IOVS =================')
    iovlist = cmapi.listLastNIovs('TileOfl02CalibCisLin-RUN2-UPD4-14',100)
    for iov in iovlist:
        print ('Found iov with since %d and hash %s' % (int(iov.since),iov.payload_hash))

    print ('=================== LIST IOVS from to ======================')
    iovlist = cmapi.listIovs('TileOfl02CalibCisLin-RUN2-UPD4-13',type='run-lumi',since='214557,0',until='555555,0')
    for iov in iovlist:
        print ('Found iov with since %d and hash %s' % (int(iov.since),iov.payload_hash))

    print ('=================== LIST IOVS in Tile ======================')
    iovlist = cmapi.listIovs('TileOfl02CalibCisLin-RUN2-UPD4-14')
    print ('Retrieved list of iovs %d' % len(iovlist))
    for iov in iovlist:
        print ('Found iov with since %d and hash %s' % (int(iov.since),iov.payload_hash))

    print ('=================== LIST GROUPS in Tile ======================')
    grouplist = cmapi.selectGroups('TileOfl02CalibCisLin-RUN2-UPD4-14')
    print ('Retrieved list of groups %d' % len(grouplist.groups))
    for iov in grouplist.groups:
        print ('Found since in groups %d' % int(iov))

    print ('=================== LIST IOVS in Tile ======================')
    iovlist = cmapi.selectSnapshot('TileOfl02CalibCisLin-RUN2-UPD4-14')
    print ('Retrieved list of iovs using selectIovs %d ' % len(iovlist))
    for iov in iovlist:
        print ('Found since in iovlist %d ' % int(iov.since))


    print ('=================== Retrieve TILE payload ===================')
    foldertag = cmapi.getTag('TileOfl02CalibCisLin-RUN2-UPD4-14')
    from cool.ConditionsFolder import coolfolder
    coolf = coolfolder('/APATH',foldertag,cmapi)
    payloadlist = coolf.browseObjects('214557,0','555555,0',None,None)
    i = 0
    for apayload in payloadlist:
        i = i+1
        pyld = cmapi.getObject(apayload)
        blob = pyld.getChannelDataColumnIndex(250,0)

        print ('Retrieved payload with time and nchannels %s %s = %s' % (pyld.stime,pyld.nchans, pyld.getChannelDataColumn(250,'TileCalibBlob')))
        print ('Cool payload column index information for channel 0 ... %s of size %d %d' %  (str(blob),len(blob),len(blob)/8))
        if i>10:
            break


    print ('=================== Retrieve BIG LAR payload ===================')
    larhash = '0ce0f7c466a736877c699d526dddd9bff2cdf68b70eaf24cec3db66714b8805a'
    pyld = cmapi.getPayload(larhash)
    strinfo = (pyld.streamer_info).decode('base64')
    datastr= (pyld.data).decode('base64')
    print ('Length of payload string is %d' % len(datastr))

#print 'Full dictionary ',condcoolpyld.to_dict()

    print ('=============== Test findObject ===========')
    pyld = cmapi.findObject(1273286004572161,'LAR_ElecCalibFlat_OFC')
    coolpayload = pyld
    print ('Retrieved payload is : %s %d ' % (coolpayload.stime,coolpayload.nchans))
    if coolpayload is not None:
        print ('Retrieved payload with metadata : %s %s %s ' % (coolpayload.stime, coolpayload.nchans, coolpayload.folder_payloadspec))
        #print 'Cool payload information for channel 0 and column nSamples...',coolpayload.getChannelDataColumn(0,'nSamples')
        #print 'Cool payload column index information for channel 0 and column 4...',coolpayload.getChannelDataColumnIndex(0,4),' !'

    sys.exit()
    print ('=================== LIST IOVS in MDT =====================')
    iovlist = cmapi.listIovs('MDTRT-RUN2-UPD4-18')
    print ('Retrieved list of iovs %d ' % len(iovlist))
    for iov in iovlist:
        #print 'Found iov with since ',(iov.since),' and hash ',iov.payload_hash
        if iov.payload_hash == 'dd92113edaf7b7d3641567100dd4d8eaa283341694eb656b08a9695e422f18ee':
            pyld = cmapi.getPayload(iov.payload_hash)
            strinfo = (pyld.streamer_info).decode('base64')
            datastr= (pyld.data).decode('base64')
            print (strinfo)

    cdms_profile.print_prof_data()

##cmapi.readPayloadFromFile('test.json')
