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

from crestapi.api import GlobaltagsApi, TagsApi, IovsApi, PayloadsApi, FoldersApi
from crestapi.models import GlobalTagDto, TagDto, GlobalTagMapDto, IovDto, FolderDto
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
            print ('Options %s, args %s' % (opts, args))
            self.procopts(opts,args)
        except getopt.GetoptError as e:
            print(e)
            self.usage()
            sys.exit(-1)

        if self.useSocks:
            self.activatesocks()
        self._config = Configuration()
        self._config.host = self.urlsvc
        self.api_client = ApiClient(self._config)
        self.execute()

    def usage(self):
        print ("usage: crest_browse.py {<options>} <action> {<args>}")
        print ("Search conditions DB content using args")
        print ("Server url is needed in order to provide the system with the base url; defaults to localhost (see below)")
        print ("Action determines which rest method to call")
        print ("Actions list:")
        print (" - LS <type> [id] [type = tags, globaltags, ...] : retrieve list of resources of the selected type, use option <by> to filter it")
        print ("        ex: FIND globaltags : retrieve full list of global tags")
        print ("        ex: --by=name:BLKPA globaltags : retrieve list of global tags where name contains BLKPA")
        print (" ")
        print (" - INFO <tag name>: show metadata associated with tag. ")
        print (" ")
        print (" - TRACE <globaltag name>: show tags associated with the selected global tag. ")
        print (" ")
        print (" - IOVS <tag name>: show iovs associated to the selected tag, use option to limit time range. ")
        print (" ")
        print (" - PAYLOAD <hash>: show payload associated to the selected hash. ")
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
            print ('Analyse options %s %s'% ( o, a ))
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


        if (self.action=='LS'):
            try:
                print ('Action LS is used to retrieve object from the DB')
                print ('Found N arguments %s' % len(self.args))
                object=self.args[0]
                msg = ('LS: selected object is %s ') % (object)
                if object in [ 'globaltags', 'tags', 'folders' ]:
                    self.printmsg(msg,'cyan')
                else:
                    msg = ('LS: cannot apply command to object %s ') % (object)
                    self.printmsg(msg,'red')
                    return -1

                resp = self.ls(object)
                if self.dump:
#                    json.dump(resp, outfile)
                    for el in resp:
                        outfile.write(el['name'])
                        outfile.write('\n')
                    outfile.close()


            except Exception as e:
                sys.exit("failed on action LS: %s" % (str(e)))
                raise
        elif (self.action=='INFO'):
            try:
                print ('Action INFO is used to retrieve tags metadata associated to a given tag')
                print ('Found N arguments %s' % len(self.args))
                object=self.args[0]
                msg = ('INFO: selected tag is %s ') % (object)
                self.printmsg(msg,'cyan')
                resp = self.infotags(object)
                if self.dump:
                    json.dump(resp, outfile)
                    outfile.close()

            except Exception as e:
                sys.exit("failed on action TRACE: %s" % (str(e)))
                raise

        elif (self.action=='TRACE'):
            try:
                print ('Action TRACE is used to retrieve tags associated to a given global tag')
                print ('Found N arguments %s' % len(self.args))
                object=self.args[0]
                msg = ('TRACE: selected global tag is %s ') % (object)
                self.printmsg(msg,'cyan')
                resp = self.tracetags(object)
                if self.dump:
                    json.dump(resp, outfile)
                    outfile.close()

            except Exception as e:
                sys.exit("failed on action TRACE: %s" % (str(e)))
                raise

        elif (self.action=='IOVS'):
            try:
                print ('Action IOVS is used to retrieve IOVS associated to a given tag')
                print ('Found N arguments %s' % len(self.args))
                object=self.args[0]
                msg = ('IOVS: selected tag is %s ') % (object)
                self.printmsg(msg,'cyan')
                resp = self.selectiovs(object)
                if self.dump:
                    json.dump(resp, outfile)
                    outfile.close()

            except Exception as e:
                sys.exit("failed on action IOVS: %s" % (str(e)))
                raise

        elif (self.action=='GROUPS'):
            try:
                print ('Action GROUPS is used to retrieve GROUPS associated to a given tag')
                print ('Found N arguments %s' % len(self.args))
                object=self.args[0]
                msg = ('GROUPS: selected tag is %s ') % (object)
                self.printmsg(msg,'cyan')
                resp=None
                if '%' in object:
                    if object == '%':
                        print('Search all tags...')
                    else:
                        tagname = object.replace("%","")
                        self.by = 'name:%s' % tagname
                        self.printmsg(self.by,'red')
                    taglist = self.ls('tags')
                    print('Loop over tag list of length: %s' % len(taglist))
                    for atag in taglist:
                        print('Use tagname for groups search : %s' % atag['name'])
                        resp = self.selectgroups(atag['name'])
                        if self.dump:
        #                    json.dump(resp, outfile)
                            groupsarr = resp['groups']
                            print('Loop over array of %s groups ' % len(groupsarr))
                            for i,el in enumerate(groupsarr):
#                               print ('Use element %s %s for tag %s' % (i,el, atag['name']))
                                if i>0:
