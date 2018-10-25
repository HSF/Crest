import cx_Oracle
import json
import getopt,sys,os
import time

sys.path.append(os.path.join(sys.path[0],'..'))

from utils import cdms_profile
from cool.ConditionsCoolPayload import ConditionsCoolPayload
from utils.ConditionsManagementApi import CMApi
from utils.RunLumiManagementApi import RunLumiApi

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
    if col not in ['CHANNEL_ID','CHANNEL_NAME','IOV_SINCE','TAG_NAME','SYS_INSTIME','TAG_DESCRIPTION','IOV_BASE','NODE_ID','ORIGINAL_ID','LASTMOD_DATE','LAST_OBJECT_ID','TAG_ID','TAG_LOCK_STATUS','HAS_NEW_DATA','DESCRIPTION','IOV_UNTIL','NEW_HEAD_ID','OBJECT_ID','USER_TAG_ID','PAYLOAD_ID','P_SYS_INSTIME']:
        return True

def getPayloads(cur,schema,db,fld,tag,stime,etime,chans):
    print ('Query payload using arguments: %s %s %s %s %s %s' % (schema,db,fld,tag,stime,chans))
    sqlpq='select ATLAS_COND_TOOLS.COOL_SELECT_PKG.f_Get_PayloadIov(:schema,:db,:fld,:tag,:st,:chan) from dual'
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
    sqlpq='select NODE_DESCRIPTION,FOLDER_PAYLOADSPEC from table(cool_select_pkg.f_get_nodes(:schema,:db,:fld))'
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'fld' : fld})
    res = cur.fetchone()
    condcoolpyld.node_description=res[0]
    folderspec = res[1]
    print ('getNodeInfo has found folder specification %s' % folderspec)
    for col in res[1].split(','):
        (colname,cooltype) = col.split(':')
        condcoolpyld.extend(colname,cooltype)

    return condcoolpyld

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
def rows_to_dict_list(cursor):
    print ('Creating dictionary from cursor')
    columns = [i[0] for i in cursor.description]
    #print 'Columns are ',columns
    adictarr=[]
    headdict={}
    first=True
    modchannels=[]
#   
    print ('rows in cursor are...%s' % cursor)
    for row in cursor:
        if first:
            print ('Retrieved first row...')
        row_dict={}
        # Build the ROW object dictionary that is then used for the data....
        for i, col in enumerate(columns):
           if col == 'IOV_SINCE':
              row_dict['IOV_SINCE'] = row[i]
           
           if select_columns(col):
              row_dict[col] = row[i]
              #print 'Reading data for column: ',col
              
        adictarr.append(row_dict)
        first=False
#    return [dict(zip(columns, row)) for row in cursor]
    print ('Check modified channels...loop over %s' % len(adictarr))
    return adictarr

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

def resolveTag(cur,schema,db,fld,gtag):
    sqlpq='select TAG_NAME from table(cool_select_pkg.f_GetAll_TagsForGtag(:schema,:db,:gtag)) where NODE_FULLPATH=:fld'
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'gtag': gtag, 'fld' : fld})
    res = cur.fetchone()
    print (res[0])
    return res[0]
        
def listIovs(cur,schema,db,fld,tag):
    sqlpq='select distinct IOV_SINCE from table(cool_select_pkg.f_Get_Iovs(:schema,:db,:fld,:tag)) order by IOV_SINCE asc'
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'fld' : fld,'tag': tag })
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
    for o,a in opts:
        print ('Analyse options %s %s' % (o, a))
        if (o=='--socks'):
            useSocks=True
        if (o=='--start'):
            sincetime=int(a)
            untiltime=int(sincetime+1)
        if (o=='--end'):
            untiltime=a
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
schema='ATLAS_COOLONL_TRIGGER'
db='CONDBR2'
folder='/TRIGGER/LUMI/LBLB'
tag=None
gtag=None
urlsvc = os.getenv('CDMS_HOST', 'http://conddb02.cern.ch:8080/phycdb/v1/rest')
dburl='ATLAS_COND_TOOLS_R/{0}@atlr-s.cern.ch:10121/atlr.cern.ch'.format(os.getenv('COND_TOOLS_PWD'))
#dburl='ATLAS_COND_TOOLS_R/{0}@localhost:10121/atlr.cern.ch'.format(os.getenv('COND_TOOLS_PWD'))

url=dburl
jsondump=True

if __name__ == '__main__':
    try:
        _command = sys.argv[0]
        
        longopts=['socks','start=','end=','url=','schema=','db=','folder=','tag=','gtag=','jsondump']        
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
        
        cmapi = RunLumiApi()
        
        iovlist=[]
        print ('Loading the existing iov list...')
        
        cdmsruninfolist=cmapi.listLastNRunLumiInfo(10)
        existingiovslist = [ x.since for x in cdmsruninfolist ]
        if len(existingiovslist) == 0:
            existingiovslist.append(0)
        print ('length of last entries in run info list is %s' % len(existingiovslist))
        
        res = listIovs(cur,schema,db,folder,tag)
        print ('Length of retrieved list of iovs from COOL %s' % len(res))

        for row in res:
            stime=row[0]
            if stime > existingiovslist[0]:
                iovlist.append(stime)
                
        print ('length of iov list is: %s'%len(iovlist))
        time.sleep(3)

        if sincetime is not None:
            iovlist=[]
            iovlist.append(sincetime)
            print ('iov list overridden by start time option...: %s'% iovlist)

        iiov=0

        for stime in iovlist:
            if iiov%1000 == 0:
                cdms_profile.print_prof_data()
            iiov=iiov+1
            print ('Load data for time : %s' % stime)

            sincetime=stime
            ## the until time is ignored in reality
            untiltime=stime+10
            
            res = getPayloads(cur,schema,db,folder,tag,sincetime,untiltime,None)
            for row in res:
                rcur=row[0]
                
            (run,lumi) = cmapi.stimeToRunlumi(sincetime)
# Now we parse the resultset
            pyldarr = rows_to_dict_list(rcur)
            ap = pyldarr[0]
            print ('Retrieved payload %s ' % ap)
            runlumipayload={ 'since' : sincetime, 'run' : run,'lb': lumi, 'starttime' : ap['StartTime'], 'endtime' : ap['EndTime']}
            print ('Inserting payload %s ' % runlumipayload)

            cmapi.insertRunLumiInfo(since=runlumipayload['since'], run=runlumipayload['run'], lb=runlumipayload['lb'], starttime=runlumipayload['starttime'], endtime=runlumipayload['endtime'])

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
