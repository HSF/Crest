#!/usr/bin/env python
# encoding: utf-8
'''
cliadmin.phcli -- shortdesc
    General conditions DB management CLI.
cliadmin.phcli is a description

It defines classes_and_methods

@author:     user_name

@copyright:  2015 organization_name. All rights reserved.

@license:    license

@contact:    user_email
@deffield    updated: Updated
'''

import sys, os, pickle, getopt,re
import json
import os.path
import traceback

#from pygments import highlight, lexers, formatters
from xml.dom import minidom
#from clint.textui import colored
from datetime import datetime

sys.path.append(os.path.join(sys.path[0],'..'))

from crestapi.api import GlobaltagsApi, TagsApi, GlobaltagmapsApi, IovsApi, PayloadsApi, AdminApi
from crestapi.models import GlobalTagDto, TagDto, GlobalTagMapDto, IovDto,PayloadDto
from crestapi import ApiClient
from crestapi.rest import ApiException
from crestapi.configuration import Configuration
from pprint import pprint

try:
    from StringIO import StringIO
except ImportError:
    from io import StringIO

class PhysDBDriver():
    def __init__(self):
    # process command line options
        try:
            self._command = sys.argv[0]
            self.useSocks = False
            self.snap = 0
            self.t0 = 0
            self.tMax = 'INF'
            self.debug = False
            self.trace = 'on'
            self.by = 'none'
            self.sort = 'none'
            self.page = 0
            self.pagesize = 1000
            self.iovspan = 'time'
            self.jsondump=False
            self.dump=False
            self.user='none'
            self.passwd='none'
            self.outfilename=''
            self.api_client=None
            self.phtools=None
            self.urlsvc=os.getenv('CREST_HOST', 'http://crest-undertow.web.cern.ch/crestapi')
            longopts=['help','socks','out=','jsondump','t0=','tMax=','snap=','url=','debug','trace=','by=','page=','pagesize=','iovspan=','user=','pass=','sort=']
            opts,args=getopt.getopt(sys.argv[1:],'',longopts)
            print ('%s, %s' % (opts, args))
            self.procopts(opts,args)
        except getopt.GetoptError as e:
            print (e)
            self.usage()
            sys.exit(-1)

        if self.useSocks:
            self.activatesocks()
        self._config = Configuration()
        self._config.host = self.urlsvc
        self.api_client = ApiClient(self._config)

        self.execute()

    def usage(self):
        print
        print ("usage: crest_mgr.py {<options>} <action> {<args>}")
        print ("Manage conditions DB content using args")
        print ("Server url is needed in order to provide the system with the base url; defaults to localhost (see below)")
        print ("Action determines which rest method to call")
        print ("Actions list:")
        print (" - ADD <type> [json-filename] [type = tags, globaltags, ...] : add the selected resource using the json file provided in input")
        print ("        ex: ADD globaltags <json-filename>: add a new global tag with the content taken from json file")
        print (" ")
        print (" - INSERT <tag> <since> blob-file <json-payload> : add a new IOV with its payload + metadata")
        print ("        ex: INSERT my-tag 10000 myblob.blob mypayload.json: add a payload and then the iov in the correspoding tag")
        print (" ")
        print (" - RM <type> [id] [type = tags, globaltags, ...] : remove the selected resource")
        print ("        ex: RM globaltags MY_TEST02_GTAG : remove the global tag identified by MY_TEST02_GTAG")
        print (" ")
        print (" ")
        print ("Options: ")
