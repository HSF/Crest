'''
Created on Nov 24, 2017

@author: formica
'''

import cmd

import sys,os
import readline
import logging
import atexit
import argparse
from svom.messaging import CrestDbIo

from pip._vendor.pyparsing import empty

log = logging.getLogger( __name__ )
log.setLevel( logging.INFO )

handler = logging.StreamHandler()
format = "%(levelname)s:%(name)s: %(message)s"
handler.setFormatter( logging.Formatter( format ) )
log.addHandler( handler )

sys.path.append(os.path.join(sys.path[0],'..'))
historyFile = '.crestconsole_hist'

class CrestConsoleUI(cmd.Cmd):
    """Simple command processor example."""
    cm = None
    prompt ='(Crest): '
    homehist = os.getenv('CDMS_HISTORY_HOME', os.environ["HOME"])
    histfile = os.path.join( homehist, historyFile)
    host=None
    def init_history(self, histfile):
        readline.parse_and_bind( "tab: complete" )
        readline.set_history_length( 100 )
        if hasattr( readline, "read_history_file" ):
            try:
                readline.read_history_file( histfile )
            except IOError:
                pass
            atexit.register( self.save_history, histfile )

    def save_history(self, histfile):
        print ('Saving history in %s' % histfile)
        readline.write_history_file( histfile )
    def set_host(self, url):
        self.host = url

    def get_args(self, line=None):
        argv=line.split()
        parser = argparse.ArgumentParser(description="parser options for cli")
        group = parser.add_mutually_exclusive_group()
        group.add_argument("-v", "--verbose", action="store_true")
        group.add_argument("-q", "--quiet", action="store_true")
        parser.add_argument("-t", "--tag", help="the tag name")
        parser.add_argument("-f", "--format", default="short", help="the output format")
        parser.add_argument("-p", "--hash", help="the payload hash")
        parser.add_argument("-c", "--cut", help="additional selection parameters")
        parser.add_argument("-H", "--header", default="BLOB", help="set header request for payload: BLOB, JSON, ...")
        return parser.parse_args(argv)

    def do_connect(self,url=None):
        """connect [url]
        Use the url for server connections"""
        if not url:
            url = self.host
        self.cm = CrestDbIo(server_url=url)
        print(f'Connected to {url}')

    def do_ls(self, pattern):
        """ls [tag name pattern]
        Search for tags which contain the input pattern"""
        out = None
        fmt = 'short'
        if pattern:
            print ("Searching tags %s " % pattern)
            args = self.get_args(pattern)
            fmt = args.format
            out = self.cm.search_tags(name=args.tag)
        else:
            print ('Search all tags')
            out = self.cm.search_tags()
        crest_print(out,format=fmt)

    def do_iovs(self, line):
        """iovs [-t sometag ...]
        Search for iovs in the given tag and eventually other parameters"""
        out = None
        fmt = 'short'
        if line:
            print ("Searching iovs using %s " % line)
            args = self.get_args(line)
            fmt = args.format
            if args.cut:
                cutstringarr = args.cut.split()
                cdic = {}
                for el in cutstringarr:
                    (k,v) = el.split('=')
                    cdic[k] = v
                print('use cut params : %s' % cdic)
                out = self.cm.search_iovs(tagname=args.tag,**cdic)
            else:
                out = self.cm.search_iovs(tagname=args.tag)

        else:
            print ('Cannot search iovs without a tagname parameter')
            print ('Optional arguments are: -c insertionTime>222222 , where the time needs to be expressed in ms')
        crest_print(out,fmt)

    def do_get(self, line):
        """get [-p somehash ...]
        Search for payload with the given hash, eventually add an header param"""
        out = None
        fmt = 'short'
        if line:
            print ("Searching payload using %s " % line)
            args = self.get_args(line)
            fmt = args.format
            if args.header:
                self.cm.set_header({"X-Crest-PayloadFormat" : args.header})
            out = self.cm.get_payload(phash=args.hash)
        else:
            print ('Cannot get payload without a hash parameter')
        print(f'Output is {out}')


    def do_exit(self, line):
        return True
    def do_quit(self, line):
        return True
    def emptyline(self):
        pass

    def preloop(self):
        self.init_history(self.histfile)

    def postloop(self):
        print

    def socks(self):
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

def crest_print(crestdata, format='all'):
    if 'size' not in crestdata.keys():
        print('Cannot find results to print')
        return
    size=crestdata['size']
    print(f'Retrieved {size} lines')
    dataarr = crestdata['resources']
    if (crestdata['format'] == 'TagSetDto'):
        if format == 'all':
            print('{name:40s} {instime:28.28s} {endtime:15.15s} {synchro:10s} {desc:60.60s}'.format(name='tag name',instime='Insertion time',endtime='End time',synchro='Synchro',desc='Description'))
            for xt in dataarr:
                print('{name:40s} {instime:28.28s} {endtime:15d} {synchro:10s} {desc:60.60s}'.format(name=xt['name'],instime=xt['insertionTime'],endtime=xt['endOfValidity'],synchro=xt['synchronization'],desc=xt['description']))
        else:
            print('{name:60.60s} {instime:28.28s}'.format(name='tag name',instime='Insertion time'))
            for xt in dataarr:
                print('{name:60.60s} {instime:28.28s} '.format(name=xt['name'],instime=xt['insertionTime']))
    elif (crestdata['format'] == 'IovSetDto'):
        print('{name:15.15s} {instime:28.28s} {hash:65s}'.format(name='since',instime='Insertion time',hash='HASH'))
        for xt in dataarr:
            print('{since:15d} {instime:28s} {hash:65s}'.format(since=xt['since'],instime=xt['insertionTime'],hash=xt['payloadHash']))
    else:
        print(crestdata)
if __name__ == '__main__':
        # Parse arguments
    parser = argparse.ArgumentParser(description='Crest browser.')
    parser.add_argument('--host', default='localhost',
                        help='Host of the Crest service (default: svomtest.svom.fr)')
    parser.add_argument('--api', default='crestapi',
                        help='Base name of the api (default: crestapi)')
    parser.add_argument('--port', default='8090',
                        help='Port of the Crest service (default: 8090)')
    parser.add_argument('--socks', action='store_true',
                        help='Activate socks (default: false)')
    parser.add_argument('--ssl', action='store_true',
                        help='Activate ssl (default: false)')
    args = parser.parse_args()

    prot = "http"
    if args.ssl:
        prot = "https"
    host = "{0}://{1}:{2}/{3}".format(prot,args.host,args.port,args.api)
    log.info('The host is set to %s' % host)
    os.environ['CDMS_HOST']=host
    ui = CrestConsoleUI()
    ui.set_host(host)
    ui.do_connect()
    log.info('Start application')
    if args.socks:
        log.info("Activating socks on localhost:3129 ; if you want another address please set CDMS_SOCKS_HOST and _PORT env vars")
        ui.socks()

    ui.cmdloop()