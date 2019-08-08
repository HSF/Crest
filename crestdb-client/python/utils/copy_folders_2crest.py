#!/usr/bin/env python
# encoding: utf-8
'''
copy_folders_2crest -- shortdesc
    Copy folders from COOL to Crest.

It defines classes_and_methods

@author:     formica

@copyright:  2019 ATLAS. All rights reserved.

@license:    GPL

@contact:    andrea.formica@cern.ch
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

import crestapi

from crestapi.api import GlobaltagsApi, TagsApi, GlobaltagmapsApi, IovsApi, PayloadsApi, AdminApi, FoldersApi
from crestapi.models import FolderDto, GlobalTagDto, TagDto, TagMetaDto, GlobalTagMapDto, IovDto, PayloadDto, IovSetDto, IovPayloadDto
from crestapi import ApiClient as CrestApiClient
from crestapi.rest import ApiException as CrestApiException
from crestapi.configuration import Configuration as CrestConfiguration

import coolr
from pip._vendor.pyparsing import empty
from coolr.api import NodesApi, SchemasApi
from coolr.models import SchemaDto, NodeDto
from coolr import ApiClient as CoolApiClient
from coolr.rest import ApiException as CoolApiException
from coolr.configuration import Configuration as CoolConfiguration

from pprint import pprint

try:
    from StringIO import StringIO
except ImportError:
    from io import StringIO

class Cool2CrestDriver():
    def __init__(self):
    # process command line options
        try:
            self._command = sys.argv[0]
            self.useSocks = False
            self.debug = False
            self.by = 'none'
            self.sort = 'none'
            self.page = 0
            self.pagesize = 1000
            self.jsondump=False
            self.dump=False
            self.user='none'
            self.passwd='none'
            self.outfilename=''
            self.coolapi_client=None
            self.crestapi_client=None
            self.coolrurlsvc=os.getenv('COOLR_HOST', 'http://atlasfrontier07.cern.ch:9090/coolrapi')
            self.cresturlsvc=os.getenv('CREST_HOST', 'http://localhost:8080/crestapi')
            longopts=['help','socks','out=','jsondump','coolurl=','cresturl=','debug','by=','page=','pagesize=','user=','pass=','sort=']
            opts,args=getopt.getopt(sys.argv[1:],'',longopts)
            print ('%s, %s' % (opts, args))
            self.procopts(opts,args)
        except getopt.GetoptError as e:
            print (e)
            self.usage()
            sys.exit(-1)

        if self.useSocks:
            self.activatesocks()
        self._coolconfig = CoolConfiguration()
        self._coolconfig.host = self.coolrurlsvc
        self.coolrapi_client = CoolApiClient(self._coolconfig)
        self._crestconfig = CrestConfiguration()
        self._crestconfig.host = self.cresturlsvc
        self.crestapi_client = CrestApiClient(self._crestconfig)

        self.execute()

    def usage(self):
        print
        print ("usage: crest_mgr.py {<options>} <action> {<args>}")
        print ("Manage conditions DB content using args")
        print ("Server url is needed in order to provide the system with the base url; defaults to localhost (see below)")
        print ("Action determines which rest method to call")
        print ("Actions list:")
        print (" - COPY <schema> <dbname> ")
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
        print ("  --coolurl [localhost:8080/crestapi]: use a specific server ")
        print ("  --cresturl [localhost:8080/crestapi]: use a specific server ")
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
            if (o=='--coolurl'):
                self.coolurlsvc=a
            if (o=='--cresturl'):
                self.cresturlsvc=a
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


        if (self.action=='COPY'):
            try:
                print ('Action COPY is used to copy resources from COOL to Crest')
                print ('Found N arguments %s' % len(self.args))
                if len(self.args)<2:
                    sys.exit("Number of arguments is wrong...may be you forgot filename?")
                    raise
                schema=self.args[0]
                db=self.args[1]
                msg = ('COPY: selected schema is %s and db %s') % (schema,db)
                self.printmsg(msg,'cyan')
                self.copy(schema,db)
            except Exception as e:
                sys.exit("failed on action COPY: %s" % (str(e)))
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

    def copy(self,schema,db):
        # create an instance of the API class
        coolrapi_instance = NodesApi(self.coolrapi_client)
        crestapi_instance = FoldersApi(self.crestapi_client)
        try:
        # Finds a GlobalTagDtos lists.
            api_response = coolrapi_instance.list_nodes(schema=schema, db=db)
            for anodedto in api_response:
                print('Copy folder %s to crest' % anodedto.node_fullpath)
                node_name = anodedto.node_fullpath.replace('/','_')[1:]
                print('Store with node name %s' % node_name)
                tmptag = anodedto.node_fullpath[1:].lower().split('/')
                tmptag = [ x.capitalize() for x in tmptag]
                tag_pattern = "".join(tmptag)
                group_role = 'atlas-cond'
                print('Use tag pattern %s' % tag_pattern)
                try:
                    dto = FolderDto(anodedto.node_fullpath,anodedto.schema_name,node_name,anodedto.node_description,tag_pattern,group_role)
                    crest_response = crestapi_instance.create_folder(dto)
                    print(crest_response)
                except CrestApiException as e:
                    print ("Exception when calling FoldersApi->create_folder: %s\n" % e)
        except CoolApiException as e:
            print ("Exception when calling NodesApi->list_nodes: %s\n" % e)
        return



if __name__ == '__main__':
    Cool2CrestDriver()