###        print ("  --socks activate socks proxy on localhost 3129 "
        print ("  --debug activate debugging output ")
        print ("  --socks activate a socks proxy on localhost:3129 ")
        print ("  --out={filename} activate dump on filename ")
        print ("  --jsondump activate a dump of output lines in json format ")
        print ("  --by : comma separated list of conditions for filtering a query (e.g.: by=name:pippo,value<10). DEFAULT=%s" % self.by)
        print ("  --sort : comma separated list of conditions for sorting the output of a query (e.g.: sort=name:DESC,id:ASC). DEFAULT=%s" % self.sort)
        print ("  --page [0,...N]: page number to retrieve; use it in combination with page size. DEFAULT=%s" % self.page)
        print ("  --pagesize [1000,30,...]: page size; use it in combination with page. DEFAULT=%s" % self.pagesize)
        print ("  --url [localhost:8080/crestapi]: use a specific server ")
        print ("  --t0={t0 for iovs}. DEFAULT=%s" % self.t0)
        print ("  --tMax={tMax for iovs}. DEFAULT=%s" % self.tMax)
        print ("  --snap={snapshot time in milli seconds since EPOCH}. DEFAULT=%s" % self.snap)
        print ("  --iovspan={time|date|runlb|timerun|daterun}. DEFAULT=%s" % self.iovspan)
        print ("         time: iov in COOL format, allows Inf for infinity")
        print ("         date: iov in yyyyMMddHHmmss format")
        print ("         runlb: iov in run-lb format, only for run based folders ")
        print ("         the others make the conversion to run number, does not allow Inf for infinity ")
        print ("Examples: ")
        print (" TO BE DONE... ")

    def procopts(self,opts,args):
        "Process the command line parameters"
        for o,a in opts:
            print ('Analyse options %s %s' %(o,a))
            if (o=='--help'):
                self.usage()
                sys.exit(0)
            if (o=='--socks'):
                self.useSocks=True
            if (o=='--out'):
                self.dump=True
                self.outfilename=a
            if (o=='--debug'):
                self.debug=True
            if (o=='--jsondump'):
                self.jsondump=True
            if (o=='--url'):
                self.urlsvc=a
            if (o=='--snap'):
                self.snap=a
            if (o=='--t0'):
                self.t0=a
            if (o=='--tMax'):
                self.tMax=a
            if (o=='--user'):
                self.user=a
            if (o=='--trace'):
                self.trace=a
            if (o=='--by'):
                self.by=a
            if (o=='--sort'):
                self.sort=a
            if (o=='--page'):
                self.page=a
            if (o=='--pagesize'):
                self.pagesize=a
            if (o=='--pass'):
                self.passwd=a
            if (o=='--iovspan'):
                self.iovspan=a

        if (len(args)<2):
            raise getopt.GetoptError("Insufficient arguments - need at least 3, or try --help")
        self.action=args[0].upper()
        self.args=args[1:]

    def execute(self):
        msg = ('Execute the command for action %s and arguments : %s ' ) % (self.action, str(self.args))
        self.printmsg(msg,'cyan')

        start = datetime.now()

        if self.dump:
            outfile = open(self.outfilename,"w")
        _dict = {}
        params = {}


        if (self.action=='ADD'):
            try:
                print ('Action ADD is used to add a resource in the DB')
                print ('Found N arguments %s' % len(self.args))
                if len(self.args)<2:
                    sys.exit("Number of arguments is wrong...may be you forgot filename?")
                    raise
                object=self.args[0]
                msg = ('ADD: selected object is %s ') % (object)
                if object in [ 'globaltags', 'tags', 'maps' ]:
                    self.printmsg(msg,'cyan')
                else:
                    msg = ('ADD: cannot apply command to object %s ') % (object)
                    self.printmsg(msg,'red')
                    return -1

                filename = self.args[1]
                self.add(object,filename)

            except Exception as e:
                sys.exit("failed on action ADD: %s" % (str(e)))
                raise

        elif (self.action=='INSERT'):
            try:
                print ('Action INSERT is used to add a payload resource in the DB, with a since and a tagname')
                print ('Found N arguments %s' % len(self.args))
                if len(self.args)<3:
                    sys.exit("Number of arguments is wrong...may be you forgot ID?")
                    raise
                tagname=self.args[0]
                since=self.args[1]
                filename = self.args[2]
                #payload = self.args[3]
