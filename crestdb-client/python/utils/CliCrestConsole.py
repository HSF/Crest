'''
Created on Nov 24, 2017

@author: formica
'''

import cmd

import sys,os
import readline
import logging
import atexit
import argparse

from pip._vendor.pyparsing import empty

log = logging.getLogger( __name__ )
log.setLevel( logging.INFO )

handler = logging.StreamHandler()
format = "%(levelname)s:%(name)s: %(message)s"
handler.setFormatter( logging.Formatter( format ) )
log.addHandler( handler )

sys.path.append(os.path.join(sys.path[0],'..'))
historyFile = '.crestconsole_hist'

from ConditionsManagementApi import CrestConsole

class CrestConsoleUI(cmd.Cmd):
    """Simple command processor example."""
    cm = CrestConsole()
    prompt ='(Crest): '
    homehist = os.getenv('CDMS_HISTORY_HOME', os.environ["HOME"])
    histfile = os.path.join( homehist, historyFile)

    def init_history(self, histfile):
        readline.parse_and_bind( "tab: complete" )
        readline.set_history_length( 100 )
        if hasattr( readline, "read_history_file" ):
            try:
                readline.read_history_file( histfile )
            except IOError:
                pass
            atexit.register( self.save_history, histfile )

    def setHost(self,url):
        self.cm.connect(url)

    def save_history(self, histfile):
        print ('Saving history in %s' % histfile)
        readline.write_history_file( histfile )

    def do_connect(self,url):
        """connect [url]
        Use the url for server connections"""
        out = self.cm.connect(url)
        print(out)
        
    def do_ls(self, pattern):
        """ls [tag name pattern]
        Search for tags which contain the input pattern"""
        if pattern:
            print ("Searching %s " % pattern)
        else:
            print ('Search all tags')
        out = self.cm.lstags(pattern)
        print (out)

    def do_lsgtags(self, pattern):
        """lsgtags [tag name pattern]
        Search for global tags which contain the input pattern"""
        if pattern:
            print ("Searching %s " % pattern)
        else:
            print ('Search all global tags')
        out = self.cm.lsgtags(pattern)
        print (out)

    def do_usetag(self, tagname):
        """usetag [tag name]
        Select the tag for successive browsing input (less, tail etc...)"""
        if tagname:
            print ("Selecting %s " % tagname)
        else:
            print ('Cannot select Null tag name, please insert a tag name')
            return
        self.cm.usetag(tagname)
        print ('Tag has been selected')

    def do_usechan(self, chanid):
        """usechan [chanid]
        Select an individual channel for successive browsing input (dump hash etc...).
        Use 'all' to select all channels."""
        if chanid:
            print ("Selecting %s " % chanid)
        else:
            print ('Cannot select Null channel id, please insert one')
            return
        self.cm.usechan(chanid)
        print ('Channel has been selected')

    def do_taginfo(self, line):
        """taginfo
        Select only metadata about the tag. It supposes that a tag has been selected."""
        out = self.cm.listinfo()
        print (out)

    def do_savetag(self,line):
        """savetag [name:xxx,timeType:{time|run-lumi}, objectType:none, synchronization:all, description:a new tag, lastValidatedTime:0, endOfValidity:0]
        Save the tag specified by the argument line. A part from name param, all others are optional."""
        out = self.cm.savetag(line)
        print (out)

    def do_insertiov(self,line):
        """insertiov [name:the tag name,since:the since time, data: filename with data, version:none]
        Save the file specified by the argument line using the given tag and since time. A part from name,since and data parameters, all others are optional."""
        out = self.cm.insertiov(line)
        print (out)

    def do_insertiovpyld(self,line):
        """insertiov [name:the tag name,since:the since time, data: filename with data]
        Save the file specified by the argument line using the given tag and since time. A part from name,since and data parameters, all others are optional."""
        out = self.cm.insertiovpyld(line)
        print (out)

    def do_trace(self, globaltag):
        """trace [globaltag name]
        Search for global tags associations."""
        if globaltag:
            print ("Searching %s " % globaltag)
        else:
            print ('Please select a global tag name...')
            return
        out = self.cm.tracetags(globaltag)
        print (out)

    def do_less(self, line, page=0, size=20):
        """less [page] [size]
        List the iovs in the selected tag. The input parameters are for pagination."""
        dargs=[]
        if line:
            dargs = line.split()
        if len(dargs) > 0:
            page=dargs[0]
        if len(dargs) > 1:
            size=dargs[1]

        out = self.cm.lsiovs(page,size)
        print (out)
        print ('number of selected iovs is %s' % (len(out)-1))

    def do_tail(self, niovs):
        """tail [niovs]
        List the last niovs in the selected tag. The ordering is by since time DESC."""
        if not niovs:
            niovs=20
        print ('Listing %s' % niovs)
        out = self.cm.tailiovs(niovs)
        print (out)
        print ('number of selected iovs is %s' % (len(out)-1))

    def do_select(self, line):
        """select [optional: since=xxx] [optional: until=xxx] [optional: iovbase={time|runlumi}]
        Select iovs in the given range for the selected tag. The optional arguments can be used to limit to the given range.
        The intervals can be given as time in nanoseconds or run,lb. If you use a range you should also specify the iovbase."""
        isrunlumi = False
        timearg = ''
        dargs=[]
        if line:
            dargs = line.split()
        argdic = { 'since': '0', 'until' : 'INF', 'iovbase' : 'time'}
        if len(dargs) > 1:
            for nar in dargs:
                oa=nar.split('=')
                argdic[oa[0]] = oa[1]
        since=argdic['since']
        until=argdic['until']
        iovbase=argdic['iovbase']
        print('Using selection: %s %s %s' % (since,until,iovbase))
        if since is not None:
            isrunlumi = ',' in since
            if isrunlumi:
                if iovbase != 'runlumi':
                    print('Warning: You seem to have used run-lumi but specified iovbase as time...')
                runlb = since.split(',')
                timearg = ' '.join(runlb)
            else:
                if iovbase == 'runlumi':
                    print('Warning: You seem to have used times but specified iovbase as runlumi...')
                timearg = '%s' % since
            print ('use since %s '% (timearg))

        if until is not None:
            if isrunlumi:
                runlbuntil = until.split(',')
                timearg = '%s %s' % (timearg, ' '.join(runlbuntil))
            else:
                timearg = '%s %s ' % (timearg, until)

        if isrunlumi:
            print('Select run lumi range: %s' % timearg)
            self.cm.userunlumi(timearg)
        else:
            print('Select time range: %s' % timearg)
            self.cm.usetimes(timearg)

        out = self.cm.selectiovs()
        print(out)

    def do_dump(self, line=None, column='all'):
        """dump [hash] [optional: column=xxx:yyy] [optional: filename=xxx] [optional: format={json}]
        Dump the payload corresponding to hash.
        If column is provided, a single column can be dumped, by using the format specified after the ":", as it is in the specifications.
        If filename is provided, the payload will be stored into a file. The column selection is ignored. To generate a default filename use 'default'.
        If format is provided, the file can be dumped using that format. This option is taking only json for the moment."""
        out = None
        dargs = line.split()
        argdic = { 'filename': None, 'column' : column, 'format' : None}
        hashs = dargs[0]
        if len(dargs) > 1:
            nextargs=dargs[1:]
            print ('Parse other options: %s ' % nextargs)
            try:
                for nar in nextargs:
                    oa=nar.split('=')
                    argdic[oa[0]] = oa[1]
            except Exception as e:
                print('Error in parsing options...')
        try:
            if argdic['filename']:
                if not argdic['format']:
                    fmt='json'
                print ('Use dump with arguments: %s %s %s' % (hashs,argdic['filename'],argdic['format']))
                out = self.cm.dumppayloadtofile(hashs,argdic['filename'],argdic['format'])
            else:
                column=argdic['column']
                print ('Use dump with arguments: %s %s' % (hashs,column))
                out = self.cm.dumppayload(hashs, column)
            print (out)
            print ('Payload output %s' % (len(out)-1))
        except Exception as e:
            print (e)

    def do_get(self,mhash):
        """get [hash]
        Dump information on payload metadata. This method does not dump the full payload."""
        out = self.cm.getpayload(mhash)
        print (out)

    def do_listchans(self,line):
        """listchans
        Print channels from the last payload selected."""
        out = self.cm.listchans()
        print(out)

    def do_pwd(self,line):
        """pwd
        Print selected tag."""
        out = self.cm.pwd()
        print(out)

    def do_pws(self,line):
        """pws
        Print selected channels and tag."""
        out = self.cm.pws()
        print(out)

    def do_reset(self,line):
        """reset
        Reset selections on channel and time ranges if any."""
        out = self.cm.reset()
        print ('Reset channel and time selections...')

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
    host = "{0}://{1}:{2}/{3}".format(prot,args.host,args.port,args.api)
    log.info('The host is set to %s' % host)
    os.environ['CDMS_HOST']=host
    ui = CrestConsoleUI()
    log.info('Start application')
    ui.setHost(host)
    if args.socks:
        log.info("Activating socks on localhost:3129 ; if you want another address please set CDMS_SOCKS_HOST and _PORT env vars")
        ui.socks()

    ui.cmdloop()
