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
from crestutils import *
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
        self.loc_parser.add_argument('cmd', nargs='?', default='iovs')
        self.loc_parser.add_argument('-h', '--help', action="store_true", help='show this help message')
        self.loc_parser.add_argument("-t", "--tag", help="the tag name")
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
        """ls <datatype> [-t tag_name] [-T globaltag_name] [other options: --size, --page, --sort, --format]
        Search for data collection of different kinds: iovs, tags, globaltags.
        datatype: iovs, tags, globaltags, trace, backtrace
        Type ls -h for help on available options (not all will be appliable depending on the chosen datatype)
        """
        out = None
        fmt = 'short'
        if pattern:
            log.info ("Searching tags %s " % pattern)
            args = self.get_args(pattern)
            if args.help:
                self.loc_parser.print_help()
                return
            cmd = args.cmd
            log.info (f'Searching {cmd}')
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

            fields = []
            if args.format:
                if args.format == 'help':
                    if cmd == 'globaltags':
                        print(f'Fields for {cmd} are {gtagfieldsdic.keys()}')
                    elif cmd == 'tags':
                        print(f'Fields for {cmd} are {tagfieldsdic.keys()}')
                    elif cmd == 'iovs':
                        print(f'Fields for {cmd} are {iovfieldsdic.keys()}')
                    else:
                        print('The command is not known or does not need fields specifications')
                    return
                fields = args.format.split(',')

            if cmd == 'tags':
                if tagname is None:
                    tagname = "%"
                cdic['name'] = tagname
                out = self.cm.search_tags(page=args.page,size=args.size,sort=args.sort,**cdic)
            elif cmd == 'iovs':
                if 'name' in args.sort:
                    args.sort = 'id.insertionTime:ASC'
                print(f'Using tag {tagname} and dic {cdic}')
                out = self.cm.search_iovs(page=args.page,size=args.size,sort=args.sort,tagname=tagname,**cdic)
            elif cmd == 'globaltags':
                if gtagname is None:
                    gtagname = "%"
                cdic['name'] = gtagname
                out = self.cm.search_globaltags(page=args.page,size=args.size,sort=args.sort,**cdic)
            elif cmd == 'trace':
                if gtagname is None:
                    print('Select a global tag name')
                    return
                out = self.cm.search_maps(name=gtagname,mode='Trace')
                out['format'] = 'GlobalTagMapSetDto'
            elif cmd == 'backtrace':
                if tagname is None:
                    print('Select a tag name')
                    return
                out = self.cm.search_maps(name=tagname,mode='BackTrace')
                out['format'] = 'GlobalTagMapSetDto'
            else:
                print(f'Command {cmd} is not recognized in this context')
        else:
            log.info ('Search all tags')
            out = self.cm.search_tags()
        crest_print(out,format=fmt)

    def do_createtag(self, pattern):
        """createtag -h
        Create a new tag, the name is provided via --tag ATAG"""
        out = None
        fmt = 'short'
        if pattern:
            log.info ("Create tags %s " % pattern)
            args = self.get_args(pattern)
            if args.help:
                self.loc_parser.print_help()
                return
            fmt = args.format
            out = self.cm.create_tags(name=args.tag)
        else:
            log.info ('Cannot create a tag without arguments')
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

    def do_info(self, pattern):
        """info [tagsize|payload] [-t sometag] [-p payloadhash]
        Search for meta information on a given tag, or on a given payload"""
        out = None
        cmd = None
        tagname = None
        phash = None
        cdic = {}
        if pattern:
            args = self.get_args(pattern)
            if args.help:
                self.loc_parser.print_help()
                return
            cmd = args.cmd
            log.info (f'Searching {cmd}')
            if args.tag:
                tagname = args.tag
            if args.hash:
                phash = args.hash

            fields = []
            if cmd == 'tagsize':
                if args.snapshot:
                    cdic['snapshot'] = args.snapshot
                if tagname is None:
                    print('Cannot get size without a precise tag selection')
                    return
                out = self.cm.select(cmd='size',tagname=tagname,**cdic)
            elif cmd == 'payload':
                if 'phash' is None:
                    print('Cannot get size without a precise tag selection')
                    return
                cdic['info'] = 'meta'
                out = self.cm.get_payload(phash=phash,**cdic)
            else:
                print(f'Command {cmd} is not recognized in this context')
        else:
            log.info ('Need an input line...type -h for help')
        crest_print(out,format=fields)

    def do_select(self, line):
        """select [iovs|groups|ranges|size] -t sometag -s snapshot -c since=1000,until=2000
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