#                self.insertiov(tagname,since,filename,payload)
                self.insert_iov_payload(tagname,since,filename)

            except Exception as e:
                exc_type, exc_value, exc_traceback = sys.exc_info()
                print ("*** print_tb:")
                traceback.print_tb(exc_traceback, limit=1, file=sys.stdout)
                print ("*** print_exception:")
                traceback.print_exception(exc_type, exc_value, exc_traceback,
                              limit=2, file=sys.stdout)

                sys.exit("failed on action INSERT: %s" % (str(e)))
                raise

        elif (self.action=='HASH'):
            try:
                print ('Action HASH is used to compute a payload hash given a file in input')
                print ('Found N arguments %s' % len(self.args))
                if len(self.args)<1:
                    sys.exit("Number of arguments is wrong...may be you forgot file name?")
                    raise
                filename = self.args[0]
                self.gethashfromfile(filename)

            except Exception as e:
                exc_type, exc_value, exc_traceback = sys.exc_info()
                print ("*** print_tb:")
                traceback.print_tb(exc_traceback, limit=1, file=sys.stdout)
                print ("*** print_exception:")
                traceback.print_exception(exc_type, exc_value, exc_traceback,
                              limit=2, file=sys.stdout)

                sys.exit("failed on action INSERT: %s" % (str(e)))
                raise

        elif (self.action=='RM'):
            try:
                print ('Action RM is used to delete a resource from the DB')
                print ('Found N arguments %s' % len(self.args))
                if len(self.args)<2:
                    sys.exit("Number of arguments is wrong...may be you forgot ID?")
                    raise
                object=self.args[0]
                msg = ('RM: selected type is %s ') % (object)
                self.printmsg(msg,'cyan')
                if object in [ 'globaltags', 'tags' ]:
                    self.printmsg(msg,'cyan')
                else:
                    msg = ('RM: cannot apply command to object %s ') % (object)
                    self.printmsg(msg,'red')
                    return -1

                resourceid = self.args[1]
                self.rm(object,resourceid)

            except Exception as e:
                exc_type, exc_value, exc_traceback = sys.exc_info()
                print ("*** print_tb:")
                traceback.print_tb(exc_traceback, limit=1, file=sys.stdout)
                print ("*** print_exception:")
                traceback.print_exception(exc_type, exc_value, exc_traceback,
                              limit=2, file=sys.stdout)

                sys.exit("failed on action RM: %s" % (str(e)))
                raise

        else:
            print ("Command not recognized: please type -h for help")


        tend=datetime.now()
        print
        print ('Time spent (ms): %s ' % (tend-start))

    def printmsg(self,msg,color):
        try:
            from clint.textui import colored
            if color == 'cyan':
                print (colored.cyan(msg))
            elif color == 'blue':
                print (colored.blue(msg))
            elif color == 'green':
                print (colored.green(msg))
            elif color == 'red':
                print (colored.red(msg))

        except:
            print (msg)

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
            print ('Activated socks proxy on %s : %s' % (SOCKS5_PROXY_HOST,SOCKS5_PROXY_PORT))
        except:
            print ('Error in socks activation')

    def insert_iov_payload(self,tag,since,pyldf):
        papi_instance = PayloadsApi(self.api_client)
        try:
            papi_response = papi_instance.store_payload_with_iov_multi_form(pyldf,tag,since)
            print('Stored payload %s' % papi_response)
        except ApiException as e:
            print ("Exception when calling PayloadsApi->store_payload_with_iov_multi_form: %s\n" % e)

    def insertiov(self,tag,since,pyldf,payload):
        # pyldf is the BLOB
        # payload is the PayloadDto file
        papi_instance = PayloadsApi(self.api_client)
        iapi_instance = IovsApi(self.api_client)
        try:
            print ('Insert iov: %s %s %s %s' % (tag,since,pyldf,payload))
            data=''
            mhash = self.gethashfromfile(pyldf)
            dtodict = {}
            with open(payload, 'r') as myfile:
                data=myfile.read().replace('\n', '')  # Why does it need to replace \n ????
                #data=myfile.read()
                dtodict = json.loads(data)

            print ("loaded payload dto %s with hash %s !" % (data,dtodict['hash']))
            if dtodict['hash'] == '' or dtodict['hash'] == 'none':
                dtodict['hash'] = mhash
            tojsondto = json.dumps(dtodict)
            print ('Store dto %s and file %s' % (tojsondto,pyldf))
            papi_response = papi_instance.create_payload_multi_form(pyldf,tojsondto)
