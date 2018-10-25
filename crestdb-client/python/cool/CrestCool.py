
Int32Max = 2147483647
UInt32Max = 4294967295
ValidityMax = (Int32Max << 32)|(UInt32Max)
DefaultSnapshot = 2500000000

class DatabaseSvc(object):


    def __init__(self):
        from ConditionsManagementApi import CoolCMApi
        print 'Initialize new cool api'
        self.__cmapi = CoolCMApi()
        self.__folder = ''
        self.__coolfolder = None

    def getFolder(self,folderpath):
        self.__folder = folderpath
        tagdto = None
        ## Just in case we can use the folder as a tag, to see if it works.
        ## if not, then the tag should be given in later methods.
        try:
            tagdto = self.__cmapi.getTag(folderpath)
        except Exception, e:
            print 'Cannot find a tag using ',folderpath
        
        from ConditionsFolder import coolfolder
        self.__coolfolder = coolfolder(folderpath,tagdto,self.__cmapi)
        return self.__coolfolder
    
    def closeDatabase(self):
        return

class ChannelSelection(object):
    def __init__(self,channum):
        self._chan = channum

    def channel(self):
        return self._chan


if __name__ == '__main__':
    
#    import CDMSCool as cool
    print 'Parameters from cool ',Int32Max
    cooldb = DatabaseSvc()
    folder = cooldb.getFolder('TileOfl02CalibCisLin-RUN2-UPD4-14')
    
    obj =  folder.findObject('314557,0', 240, 'TileOfl02CalibCisLin-RUN2-UPD4-14')
    print 'Retrieved payload for channel 240',obj.to_str()
    blob = obj.payload()[0]
    obj =  folder.findObject('314557,0', 1000, 'TileOfl02CalibCisLin-RUN2-UPD4-14')
    print 'Retrieved payload for chan 1000 ',obj.to_str()
    blob = obj.payload()[0]
    print 'Found blob for channel 240 ', blob
