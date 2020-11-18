"""
 HTTP client tool to exchange with VHF-PKT-DB

 Retries requests with power law delays and a max tries limit

 @author: henri.louvin@cea.fr
"""

# Log
import logging
# JSON
import json
from datetime import datetime

from .httpio import HttpIo

log = logging.getLogger('crestdb_client')

def create_search(cdic={}, by=''):
    # prepare request arguments
    # assume that the values in the dictionary are arrays
    by_crit = by
    for key, lval in cdic.items():
        for val in lval:
            a = val
            if val[0] not in ['>', '<', ':']:
                a = ':' + val
            by_crit += (f',{key}{a}')
    if len(by) == 0:
        return by_crit[1:]
    return by_crit



class CrestDbIo(HttpIo):
    """
    (A)synchronous HTTP client
    """

    def __init__(self, server_url=None, max_tries=1,  # pylint: disable=R0913
                 backoff_factor=1, asynchronous=False, loop=None, token=None):
        if server_url is None:
            server_url = 'http://svom-fsc-1.lal.in2p3.fr:20097/api'
        super().__init__(server_url,
                         max_tries=max_tries,
                         backoff_factor=backoff_factor,
                         asynchronous=asynchronous,
                         loop=loop)
        self.endpoints = {'tags': '/tags', 'iovs': '/iovs', \
                          'payloads': '/payloads', 'globaltags': '/globaltags', \
                          'monitoring': '/monitoring',
                          'maps': '/globaltagmaps'}

        self.headers = {"Content-Type": "application/json", "Accept": "application/json"}
        self.crest_headers = {"X-Crest-PayloadFormat": "BLOB"}
        if token:
            authstr = f'{token["token_type"]} {token["access_token"]}'
            self.headers['authorization'] = authstr

    def set_header(self, hdr):
        """
        set {hdr} as self.crest_headers
        """
        # example : {"X-Crest-PayloadFormat" : "JSON"}
        for k, v in hdr.items():
            self.crest_headers[k] = v

    def build_header(self, hdr={}):
        """
        append {hdr} to self.headers
        """
        # example : {"X-Crest-PayloadFormat" : "JSON"}
        for k, v in self.headers.items():
            hdr[k] = v
        for k, v in self.crest_headers.items():
            hdr[k] = v
