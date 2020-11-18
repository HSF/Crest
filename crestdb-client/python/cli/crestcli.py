'''
Created on Nov 24, 2017

@author: formica
'''

import sys, os
import readline
import logging
import atexit
import argparse
from datetime import datetime
from crest.io import CrestDbIo
from crest.utils import *
from collections import defaultdict
import json

from pip._vendor.pyparsing import empty

log = logging.getLogger(__name__)
log.setLevel(logging.INFO)

handler = logging.StreamHandler()
format = "%(levelname)s:%(name)s: %(message)s"
handler.setFormatter(logging.Formatter(format))
log.addHandler(handler)

sys.path.append(os.path.join(sys.path[0], '..'))


class CrestCli(object):
    """Simple command processor example."""

    def __init__(self, server_url=None):
        if server_url is None:
            server_url = 'http://aiatlas137.cern.ch:8080/crestapi'

        cm = None
        host = None
        loc_parser = None
        import re
        rr = r"""
            ([<|>|:]+)  # match one of the symbols
        """
        rr = re.compile(rr, re.VERBOSE)

    def set_host(self, url):
        self.host = url

    def set_parser(self, prs):
        self.loc_parser = prs

    def do_connect(self, url=None, token=None):
        """connect [url]
        Use the url for server connections"""
        if not url:
            url = self.host
        self.cm = CrestDbIo(server_url=url, token=token)
        log.info(f'Connected to {url}')

    def do_ls(self, args=None):
        """ls <datatype> [-t tag_name] [-T globaltag_name] [other options: --size, --page, --sort, --format]
        Search for data collection of different kinds: iovs, tags, globaltags.
        datatype: iovs, tags, globaltags, trace, backtrace
        Type ls -h for help on available options (not all will be appliable depending on the chosen datatype)
        """
        out = None
        cmd = 'tags'
        tagname = None
        gtagname = None
        cdic = {}
        print(f'do_ls method is using {args}')
        if args:
            if 'type' in args:
                cmd = args['type']
            log.info(f'Searching for data of type {cmd}')
            if 'tag' in args:
                tagname = args['tag']
            if 'globaltag' in args:
                gtagname = args['globaltag']

            # Initialize cuts using the argument string.
            cdic = parse_cuts_arr(args['cut'])
            log.info('use cut params : %s' % cdic)

            fields = []
            if 'fields' in args:
                log.debug('fields is %s' % args.get('fields'))
                if 'none' == args['fields']:
                    fields = []
                elif 'help' == args['fields']:
                    print_help(cmd)
                    return
                else:
                    fields = args.get('fields').split(',')
                    log.info(f'Print columns {fields}')

            # Check the data type and search
            log.info(f'Launch ls with type {cmd}')
            if cmd == 'tags':
                if tagname is None:
                    tagname = "%"
                cdic['name'] = [tagname]
                log.info(f'Search tags using {tagname}')
                out = self.cm.search_tags(page=args['page'], size=args['size'], sort=args['sort'], **cdic)
            elif cmd == 'iovs':
                if 'name' in args['sort']:
                    args['sort'] = 'id.insertionTime:ASC'
                log.info(f'Search iovs using {tagname} and dic {cdic}')
                out = self.cm.search_iovs(page=args['page'], size=args['size'], sort=args['sort'], tagname=tagname,
                                          **cdic)
            elif cmd == 'globaltags':
                if gtagname is None:
                    gtagname = "%"
                cdic['name'] = [gtagname]
                log.info(f'Search globaltags using {gtagname}')
                out = self.cm.search_globaltags(page=args['page'], size=args['size'], sort=args['sort'], **cdic)
            elif cmd == 'trace':
                if gtagname is None:
                    print('Cannot trace : select a global tag name')
                    return
                log.info(f'Search globaltag trace using {gtagname}')
                out = self.cm.search_maps(name=gtagname, mode='Trace')
                out['format'] = 'GlobalTagMapSetDto'
            elif cmd == 'backtrace':
                if tagname is None:
                    print('Cannot backtrace: select a tag name')
                    return
                log.info(f'Search tag backtrace using {tagname}')
                out = self.cm.search_maps(name=tagname, mode='BackTrace')
                out['format'] = 'GlobalTagMapSetDto'
            else:
                print(f'Command {cmd} is not recognized in this context')
        crest_print(out, format=fields)

    def do_create(self, args):
        """create <datatype> --params k=v,k1=v1,....
        Create a new tag or global tag, using a series of k=val pairs, separated
        by commas.
        """
        out = None
        cmd = None
        pdic = {}
        tname = None
        gtname = None
        mode = ''
        if args:
            if 'type' in args:
                cmd = args['type']
            log.info(f'Creating object for type {cmd}')
            if 'tag' in args and cmd in ['tags', 'maps']:
                tname = args['tag']
            if 'globaltag' in args and cmd in ['globaltags', 'maps']:
                gtname = args['globaltag']

            if 'params' in args and args['params'] is not None:
                pdic = parse_params(args['params'])

            if 'mode' in args:
                mode = args['mode']

            if cmd == 'tags':
                log.info(f'Creating tag {tname} and args {pdic}')
                if len(pdic.items()) == 0:
                    print(f'Missing information; provide at least the timeType parameter!')
                    print(f'''Example params: 
                    payloadSpec=JSON, 
                    timeType=time, 
                    description=a tag, 
                    synchronization=none
                    ''')
                    return
                out = self.cm.create_tags(name=tname, mode=mode, **pdic)
            elif cmd == 'globaltags':
                log.info(f'Creating globaltag {tname} and args {pdic}')
                if 'update' == mode:
                    log.error(f'Not implemented for the moment in update mode....')
                    return
                if len(pdic.items()) == 0:
                    print(f'Missing information; provide at least the description parameter!')
                    print(f'''Example params: 
                    validity=0, 
                    release=rel-test, 
                    scenario=test, 
                    type=T, 
                    description=a new global tag, 
                    workflow=all
                    ''')
                    return

                out = self.cm.create_globaltags(name=gtname, **pdic)
            elif cmd == 'maps':
                log.info(f'Creating mapping for tag {tname} to gtag {gtname} and args {pdic}')
                if 'update' == mode:
                    log.error(f'Not implemented for the moment in update mode....')
                    return
                if len(pdic.items()) == 0:
                    print(f'Missing information; provide at least the record parameter!')
                    print(f'''Example params: 
                    record=generic-tag-type, 
                    label=none
                    ''')
                    return
                out = self.cm.create_maps(tag=tname, globaltag=gtname,
                                          record=pdic['record'], label=pdic['label'])
            else:
                log.error(f'Command {cmd} is not recognized in this context')

        else:
            log.info('Cannot create object without arguments')
        print(f'Response is : {out}')

    def do_upload(self, args):
        """upload -h
        Upload a file in a tag, the name is provided via --tag ATAG.
        You should also provide a since time and a file name in input."""
        out = None
        cmd = None
        if args:
            if 'type' in args:
                cmd = args['type']
            log.info("Create payload using %s " % args)
            if args['inpfile'] is None:
                print(f'Missing information; provide inpfile, since and tag!')
                print(f'''Example params: 
                --inpfile <the file> --since <the time> --tag <the tag>
                ''')
                return

            ifile = args['inpfile']
            since = args['since']
            tagname = args['tag']
            print(f'Upload file {ifile} @ {since} in tag {tagname}')
            out = self.cm.create_payload(tag=tagname, filename=ifile, since=since)
        else:
            log.info('Cannot create a payload without arguments: need an input file, a since and a tag')
        print(f'Response is : {out}')

    def do_info(self, args):
        """info [tagsize|payload] [-t sometag] [-p payloadhash]
        Search for meta information on a given tag, or on a given payload"""
        out = None
        cmd = None
        fields = []
        tagname = None
        phash = None
        cdic = {}
        cmd = None
        print(f'do_info method is using {args}')
        if args:
            if 'type' in args:
                cmd = args['type']
            log.info(f'Searching info for data of type {cmd}')
            if 'tag' in args:
                tagname = args['tag']
            if 'hash' in args:
                phash = args['hash']
            log.info(f'Get infos for {cmd}')

            if 'fields' in args:
                if 'help' == fields:
                    log.info(f'Getting help for type {args.type}')
                    print_help(args.type)
                    return
                fields = args['fields'].split(',')

            if cmd == 'tagsize':
                if 'snapshot' in args:
                    cdic['snapshot'] = args['snapshot']
                if tagname is None:
                    print('Cannot get size without a precise tag selection')
                    return
                out = self.cm.select(cmd='size', tagname=tagname, **cdic)
            elif cmd == 'payload':
                if phash is None:
                    print('Cannot get size without a precise hash selection')
                    return
                cdic['info'] = 'meta'
                out = self.cm.get_payload(phash=phash, **cdic)
            else:
                print(f'Command {cmd} is not recognized in this context')
        else:
            log.info('Need an input line...type -h for help')
        crest_print(out, format=fields)

    def do_monitor(self, args):
        """monitor [payloads] -t sometagpattern
        Provide monitoring information on a bunch of tags"""
        out = None
        cdic = {}
        cmd = None
        tagname = None
        fields = ['tagname', 'niovs', 'totvolume', 'avgvolume']
        if args:
            if 'cmd' in args:
                cmd = args['type']
            log.info(f'Selecting for types {cmd}')
            if 'tag' in args:
                tagname = args['tag']
            out = self.cm.get_summary(cmd=cmd, tagname=tagname, **cdic)
        else:
            log.info('Cannot search paylaods monitoring without tagname parameter')
        crest_print(out, format=fields)

    def do_select(self, args):
        """select [iovs|groups|ranges|size|iovpayloads] -t sometag -s snapshot -c since=1000,until=2000
        Select for iovs in the given tag, since and until can be defined using --cut"""
        out = None
        cdic = {}
        """
        The cut parameters are stored in a dictionary of the kind: key = [v1, v2, ..]
        """
        cmd = None
        tagname = None
        if args:
            if 'cmd' in args:
                cmd = args['type']
            log.info(f'Selecting for types {cmd}')
            if 'tag' in args:
                tagname = args['tag']
            if 'hash' in args:
                phash = args['hash']
            log.info(f'Get infos for {cmd}')

            if 'cut' in args and args['cut'] is not None:
                cdic = parse_params(args['cut'])
                log.info('use cut params : %s' % cdic)

            if 'snapshot' in args:
                cdic['snapshot'] = [args['snapshot']]

            out = self.cm.select(cmd=cmd, tagname=tagname, **cdic)

        else:
            log.info('Cannot search iovs without a tagname parameter')
            log.info(
                'Optional arguments are: -c since=222222,until=3333333 ; in addition also a snapshot can be provided')
        fmt = []
        if cmd in ['ranges', 'groups']:
            fmt = ['since']
        crest_print(out, format=fmt)

    def do_get(self, args):
        """get -p somehash [-i -H BLOB {JSON}]
        Search for payload with the given hash, eventually add an header param to determine the output format. The -i option can be used to get only meta data."""
        cmd = None
        phash = None
        if args:
            if 'cmd' in args:
                cmd = args['cmd']
            log.info(f'Searching for types {cmd}')
            if 'hash' in args:
                phash = args['hash']
            log.info(f'Get data for {cmd}')
            if 'header' in args:
                self.cm.set_header({"X-Crest-PayloadFormat": args['header']})
            out = self.cm.get_payload(phash=phash)
        else:
            log.info('Cannot get payload without a hash parameter')
        log.info(f'Output is {out}')

    def do_convert(self, line):
        """convert date
        Convert a date to UTC unix time."""
        dt = datetime.fromisoformat(line)
        log.info('create time from string %s %s' % (line, dt.timestamp()))
        since = int(dt.timestamp() * 1000)
        print(f'date {line} = {since}')

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


