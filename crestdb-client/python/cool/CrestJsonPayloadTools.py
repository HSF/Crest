
import json
from dictdiffer import diff, patch, swap, revert

class JsonPayloadTools(object):


    def __init__(self):
        print('Init tools')
        
    def loadFromJson(self,filename):
        d = {}
        with open(filename) as json_data:
            d = json.load(json_data)
            print ('length of json: %d' % len(d))
        return d['DATA']
    
    def diff(self,filea,fileb):
        from JsonCoolPayload import ConditionsCoolPayload
        cp = ConditionsCoolPayload()
        dica = self.loadFromJson(filea)
        dicb = self.loadFromJson(fileb)
        
        result = diff(dica, dicb)
        
        print('List of differences %s' % result)
        ld = list(result)
        print('Length %s' % len(ld))

        for a in ld:
            print('Found difference for %s' % (str(a)))

if __name__ == '__main__':
    import sys
#    import CDMSCool as cool
    print ('Test diff on 2 files ')
    
    jpt = JsonPayloadTools()
    fa=sys.argv[1]
    fb=sys.argv[2]
    print ('Use files: %s %s' % (fa,fb))
    jpt.diff(fa,fb)