## Api response should be payloaddto
            #pprint(api_response)
            print ('Stored payload for hash %s' % papi_response)
            payloadhash = papi_response.hash
            pprint(payloadhash)
## Now store iov
            iov = IovDto(tag_name=tag, since=since, insertion_time=None, payload_hash=payloadhash)
            iapi_response = iapi_instance.create_iov(iov)
            pprint(iapi_response)
        except ApiException as e:
            print ("Exception when calling PayloadsApi->create_payload_multi_form: %s\n" % e)
            print ("Exception when calling IovsApi->create_iov: %s\n" % e)

    def rm(self,objtype, resid):

        api_instance = AdminApi(self.api_client)
        if objtype == 'globaltags':
            try:
                api_response = api_instance.remove_global_tag(resid)
                pprint(api_response)
            except ApiException as e:
                print ("Exception when calling AdminApi->remove_global_tag: %s\n" % e)
        elif objtype == 'tags':
            try:
                api_response = api_instance.remove_tag(resid)
                pprint(api_response)
            except ApiException as e:
                print ("Exception when calling AdminApi->remove_tag: %s\n" % e)
        else:
            print ('Cannot use resource %s' % objtype)
        return

    def add(self,objtype,filename):
        if objtype == 'globaltags':
            api_instance = GlobaltagsApi(self.api_client)
            try:
                f = open(filename, 'r')
                jsondtodict = f.read()
                dtodict = json.loads(jsondtodict)
                snapt = datetime.utcfromtimestamp(dtodict['snapshotTimeMilli'])
                snapttz = snapt.astimezone()
                print('Created datetime obj: %s , %s printable as %s'% (snapt,snapttz,snapt.astimezone().isoformat()))
                dto = GlobalTagDto(dtodict['name'],dtodict['validity'],dtodict['description'],dtodict['release'],
                                   None,snapttz,
                                   dtodict['scenario'],dtodict['workflow'],dtodict['type'],
                                   None,None)

                forcemode = False
                msg = ('Created dto resource %s ' ) % (dto.to_dict())
                pprint(msg)
                api_response = api_instance.create_global_tag(dto,force=forcemode)
                pprint(api_response)
            except ApiException as e:
                print ("Exception when calling GlobaltagsApi->create_global_tag: %s\n" % e)
        elif objtype == 'tags':
            api_instance = TagsApi(self.api_client)
            try:
                f = open(filename, 'r')
                jsondtodict = f.read()
                print ('Loaded json from file %s' % jsondtodict)
                dtodict = json.loads(jsondtodict)
                print ('Create dto dictionary from json %s ' % dtodict)
                dto = TagDto(dtodict['name'],dtodict['timeType'],dtodict['objectType'],dtodict['synchronization'],
                                   dtodict['description'],dtodict['lastValidatedTime'],dtodict['endOfValidity'],
                                   None,None)

                forcemode = False
                msg = ('Created dto resource %s ' ) % (dto.to_dict())
                pprint(msg)
                api_response = api_instance.create_tag(dto)
                pprint(api_response)
            except ApiException as e:
                print ("Exception when calling TagsApi->create_tag: %s\n" % e)

        elif objtype == 'maps':
            api_instance = GlobaltagmapsApi(self.api_client)
            try:
                f = open(filename, 'r')
                jsondtodict = f.read()
                dtodict = json.loads(jsondtodict)
                dto = GlobalTagMapDto(dtodict['globalTagName'],dtodict['record'],dtodict['label'],dtodict['tagName'])
                msg = ('Created dto resource %s ' ) % (dto.to_dict())
                pprint(msg)
                api_response = api_instance.create_global_tag_map(dto)
                pprint(api_response)
            except ApiException as e:
                print ("Exception when calling GlobaltagmapsApi->create_global_tag_map: %s\n" % e)
        else:
            print ('Cannot use resource %s' % objtype)
        return


    def ls(self,objtype):
        by = self.by # str | by: the search pattern {none} (optional) (default to none)
        page = self.page # int | page: the page number {0} (optional) (default to 0)
        size = self.pagesize # int | size: the page size {1000} (optional) (default to 1000)
        sort = self.sort # str | sort: the sort pattern {name:ASC} (optional) (default to name:ASC)
        if sort == 'none':
            sort = 'name:DESC'
        if objtype == 'globaltags':
        # create an instance of the API class
            api_instance = GlobaltagsApi(self.api_client)
            try:
            # Finds a GlobalTagDtos lists.
                api_response = api_instance.list_global_tags(by=by, page=page, size=size, sort=sort)
                pprint(api_response)
            except ApiException as e:
                print ("Exception when calling GlobaltagsApi->list_global_tags: %s\n" % e)

        elif objtype == 'tags':
            api_instance = TagsApi(self.api_client)
            try:
            # Finds a TagDtos lists.
                api_response = api_instance.list_tags(by=by, page=page, size=size, sort=sort)
                pprint(api_response)
            except ApiException as e:
                print ("Exception when calling TagsApi->list_tags: %s\n" % e )
        else:
            print ('Cannot use resource %s' % objtype)
        return

    def tracetags(self,globaltagname,record='',label=''):
        print ('Trace global tag %s ' % globaltagname)
        api_instance = GlobaltagsApi(self.api_client)
        try:
        # Finds a TagDtos lists associated to the global tag name in input.
            api_response = api_instance.find_global_tag_fetch_tags(globaltagname, record=record, label=label)
            pprint(api_response)
        except ApiException as e:
            print ("Exception when calling GlobaltagsApi->find_global_tag_fetch_tags: %s\n" % e)
        return

    def selectiovs(self,tagname):
        # create an instance of the API class
        api_instance = IovsApi(self.api_client)
        since = self.t0 # str | since: the since time as a string {0} (optional) (default to 0)
        until = self.tMax # str | until: the until time as a string {INF} (optional) (default to INF)
        snapshot = self.snap # int | snapshot: the snapshot time {0} (optional) (default to 0)

        try:
        # Select iovs for a given tagname and in a given range.
            api_response = api_instance.select_iovs(tagname=tagname, since=since, until=until, snapshot=snapshot)
            pprint(api_response)
        except ApiException as e:
            print ("Exception when calling IovsApi->select_iovs: %s\n" % e)
        return

    def getpayload(self,pyldhash):
        # create an instance of the API class
        api_instance = PayloadsApi(self.api_client)
        try:
        # Select iovs for a given tagname and in a given range.
            print ('Selecting payload using hash %s' % pyldhash)
            api_response = api_instance.get_payload(pyldhash)
            pprint(api_response)
        except ApiException as e:
            print ("Exception when calling PayloadsApi->get_payload: %s\n" % e)
        return

    def gethashfromfile(self,datafile):
        try:
            import hashlib
        # Select iovs for a given tagname and in a given range.
            print ('Compute payload hash from file %s' % datafile)
            with open(datafile, mode='rb') as file: # b is important -> binary
                fileContent = file.read()
                mh=hashlib.sha256(fileContent).hexdigest()
                pprint(mh)
                return mh
        except Exception as e:
            print ("Exception when calling gethashfromfile: %s\n" % e)
        return "none"


if __name__ == '__main__':
    PhysDBDriver()
