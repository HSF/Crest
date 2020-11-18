'''
Created on Nov 24, 2017

@author: formica
'''

import sys, os
import logging
import atexit
from prettytable import PrettyTable
from beautifultable import BeautifulTable
from collections import defaultdict
from termcolor import colored

print('__file__={0:<35} | __name__={1:<20} | __package__={2:<20}'.format(__file__, __name__, str(__package__)))

shortheaddic = {
    'globaltags': ['name', 'release', 'workflow', 'snapshotTime'],
    'tags': ['name', 'timeType', 'synchronization', 'insertionTime']
}
gtagfieldsdic = {
    'name': '{name:25s}',
    'release': '{release:20s}',
    'workflow': '{workflow:20s}',
    'scenario': '{scenario:20s}',
    'validity': '{validity:10d}',
    'description': '{description:50s}',
    'snapshotTime': '{snapshotTime:30s}',
}
gtagfieldsdicheader = {
    'name': {'key': '{name:25s}', 'val': 'GlobalTag'},
    'release': {'key': '{release:20s}', 'val': 'Release'},
    'workflow': {'key': '{workflow:20s}', 'val': 'Workflow'},
    'scenario': {'key': '{scenario:20s}', 'val': 'Scenario'},
    'validity': {'key': '{validity:10s}', 'val': 'Validity'},
    'description': {'key': '{description:50s}', 'val': 'Description'},
    'snapshotTime': {'key': '{snapshotTime:30s}', 'val': 'Snapshot Time'}
}
iovfieldsdic = {
    'since': '{since:15d}',
    'payloadHash': '{payloadHash:65s}',
    'insertionTime': '{insertionTime:30s}',
}
iovfieldsdicheader = {
    'since': {'key': '{since:15s}', 'val': 'since'},
    'payloadHash': {'key': '{payloadHash:65s}', 'val': 'Hash'},
    'insertionTime': {'key': '{insertionTime:30s}', 'val': 'Insertion Time'}
}
iovpyldfieldsdic = {
    'since': '{since:20d}',
    'insertionTime': '{insertionTime:30s}',
    'payloadHash': '{payloadHash:65s}',
    'size': '{size:12d}',
    'objectType': '{objectType:15s}',
    'version': '{version:10s}',
    'streamerInfo': '{streamerInfo:50s}',
}
iovpyldfieldsdicheader = {
    'since': {'key': '{since:20s}', 'val': 'since'},
    'insertionTime': {'key': '{insertionTime:30s}', 'val': 'Insertion Time'},
    'payloadHash': {'key': '{payloadHash:65s}', 'val': 'Hash'},
    'size': {'key': '{size:12s}', 'val': 'Size(Bytes)'},
    'objectType': {'key': '{objectType:15s}', 'val': 'Type'},
    'version': {'key': '{version:10s}', 'val': 'Version'},
    'streamerInfo': {'key': '{streamerInfo:50s}', 'val': 'StreamerInfo'},
}
tagfieldsdic = {
    'name': '{name:25s}',
    'timeType': '{timeType:10s}',
    'payloadSpec': '{payloadSpec:10s}',
    'synchronization': '{synchronization:10s}',
    'lastValidatedTime': '{lastValidatedTime:10d}',
    'endOfValidity': '{endOfValidity:20d}',
    'description': '{description:50s}',
    'insertionTime': '{insertionTime:30s}',
}
tagfieldsdicheader = {
    'name': {'key': '{name:25s}', 'val': 'Tag'},
    'timeType': {'key': '{timeType:10s}', 'val': 'Type'},
    'payloadSpec': {'key': '{payloadSpec:10s}', 'val': 'Payload'},
    'synchronization': {'key': '{synchronization:10s}', 'val': 'Synchro'},
    'lastValidatedTime': {'key': '{lastValidatedTime:10s}', 'val': 'Last-Valid'},
    'endOfValidity': {'key': '{endOfValidity:20s}', 'val': 'End-Valid'},
    'description': {'key': '{description:50s}', 'val': 'Description'},
    'insertionTime': {'key': '{insertionTime:30s}', 'val': 'Insertion Time'}
}
mapfieldsdic = {
    'record': '{record:30.30s}',
    'label': '{label:40.40s}',
    'globalTagName': '{globalTagName:30.30s}',
    'tagName': '{tagName:50s}',
}
mapfieldsdicheader = {
    'record': {'key': '{record:30.30s}', 'val': 'Record'},
    'label': {'key': '{label:40.40s}', 'val': 'Label'},
    'globalTagName': {'key': '{globalTagName:30.30s}', 'val': 'GlobalTag'},
    'tagName': {'key': '{tagName:50s}', 'val': 'Tag'},
}
pyldfieldsdic = {
    'version': '{version:15s}',
    'objectType': '{objectType:25s}',
    'size': '{size:10d}',
    'insertionTime': '{insertionTime:30s}',
}
pyldfieldsdicheader = {
    'version': {'key': '{version:15s}', 'val': 'Version'},
    'objectType': {'key': '{objectType:25s}', 'val': 'Object'},
    'size': {'key': '{size:10s}', 'val': 'Size(Bytes)'},
    'insertionTime': {'key': '{insertionTime:30s}', 'val': 'Insertion Time'}
}


def parse_params(par=None):
    cdic = {}
    if par is not None:
        pstringarr = par.split(',')
        for el in pstringarr:
            (k, v) = el.split('=')
            cdic[k] = f'{v}'
    return cdic