#                                    print ('Dump in file element %s %s %s' % (i,str(int(el)),str(int(groupsarr[i-1]))))
                                    outfile.write('%s,%s,%s' % (atag['name'],str(int(groupsarr[i-1])),str(int(el))))
                                    outfile.write('\n')

                    outfile.close()
                    sys.exit(0)
                else:
                    resp = self.selectgroups(object)

                if self.dump:
                    json.dump(resp, outfile)
                    outfile.close()

            except Exception as e:
                sys.exit("failed on action GROUPS: %s" % (str(e)))
                raise

        elif (self.action=='PAYLOAD'):
            try:
                print ('Action PAYLOAD is used to retrieve PAYLOAD associated to a given hash')
                print ('Found N arguments %s' % len(self.args))
                object=self.args[0]
                msg = ('PAYLOAD: selected hash is %s ') % (object)
                self.printmsg(msg,'cyan')
                resp = self.getpayload(object)
                if self.dump:
                    json.dump(resp, outfile)
                    outfile.close()

            except Exception as e:
                exc_type, exc_value, exc_traceback = sys.exc_info()
                print ("*** print_tb:")
                traceback.print_tb(exc_traceback, limit=1, file=sys.stdout)
                print ("*** print_exception:")
                traceback.print_exception(exc_type, exc_value, exc_traceback,
                              limit=2, file=sys.stdout)

                sys.exit("failed on action PAYLOAD: %s" % (str(e)))
                raise


        else:
            print ("Command not recognized: please type -h for help")


        tend=datetime.now()
        print ('Time spent (ms): %s' % (tend-start))

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

    def ls(self,objtype):
        by = self.by # str | by: the search pattern {none} (optional) (default to none)
        page = self.page # int | page: the page number {0} (optional) (default to 0)
        size = self.pagesize # int | size: the page size {1000} (optional) (default to 1000)
        sort = self.sort # str | sort: the sort pattern {name:ASC} (optional) (default to name:ASC)
        if sort == 'none':
            sort = 'name:DESC'
        if objtype == 'globaltags':
            print ('Retrieve list of global tags from DB')
        # create an instance of the API class
            api_instance = GlobaltagsApi(self.api_client)
            try:
            # Finds a GlobalTagDtos lists.
                api_response = api_instance.list_global_tags(by=by, page=page, size=size, sort=sort)
                dictresp = self.api_client.sanitize_for_serialization(api_response)
                print (json.dumps(dictresp, indent=4, sort_keys=True))
                return dictresp
            except ApiException as e:
                print ("Exception when calling GlobaltagsApi->list_global_tags: %s\n" % e)

        elif objtype == 'tags':
            print ('Retrieve list of tags from DB')
            api_instance = TagsApi(self.api_client)
            try:
            # Finds a TagDtos lists.
                api_response = api_instance.list_tags(by=by, page=page, size=size, sort=sort)
                dictresp = self.api_client.sanitize_for_serialization(api_response)
                print (json.dumps(dictresp, indent=4, sort_keys=True))
                return dictresp

            except ApiException as e:
                print ("Exception when calling TagsApi->list_tags: %s\n" % e)

        elif objtype == 'folders':
            api_instance = FoldersApi(self.api_client)
            try:
            # Finds a TagDtos lists.
                api_response = api_instance.list_folders(by=by, sort=sort)
                pprint(api_response)
            except ApiException as e:
                print ("Exception when calling FoldersApi->list_folders: %s\n" % e )

        else:
            print ('Cannot use resource %s ' % objtype )
        return

    def infotags(self,tagname):
        print ('Trace global tag %s ' % tagname)
        api_instance = TagsApi(self.api_client)
        try:
        # Finds a TagDtos lists associated to the global tag name in input.
            api_response = api_instance.find_tag_meta(tagname)
            dictresp = self.api_client.sanitize_for_serialization(api_response)
            print (json.dumps(dictresp, indent=4, sort_keys=True))
            return dictresp

        except ApiException as e:
            print ("Exception when calling TagsApi->find_tag_meta: %s\n" % e)
        return

    def tracetags(self,globaltagname,record='',label=''):
        print ('Trace global tag %s ' % globaltagname)
        api_instance = GlobaltagsApi(self.api_client)
        try:
        # Finds a TagDtos lists associated to the global tag name in input.
            api_response = api_instance.find_global_tag_fetch_tags(globaltagname, record=record, label=label)
            dictresp = self.api_client.sanitize_for_serialization(api_response)
            print (json.dumps(dictresp, indent=4, sort_keys=True))
            return dictresp

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
            dictresp = self.api_client.sanitize_for_serialization(api_response)
            print (json.dumps(dictresp, indent=4, sort_keys=True))
            print ('Retrieved list of %d iovs for tag %s' % (len(api_response),tagname))
            return dictresp

        except ApiException as e:
            print ("Exception when calling IovsApi->select_iovs: %s\n" % e)
        return

    def selectgroups(self,tagname):
        # create an instance of the API class
        api_instance = IovsApi(self.api_client)
        snapshot = self.snap # int | snapshot: the snapshot time {0} (optional) (default to 0)

        try:
        # Select iovs for a given tagname and in a given range.
            api_response = api_instance.select_groups(tagname=tagname, snapshot=snapshot)
            dictresp = self.api_client.sanitize_for_serialization(api_response)
            print (json.dumps(dictresp, indent=4, sort_keys=True))
            print ('Retrieved list of %d groups for tag %s' % (len(api_response.groups),tagname))
            return dictresp

        except ApiException as e:
            print ("Exception when calling IovsApi->select_groups: %s\n" % e)
        return

    def getpayload(self,pyldhash):
        # create an instance of the API class
        api_instance = PayloadsApi(self.api_client)
        try:
        # Select iovs for a given tagname and in a given range.
            print ('Selecting payload using hash %s' % pyldhash)
            api_response = api_instance.get_blob(pyldhash)
            #            dictresp = self.api_client.sanitize_for_serialization(api_response)
            #print (json.dumps(dictresp, indent=4, sort_keys=True))
            print (api_response)
            dictresp=api_response
            return dictresp

        except ApiException as e:
            print ("Exception when calling PayloadsApi->get_payload: %s\n" % e)
        return

if __name__ == '__main__':
    PhysDBDriver()
