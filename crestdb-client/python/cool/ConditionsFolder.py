
# python 2 and python 3 compatibility library
import logging

from ConditionsCoolPayload import ConditionsCoolPayload
import CrestCool


class coolfolder(object):
    def __init__(self,folder,tagdto,cmapi):
        if tagdto is not None:
            print 'Initialise folder using tag...',tagdto.name
        self._folderpath = folder
        self._tagdto = tagdto
        self._cmapi = cmapi
        self._obj = None
        self._stime = None
        self._payload = None
        
        logging.basicConfig(format='%(asctime)s %(message)s')
        self._logger = logging.getLogger('CoolFolder')
        self._logger.setLevel(logging.INFO)
        self._logger.debug('End of folder initialization')

    def description(self):
        return self._tagdto.description

    def specifications(self):
        return self._tagdto.object_type

    def tag(self):
        return self._tagdto.name
    
    def timetype(self):
        return self._tagdto.time_type
    
    def resolveTag(self,globaltagname):
        tagname = self._cmapi.resolveTag(globaltagname,folder=self._folderpath)
        ## Attention, here we modify the tagdto for this folder....
        if tagname is not None:
            self._logger.debug( 'Look for tag %s associated to global tag %s ' % (tagname, globaltagname ))
            self._tagdto = self._cmapi.getTag(tagname)
                               
        return tagname

    def findObject(self, stime,channelSel=None,tag=None):
        self._logger.debug('findObject: calling with arguments %s %s %s' % (str(stime) , str(channelSel), str(tag)))
        channel = channelSel
        if isinstance(channelSel,CrestCool.ChannelSelection):
            channel = channelSel.channel()
        
        if self._stime == stime:
            # now check tag
            if self._tagdto is not None and self._tagdto.name == tag and self._payload is not None:
                self._payload.setChannel(channel)
                self._logger.debug( 'Use cached data for channel %s ' % channel )
                return self._payload
        self._stime = stime
        if tag is not None and self._tagdto is None:
            self._tagdto = self._cmapi.getTag(tag)
        self._logger.debug( 'findObject from API is called with arguments %s %s ' % (stime,tag) )
        self._obj = self._cmapi.findObject(stime,tag)
        self._logger.debug( 'findObject has retrieved %s ' % (str(self._obj)) )

        self._payload = payload(self._obj,channel)
        ##self._payload.setChannel(channelSel)
        self._logger.info('Retrieved from DB payload data for channel %s ' % channel)

        return self._payload

    def browseObjects(self,since,until,channelSel,tag):
        tagname = None
        timeformat = 'time'
        channel = channelSel
        if tag is not None:
            if self._tagdto is None or self._tagdto.name != tag:
                self._tagdto = self.getTag(tag)
        else:
            if self._tagdto is None:
                raise Exception("Cannot load object from None folder or tag")
        
        self._logger.debug('browseObjects : calling with arguments %s %s %s %s ' % (since,until,channelSel,tag))
    
        if isinstance(channelSel,CrestCool.ChannelSelection):
            channel = channelSel.channel()
      
        self._logger.debug('browseObjects after channel selection method %s %s' % (channel,type(channel)))
      
        tagname = self._tagdto.name
        timeformat = self._tagdto.time_type
        search_params = {}
        if timeformat == 'run-lumi' and isinstance(since,str) and ',' in since:
            run_lumi = [x.strip() for x in since.split(',')]
            (run,lumi) = (run_lumi[0],run_lumi[1])
            self._logger.debug('Use until from run and lumi %s %s ' % (run,lumi))
            search_params['since'] = self._cmapi.runlumiToStime(run,lumi)
            run_lumi = [x.strip() for x in until.split(',')]
            (run,lumi) = (run_lumi[0],run_lumi[1])
            self._logger.debug('Use until from run and lumi %s %s ' % (run,lumi))
            search_params['until'] = self._cmapi.runlumiToStime(run,lumi)
        else:
            search_params['since'] = int(since)
            search_params['until'] = int(until)

        self._logger.debug('browseObjects search iovs using %s' % (search_params))

        iovlist = self._cmapi.browseObjects(search_params['since'],search_params['until'],tagname)
        self._logger.debug('browseObjects retrieved list of iovs of size %d' % (len(iovlist)))
        
        pyldcont = iovcontainer(self._cmapi,iovlist,channel)
        return pyldcont


