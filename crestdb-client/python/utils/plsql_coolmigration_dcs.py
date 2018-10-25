import cx_Oracle
import json
import getopt,sys,os
import time
import numbers
sys.path.append(os.path.join(sys.path[0],'..'))

from utils import cdms_profile
from cool.JsonCoolPayload import ConditionsCoolPayload
from utils.ConditionsManagementApi import CMApi

from xml.dom import minidom
#from clint.textui import colored
from datetime import datetime

sys.path.append(os.path.join(sys.path[0],'..'))

from crestapi.apis import GlobaltagsApi, TagsApi, IovsApi, PayloadsApi
from crestapi.models import GlobalTagDto, TagDto, GlobalTagMapDto, IovDto
from crestapi.apis import RuninfoApi
from crestapi.models import RunLumiInfoDto
from crestapi import ApiClient
from crestapi.rest import ApiException
from pprint import pprint
from copy import copy


cutselection = [
        {'/MDT/DCS/HV' : {'iMon_ML1' : 0.5, 'v0set_ML1' : 10, 'v1set_ML1' : 10, 'chanErrorFlag_ML1' : 0, 'IOffset_ML1' : 0, 'IScale_ML1' : 0.5, 'iMon_ML2' : 0.5, 'v0set_ML2' : 10, 'v1set_ML2' : 10, 'chanErrorFlag_ML2' : 0, 'IOffset_ML2' : 0, 'IScale_ML2' : 0.5}},   
        {'/TILE/DCS/HV' : {'HVOU' : 0.5, 'TEMP' : 1.0}}   
    ]
    

def select_columns(col):
    if col not in ['IOV_SINCE','TAG_NAME','SYS_INSTIME','TAG_DESCRIPTION','IOV_BASE','NODE_ID','ORIGINAL_ID','LASTMOD_DATE','LAST_OBJECT_ID','TAG_ID','TAG_LOCK_STATUS','HAS_NEW_DATA','DESCRIPTION','IOV_UNTIL','NEW_HEAD_ID','OBJECT_ID','USER_TAG_ID','PAYLOAD_ID','P_SYS_INSTIME']:
        return True

def getPayloads(cur,schema,db,fld,tag,stime,etime,chans):
    print ('Query payload using arguments: %s %s %s %s %s %s' % (schema,db,fld,tag,stime,chans))
    sqlpq='select ATLAS_COND_TOOLS.COOL_SELECT_PKG.f_Get_PayloadIovs(:schema,:db,:fld,:tag,:st,:et,:chan) from dual'
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'fld' : fld, 'tag' : tag, 'st' : stime, 'et' : etime, 'chan' : chans})
    res = cur.fetchall()
    return res

def getPayloadsAtIov(cur,schema,db,fld,tag,stime,chans):
    print ('Query payload using arguments: %s %s %s %s %s %s' % (schema,db,fld,tag,stime,chans))
    sqlpq='select ATLAS_COND_TOOLS.COOL_SELECT_PKG.f_Get_PayloadAtIov(:schema,:db,:fld,:tag,:st,:chan) from dual'
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'fld' : fld, 'tag' : tag, 'st' : stime, 'chan' : chans})
    res = cur.fetchall()
    return res

#
#ORA-20001: Error in schema ATLAS_COOLONL_TDAQ and statment  select  iovs.*,  channels.*,  NULL as TAG_NAME, 'time' as IOV_BASE  from ATLAS_COOLONL_TDAQ.CONDBR2_F0004_IOVS iovs left join ATLAS_COOLONL_TDAQ.CONDBR2_F0004_CHANNELS channels on iovs.channel_id=channels.channel_id  where  :tagname is null AND  (iovs.new_head_id=0) AND (:ptime BETWEEN iovs.iov_since and iovs.iov_until AND iovs.iov_until > :ptime)
#ORA-06512: at "ATLAS_COND_TOOLS.COOL_SELECT_PKG", line 2837
#ORA-06512: at line 17
#
#