def parse_cuts_arr(cut=None):
    import re
    rr = r"""
        ([<|>|:]+)  # match one of the symbols
    """
    rr = re.compile(rr, re.VERBOSE)
    cdic = defaultdict(list)
    if cut is not None:
        ccut = cut.replace('=', ':')
        cutstringarr = ccut.split(',')
        for el in cutstringarr:
            ss = rr.findall(el)
            if len(ss) > 0:
                (k, v) = el.split(ss[0])
                cdic[k].append(f'{ss[0]}{v}')
            else:
                print(f'WARNING in parse_cuts_arr: split symbol not found, should be one in \n{rr}')
    return cdic


def crest_print(crestdata, format=[]):
    if crestdata is None or 'size' not in crestdata.keys():
        print('Cannot find results to print')
        return
    size = crestdata['size']
    dataarr = []
    print(f'Found data list of size {size}')
    if 'resources' in crestdata:
        print(f'Retrieved {size} lines')
        dataarr = crestdata['resources']

    if (crestdata['format'] == 'TagSetDto'):
        if 'short' in format:
            format = shortheaddic['tags']
        # dprint(format, tagfieldsdicheader, tagfieldsdic, dataarr)
        prettyprint(format, tagfieldsdicheader, dataarr)
    elif (crestdata['format'] == 'GlobalTagSetDto'):
        # dprint(format, gtagfieldsdicheader, gtagfieldsdic, dataarr)
        if 'short' in format:
            format = shortheaddic['globaltags']
        prettyprint(format, gtagfieldsdicheader, dataarr)
    elif (crestdata['format'] == 'IovSetDto'):
        if crestdata['datatype'] == 'count':
            size = crestdata['size']
            prettyprint(['size'], None, [size])
        else:
            # dprint(format, iovfieldsdicheader, iovfieldsdic, dataarr)
            prettyprint(format, iovfieldsdicheader, dataarr)
    elif (crestdata['format'] == 'IovPayloadSetDto'):
        # dprint(format, iovpyldfieldsdicheader, iovpyldfieldsdic, dataarr)
        prettyprint(format, iovpyldfieldsdicheader, dataarr)
    elif (crestdata['format'] == 'GlobalTagMapSetDto'):
        # dprint(format, mapfieldsdicheader, mapfieldsdic, dataarr)
        prettyprint(format, mapfieldsdicheader, dataarr)
    elif (crestdata['format'] == 'PayloadSetDto' and crestdata['datatype'] == 'JSON'):
        # dprint(format, pyldfieldsdicheader, pyldfieldsdic, dataarr)
        prettyprint(format, pyldfieldsdicheader, dataarr)
    elif (crestdata['format'] == 'PayloadTagInfoSetDto'):
        # dprint(format, pyldfieldsdicheader, pyldfieldsdic, dataarr)
        datamon = []
        for d in dataarr:
            nd = d
            nd['totvolume'] = round((d['totvolume'] / 1000.), 3)
            nd['avgvolume'] = round((d['avgvolume'] / 1000.), 3)
            datamon.append(nd)
        print(f'Volume are expressed in KB')
        prettyprint(format, None, datamon)
    else:
        print(crestdata)


def dprint(format, headerdic, datadic, cdata):
    if len(format) == 0:
        format = datadic.keys()
    headerfmtstr = ' '.join([headerdic[k]['key'] for k in format])
    headic = {}
    for k in format:
        headic[k] = headerdic[k]['val']
    print(headerfmtstr.format(**headic))
    # print('Use format %s' % format)
    fmtstr = ' '.join([datadic[k] for k in format])
    # print('Format string %s'%fmtstr)
    for xt in cdata:
        adic = {}
        for k in format:
            if xt[k] is None:
                xt[k] = ' - '
            adic[k] = xt[k]
        # print('Use dictionary %s'%adic)
        print(fmtstr.format(**adic))


def prettyprint(format, headerdic, cdata, mw=190):
    try:
        x = BeautifulTable(maxwidth=mw)
        x.set_style(BeautifulTable.STYLE_MARKDOWN)
        fmt = format
        if len(format) == 0:
            fmt = headerdic.keys()
        colheadfmt = []
        for a in fmt:
            colheadfmt.append(colored(a, 'red'))
        x.columns.header = colheadfmt
        if cdata is None or len(cdata) == 0:
            nx = BeautifulTable(maxwidth=mw)
            nx.rows.append([i for i in colheadfmt])
            print(nx)
        else:
            for xt in cdata:
                line = []
                for k in fmt:
                    line.append(xt[k])
                x.rows.append(line)
            x.columns.alignment = BeautifulTable.ALIGN_RIGHT
            print(x)
    except Exception as e:
        print(f'Cannot print using format {format}')
#
# def prettyprint(format, headerdic, cdata):
#     x = PrettyTable()
#     fmt = format
#     if len(format) == 0:
#         fmt = headerdic.keys()
#
#     x.field_names = fmt
#     if cdata is not None:
#         for xt in cdata:
#             line = []
#             for k in x.field_names:
#                 line.append(xt[k])
#             x.add_row(line)
#     x.align = 'l'
#     print(x)


def print_help(data_type):
    print(f'Fields for {data_type} are: ')
    if (data_type == 'tags'):
        prettyprint(format=[], headerdic=tagfieldsdicheader, cdata=[])
    elif (data_type == 'globaltags'):
        prettyprint(format=[], headerdic=gtagfieldsdicheader, cdata=[])
    elif (data_type == 'iovs'):
        prettyprint(format=[], headerdic=iovfieldsdicheader, cdata=[])
    elif (data_type == 'iovpayloads'):
        prettyprint(format=[], headerdic=iovpyldfieldsdicheader, cdata=[])
    elif (data_type == 'maps'):
        prettyprint(format=[], headerdic=mapfieldsdicheader, cdata=[])
    elif (data_type == 'payloads'):
        prettyprint(format=[], headerdic=pyldfieldsdicheader, cdata=[])
    else:
        print('Cannot find help for this kind of data_type')
