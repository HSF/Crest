'''
Created on Nov 24, 2017

@author: formica
'''

import cmd

import sys, os
import readline
import logging
import atexit
import argparse
from datetime import datetime
from crest.io import CrestDbIo
from crest.utils import *
from pip._vendor.pyparsing import empty
from collections import defaultdict
from crestcli import CrestCli

log = logging.getLogger(__name__)
log.setLevel(logging.DEBUG)

handler = logging.StreamHandler()
format = "%(levelname)s:%(name)s: %(message)s"
handler.setFormatter(logging.Formatter(format))
log.addHandler(handler)

sys.path.append(os.path.join(sys.path[0], '..'))
historyFile = '.crestconsole_hist'


class CrestConsoleUI(cmd.Cmd):
    """Simple command processor example."""
    cm = None
    prompt = '(Crest): '
    homehist = os.getenv('CDMS_HISTORY_HOME', os.environ["HOME"])
    histfile = os.path.join(homehist, historyFile)
    host = None
    loc_parser = None
    import re
    rr = r"""
        ([<|>|:]+)  # match one of the symbols
    """
    rr = re.compile(rr, re.VERBOSE)

    def init_history(self, histfile):
        readline.parse_and_bind("tab: complete")
        readline.set_history_length(100)
        if hasattr(readline, "read_history_file"):
            try:
                readline.read_history_file(histfile)
            except IOError:
                pass
            atexit.register(self.save_history, histfile)

    def save_history(self, histfile):
        log.info('Saving history in %s' % histfile)
        readline.write_history_file(histfile)

    def set_host(self, url):
        self.host = url

    def get_args(self, line=None):
        from shlex import split
        argv = split(line)
        print(f'CrestConsole arguments split into array : {argv}')
        self.loc_parser = argparse.ArgumentParser(description="parser options for cli", add_help=False)
        group = self.loc_parser.add_mutually_exclusive_group()
        group.add_argument("-v", "--verbose", action="store_true")
        group.add_argument("-q", "--quiet", action="store_true")
        ##self.loc_parser.add_argument('cmd', nargs='?', default='iovs')
        self.loc_parser.add_argument('type', nargs='?', default='iovs')
        self.loc_parser.add_argument('-h', '--help', action="store_true", help='show this help message')
        self.loc_parser.add_argument("-t", "--tag", help="the tag name")
        self.loc_parser.add_argument("-T", "--globaltag", help="the global tag name")
        self.loc_parser.add_argument("--params",
                                     help="the string containing k=v pairs for tags or global tags creation")
        self.loc_parser.add_argument("--inpfile", help="the input file to upload")
        self.loc_parser.add_argument("--mode", default="create", help="the mode is used to switch among create or update.")
        self.loc_parser.add_argument("--since", help="the since time for the payload.")
        self.loc_parser.add_argument("--page", default="0", help="the page number.")
        self.loc_parser.add_argument("--size", default="100", help="the page size.")
        self.loc_parser.add_argument("--sort", default="name:ASC", help="the sort parameter (depend on the selection).")
        self.loc_parser.add_argument("-f", "--fields", default='none',
                            help="the list of fields to show, separated with a comma. Use -f help to get the available fields.")
        #self.loc_parser.add_argument("-f", "--format", help="the output format, use <help> for details")
        self.loc_parser.add_argument("-p", "--hash", help="the payload hash")
        self.loc_parser.add_argument("-c", "--cut", help="additional selection parameters")
        self.loc_parser.add_argument("-g", "--groups", action="store_true", help="use to select groups instead of iovs")
        self.loc_parser.add_argument("-s", "--snapshot", default="0",
                                     help="add a snapshot time in ms for iovs and groups requests")
        self.loc_parser.add_argument("-H", "--header", default="BLOB",
                                     help="set header request for payload: BLOB, JSON, ...")
        return self.loc_parser.parse_args(argv)

    def do_connect(self, url=None):
        """connect [url]
        Use the url for server connections"""
        if not url:
            url = self.host
        self.cm = CrestCli(server_url=url)
        self.cm.set_host(url)
        self.cm.do_connect()
        log.info(f'Connected to {url}')

    def do_ls(self, line):
        """ls <datatype> [-t tag_name] [-T globaltag_name] [other options: --size, --page, --sort, --fields]
        Search for data collection of different kinds: iovs, tags, globaltags.
        datatype: iovs, tags, globaltags, trace, backtrace
        Type ls -h for help on available options (not all will be appliable depending on the chosen datatype)
        """
        largs = None
        if line:
            largs = self.get_args(line)
            if largs.help:
                self.loc_parser.print_help()
                return
        argsdic = vars(largs)
        self.cm.do_ls(argsdic)

    def do_create(self, line):
        """create -h
        Create a new tag, global tag, or mapping using a series of k=val pairs, separated
        by commas: create tags [globaltags, maps]
        """
        largs = None
        if line:
            largs = self.get_args(line)
            if largs.help:
                self.loc_parser.print_help()
                return
        argsdic = vars(largs)
        self.cm.do_create(argsdic)

    def do_update(self, line):
        """update -h
        Update a tag, global tag, or mapping, using a series of k=val pairs, separated
        by commas: update tags [globaltags, maps]
        """
        largs = None
        if line:
            largs = self.get_args(line)
            if largs.help:
                self.loc_parser.print_help()
                return
        argsdic = vars(largs)
        argsdic['mode'] = 'update'
        self.cm.do_create(argsdic)

    def do_upload(self, line):
        """upload -h
        Upload a file in a tag, the name is provided via --tag ATAG.
        You should also provide a since time and a file name in input."""
        largs = None
        if line:
            largs = self.get_args(line)
            if largs.help:
                self.loc_parser.print_help()
                return
        argsdic = vars(largs)
        self.cm.do_upload(argsdic)

    def do_info(self, line):
        """info [tagsize|payload] [-t sometag] [-p payloadhash]
        Search for meta information on a given tag, or on a given payload"""
        largs = None
        if line:
            largs = self.get_args(line)
            if largs.help:
                self.loc_parser.print_help()
                return
        argsdic = vars(largs)
        self.cm.do_info(argsdic)


    def do_select(self, line):
        """select [iovs|groups|ranges|size|iovpayloads] -t sometag -s snapshot -c since=1000,until=2000
        Select for iovs in the given tag, since and until can be defined using --cut"""
        largs = None
        if line:
            largs = self.get_args(line)
            if largs.help:
                self.loc_parser.print_help()
                return
        argsdic = vars(largs)
        self.cm.do_select(argsdic)

    def do_get(self, line):
        """get -p somehash [-i -H BLOB {JSON}]
        Search for payload with the given hash, eventually add an header param to determine the output format.
        The -i option can be used to get only meta data.
        """
        largs = None
        if line:
            largs = self.get_args(line)
            if largs.help:
                self.loc_parser.print_help()
                return
        argsdic = vars(largs)
        self.cm.do_get(argsdic)

    def do_convert(self, line):
        """convert date
        Convert a date string using format  to unix time."""