class iovcontainer(object):

    def __init__(self, cmapi, iovlist, channelSel):
        
        if iovlist is not None:
            print 'Initialise iovcontainer using iovlist of length...',len(iovlist)
        self._iovlist = []
        if iovlist is not None:
            self._iovlist = iovlist
        self._currindex = 0
        self._cmapi = cmapi
        self._channelSel = channelSel
        self._cooliovlist = []
        logging.basicConfig(format='%(asctime)s %(message)s')
        self._logger = logging.getLogger('CoolIovContainer')
        self._logger.setLevel(logging.INFO)
        self._logger.debug('iovcontainer has been initialized with iovlist %d ' % len(iovlist))

        self.initcooliovs()

    def initcooliovs(self):
        for i in xrange(len(self._iovlist)):
            previov = { 'since' : 0 , 'until' : 0 }
            aniov = self._iovlist[i]
            nextiov = None
            if i < len(self._iovlist)-1:
                nextiov = self._iovlist[i+1]
            previov['since'] = aniov.since
            if nextiov is not None:
                previov['until'] = nextiov.since
            else:
                previov['until'] = CrestCool.ValidityMax
            
            ciov = cooliov(self._cmapi,previov,aniov,self._channelSel)
            self._logger.debug('iovcontainer adding cool iov %s for index %d ' % (ciov,i))
            
            self._cooliovlist.append(ciov)
    
    def size(self):
        return len(self._cooliovlist)

    def isEmpty(self):
        if self.size() > 0:
            return False
        return True

    def next(self):
        if self._currindex + 1 >= len(self._iovlist):
            return None
        return self._iovlist[self._currindex + 1]


    def currentRef(self):
        if self._currindex >= self.size:
            raise Exception("Cannot access reference at index %s " % self._currindex)
        iovobj = self._cooliovlist[self._currindex]
        return iovobj
        

    def currentIndex(self):
        return self._currindex

    def goToNext(self):
        self._currindex = self._currindex + 1
        if self._currindex >= self.size():
            return False
        return True


class cooliov(object):
    def __init__(self,cmapi,iovdict,iovdto,channelSel):
        self._iov = iovdict
        self._iovdto = iovdto
        self._channelSel = channelSel
        self._cmapi = cmapi
        logging.basicConfig(format='%(asctime)s %(message)s')
        self._logger = logging.getLogger('CoolIov')
        self._logger.setLevel(logging.INFO)
        self._logger.debug('cooliov has been initialized with iov dictionary %s and dto %s ' % (iovdict,iovdto))
    
    def since(self):
        return self._iov['since']

    def until(self):
        return self._iov['until']

    def payload(self):
        self._logger.debug('payloadcontainer.getObject seek for hash %s' % (self._iovdto))
        pyld = self._cmapi.getPayload(self._iovdto.payload_hash)
        coolpayload = self._cmapi.getCoolJson(pyld)
        self._currpayload = payload(coolpayload,self._channelSel)
        self._logger.debug('payloadcontainer.getObject created the payload object as in cool %s' % (self._currpayload.to_str()))
        self._currpayload.setuntil(self.until())
        return self._currpayload.payload()



class oldpayloadcontainer(object):
    
    def __init__(self, cmapi, iovlist, channelSel):
        
        self._iovlist = []
        if iovlist is not None:
            self._iovlist = iovlist
        self._channelSel = channelSel
        self._currindex = 0
        self._cmapi = cmapi
        self._cooliovlist = []
        logging.basicConfig(format='%(asctime)s %(message)s')
        self._logger = logging.getLogger('CoolPayloadContainer')
        self._logger.setLevel(logging.DEBUG)
        self._logger.debug('payloadcontainer has been initialized with iovlist %d ' % len(iovlist))
    
    
    def __getitem__(self,key):
        if isinstance(key,str):
            # Interpret this as a since time, it should be found in the iovlist
            since = [ x for x in self._iovlist if x.since == long(key)]
            self._iovdto = since
            return self.getObject()
        
        if isinstance(key,(int,long)):
            # Interprete this as an index...
            since = self._iovlist[key]
            self._iovdto = since
            return self.getObject()
        
        return None
    
    def size(self):
        return len(self._iovlist)
    
    def isEmpty(self):
        if self.size() > 0:
            return False
        return True
    
    def next(self):
        if self._currindex + 1 >= len(self._iovlist):
            return None
        return self._iovlist[self._currindex + 1]
    
    
    def currentRef(self):
        if self._currindex >= self.size:
            raise Exception("Cannot access reference at index %s " % self._currindex)
        self._iovdto = self._iovlist[self._currindex]
        self._logger.debug('payloadcontainer.currentRef selecting iov and hash %s %s' % (self._iovdto.since,self._iovdto.payload_hash))
        return self.getObject()
    
    def getObject(self):
        self._logger.debug('payloadcontainer.getObject seek for hash %s' % (self._iovdto))
        pyld = self._cmapi.getPayload(self._iovdto.payload_hash)
        coolpayload = self._cmapi.getCoolJson(pyld)
        self._currpayload = payload(coolpayload,self._channelSel)
        self._logger.debug('payloadcontainer.getObject created the payload object as in cool %s' % (self._currpayload.to_str()))
        nextiov = self.next()
        if nextiov is not None:
            self._currpayload.setuntil(nextiov.since)
        return self._currpayload
    
    def currentIndex(self):
        return self._currindex
    
    def goToNext(self):
        self._currindex = self._currindex + 1
        if self._currindex >= len(self._iovlist):
            return False
        return True