def getNodeInfo(cur,schema,db,fld,condcoolpyld):
    sqlpq='select NODE_DESCRIPTION,FOLDER_PAYLOADSPEC,IOV_BASE from table(cool_select_pkg.f_getall_nodes(:schema,:db,:fld))'
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'fld' : fld})
    res = cur.fetchone()
    condcoolpyld.node_description=res[0]
    folderspec = res[1]
    print ('getNodeInfo has found folder specification %s' % folderspec)
    for col in res[1].split(','):
        (colname,cooltype) = col.split(':')
        condcoolpyld.extend(colname,cooltype)

    iovbase=res[2]
    return (iovbase,condcoolpyld)

def dict_to_binary(the_dict):
    str = json.dumps(the_dict)
    binary = str.encode('base64')
#    binary = ' '.join(format(ord(letter), 'b') for letter in str)
    return binary

def binary_to_dict(the_bin):
    jsonstr = the_bin.decode('base64')
    the_dict = json.loads(jsonstr)
    return the_dict

@cdms_profile.profile
def rows_to_dict_list(cursor, sincetime, condcoolpyld):
    print ('Creating dictionary from cursor')
    columns = [i[0] for i in cursor.description]
    #print 'Columns are ',columns
    adictarr=[]
    headdict={}
    first=True
    modchannels=[]
    iovbase = None
    print ('rows in cursor are...%s' % cursor)
    for row in cursor:
        if first:
            print ('Retrieved first row...')
        row_dict={}
        row_dict['DATA'] = []
        # Build the ROW object dictionary that is then used for the data....
        for i, col in enumerate(columns):
           if first and col == 'TAG_NAME':
              condcoolpyld.tag_name = row[i]
           if first and col == 'IOV_BASE':
              iovbase = row[i]

           if col == 'IOV_SINCE':
              row_dict['IOV_SINCE'] = row[i]
           
           if select_columns(col):
              #print ('Appending data for column: %s values %s' % (col,row[i]))
              if col not in ['CHANNEL_ID','CHANNEL_NAME']:
                  if type(row[i]) == cx_Oracle.LOB :
                     bindata=row[i].read()
                     row_dict['DATA'].append(bindata.encode('base64'))
                     #print 'Read the full blob from oracle...'
                  else:
                      row_dict['DATA'].append(row[i])
              else:
                  row_dict[col] = row[i]
              if first and not col in ['CHANNEL_ID','CHANNEL_NAME']:
                  cooltype = condcoolpyld.gettype(col)
                  if cooltype is None:
                    raise Exception('Cannot add column for unknown type')
              
        adictarr.append(row_dict)
        first=False
#    return [dict(zip(columns, row)) for row in cursor]
    print ('Check modified channels...loop over %s' % len(adictarr))
    for chan_row in adictarr:
        #print 'element in dict arr: ',chan_row
        #channame = chan_row['CHANNEL_NAME']
        chanid =chan_row['CHANNEL_ID']
        chaniovsince = chan_row['IOV_SINCE']
        del chan_row['CHANNEL_NAME']
        del chan_row['CHANNEL_ID']
        del chan_row['IOV_SINCE']

        if sincetime == chaniovsince:
            modchannels.append(chanid)
        condcoolpyld.addRecord(chanid,chaniovsince,chan_row['DATA'])

    condcoolpyld.modified_channels = modchannels
    return (iovbase,condcoolpyld)

def resolveSchemaTag(cur,schema,db,fld,gtag):
    sqlpq='select SCHEMA_NAME,TAG_NAME from table(cool_select_pkg.f_GetAll_TagsForGtag(:schema,:db,:gtag)) where NODE_FULLPATH=:fld'
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'gtag': gtag, 'fld' : fld})
    res = cur.fetchone()
    print (res)
    if res is None:
        return (None,None)
    print ('Found %s %s ' % (res[0],res[1]))
    return (res[0],res[1])

