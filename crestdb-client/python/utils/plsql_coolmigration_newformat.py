import cx_Oracle
import json
import getopt,sys,os
import time

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

def select_columns(col):
    if col not in ['IOV_SINCE','TAG_NAME','SYS_INSTIME','TAG_DESCRIPTION','IOV_BASE','NODE_ID','ORIGINAL_ID','LASTMOD_DATE','LAST_OBJECT_ID','TAG_ID','TAG_LOCK_STATUS','HAS_NEW_DATA','DESCRIPTION','IOV_UNTIL','NEW_HEAD_ID','OBJECT_ID','USER_TAG_ID','PAYLOAD_ID','P_SYS_INSTIME']:
        return True

def getPayloads(cur,schema,db,fld,tag,stime,etime,chans):
    print ('Query payload using arguments: %s %s %s %s %s %s' % (schema,db,fld,tag,stime,chans))
    sqlpq='select ATLAS_COND_TOOLS.COOL_SELECT_PKG.f_Get_PayloadIov(:schema,:db,:fld,:tag,:st,:chan) from dual'
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'fld' : fld, 'tag' : tag, 'st' : stime, 'chan' : chans})
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
    if tag is None:
        sqlpq='select distinct IOV_SINCE from table(cool_select_pkg.f_Get_SvIovs(:schema,:db,:fld)) order by IOV_SINCE asc'
        print ('ListIovs SV: %s %s %s %s' % (sqlpq,schema,db,fld))
        cur.prepare(sqlpq)
        cur.execute(None, {'schema': schema, 'db' : db, 'fld' : fld })
    else:
        print ('ListIovs: %s %s %s %s %s' % (sqlpq,schema,db,fld,tag))
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
        if (o=='--tag'):
            tag=a
            if a == 'None':
                tag=None
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
#schema='ATLAS_COOLOFL_TILE'
db='CONDBR2'
#folder='/TILE/OFL02/CALIB/CIS/LIN'
#tag='TileOfl02CalibCisLin-RUN2-UPD4-13'
schema='ATLAS_COOLONL_LAR'
folder='/LAR/Configuration/FEBConfig/Calibration/EMBC2'
tag=None

schema="ATLAS_COOLONL_LAR"
folder="/LAR/Configuration/FEBConfig/Calibration/EMECA3"
tag=None
folder="/LAR/Configuration/FEBConfig/Calibration/EMECA1"

gtag=None
tracedump=False
domap=False
#schema='ATLAS_COOL%'
urlsvc='http://conddb02.cern.ch:8080/phycdb/v1/rest'
urlsvc = os.getenv('CDMS_HOST', 'http://conddb02.cern.ch:8080/phycdb/v1/rest')
dburl='ATLAS_COND_TOOLS_R/{0}@atlr-s.cern.ch:10121/atlr.cern.ch'.format(os.getenv('COND_TOOLS_PWD'))
#dburl='ATLAS_COND_TOOLS_R/{0}@localhost:10121/atlr.cern.ch'.format(os.getenv('COND_TOOLS_PWD'))

url=dburl
jsondump=True

if __name__ == '__main__':
    try:
        _command = sys.argv[0]

        longopts=['socks','start=','end=','url=','schema=','db=','folder=','tag=','gtag=','jsondump','tracedump','domap']
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

        if gtag is not None and '%' not in schema:
            print ('Resolve the tag via global tag %s' % gtag)
            tag=resolveTag(cur,schema,db,folder,gtag)
        elif gtag is not None and '%' in schema:
            print ('If tracedump is active, just dump a file with the folder / gtag associations')
            if tracedump:
                resolveSchemaFolders(cur,schema,db,gtag);
                sys.exit(-1)
            if domap:
                ltags = resolveSchemaFolders(cur,schema,db,gtag);
                for arow in ltags:
                    print ('Search for tag %s' % arow['tag'])
                    ftlist = cmapi.listTags(arow['tag'])
                    print ('retrieved tag list %s ' % ftlist)
                    for atag in ftlist:
                        if 'SHAUNROE' in atag.name:
                            # this is a tag existing...associate with gtag
                            gt = cmapi.getGlobalTag(gtag)
                            if gt is None:
                                cmapi.createGlobalTag(gtag)
                            cmapi.createGlobalTagMap(gtag, atag.name,label=arow['node'],record=arow['schema'])
                sys.exit(-1)
            print ('Resolve the schema and tag via global tag %s' % gtag)
            (schema,tag)=resolveSchemaTag(cur,schema,db,folder,gtag)
            if schema is None:
                print ('This is a single version probably...you should handle it in a different way')
                raise Exception('Single version folder....')
            print ('Found schema %s and tag %s' % (schema,tag))

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
            res = listIovsFrom(cur, schema, db, folder, tag, sincetime, untiltime)
        else:
            res = listIovs(cur,schema,db,folder,tag)
        print ('Length of retrieved list of iovs from COOL %s' % len(res))

        for row in res:
            stime=row[0]
            if stime not in existingiovslist:
                iovlist.append(stime)
            else:
                existingiovslist.remove(stime)
        print ('length of iov list is: %s'%len(iovlist))
        time.sleep(3)

        iiov=0
        #tagname=None
        coolpayload = ConditionsCoolPayload()
        iovbase,coolpayload = getNodeInfo(cur,schema,db,folder,coolpayload)

        for stime in iovlist:
            coolpayload.clean()
            if iiov%1000 == 0:
                cdms_profile.print_prof_data()
            iiov=iiov+1
            print ('Load data for time : %s' % stime)
            coolpayload.stime = stime

            sincetime=stime
            ## the until time is ignored in reality
            untiltime=stime+10

            if folder == '/TRIGGER/LUMI/PerBcidDeadtime':
                tag='HEAD'
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

            cmapi.storeObject(tagname,coolpayload.stime,cdblob,version='1',object_type='SHAUNROE_JSON',insertion_time=instime,streamer_info=strinfo)

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
