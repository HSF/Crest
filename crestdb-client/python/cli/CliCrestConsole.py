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
from datetime import datetime
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

gtagfieldsdic = {
    'name' : '{name:25.25s}',
    'release' : '{release:10s}',
    'workflow' : '{workflow:10s}',
    'scenario' : '{scenario:10s}',
    'validity' : '{validity:10d}',
    'description' : '{description:50s}',
    'snapshotTime' : '{snapshotTime:30s}',
}
gtagfieldsdicheader = {
    'name' : {'key':'{name:25.25s}','val' : 'GlobalTag'},
    'release' : {'key':'{release:10s}','val' : 'Release'},
    'workflow' :  {'key':'{workflow:10s}','val' : 'Workflow'},
    'scenario' : {'key':'{scenario:10s}','val' : 'Scenario'},
    'validity' :  {'key':'{validity:10s}','val' : 'Validity'},
    'description' : {'key':'{description:50s}','val' : 'Description'},
    'snapshotTime' : {'key':'{snapshotTime:30s}', 'val' : 'Snapshot Time'}
}
iovfieldsdic = {
    'since' : '{since:15d}',
    'payloadHash' : '{payloadHash:50s}',
    'insertionTime' : '{insertionTime:30s}',
}
iovfieldsdicheader = {
    'since' : {'key':'{since:15s}','val' : 'since'},
    'payloadHash' : {'key':'{payloadHash:50s}','val' : 'Hash'},
    'insertionTime' : {'key':'{insertionTime:30s}', 'val' : 'Insertion Time'}
}
tagfieldsdic = {
    'name' : '{name:25.25s}',
    'timeType' : '{timeType:10s}',
    'payloadSpec' : '{payloadSpec:10s}',
    'synchronization' : '{synchronization:10s}',
    'lastValidatedTime' : '{lastValidatedTime:10d}',
    'endOfValidity' : '{endOfValidity:10d}',
    'description' : '{description:50s}',
    'insertionTime' : '{insertionTime:30s}',
}
tagfieldsdicheader = {
    'name' : {'key':'{name:25.25s}','val' : 'Tag'},
    'timeType' : {'key':'{timeType:10s}','val' : 'Type'},
    'payloadSpec' :  {'key':'{payloadSpec:10s}','val' : 'Payload'},
    'synchronization' : {'key':'{synchronization:10s}','val' : 'Synchro'},
    'lastValidatedTime' :  {'key':'{lastValidatedTime:10s}','val' : 'Last-Valid'},
    'endOfValidity' :  {'key':'{endOfValidity:10s}','val' : 'End-Valid'},
    'description' : {'key':'{description:50s}','val' : 'Description'},
    'insertionTime' : {'key':'{insertionTime:30s}', 'val' : 'Insertion Time'}
}

