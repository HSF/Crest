'''
Created on Nov 24, 2017

@author: formica
'''

import sys,os
import logging
import atexit

gtagfieldsdic = {
    'name' : '{name:25s}',
    'release' : '{release:20s}',
    'workflow' : '{workflow:20s}',
    'scenario' : '{scenario:20s}',
    'validity' : '{validity:10d}',
    'description' : '{description:50s}',
    'snapshotTime' : '{snapshotTime:30s}',
}
gtagfieldsdicheader = {
    'name' : {'key':'{name:25s}','val' : 'GlobalTag'},
    'release' : {'key':'{release:20s}','val' : 'Release'},
    'workflow' :  {'key':'{workflow:20s}','val' : 'Workflow'},
    'scenario' : {'key':'{scenario:20s}','val' : 'Scenario'},
    'validity' :  {'key':'{validity:10s}','val' : 'Validity'},
    'description' : {'key':'{description:50s}','val' : 'Description'},
    'snapshotTime' : {'key':'{snapshotTime:30s}', 'val' : 'Snapshot Time'}
}
iovfieldsdic = {
    'since' : '{since:15d}',
    'payloadHash' : '{payloadHash:65s}',
    'insertionTime' : '{insertionTime:30s}',
}
iovfieldsdicheader = {
    'since' : {'key':'{since:15s}','val' : 'since'},
    'payloadHash' : {'key':'{payloadHash:65s}','val' : 'Hash'},
    'insertionTime' : {'key':'{insertionTime:30s}', 'val' : 'Insertion Time'}
}
tagfieldsdic = {
    'name' : '{name:25s}',
    'timeType' : '{timeType:10s}',
    'payloadSpec' : '{payloadSpec:10s}',
    'synchronization' : '{synchronization:10s}',
    'lastValidatedTime' : '{lastValidatedTime:10d}',
    'endOfValidity' : '{endOfValidity:20d}',
    'description' : '{description:50s}',
    'insertionTime' : '{insertionTime:30s}',
}
tagfieldsdicheader = {
    'name' : {'key':'{name:25s}','val' : 'Tag'},
    'timeType' : {'key':'{timeType:10s}','val' : 'Type'},
    'payloadSpec' :  {'key':'{payloadSpec:10s}','val' : 'Payload'},
    'synchronization' : {'key':'{synchronization:10s}','val' : 'Synchro'},
    'lastValidatedTime' :  {'key':'{lastValidatedTime:10s}','val' : 'Last-Valid'},
    'endOfValidity' :  {'key':'{endOfValidity:20s}','val' : 'End-Valid'},
    'description' : {'key':'{description:50s}','val' : 'Description'},
    'insertionTime' : {'key':'{insertionTime:30s}', 'val' : 'Insertion Time'}
}
mapfieldsdic = {
    'record' : '{record:30.30s}',
    'label' : '{label:40.40s}',
    'globalTagName' : '{globalTagName:30.30s}',
    'tagName' : '{tagName:50s}',
}
mapfieldsdicheader = {
    'record' : {'key':'{record:30.30s}', 'val' : 'Record'},
    'label' : {'key':'{label:40.40s}', 'val' : 'Label'},
    'globalTagName' : {'key':'{globalTagName:30.30s}','val' : 'GlobalTag'},
    'tagName' : {'key':'{tagName:50s}','val' : 'Tag'},
}
pyldfieldsdic = {
    'version' : '{version:15s}',
    'objectType' : '{objectType:25s}',
    'size' : '{size:10d}',
    'insertionTime' : '{insertionTime:30s}',
}
pyldfieldsdicheader = {
    'version' : {'key':'{version:15s}','val' : 'Version'},
    'objectType' : {'key':'{objectType:25s}','val' : 'Object'},
    'size' : {'key':'{size:10s}','val' : 'Size(Bytes)'},
    'insertionTime' : {'key':'{insertionTime:30s}', 'val' : 'Insertion Time'}
}
def crest_print(crestdata, format=[]):
    if crestdata is None or 'size' not in crestdata.keys():
        log.info('Cannot find results to print')
        return
    size=crestdata['size']
    dataarr = []
    if 'resources' in crestdata:
        print(f'Retrieved {size} lines')
        dataarr = crestdata['resources']
    if (crestdata['format'] == 'TagSetDto'):
        dprint(format,tagfieldsdicheader,tagfieldsdic,dataarr)

    elif (crestdata['format'] == 'GlobalTagSetDto'):
        dprint(format,gtagfieldsdicheader,gtagfieldsdic,dataarr)

    elif (crestdata['format'] == 'IovSetDto'):
        if crestdata['datatype'] == 'count':
            size = crestdata['size']
            print(f'Found number of iovs: {size}')
        else:
            dprint(format,iovfieldsdicheader,iovfieldsdic,dataarr)

    elif (crestdata['format'] == 'GlobalTagMapSetDto'):
        dprint(format,mapfieldsdicheader,mapfieldsdic,dataarr)
    elif (crestdata['format'] == 'PayloadSetDto' and crestdata['datatype'] == 'JSON'):
        dprint(format,pyldfieldsdicheader,pyldfieldsdic,dataarr)
    else:
        print(crestdata)

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