def resolveSchemaFolders(cur,schema,db,gtag):
    sqlpq='select SCHEMA_NAME,TAG_NAME, NODE_FULLPATH from table(cool_select_pkg.f_GetAll_TagsForGtag(:schema,:db,:gtag))'
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'gtag': gtag})
    res = cur.fetchall()
    ltags = []
    for row in res:
        print ('%s %s %s' % (row[0],row[1],row[2]))
        ft = { 'schema' : row[0], 'tag': row[1], 'node' :row[2]}
        ltags.append(ft)
    return ltags

def resolveTag(cur,schema,db,fld,gtag):
    sqlpq='select TAG_NAME from table(cool_select_pkg.f_GetAll_TagsForGtag(:schema,:db,:gtag)) where NODE_FULLPATH=:fld'
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'gtag': gtag, 'fld' : fld})
    res = cur.fetchone()
    print (res[0])
    return res[0]
        
def listIovs(cur,schema,db,fld,tag):
    sqlpq='select distinct IOV_SINCE from table(cool_select_pkg.f_Get_Iovs(:schema,:db,:fld,:tag)) order by IOV_SINCE asc'
    print ('%s %s %s %s %s' % (sqlpq,schema,db,fld,tag))
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'fld' : fld,'tag': tag })
    res = cur.fetchall()
    return res

def listIovsFrom(cur,schema,db,fld,tag,since,until):
    sqlpq='select distinct IOV_SINCE from table(cool_select_pkg.f_Get_IovsRange(:schema,:db,:fld,:tag,:st,:et)) order by IOV_SINCE asc'
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'fld' : fld,'tag': tag, 'st' : since, 'et': until })
    res = cur.fetchall()
    return res

def listIovsDcsFrom(cur,schema,db,fld,tag,since,until):
    sqlpq='select  min(IOV_SINCE) from table(cool_select_pkg.f_Get_IovsRange(:schema,:db,:fld,:tag,:st,:et)) group by SYS_INSTIME order by min(IOV_SINCE) asc'
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'fld' : fld,'tag': tag, 'st' : since, 'et': until })
    res = cur.fetchall()
    return res

def checkColumn(acdata,acprevdata,maxdiff):
    if acdata is None and acprevdata is None:
        return True
    if acdata is None and acprevdata is not None:
        return False
    if acdata is not None and acprevdata is None:
        return False
    if isinstance(acdata, numbers.Number):
        diff = abs(acdata-acprevdata)
        if diff > maxdiff:
            return False
    if isinstance(acdata,str):
        if acdata != acprevdata:
            return False
    if isinstance(acdata,bool):
        if acdata != acprevdata:
            return False
    return True
    
def checkChannel(jb,prevjb,folder):
    c = {}
    for k in jb.keys():
        c[k] = tuple(d[k] for d in [jb,prevjb])
    
    cutsarr = [ x[folder] for x in cutselection if folder in x ]
    cuts = cutsarr[0]
    for k,v in c.items(): 
        ccol = k 
        if '/TILE/DCS/HV' == folder:
            ccol = k[0:4]
        mcut = None
        if ccol in cuts.keys():
            mcut = cuts[ccol]
        cdiff = checkColumn(v[0], v[1], mcut)
        if not cdiff:
            return False
    return True

def isequal(jb,prevjb,folder):
    if prevjb is not None:
        #print('Here I compare %s with the previous %s' % (jb,prevjb))
        ncprev = prevjb.listChannels()
        for achan in ncprev:
            #print('getting data dictionary for %s' % achan)
            acdata = jb.getChannelData(achan)
            acprevdata = prevjb.getChannelData(achan)
            if acdata != acprevdata:
                iseq = checkChannel(acdata, acprevdata, folder)
                if not iseq:
                    print ('content differ in channel %s : insert this as a separate IOV ! ' % achan)
                    return False
    else:
        print('the previous blob does not exists...return false')
        return False
    
    print('The 2 blobs are equals...do not store the new one')
    return True    
    
