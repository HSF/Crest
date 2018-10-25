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
def storefile(f,directory,tagname):
    if f.endswith(".blob"):
        blobfname = os.path.join(directory, f)
        print(blobfname)   
        filenum = f.split('_')[1].split('.')[0]
        print('Use iov %s' % filenum)  
        cm.storeObject(tagname,filenum,blobfname,version='1',object_type='random gen',dataflag='fromfile')
    
    
if __name__ == '__main__':
    cm = CMApi()
    pfg = PayloadFileGen(100,20,'pgen')
    pfg.generate()
    
    directory = pfg.directory()
    
    try:
        tagname = 'TEST_100Kb'
        cm.createTag(tagname,time_type='run')
        for f in os.listdir(directory):
            storefile(f,directory,tagname)
   
    except Exception as e :
        print (e)

    cdms_profile.print_prof_data()
    pass