def authenticate(tennant='dev-andrea-1971.eu.auth0.com', client_payload=''):
    import http.client
    conn = http.client.HTTPSConnection(tennant)
    payload = client_payload
    headers = {'content-type': "application/json"}
    conn.request("POST", "/oauth/token", payload, headers)

    res = conn.getresponse()
    data = res.read()
    print(data.decode("utf-8"))
    tokdic = json.loads(data.decode("utf-8"))
    return tokdic


if __name__ == '__main__':
    # Parse arguments
    parser = argparse.ArgumentParser(description='Crest browser.', add_help=False)
    parser.add_argument('cmd', nargs='?', choices=['ls', 'select', 'create', 'get', 'monitor', 'update'], default='ls')
    parser.add_argument('--type', choices=['tags', 'iovs', 'payloads', 'groups', 'ranges', 'size',
                                           'globaltags', 'trace', 'backtrace', 'iovpayloads', 'maps'], default='tags')
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
    parser.add_argument('-h', '--help', action="store_true", help='show this help message')
    parser.add_argument("-t", "--tag", help="the tag name")
    parser.add_argument("-T", "--globaltag", help="the global tag name")
    parser.add_argument("--params", help="the string containing k=v pairs for tags or global tags creation")
    parser.add_argument("--token", action='store_true', help="get a token for later usage")
    parser.add_argument("--inpfile", help="the input file to upload")
    parser.add_argument("--since", help="the since time for the payload.")
    parser.add_argument("--page", default="0", help="the page number.")
    parser.add_argument("--size", default="100", help="the page size.")
    parser.add_argument("--sort", default="name:ASC", help="the sort parameter (depend on the selection).")
    parser.add_argument("-f", "--fields", default='none',
                        help="the list of fields to show, separated with a comma. Use -f help to get the available fields.")
    parser.add_argument("-p", "--hash", help="the payload hash")
    parser.add_argument("-c", "--cut", help="additional selection parameters. e.g. since=1000,until=2000")
    parser.add_argument("-g", "--groups", action="store_true", help="use to select groups instead of iovs")
    parser.add_argument("-s", "--snapshot", default="0", help="add a snapshot time in ms for iovs and groups requests")
    parser.add_argument("-H", "--header", default="BLOB", help="set header request for payload: BLOB, JSON, ...")
    parser.add_argument("-m", "--mode", default="create", help="the mode for the create commant : [create | update]")

    args = parser.parse_args()
    if args.help:
        parser.print_help()
        sys.exit()

    token = None
    if args.token:
        ## read client id
        client_payload = ''
        with open('.tennant', 'r') as tennant_file:
            client_payload = tennant_file.read()
        ## try to read local token file
        try:
            with open('.auth0token', 'r') as local_file:
                token = json.load(local_file)
        except FileNotFoundError as e:
            log.error('Exception in reading file for token....')

        if token is None:
            log.info(f'Generate new token...use id: {client_payload}')
            token = authenticate(client_payload=client_payload)
            with open('.auth0token', 'w') as local_file:
                json.dump(token, local_file)
        log.info(f'Using token {token} for requests')

    prot = "http"
    if args.ssl:
        prot = "https"
    host = "{0}://{1}:{2}/{3}".format(prot, args.host, args.port, args.api)
    log.info('The host is set to %s' % host)
    os.environ['CDMS_HOST'] = host
    ui = CrestCli()
    ui.set_host(host)
    ui.do_connect(token=token)

    log.info('Start application')
    if args.socks:
        log.info("Activating socks on localhost:3129\n if you want another address please set CDMS_SOCKS_HOST env.")
        ui.socks()

    ui.set_parser(parser)
    argsdic = vars(args)
    if args.fields:
        if args.fields == 'help':
            log.info(f'Getting help for type {args.type}')
            print_help(args.type)
            sys.exit()
    if args.params:
        if args.params == 'help':
            log.info(f'Getting help for type {args.type}')
            print_help(args.type)
            sys.exit()
    if args.cmd in ['ls']:
        log.info(f'Launch ls command on {args.type}')
        ui.do_ls(argsdic)
    elif args.cmd in ['select']:
        log.info(f'Launch select command on {args.type}')
        ui.do_select(argsdic)
    elif args.cmd in ['create']:
        log.info(f'Launch create command on {args.type}')
        if args.type == 'payloads':
            ui.do_upload(argsdic)
        else:
            ui.do_create(argsdic)
    elif args.cmd in ['get']:
        log.info(f'Launch get command on {args.type}')
        ui.do_get(argsdic)
    elif args.cmd in ['monitor']:
        log.info(f'Launch monitor command on {args.type}')
        ui.do_monitor(argsdic)
    else:
        log.info(f'Cannot launch command {args.cmd}')