def procopts(opts,args):
    "Process the command line parameters"
    global sincetime
    global untiltime
    global url
    global schema
    global db
    global folder
    global tag
    global gtag
    global jsondump
    global tracedump
    global domap
    for o,a in opts:
        print ('Analyse options %s %s' % (o, a))
        if (o=='--socks'):
            useSocks=True
        if (o=='--start'):
            sincetime=int(a)
            if untiltime is None:
                untiltime=int(sincetime+1)
        if (o=='--end'):
            untiltime=int(a)
        if (o=='--url'):
            url=a
        if (o=='--schema'):
            schema=a
        if (o=='--db'):
            db=a
        if (o=='--folder'):
            folder=a
        if (o=='--gtag'):
            gtag=a
        if (o=='--jsondump'):
            jsondump=True
        if (o=='--tracedump'):
            tracedump=True
        if (o=='--domap'):
            domap=True

    print ('Arguments: %s %s %s' % (sincetime,untiltime,url))
    if (len(args)<0):
        raise getopt.GetoptError("Insufficient arguments - need at least 1, or try --help")


def usage():
    print ('This is the help')

#General variables
useSocks = False
#sincetime=int(944214200287232)
#untiltime=int(sincetime+1000)
sincetime=None
untiltime=None
url='ATLAS_COND_TOOLS_R/{0}@ATLR'.format(os.getenv('COND_TOOLS_PWD'))
schema='ATLAS_COOLOFL_DCS'
db='CONDBR2'
folder='/TILE/DCS/HV'
tag='None'
gtag=None
tracedump=False
domap=False
schema='ATLAS_COOL%'
urlsvc='http://conddb02.cern.ch:8080/phycdb/v1/rest'
urlsvc = os.getenv('CDMS_HOST', 'http://conddb02.cern.ch:8080/phycdb/v1/rest')
dburl='ATLAS_COND_TOOLS_R/{0}@atlr-s.cern.ch:10121/atlr.cern.ch'.format(os.getenv('COND_TOOLS_PWD'))
#dburl='ATLAS_COND_TOOLS_R/{0}@localhost:10121/atlr.cern.ch'.format(os.getenv('COND_TOOLS_PWD'))

url=dburl
jsondump=True

if __name__ == '__main__':
    try:
        _command = sys.argv[0]
        
        longopts=['socks','start=','end=','url=','schema=','db=','folder=','gtag=','jsondump','tracedump','domap']        
        opts,args=getopt.getopt(sys.argv[1:],'',longopts)
        print ('Arguments: %s %s' % (opts, args))
        procopts(opts,args)
        
        print ('Connect using %s' % url)
        try:
            con = cx_Oracle.connect(url)
            print ('connection version...%s' % con.version)
        except Exception as e:
            print (e)
        cur = con.cursor()
        
        cmapi = CMApi()
       
        iovlist=[]
        print ('Loading the existing iov list...')
        tagname = tag
        if tagname is None or tagname == 'None':
            print ('Search if iovs are already existing; change tagname to folder name %s but changing slashes...' % folder)
            tmptag = folder.replace("/", "_")
            tagname=tmptag[1:]
        
        tagname = ('%s_SHAUNROE') % tagname
        
        cdmsiovlist=cmapi.selectSnapshot(tagname)
        existingiovslist = [ x.since for x in cdmsiovlist ]
        
        print ('length of existing iov list is %s' % len(existingiovslist))
        
        res = []
        if sincetime is not None:
            print ('Use iov selection in range %s %s' % (sincetime,untiltime))
            #res = listIovsDcsFrom(cur, schema, db, folder, tag, sincetime, untiltime)
            res = listIovsFrom(cur, schema, db, folder, tag, sincetime, untiltime)
        else:
            print('Cannot run DCS loading without RANGE in time')
            sys.exit(-1)
        print ('Length of retrieved list of iovs from COOL %s' % len(res))

        previov=None
        for row in res:
            stime=row[0]
            tiov={}
            if stime not in existingiovslist:
                if previov is None:
                    previov = { 'since': stime, 'until' : None}
                else:
                   tiov = previov
                   tiov['until'] = stime 
                   iovlist.append(tiov)
                   previov = { 'since': stime, 'until' : None}
            else:
                existingiovslist.remove(stime)
        print ('length of iov list is: %s'%len(iovlist))
        time.sleep(3)

        iiov=0
        #tagname=None
        coolpayload = ConditionsCoolPayload()
        iovbase,coolpayload = getNodeInfo(cur,schema,db,folder,coolpayload)
        prevjblob=None
        for eliov in iovlist:
            stime = eliov['since']
            coolpayload.clean()
            #print('We cleaned the cool json payload container....now check the previous %s '%prevjblob)
            if iiov%1000 == 0:
                cdms_profile.print_prof_data()
            iiov=iiov+1
            print ('Load data for time : %s' % stime)
            coolpayload.stime = stime

            sincetime=stime
            ## the until time is ignored in reality
            untiltime=stime+1
            
            res = getPayloads(cur,schema,db,folder,tag,sincetime,untiltime,None)
            for row in res:
                rcur=row[0]

