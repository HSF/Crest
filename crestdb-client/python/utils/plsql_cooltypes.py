import cx_Oracle
import json
import getopt,sys,os
import time

sys.path.append(os.path.join(sys.path[0],'..'))

from utils import cdms_profile

from xml.dom import minidom
#from clint.textui import colored
from datetime import datetime

sys.path.append(os.path.join(sys.path[0],'..'))

from pprint import pprint

#

def getNodeInfo(cur,schema,db,fld):
    sqlpq='select SCHEMA_NAME,NODE_DESCRIPTION,FOLDER_PAYLOADSPEC from table(cool_select_pkg.f_getall_nodes(:schema,:db,:fld)) order by SCHEMA_NAME'
    cur.prepare(sqlpq)
    cur.execute(None, {'schema': schema, 'db' : db, 'fld' : fld})
    res = cur.fetchall()
    return res

def printascii(schemasum,columns):
    header=''
    for t in columns:
        header = '%s\t%10s' % (header,t)
    print(' schema_name\t\t%s\tTotal' % header)
    totbytype = {}
    totbyschema = {}
    for aschema, sumval in schemasum.items():
        row = ''
        totfields = 0
        for t in columns:
            if t not in sumval:
                sumval[t] = 0
            if t not in totbytype:
                totbytype[t] = 0
            totbytype[t] = totbytype[t]+sumval[t]
            totfields = totfields + sumval[t]
            row = '%s\t%10s' % (row,sumval[t])
        totbyschema[aschema] = totfields
        print('%s\t%s\t%s' % (aschema,row,totfields))
    totrow=''
    for t in columns:
        totrow = '%s\t%10s' % (totrow,totbytype[t])
    totschema=0
    for k,v in totbyschema.items():
        totschema = totschema+v
    print('TotalByType\t%s\t%s' % (totrow,totschema))
    
def getTypes(summary,folderspec):
    coolcol = folderspec.split(',')
    for col in coolcol:
        coltype = col.split(':')
        ctype = coltype[1][:3]
        if ctype not in summary:
            summary[ctype] = 1
        else:
            summary[ctype] = summary[ctype] + 1
    return summary

def procopts(opts,args):
    "Process the command line parameters"
    global url
    global schema
    global db
    global folder
    global jsondump
    global tracedump
    for o,a in opts:
        print ('Analyse options %s %s' % (o, a))
        if (o=='--socks'):
            useSocks=True
        if (o=='--url'):
            url=a
        if (o=='--schema'):
            schema=a
        if (o=='--db'):
            db=a
        if (o=='--folder'):
            folder=a
        if (o=='--jsondump'):
            jsondump=True
        if (o=='--tracedump'):
            tracedump=True

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
schema='ATLAS_COOL%'
db='CONDBR2'
folder='%'
tracedump=False
schema='ATLAS_COOL%'
dburl='ATLAS_COND_TOOLS_R/{0}@atlr-s.cern.ch:10121/atlr.cern.ch'.format(os.getenv('COND_TOOLS_PWD'))
#dburl='ATLAS_COND_TOOLS_R/{0}@localhost:10121/atlr.cern.ch'.format(os.getenv('COND_TOOLS_PWD'))

url=dburl
jsondump=True

if __name__ == '__main__':
    try:
        _command = sys.argv[0]
        
        longopts=['socks','url=','schema=','db=','folder=','jsondump','tracedump']        
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
        
        print ('rows in cursor are...%s' % cur)
        cursor = getNodeInfo(cur, schema, db, folder)
        schema_sum = {}
        columns = []
        for row in cursor:
            print (row)
            if row[0] not in schema_sum:
                schema_sum[row[0]] = {}
            print ('Before update...%s: %s' % (row[0],schema_sum[row[0]]))
            schema_sum[row[0]] = getTypes(schema_sum[row[0]],row[2])
            for akey, aval in schema_sum[row[0]].items():
                print ('Looping over schema summary items: %s' % akey)
                if akey not in columns:
                    columns.append(akey)
            print ('After update...%s: %s' % (row[0],schema_sum[row[0]]))
        
        print ('Summary for schema based types occurrences: %s' % schema_sum)
        if jsondump:
            fname='summarydata.json'
            with open(fname, 'wr') as outfile:
                json.dump(schema_sum, outfile)

        printascii(schema_sum,columns)
        
        cdms_profile.print_prof_data()
        cur.close()
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
