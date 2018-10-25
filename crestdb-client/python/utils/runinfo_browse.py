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
from __future__ import absolute_import

import sys, os, pickle, getopt,re
import json
import os.path
import traceback

#from pygments import highlight, lexers, formatters
from xml.dom import minidom
#from clint.textui import colored
from datetime import datetime, date
import time

sys.path.append(os.path.join(sys.path[0],'..'))

from RunLumiManagementApi import RunLumiApi
from crestapi.rest import ApiException
from pprint import pprint

try:
    from StringIO import StringIO
except ImportError:
    from io import StringIO

class RunLumiDriver():
    def __init__(self):
    # process command line options
        try:
            self._command = sys.argv[0]
            self.useSocks = False
            self.t0 = 0
            self.tMax = 'INF'
            self.debug = False
            self.by = 'none'
            self.sort = 'since:ASC'
            self.page = 0
            self.pagesize = 1000
            self.iovspan = 'time'
            self.jsondump=False
            self.dump=False
            self.outfilename=''
            self.urlsvc=os.getenv('CDMS_HOST', 'http://localhost:8080/crestapi')
            longopts=['help','socks','out=','jsondump','t0=','tMax=','url=','debug','by=','page=','pagesize=','iovspan=','sort=']
            opts,args=getopt.getopt(sys.argv[1:],'',longopts)
            print ('Options %s, args %s' % (opts, args))
            self.procopts(opts,args)
        except getopt.GetoptError as e:
            print(e)
            self.usage()
            sys.exit(-1)

        if self.useSocks:
            self.activatesocks()
        self.execute()

    def usage(self):
        print ("usage: runinfo_browse.py {<options>} select")
        print ("Search Crest DB content using args")
        print ("Server url is needed in order to provide the system with the base url; defaults to localhost (see below)")
        print (" ")
        print ("Options: ")