class payload(object):

    def __init__(self,coolpayload, channelSel):
        if not isinstance(coolpayload,ConditionsCoolPayload):
            raise Exception("Cannot construct payload object using this type...")
        
        self._cp = coolpayload
        self._cpchan = channelSel
        self._row = []
        self._cpchanrow = {}
        self._chdata = {}
        self._until = CrestCool.ValidityMax
        logging.basicConfig(format='%(asctime)s %(message)s')
        self._logger = logging.getLogger('CoolPayload')
        self._logger.setLevel(logging.INFO)
        self._columns = []
        self.initchanrow()

    def __getitem__(self,key):
        self._logger.debug('Getting item for key %s ' % key)

        if isinstance(key,str):
            return self._cpchanrow[key]
        if isinstance(key,(int,long)):
            return self._row[key]

    def listChannels(self):
        return self._cp.listChannels()
        
    def getChannel(self,channelSel):
        if channelSel is None:
            return self._cp.data_array
        self._cpchan = int(channelSel)
        return self._cp.getChannelData(self._cpchan)

    def setChannel(self,channelSel):
        self._cpchan = int(channelSel)
        self.initchanrow()
    
    def setuntil(self, until):
        if until is not None:
            self._until = until
    
    def since(self):
        return self._cp.stime

    def until(self):
        return self._until
    
    def getColumn(self,index):
        (col,typ) = self._columns[index]
        return col

    def initchanrow(self):
        self._logger.debug('initialise row %s ' % self._cp)

        columns = self._cp.folder_payloadspec.split(',')
        self._columns = [ name.split(':') for i,name in enumerate(columns) ]
        self._logger.debug('columns are %s ' % columns)

        self._logger.info('Look for the first channel if no selection is done...%s' % str(self._cpchan))
        if self._cpchan is None:
            self._cpchan = self.listChannels()[0]
            self._logger.info('selecting... %d ' % self._cpchan)

        if int(self._cpchan) < 0:
            return
        self._chdata = self.getChannel(self._cpchan)
        ##print 'Retrieved payload row for channel ',self._cpchan,chdata
        dataarr = []
        
        for (colname,ptype) in self._columns:
            columndata = self._chdata[colname]
            if 'Blob' in ptype:
                columndata = self._chdata[colname].decode('base64')
            if 'String16M' in ptype:
                columndata = self._chdata[colname].decode('base64')
            self._logger.debug('columndata %s ' % columndata)

            dataarr.append(columndata)
            self._cpchanrow[colname] = columndata

        self._row = dataarr
        self._logger.debug('Selected channel row %s ' % self._row)
#print 'Initialize array of data to be used with an index access for column ',self._row,columns,self._cpchan

    def size(self):
        columns = self._cp.folder_payloadspec.split(',')
        return len(columns)

    def payload(self):
        '''
            This method should return an array
        '''
        if len(self._row) <= 0:
            self.initchanrow()
        
        #print 'Retrieve payload array ',len(self._row)
        return self

    def getCoolPayload(self):
        return self._cp
    
    def to_str(self):
        print 'Print cool obj....'
        chansince = None
        if self._chdata is not None and bool(self._chdata) :
            print 'use chdata ',self._chdata
            if 'IOV_SINCE' in self._chdata:
                chansince = self._chdata['IOV_SINCE']
            else:
                chansince = self._cp.stime
        pstr = ("Cool Payload @ %s [chan since %s] has nchans %d and selected channel is %s " % (str(self._cp.stime),chansince,self._cp.nchans,self._cpchan))
        return pstr

    def __str__(self):
        return self.to_str()