##        print(f'Use header : {hdr}')
        return hdr

    def search_tags(self, page=0, size=100, sort='name:ASC', **kwargs):
        """
        request and export data from the database in json format
        usage example: search_tags(name='SVOM', payloadspec=JSON)
        ?by=name:SVOM,payloadspec:JSON”
        """
        # define output fields
        valid_filters = ['name', 'payloadspec', 'timetype']

        # check request validity
        if not set(kwargs.keys()).issubset(valid_filters):
            log.error('Requested filters should be in %s', valid_filters)

        # prepare request arguments
        by_crit = create_search(cdic=kwargs)
        criteria = {'by': by_crit}
        log.debug(f'Use criteria {by_crit}')
        criteria['page'] = page
        criteria['size'] = size
        criteria['sort'] = sort
        loc_headers = self.build_header()
        # send request
        resp = self.get(self.endpoints['tags'], params=criteria, headers=loc_headers)
        return resp.json()

    def search_maps(self, name=None, mode='Trace'):
        """
        request and export data from the database in json format
        usage example: search_maps(name='A-GT-OR-T-NAME')
        mode: trace|backtrace
        name: if mode is trace, then this represents a GT name, otherwise is a Tag name
        """
        # prepare headers
        map_header = {'X-Crest-MapMode': mode}
        # send request
        loc_headers = self.build_header(hdr=map_header)

        resp = self.get(self.endpoints['maps'] + f'/{name}', headers=loc_headers)
        return resp.json()

    def search_globaltags(self, page=0, size=100, sort='name:ASC', **kwargs):
        """
        request and export data from the database in json format
        usage example: search_globaltags(name='SVOM')
        """
        # define output fields
        valid_filters = ['name', 'scenario', 'release', 'workflow']

        # check request validity
        if not set(kwargs.keys()).issubset(valid_filters):
            log.error('Requested filters should be in %s', valid_filters)

        # prepare request arguments
        by_crit = create_search(cdic=kwargs)
        criteria = {'by': by_crit}
        log.debug(f'Use criteria {by_crit}')
        criteria['page'] = page
        criteria['size'] = size
        criteria['sort'] = sort
        loc_headers = self.build_header()

        # send request
        resp = self.get(self.endpoints['globaltags'], params=criteria, headers=loc_headers)
        return resp.json()

    def search_iovs(self, page=0, size=100, sort='id.since:ASC', tagname=None, **kwargs):
        """
        request and export data from the database in json format
        usage example:
        search_iovs(tagname='SVOM', insertionTime='1574429040079')
        """
        # define output fields
        valid_filters = ['insertionTime', 'since']

        # check request validity
        if not set(kwargs.keys()).issubset(valid_filters):
            log.error('Requested filters should be in %s', valid_filters)

        # prepare request arguments
        by_crit = f'tagname:{tagname}'
        by_crit = create_search(kwargs, by_crit)

        criteria = {'by': by_crit}
        log.debug(f'Use criteria {by_crit}')
        criteria['page'] = page
        criteria['size'] = size
        criteria['sort'] = sort
        loc_headers = self.build_header()

        print(f'Iov search request using {criteria}')
        # send request
        resp = self.get(self.endpoints['iovs'], params=criteria, headers=loc_headers)
        return resp.json()

    def get_summary(self, cmd='payloads', tagname=None, **kwargs):
        """
        request monitoring data for a tag from the database in json format
        usage example: get_summary(cmd='payloads',tagname='SVOM')
        The possible options for cmd are: payloads
        """
        # define output fields
        valid_filters = []

        # check request validity
        if not set(kwargs.keys()).issubset(valid_filters):
            log.error('Requested filters should be in %s', valid_filters)

        # prepare request arguments
        criteria = {'tagname': tagname}
        cmddic = {'payloads': '/payloads'}
        loc_headers = self.build_header()

        resp = self.get(self.endpoints['monitoring'] + cmddic[cmd], params=criteria, headers=loc_headers)
        return resp.json()

    def select(self, cmd='groups', tagname=None, **kwargs):
        """
        request and export iovs or groups data from the database in json format
        usage example: select(cmd='iovs',tagname='SVOM', snapshot='1574429040079')
        The possible options for cmd are: iovs, groups, ranges
        """
        # define output fields
        valid_filters = ['snapshot', 'since', 'until']

        # check request validity
        if not set(kwargs.keys()).issubset(valid_filters):
            log.error('Requested filters should be in %s', valid_filters)

        # prepare request arguments
        criteria = {'tagname': tagname}
        for key, val in kwargs.items():
            criteria[key] = val
        cmddic = {'groups': '/selectGroups', 'iovs': '/selectIovs',
                  'iovpayloads': '/selectIovPayloads', 'ranges': '/selectIovs',
                  'size': '/getSize'}
        # send request
        loc_headers = {"X-Crest-Query": "groups"}
        if cmd == 'ranges':
            loc_headers = {"X-Crest-Query": "ranges"}
        loc_headers = self.build_header(hdr=loc_headers)
        print(f'Using url {cmddic[cmd]} and parameters {criteria} and headers {loc_headers}')
        resp = self.get(self.endpoints['iovs'] + cmddic[cmd], params=criteria, headers=loc_headers)
        return resp.json()

    def create_tags(self, name=None, mode='create', **kwargs):
        """
        import data into the database in json format
        usage example: create_tags(name='SVOM-01', payloadSpec='JSON',
        timeType='time',description='a tag',synchronization='none')
        """
        # define output fields
        valid_fields = ['payloadSpec', 'timeType', 'description', 'synchronization']

        # check request validity
        if not set(kwargs.keys()).issubset(valid_fields):
            log.error('Requested fields should be in %s', valid_fields)

        # prepare request arguments
        body_req = {
            'name': name,
            'payloadSpec': 'JSON',
            'timeType': 'time',
            'description': 'a new tag',
            'synchronization': 'none',
            'lastValidatedTime': 0,
            'endOfValidity': 0,
            'insertionTime': None,
            'modificationTime': None}
        for key, val in kwargs.items():
            body_req[key] = val
        log.info('Create tag : %s', json.dumps(body_req))
        loc_headers = self.build_header()
        print(f'create tags: use header {loc_headers}')
        # send request
        resp = None
        if 'create' == mode:
            resp = self.post(self.endpoints['tags'], json=body_req, headers=loc_headers)
        elif 'update' == mode:
            resp = self.put(self.endpoints['tags'] + f'/{name}', json=body_req, headers=loc_headers)
        return resp.json()

    def create_maps(self, tag=None, globaltag=None, record=None, label=None):
        """
        import data into the database in json format
        usage example: create_maps(tag='MXT-CONFIG-01', globaltag='SVOM-01',
        record='ok',label='mxt-config')
        """
        # prepare request arguments
        body_req = {
            'globalTagName': globaltag,
            'record': record,
            'label': label,
            'tagName': tag
        }
        log.info('Create mapping : %s', json.dumps(body_req))
        loc_headers = self.build_header()

        # send request
        resp = self.post(self.endpoints['maps'], json=body_req, headers=loc_headers)
        return resp.json()

    def create_iovs(self, name=None, since=None, phash=None, **kwargs):
        """
        import data into the database in json format
        usage example: create_iovs(name='SVOM-01', phash='somehash',
        since=1000)
        """
        # define output fields
        valid_fields = ['insertionTime']

        # check request validity
        if not set(kwargs.keys()).issubset(valid_fields):
            log.error('Requested fields should be in %s', valid_fields)

        # prepare request arguments
        body_req = {
            'tagName': name,
            'since': since,
            'payloadHash': phash,
            'insertionTime': None}
        loc_headers = self.build_header()

        for key, val in kwargs.items():
            body_req[key] = val
        log.info('Create iov if the hash is known : %s', json.dumps(body_req))
        # send request
        resp = self.post(self.endpoints['iovs'], json=body_req, headers=loc_headers)
        return resp.json()

    def create_globaltags(self, name=None, **kwargs):
        """
        import data into the database in json format
        usage example: create_globaltags(name='GT-SVOM-01', validity=0,
        description='some gt',release='a release',scenario='none',workflow='onl',
        type='T',snapshotTime='2020-01-01T10:00:00')
        """
        # define output fields
        valid_fields = ['validity', 'description', 'type', 'release', \
                        'scenario', 'workflow', 'snapshotTime']

        # check request validity
        if not set(kwargs.keys()).issubset(valid_fields):
            log.error('Requested fields should be in %s', valid_fields)

        # prepare request arguments
        body_req = {
            'name': name,
            'release': 'none',
            'type': 'T',
            'description': 'a new gtag',
            'scenario': 'none',
            'validity': 0,
            'workflow': 'all',
            'snapshotTime': None,
            'insertionTime': None}
        for key, val in kwargs.items():
            body_req[key] = val
        log.info('Create global tag : %s', json.dumps(body_req))
        loc_headers = self.build_header()

        # send request
        resp = self.post(self.endpoints['globaltags'], json=body_req, headers=loc_headers)
        return resp.json()

    def create_payload(self, filename=None, tag=None, since=None, timeformat='ms', **kwargs):
        """
        import data into the database.
        If you want to use a date as a string put timeformat = 'str' .
        usage example: create_payload(file='/tmp/temp-01.txt', tag='SVOM-TEST-01', since=1234567)
        """
        # define output fields
        valid_fields = ['endtime']
        if timeformat != 'ms':
            dtime = datetime.fromisoformat(since)
            log.info('create time from string %s %s', since, dtime.timestamp())
            since = int(dtime.timestamp() * 1000)
        # check request validity
        if not set(kwargs.keys()).issubset(valid_fields):
            log.error('Requested fields should be in %s', valid_fields)

        # prepare request arguments
        with open(filename, 'rb') as fin:
            files_req = {
                'file': (filename, fin),
                'endtime': (None, 0),
                'tag': (None, tag),
                'since': (None, since)
            }
            for key, val in kwargs.items():
                files_req[key] = (None, val)
            log.info('Create payload : %s', files_req)
            # send request
            loc_headers = {'X-Crest-PayloadFormat': 'JSON'}
            if 'authorization' in self.headers.keys():
                loc_headers['authorization'] = self.headers['authorization']
            print(f'store payload utilizes header : {loc_headers}')
            resp = self.post(self.endpoints['payloads'] + '/store',
                             files=files_req, headers=loc_headers)
        return resp.json()

    def get_payload(self, phash=None, fout='/tmp/out.blob', **kwargs):
        """
        retrieve data from the database
        usage example: get_payload(phash=  ,fout='/tmp/out.blob')
        """
        # define output fields
        valid_fields = ['info']

        # check request validity
        if not set(kwargs.keys()).issubset(valid_fields):
            log.error('Requested fields should be in %s', valid_fields)

        # prepare request arguments
        log.info('Get payload : %s', phash)
        # send request
        loc_url = self.endpoints['payloads'] + '/' + phash
        ismeta = {'info': 'all'}
        for key, val in kwargs.items():
            ismeta[key] = val
        if ismeta['info'] == 'meta':
            loc_url = loc_url + '/meta'
            self.crest_headers['X-Crest-PayloadFormat'] = 'DTO'
        log.info(f'Using get payload with meta {ismeta["info"]}')
        loc_headers = self.build_header()

        # if ismeta['info'] == 'all':
        #     respmeta = self.get(loc_url + '/meta', headers=self.crest_headers)
        #     if respmeta.status_code == 200:
        #         log.info(f'Retrieve meta information for the payload {phash}: {respmeta.json()}')
        #
        resp = self.get(loc_url, headers=loc_headers)
        # If the HTTP GET request can be served
        if resp.status_code == 200:
            # Write the file contents in the response to a file specified by local_file_path
            if self.crest_headers['X-Crest-PayloadFormat'] == 'BLOB':
                with open(fout, 'wb') as local_file:
                    for chunk in resp.iter_content(chunk_size=128):
                        local_file.write(chunk)
            else:
                fout = resp.json()
        return fout