###        print ("  --socks activate socks proxy on localhost 3129 "
        print ("  --debug activate debugging output ")
        print ("  --socks activate a socks proxy on localhost:3129 ")
        print ("  --out={filename} activate dump on filename ")
        print ("  --jsondump activate a dump of output lines in json format ")
        print ("  --by : comma separated list of conditions for filtering a query (e.g.: by=run<222222,lb<10). DEFAULT=%s" % self.by)
        print ("  --sort : comma separated list of conditions for sorting the output of a query (e.g.: sort=name:DESC,id:ASC). DEFAULT=%s" % self.sort)
        print ("  --page [0,...N]: page number to retrieve; use it in combination with page size. DEFAULT=%s" % self.page)
        print ("  --pagesize [1000,30,...]: page size; use it in combination with page. DEFAULT=%s" % self.pagesize)
        print ("  --url [localhost:8080/physconddb]: use a specific server ")
        print ("  --t0={t0 for iovs}. DEFAULT=%s" % self.t0)
        print ("  --tMax={tMax for iovs}. DEFAULT=%s" % self.tMax)
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
            if (o=='--t0'):
                self.t0=a
            if (o=='--tMax'):
                self.tMax=a
            if (o=='--by'):
                self.by=a
            if (o=='--sort'):
                self.sort=a
            if (o=='--page'):
                self.page=a
            if (o=='--pagesize'):
                self.pagesize=a
            if (o=='--iovspan'):
                self.iovspan=a
                
    def execute(self):
        msg = ('Execute the command select' )
        self.printmsg(msg,'cyan')
            
        start = datetime.now()
        cool_max_run = 2147483647
        cool_max_lb = 4294967295

        if self.dump:
            outfile = open(self.outfilename,"w")
        _dict = {}
        since = self.t0
        until = self.tMax
        runinfoApi = RunLumiApi()
        cool_max_runlb = runinfoApi.runlumiToStime(cool_max_run, cool_max_lb)
        if self.debug:
            runinfoApi.setlevel('DEBUG')
        if self.iovspan == 'time':
            print('Assuming that t0 and tMax parameters are provided as unix time in milliseconds')
            if self.tMax == 'INF':
                until = int(time.time())*1000
            since = since*1000000
            until = until*1000000
            print ('Use the following time range: %s -> %s '% (since,until))
        elif self.iovspan == 'run':
            print('Assuming that t0 and tMax parameters are provided as run numbers, lumi block is 0')
            since = '%s,0' % since
            if self.tMax == 'INF':
                until = '999999999999'
            until = '%s,0' % until
            print ('Use the following run range: %s -> %s '% (since,until))
        elif self.iovspan == 'run-lb':
            print('Assuming that t0 and tMax parameters are provided as run lumi block numbers separated by a comma [e.g.: --t0=222222,23]')
            if str(self.t0) == '0':
                since = '0,0' 
            if self.tMax == 'INF':
                until = '999999999999,0'
            print ('Use the following run range: %s -> %s '% (since,until))
        elif self.iovspan == 'cool-runlb':
            print('Assuming that t0 and tMax parameters are provided as cool combinations of run lumi block numbers')
            if self.tMax == 'INF':
                until = cool_max_runlb
            print ('Use the following run range: %s -> %s '% (since,until))
        elif self.iovspan == 'date':
            print('Assuming that t0 and tMax parameters are provided as dates in ISO format')
            dt = datetime.strptime("1970-01-01:01:00:00", "%Y-%m-%d:%H:%M:%S")
            if str(self.t0) != '0':
                print('parsing date provided in %s ' % self.t0)
                dt = datetime.strptime(self.t0, "%Y-%m-%d:%H:%M:%S")
            print('Since date is %s ' % dt)
            since = self.mtimestamp(dt)*1000
            if self.tMax == 'INF':
                dt = datetime.strptime("2050-01-01:00:00:00", "%Y-%m-%d:%H:%M:%S")
            else:
                dt = datetime.strptime(self.tMax, "%Y-%m-%d:%H:%M:%S")   
            until = self.mtimestamp(dt)*1000
            print ('Use the following run range: %s -> %s '% (since,until))
        else:
            print('Cannot recognise iovspan parameter %s ' % self.iovspan)

        self.selectruns(runinfoApi,since,until,self.iovspan)

        tend=datetime.now()
        print ('Time spent (ms): %s' % (tend-start))

    def mtimestamp(self,dt):
        ts = None
        try:
            ts = dt.timestamp()
        except Exception as e:
            ts = (dt - datetime(1970, 1, 1)).total_seconds()
        return ts

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
        

    def selectruns(self,rmapi,since,until,iovspan):
        print('Selecting run info for %s interval %s - %s using filtering %s' % (iovspan,since,until,self.by))
        try: 
            mfmt = None
            res= None
            if iovspan == 'time' or iovspan == 'date':
                # this is a timestamp based query
                mfmt='time'
            elif iovspan == 'run' or iovspan == 'run-lb':
                mfmt = 'run-lumi'
 
            res=[]
            if self.by is not None:
                print ('Filtering is requested %s: overriding all previous parameters ' % self.by)
                res = rmapi.listRunLumiInfo(by=self.by,page=self.page,size=self.pagesize,sort=self.sort)
            else:
                res = rmapi.listRunLumiInfo(since=since,until=until,type=mfmt,page=self.page,size=self.pagesize,sort=self.sort)
            for row in res:
                run = row.run
                lb=row.lb
                st = row.starttime
                et = row.endtime
                startt = datetime.fromtimestamp(int(st)/1000000000)
                endt = datetime.fromtimestamp(int(et)/1000000000)
                print('Run=%s LB=%s start=%s end=%s coolrunlb=%s coolstart=%s coolend=%s' % (run,lb,startt,endt,int(row.since),int(row.starttime),int(row.endtime)))
            #print('Retrieved results \n %s' % res)
#            print (json.dumps(dictresp, indent=4, sort_keys=True))
            return 

        except ApiException as e:
            print ("Exception when calling selectruns: %s\n" % e)
        return
    
     
if __name__ == '__main__':
    RunLumiDriver()

