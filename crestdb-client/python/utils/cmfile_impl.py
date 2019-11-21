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

from crestapi import ApiClient
from crestapi.rest import ApiException
import traceback
import cdms_profile

from ConditionsManagementApi import ConditionManagementAbstract

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
            dto = TagDto(tagname,time_type=tag_params['time_type'], payload_spec=tag_params['object_type'], synchronization=tag_params['synchronization'], description=tag_params['description'], last_validated_time=tag_params['last_validated_time'], end_of_validity=tag_params['end_of_validity'])
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
        tb = ''
        try:
            tagpath = "%s" % (self._basedir)
            taglist = [x[0] for x in os.walk(tagpath) if tagnamepattern in x[0]]

            print ('List of filtered tags %s' % taglist)
            return taglist
        except Exception as e:
            print ("Exception when calling TagsApi->find_tag: %s\n" % e)
            tb = traceback.format_exc()
        except Exception as inst:
            print(type(inst))
            print(inst)
        finally:
            print(tb)
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