# Now we parse the resultset
            (timetype,coolpayload)=rows_to_dict_list(rcur, sincetime, coolpayload)
            print ('Retrieved time type %s' % timetype)
            print ('For some reason this does not work...so override from getNodeInfo %s' % iovbase)            
            timetype=iovbase
            jblob = coolpayload.pack()
            #print ('Retrieved json blob is : %s ' % jblob)
            if iiov <= 1:
                objtype=coolpayload.folder_payloadspec
                description = coolpayload.node_description
                lastvt=-1
                eov=-1
                if tagname is None or tagname == 'None':
                    print ('Change tagname to folder name %s but changing slashes...' % folder)
                    tmptag = folder.replace("/", "_")
                    tagname=tmptag[1:]
                    tagname = ('%s_SHAUNROE') % tagname
                    coolpayload.tag_name = tagname


                atag = cmapi.getTag(tagname)
                if atag is None:
                    print ('Tag %s does not exists yet...create it...' % tagname)
                    if len(objtype) > 1000:
                        objtype = "%s_TRUNC" % objtype[:990]
                    cmapi.createTag(tagname,time_type=timetype,object_type=objtype,synchronization='SR_COPY_FROM_COOL',description=description,last_validated_time=lastvt,end_of_validity=eov)

            cdblob=json.dumps(jblob)
            sincet=coolpayload.stime
            if sincet != stime:
                print ('Warning in since time %s %s' % (sincet,stime))
                sincet=stime
                
            instime=int(time.time())
            strinfo='SHAUNROE_JSON'
            if not isequal(coolpayload,prevjblob,folder):
                print('The 2 IOV seem to differ....insert the new one')
                cmapi.storeObject(tagname,coolpayload.stime,cdblob,version='1',object_type='SHAUNROE_JSON',insertion_time=instime,streamer_info=strinfo)
                prevjblob=copy(coolpayload)
            
            if prevjblob is None:
                prevjblob=copy(coolpayload)

            if jsondump and False:
                fname='data-{0}.json'.format(stime)
                with open(fname, 'wr') as outfile:
                    json.dump(jblob, outfile)

        cdms_profile.print_prof_data()
        con.close()
    except getopt.GetoptError as e:
        print (e)
        usage()
        sys.exit(-1)
    except Exception as e1:
        print ('Exception occurred: %s' % e1)
        exc_type, exc_obj, exc_tb = sys.exc_info()
        fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
        print(exc_type, fname, exc_tb.tb_lineno)        
        sys.exit(-1)
