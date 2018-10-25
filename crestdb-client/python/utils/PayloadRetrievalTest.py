'''
Created on Oct 14, 2017

@author: formica
'''
from ConditionsManagementApi import CMApi
from PayloadFileGenerator import PayloadFileGen
import sys,os
import cdms_profile

sys.path.append(os.path.join(sys.path[0],'.'))

@cdms_profile.profile
def retrievefile(phash):
    pld = cm.getPayload(phash)    
    print ('Retrieved payload with hash %s ' % pld.hash)
    
if __name__ == '__main__':
    cm = CMApi()
    
    try:
        tagname = 'TEST_10000Kb'
        tag = cm.getTag(tagname)
        iovlist = cm.listAllIovs(tagname)
        counter=0
        for iiv in iovlist:
            counter = counter+1
            retrievefile(iiv.payload_hash)
            if counter > 5:
                break
    except Exception as e :
        print (e)

    cdms_profile.print_prof_data()
    pass
