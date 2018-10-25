
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

from crestapi.apis import GlobaltagsApi, TagsApi, GlobaltagmapsApi, IovsApi, PayloadsApi, AdminApi
from crestapi.models import GlobalTagDto, TagDto, GlobalTagMapDto, IovDto,PayloadDto
from crestapi.apis import RuninfoApi
from crestapi.models import RunLumiInfoDto
from crestapi import ApiClient
from crestapi.rest import ApiException

import cdms_profile

sys.path.append(os.path.join(sys.path[0],'../client'))


DefaultSnapshot = 2500000000

class RunLumiManagementAbstract(object):

    def listRunLumiInfo(self):
        '''
            This method allows to query RunLumiInfo in a range in time. The parameters allowed are:
            all_params = [ 'since', 'until','type' ]
            2) since : optional, a string containing a since time. Default is 0.
            3) until : optional, a string containing an until time. Default is INF.
            5) type : optional, can be "time" or "run-lumi". If "run-lumi" then it supposes
                      that the strings for since and until are expressed like : <arun>,<alumi block> 
        '''
        pass
        raise NotImplementedError('Method not implemented here')
    def listLastNRunLumiInfo(self):
        pass
        raise NotImplementedError('Method not implemented here')

    def insertRunLumiInfo(self):
        pass
        raise NotImplementedError('Method not implemented here')
        
    def runlumiToStime(self,run,lumi):
        stime = (int(run) << 32) | int(lumi)
        return stime
    
    def stimeToRunlumi(self,stime):
        run = int(stime) >> 32
        lumi = int(stime) & int('0xffffffff',16)
        return (run,lumi)
    
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
    
    def activatesocks(self):
        try:
            import socket
            import socks # you need to install pysocks (use the command: pip install pysocks)
# Configuration
            SOCKS5_PROXY_HOST = 'localhost'
            SOCKS5_PROXY_PORT = 3129

# Remove this if you don't plan to "deactivate" the proxy later
#        default_socket = socket.socket
# Set up a proxy
#            if self.useSocks:
            socks.set_default_proxy(socks.SOCKS5, SOCKS5_PROXY_HOST, SOCKS5_PROXY_PORT)
            socket.socket = socks.socksocket
            print ('Activated socks proxy on %s:%s' % (SOCKS5_PROXY_HOST,SOCKS5_PROXY_PORT))
        except:
            print ('Error activating socks...')
        


    

class RunLumiApi(RunLumiManagementAbstract):

    def __init__(self):
        print ('Initialise RunLumiApi...')
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
        self._logger = logging.getLogger('RunLumiApi')
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

        
            
    def listRunLumiInfo(self,**kwargs):
        all_params = [ 'by','since', 'until', 'type' ,'page','size','sort']

        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method select_iovs" % key
                )
            params[key] = val
        del params['kwargs']
        search_params = { 'since' : '0', 'until' : 'INF', 'type' : 'time' }
        bystr=''
        addsearch = {}
        timetypesearch = 'time'
        if 'type' in params:
            timetypesearch = params['type']
        if 'page' in params:
            addsearch['page'] = params['page']
        if 'size' in params:
            addsearch['size'] = params['size']
        if 'sort' in params:
            addsearch['sort'] = params['sort']
            
        sincesearch=''
        untilsearch=''
        if 'since' in params:
            search_params['since'] = params['since']
            sincesearch = 'starttime>%s' % search_params['since']
            if timetypesearch == 'run-lumi':
                run_lumi = [x.strip() for x in params['since'].split(',')]
                (run,lumi) = (run_lumi[0],run_lumi[1])
                self._logger.debug('Use since from run and lumi %s %s ' % (run,lumi))
                search_params['since'] = self.runlumiToStime(run,lumi)
                sincesearch = 'since>%s' % search_params['since']
            elif timetypesearch == 'cool-runlb':
                sincesearch = 'since>%s' % search_params['since']

        if 'until' in params:
            search_params['until'] = params['until']
            untilsearch = 'endtime<%s' % search_params['until']
            if timetypesearch == 'run-lumi':
                run_lumi = [x.strip() for x in params['until'].split(',')]
                (run,lumi) = (run_lumi[0],run_lumi[1])
                self._logger.debug('Use until from run and lumi %s %s ' % (run,lumi))
                search_params['until'] = self.runlumiToStime(run,lumi)
                untilsearch = 'since<%s' % search_params['until']
            elif timetypesearch == 'cool-runlb':
                untilsearch = 'since<%s' % search_params['until']
        

        api_instance = RuninfoApi(self.api_client)
        try:
            bystr=sincesearch
            if untilsearch is not '':
                bystr = "%s,%s" % (sincesearch,untilsearch)
            if 'by' in params:
                self._logger.debug('Ignore all previous entry and use by as %s ' % (params['by']))
                bystr=params['by']
            self._logger.debug('List run info using arguments: %s  %s %s' % (str(search_params),str(addsearch),bystr))
            api_response = api_instance.list_run_lumi_info(by=bystr,**addsearch)
            self._logger.debug('list runs: retrieved response of length %d ' % len(api_response))
            return api_response
        except ApiException as e:
            print ("Exception when calling RuninfoApi->list_run_lumi_info: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None
   


    def listLastNRunLumiInfo(self,niovs,**kwargs):
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
        search_params = { 'page' : 0, 'size' : niovs, 'sort' : 'starttime:DESC' }
        if 'sort' in params:
            search_params['sort'] = params['sort']
            
        api_instance = RuninfoApi(self.api_client)
        try:
            api_response = api_instance.list_run_lumi_info(
                    page=search_params['page'],size=search_params['size'],sort=search_params['sort'])
            ###print 'Retrieved list of iovs ',api_response 
            return api_response
        except ApiException as e:
            print ("Exception when calling RuninfoApi->list_run_lumi_info: %s\n" % e)
        except Exception as inst:
            print(type(inst))
            print(inst)
        return None
   
    def insertRunLumiInfo(self,**kwargs):
        all_params = [ 'since', 'run', 'lb', 'starttime', 'endtime']

        params = locals()
        for key, val in params['kwargs'].items():
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method insertRunLumiInfo" % key
                )
            params[key] = val
        del params['kwargs']
        r_params = { 'since' : 0, 'run' : 0, 'lb' : 0, 'starttime' : 0, 'endtime' : 0 }
       
        if 'since' in params:
            r_params['since'] = params['since']
        if 'run' in params:
            r_params['run'] = params['run']
        if 'lb' in params:
            r_params['lb'] = params['lb']
        if 'starttime' in params:
            r_params['starttime'] = params['starttime']
        if 'endtime' in params:
            r_params['endtime'] = params['endtime']
        
        self._logger.debug('Creating run lumi dto %s' % (r_params))

        api_instance = RuninfoApi(self.api_client)
        try:
            dto = RunLumiInfoDto(since=r_params['since'],run=r_params['run'],lb=r_params['lb'],starttime=r_params['starttime'],endtime=r_params['endtime']) 
            msg = ('Created dto resource %s ' ) % (dto.to_dict())
            api_response = api_instance.create_run_lumi_info(dto)
            return api_response
        except ApiException as e:
            print ("Exception when calling RuninfoApi->create_run_lumi_info: %s\n" % e)
            if e.status == 303: # the global tag already exists....
                return e.body
            raise
   


if __name__ == '__main__':
    
    print ('Test run lumi')
##cmapi.readPayloadFromFile('test.json')
