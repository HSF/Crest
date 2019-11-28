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


class CrestDbIo(HttpIo):
    """
    (A)synchronous HTTP client
    """
    def __init__(self, server_url=None, max_tries=5, #pylint: disable=R0913
                 backoff_factor=1, asynchronous=False, loop=None):
        if server_url is None:
            server_url = 'http://svom-fsc-1.lal.in2p3.fr:20097/api'
        super().__init__(server_url,
                         max_tries=max_tries,
                         backoff_factor=backoff_factor,
                         asynchronous=asynchronous,
                         loop=loop)
        self.endpoints = { 'tags': '/tags', 'iovs' : '/iovs', \
            'payloads' : '/payloads', 'globaltags' : '/globaltags', \
            'maps' : '/globaltagmaps' }

        self.headers = {"Content-Type" : "application/json", "Accept" : "application/json"}
        self.crest_headers = {"X-Crest-PayloadFormat" : "BLOB"}

    def set_header(self, hdr):
        """
        set {hdr} as self.crest_headers
        """
        # example : {"X-Crest-PayloadFormat" : "JSON"}
        self.crest_headers = hdr

    def search_tags(self, **kwargs):
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
        by_crit = ','.join([f'{key}:{val}' for key, val in kwargs.items()])
        criteria = {'by': by_crit}

        # send request
        resp = self.get(self.endpoints['tags'], params=criteria)
        return resp.json()

    def search_globaltags(self, **kwargs):
        """
        request and export data from the database in json format
        usage example: search_globaltags(name='SVOM')
        """
        # define output fields
        valid_filters = ['name', 'payloadspec', 'timetype']

        # check request validity
        if not set(kwargs.keys()).issubset(valid_filters):
            log.error('Requested filters should be in %s', valid_filters)

        # prepare request arguments
        for key, val in kwargs.items():
            if not val.startswith('>') and not val.startswith('<') and \
                not val.startswith(':'):
                kwargs[key] = ':'+val
        by_crit = ','.join([f'{key}{val}' for key, val in kwargs.items()])
        criteria = {'by': by_crit}

        # send request
        resp = self.get(self.endpoints['globaltags'], params=criteria)
        return resp.json()

    def search_iovs(self, tagname=None, **kwargs):
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
        for key, val in kwargs.items():
            if not val.startswith('>') and not val.startswith('<') and \
                not val.startswith(':'):
                kwargs[key] = ':'+val
        by_crit = f'tagname:{tagname},'
        by_crit += ','.join([f'{key}{val}' for key, val in kwargs.items()])
        criteria = {'by': by_crit}

        # send request
        resp = self.get(self.endpoints['iovs'], params=criteria)
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
        criteria = {'tagname': tagname }
        for key, val in kwargs.items():
            criteria[key] = val
        cmddic = { 'groups' : '/selectGroups', 'iovs' : '/selectIovs', 'ranges' : '/selectIovs'}
        # send request
        loc_headers = {"X-Crest-Query" : "iovs"}
        if cmd == 'ranges':
            loc_headers = {"X-Crest-Query" : "ranges"}

        resp = self.get(self.endpoints['iovs']+cmddic[cmd], params=criteria, headers=loc_headers)
        return resp.json()

    def create_tags(self, name=None, **kwargs):
        """
        request and export data from the database in json format
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
            'name' : name,
            'payloadSpec' : 'JSON',
            'timeType' : 'time',
            'description' : 'a new tag',
            'synchronization' : 'none',
            'lastValidatedTime' : 0,
            'endOfValidity' : 0,
            'insertionTime' : None,
            'modificationTime' : None}
        for key, val in kwargs.items():
            body_req[key] = val
        log.info('Create tag : %s', json.dumps(body_req))
        # send request
        resp = self.post(self.tags_endpoint, json=body_req, headers=self.headers)
        return resp.json()

    def create_globaltags(self, name=None, **kwargs):
        """
        request and export data from the database in json format
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
            'name' : name,
            'release' : 'none',
            'type' : 'T',
            'description' : 'a new gtag',
            'release' : 'none',
            'scenario' : 'none',
            'validity' : 0,
            'workflow' : 'all',
            'snapshotTime' : None,
            'insertionTime' : None}
        for key, val in kwargs.items():
            body_req[key] = val
        log.info('Create global tag : %s', json.dumps(body_req))
        # send request
        resp = self.post(self.endpoints['globaltags'], json=body_req, headers=self.headers)
        return resp.json()

    def create_payload(self, filename=None, tag=None, since=None, timeformat='ms', **kwargs):
        """
        request and import data into the database
        If you want to use a date as a string put format = 'str'
        usage example: create_payload(file='/tmp/temp-01.txt', tag='SVOM-TEST-01', since=1234567)
        """
        # define output fields
        valid_fields = ['endtime']
        if timeformat != 'ms':
            dtime = datetime.fromisoformat(since)
            log.info('create time from string %s %s', since, dtime.timestamp())
            since = int(dtime.timestamp()* 1000)
        # check request validity
        if not set(kwargs.keys()).issubset(valid_fields):
            log.error('Requested fields should be in %s', valid_fields)

        # prepare request arguments
        with open(filename, 'rb') as fin:
            files_req = {
                'file': (filename, fin),
                'endtime' : (None, 0),
                'tag' : (None, tag),
                'since' : (None, since)
            }
            for key, val in kwargs.items():
                files_req[key] = (None, val)
            log.info('Create payload : %s', files_req)
            # send request
            loc_url = self.payloads_endpoint+'/store'
            loc_headers = {"X-Crest-PayloadFormat" : "JSON"}

            resp = self.post(loc_url, files=files_req, headers=loc_headers)
        return resp.json()

    def get_payload(self, phash=None, fout='/tmp/out.blob', **kwargs):
        """
        retrieve data from the database
        usage example: get_payload(phash=  ,fout='/tmp/out.blob')
        """
        # define output fields
        valid_fields = []

        # check request validity
        if not set(kwargs.keys()).issubset(valid_fields):
            log.error('Requested fields should be in %s', valid_fields)

        # prepare request arguments
        log.info('Get payload : %s', phash)
        # send request
        loc_url = self.payloads_endpoint+'/'+phash
        resp = self.get(loc_url, headers=self.crest_headers)
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