def dprint(format,headerdic,datadic,cdata):
    if len(format) == 0:
        format = datadic.keys()
    headerfmtstr = ' '.join([ headerdic[k]['key'] for k in format])
    headic = {}
    for k in format:
        headic[k] = headerdic[k]['val']
    print(headerfmtstr.format(**headic))
    #print('Use format %s' % format)
    fmtstr = ' '.join([datadic[k] for k in format])
    #print('Format string %s'%fmtstr)
    for xt in cdata:
        adic = {}
        for k in format:
            if xt[k] is None:
                xt[k] = ' - '
            adic[k]=xt[k]
        #print('Use dictionary %s'%adic)
        print(fmtstr.format(**adic))

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
        self.loc_parser.add_argument('cmd', nargs='?', default='iovs')
        self.loc_parser.add_argument('-h', '--help', action="store_true", help='show this help message')
        self.loc_parser.add_argument("-t", "--tag", help="the tag name")
        self.loc_parser.add_argument("-T", "--globaltag", help="the global tag name")
        self.loc_parser.add_argument("--params", help="the string containing k=v pairs for tags or global tags creation")
        self.loc_parser.add_argument("--inpfile", help="the input file to upload")
        self.loc_parser.add_argument("--since", help="the since time for the payload.")
        self.loc_parser.add_argument("-f", "--format", default="short", help="the output format, use 'all' for details")
        self.loc_parser.add_argument("-p", "--hash", help="the payload hash")
        self.loc_parser.add_argument("-c", "--cut", help="additional selection parameters")
        self.loc_parser.add_argument("-g", "--groups", action="store_true", help="use to select groups instead of iovs")
        self.loc_parser.add_argument("-s", "--snapshot", default="0", help="add a snapshot time in ms for iovs and groups requests")
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
        cmd = None
        tagname = None
        gtagname = None
        if pattern:
            args = self.get_args(pattern)
            if args.help:
                self.loc_parser.print_help()
                return
            cmd = args.cmd
            log.info (f'Searching {cmd}')
            fmt = args.format
            if args.tag:
                tagname = args.tag
            if args.globaltag:
                gtagname = args.globaltag

            cdic = {}
            if args.cut:
                cutstringarr = args.cut.split(',')
                for el in cutstringarr:
                    ss = self.rr.findall(el)
                    ##print(el,ss)
                    (k,v) = el.split(ss[0])
                    cdic[k] = f'{ss[0]}{v}'
                log.info('use cut params : %s' % cdic)

            if cmd == 'tags':
                if tagname is None:
                    tagname = "%"
                cdic['name'] = tagname
                out = self.cm.search_tags(**cdic)
            elif cmd == 'iovs':
                out = self.cm.search_iovs(name=tagname,**cdic)
            elif cmd == 'globaltags':
                if gtagname is None:
                    gtagname = "%"
                cdic['name'] = gtagname
                out = self.cm.search_globaltags(**cdic)
            else:
                print(f'Command {cmd} is not recognized in this context')
        else:
            log.info ('Search all tags')
            out = self.cm.search_tags()
        crest_print(out,format=fmt)

    def do_create(self, pattern):
        """create -h
        Create a new tag or global tag, using a series of k=val pairs, separated
        by commas
        """
        out = None
        fmt = 'short'
        if pattern:
            args = self.get_args(pattern)
            if args.help:
                self.loc_parser.print_help()
                return
            cmd = args.cmd
            log.info (f'Creating {cmd}')
            fmt = args.format
            tname = None
            if args.tag:
                tname = args.tag
            if args.globaltag:
                tname = args.globaltag
            pdic = {}
            if args.params:
                pararr = args.params.split(',')
                for par in pararr:
                    kv = par.split('=')
                    pdic[kv[0]] = kv[1]

            if cmd == 'tags':
                out = self.cm.create_tags(name=tname, **pdic)
            elif cmd == 'globaltags':
                out = self.cm.create_globaltags(name=tname, **pdic)
            else:
                print(f'Command {cmd} is not recognized in this context')

        else:
            log.info ('Cannot create object without arguments')
        print(f'Response is : {out}')

    def do_upload(self, pattern):
        """upload -h
        Upload a file in a tag, the name is provided via --tag ATAG.
        You should also provide a since time and a file name in input."""
        out = None
        fmt = 'short'
        if pattern:
            log.info ("Create payload %s " % pattern)
            args = self.get_args(pattern)
            if args.help:
                self.loc_parser.print_help()
                return
            fmt = args.format
            print(f'Upload file {args.inpfile} @ {args.since} in tag {args.tag}')
            out = self.cm.create_payload(tag=args.tag,filename=args.inpfile,since=args.since)
        else:
            log.info ('Cannot create a payload without arguments')
        print(f'Response is : {out}')

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

    def do_select(self, line):
        """select [iovs|groups|ranges] -t sometag -s snapshot -c since=1000,until=2000
        Select for iovs in the given tag, since and until can be defined using --cut"""
        out = None
        fmt = 'short'
        cdic = {}
        if line:
            log.info ("Searching iovs using %s " % line)
            args = self.get_args(line)
            cdic['snapshot'] = args.snapshot
            if args.help:
                self.loc_parser.print_help()
                return
            fmt = args.format
            if args.cut:
                cutstringarr = args.cut.split(',')
                cdic = {}
                for el in cutstringarr:
                    (k,v) = el.split('=')
                    cdic[k] = f'{v}'
                log.info('use cut params : %s' % cdic)
            cmd = args.cmd
            out = self.cm.select(cmd=cmd,tagname=args.tag,**cdic)

        else:
            log.info ('Cannot search iovs without a tagname parameter')
            log.info ('Optional arguments are: -c since=222222,until=3333333 ; in addition also a snapshot can be provided')
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

    def do_convert(self, line):
        """convert date
        Convert a date to UTC unix time."""
        dt=datetime.fromisoformat(line)
        log.info('create time from string %s %s' % (line,dt.timestamp()))
        since=int(dt.timestamp()* 1000)
        print(f'date {line} = {since}')

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
    if crestdata is None or 'size' not in crestdata.keys():
        log.info('Cannot find results to print')
        return
    size=crestdata['size']
    print(f'Retrieved {size} lines')
    dataarr = crestdata['resources']
    if (crestdata['format'] == 'TagSetDto'):
        dprint([],tagfieldsdicheader,tagfieldsdic,dataarr)

    elif (crestdata['format'] == 'GlobalTagSetDto'):
        dprint([],gtagfieldsdicheader,gtagfieldsdic,dataarr)

    elif (crestdata['format'] == 'IovSetDto'):
        dprint([],iovsfieldsdicheader,iovsfieldsdic,dataarr)
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