#        dt = datetime.fromisoformat(line)
        dt = datetime.strptime(line, '%Y-%m-%dT%H:%M:%S.%f%z')
        log.info('create time from string %s %s' % (line, dt.timestamp()))
        since = int(dt.timestamp() * 1000)
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
            import socks  # you need to install pysocks (use the command: pip install pysocks)
            # Configuration

            # Remove this if you don't plan to "deactivate" the proxy later
            #        default_socket = socket.socket
            # Set up a proxy
            #            if self.useSocks:
            socks.set_default_proxy(socks.SOCKS5, SOCKS5_PROXY_HOST, SOCKS5_PROXY_PORT)
            socket.socket = socks.socksocket
            print('Activated socks proxy on %s:%s' % (SOCKS5_PROXY_HOST, SOCKS5_PROXY_PORT))
        except:
            print('Error activating socks...%s %s' % (SOCKS5_PROXY_HOST, SOCKS5_PROXY_PORT))


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
    host = "{0}://{1}:{2}/{3}".format(prot, args.host, args.port, args.api)
    log.info('The host is set to %s' % host)
    os.environ['CDMS_HOST'] = host
    ui = CrestConsoleUI()
    ui.set_host(host)
    ui.do_connect()
    log.info('Start application')
    if args.socks:
        log.info(
            "Activating socks on localhost:3129 ; if you want another address please set CDMS_SOCKS_HOST and _PORT env vars")
        ui.socks()

    ui.cmdloop()
