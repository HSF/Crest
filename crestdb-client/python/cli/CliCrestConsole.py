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
from crest.io import CrestDbIo

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
    loc_parser=None
    import re
    rr = r"""
        ([<|>|:]+)  # match one of the symbols
    """
    rr = re.compile(rr, re.VERBOSE)

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
        log.info ('Saving history in %s' % histfile)
        readline.write_history_file( histfile )
    def set_host(self, url):
        self.host = url

    def get_args(self, line=None):
        argv=line.split()
        self.loc_parser = argparse.ArgumentParser(description="parser options for cli",add_help=False)
        group = self.loc_parser.add_mutually_exclusive_group()
        group.add_argument("-v", "--verbose", action="store_true")
        group.add_argument("-q", "--quiet", action="store_true")
        self.loc_parser.add_argument('-h', '--help', action="store_true", help='show this help message')
        self.loc_parser.add_argument("-t", "--tag", help="the tag name")
        self.loc_parser.add_argument("-f", "--format", default="short", help="the output format")
        self.loc_parser.add_argument("-p", "--hash", help="the payload hash")
        self.loc_parser.add_argument("-c", "--cut", help="additional selection parameters")
        self.loc_parser.add_argument("-H", "--header", default="BLOB", help="set header request for payload: BLOB, JSON, ...")
        return self.loc_parser.parse_args(argv)

    def do_connect(self,url=None):
        """connect [url]
        Use the url for server connections"""
        if not url:
            url = self.host
        self.cm = CrestDbIo(server_url=url)
        log.info(f'Connected to {url}')

    def do_ls(self, pattern):
        """ls -h
        Search for tags which contain the input pattern provided via option --tag ATAG"""
        out = None
        fmt = 'short'
        if pattern:
            log.info ("Searching tags %s " % pattern)
            args = self.get_args(pattern)
            if args.help:
                self.loc_parser.print_help()
                return
            fmt = args.format
            out = self.cm.search_tags(name=args.tag)
        else:
            log.info ('Search all tags')
            out = self.cm.search_tags()
        crest_print(out,format=fmt)

    def do_iovs(self, line):
        """iovs -t sometag [-c since=<1000,insertionTime=>123456]
        Search for iovs in the given tag, other parameters can be added using --cut"""
        out = None
        fmt = 'short'
        if line:
            log.info ("Searching iovs using %s " % line)
            args = self.get_args(line)
            if args.help:
                self.loc_parser.print_help()
                return
            fmt = args.format
            if args.cut:
                cutstringarr = args.cut.split(',')
                cdic = {}
                for el in cutstringarr:
                    ss = self.rr.findall(el)
                    ##print(el,ss)
                    (k,v) = el.split(ss[0])
                    cdic[k] = f'{ss[0]}{v}'
                log.info('use cut params : %s' % cdic)
                out = self.cm.search_iovs(tagname=args.tag,**cdic)
            else:
                out = self.cm.search_iovs(tagname=args.tag)

        else:
            log.info ('Cannot search iovs without a tagname parameter')
            log.info ('Optional arguments are: -c insertionTime=>222222 , where the time needs to be expressed in ms')
        crest_print(out,fmt)

    def do_get(self, line):
        """get -p somehash [-i -H BLOB {JSON}]
        Search for payload with the given hash, eventually add an header param to determine the output format. The -i option can be used to get only meta data."""
        out = None
        fmt = 'short'
        if line:
            log.info ("Searching payload using %s " % line)
            args = self.get_args(line)
            fmt = args.format
            if args.header:
                self.cm.set_header({"X-Crest-PayloadFormat" : args.header})
            out = self.cm.get_payload(phash=args.hash)
        else:
            log.info ('Cannot get payload without a hash parameter')
        log.info(f'Output is {out}')


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
        log.info('Cannot find results to print')
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
