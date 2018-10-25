#### Author: A.Formica
##### Date of last development period: 2017/10/01 
```
   Copyright (C) 2017  A.Formica

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
# Table of Contents
1. [Description](#description)
2. [Installation](#installation)
3. [CliCrestConsole](#clicrestconsole)
4. [ConditionsManagementApi](#conditionsmanagementapi)

## Description
This python package contains tools for accessing the CrestDB server.
In the following we will describe every script in more details. Just for generic reference, here is a summary list of the script content.
 * _CliCrestConsole_ :
 	This program intend to emulate the AtlCoolConsole for browsing the CrestDB, but it contains some additional feature to insert data that can be used for testing.
 * _ConditionsManagementApi_ :
 	This programs contains several classes which expose the CrestAPI at different levels. One of these classes is intended as a python COOL API emulator for CrestAPI.
 	
## Installation
Python version >=2.7 is required to run all the commands proposed in this package.
If you are on lxplus, you can just setup an Athena release (e.g.: _asetup 21.0.20,slc6,here_) and access the tools from the _atlcond_ account as mentioned below. If you are used to this way then probably you can jump to the script description and the examples.

In case of a standalone installation you can use virtualenv to install the dependencies for the scripts.
Details on dependencies are given in each command if needed. As a general remark, consider that the following packages are needed:
  * certifi : python package
  * urllib3 : python package
  
All these packages can be installed via "pip install xxx" if you are on your own computer. 
In addition to the previous packages you need also *cx_Oracle* if you want to run the *plsql_coolmigration* script. This script has a direct connection to Oracle in order to get conditions data from COOL. 


## CliCrestConsole
This program emulates some of the aspects of the AtlCoolConsole. 
In order to run this command, you should provide the name of the host where the CrestDB is available. To do this, set the *CDMS_HOST* variable in the following way:

```sh
export CDMS_HOST=http://crest-undertow.web.cern.ch/crestapi
```
To run the script:

```
python ./CliCrestConsole.py
```

Usage:
   * __ls__ _tag-pattern [optional]_ : list the tags present in the system. E.g.: ls MDT , will print all tags containing string MDT in their name.
   * __usetag__ _tag-name_ : select a given tag
   * __tail__ _n-iovs_: print the last N iovs (order is descending). E.g.: tail 10 , will print the last 10 IOVs.
   * __less__ _page size_ : print the selected "page" of size "size". E.g.: less 0 100 will print the first 100 iovs (order is ascending).
   * __info__ : print number of IOVs in the selected tag.
   * __get__ _hash_ : extract information from the payload.
   * __dump__ _hash_: dump the payload on the screen (in progress)
   * __tofile__ _hash_: dump the payload into a file (in progress); the file name will be a combination of the tag and the hash.
   * __savetag__ _name=atagname,description=atag description,timeType=time_: create a tag (in progress). Here we use default values for the other tag fields. 
   * __insert__ _name=atagname,since=123456,data=./afilename_: insert an IOV in a tag using an external file (in progress); Be careful to express the since time in the correct way.


## ConditionsManagementApi
This file contains several classes in which we try to identify a low level python API for CrestDB and a sort of Cool wrapper.
We provide below the list of the relevant methods and their description in individual classes.

### Crest API: class CMApi
This set of functions can be considered close to the CMS usage of the data model underlying CrestDB. In short, this is how the API should appear.
 * _def createTag(self,tagname,**kwargs)_ : use the tag name and the following parameters to create a tag.

 ```
 all_params = [ 'time_type', 'object_type', 'synchronization', 'description', 'last_validated_time', 'end_of_validity' ]
```

 * _getTag(self,tagname)_ : use the tag name to retrieve a tag. It returns a tag object [TagDto](../client/docs/TagDto.md)
 
 
 * _listTags(self,tagpattern)_ : use a search string to list matching tags. It returns a list of tag objects [TagDto](../client/docs/TagDto.md)
 
 
 * _createGlobalTag(self,gtagname,**kwargs)_ : use the global tag name and the following parameters to create a global tag

 ```
        all_params = [ 'validity', 'release', 'description', 'snapshot_time', 'scenario', 'workflow', 'type' ]
 ```
 * _createGlobalTagMap(self,gtagname,tagname,**kwargs)_ : use the global tag name and the tag name to associate them in the mapping table. Additional parameters are:
 
 ```    
 		all_params = [ 'record', 'label' ]
 ```
 
 * _listGlobalTags(self,tagnamepattern)_ : use a global tag name search string to find matching global tags. It returns a list of global tag objects [GlobalTagDto](../client/docs/GlobalTagDto.md).


 * _listIovs(self,tagname,**kwargs)_ : list iovs in a given tag; a range in time can be specified. The meaning of the range in time provided can be specified by the _type_ parameter (for example if you want to provide run-lumi, or time etc...).It returns a list of [IovDto](../client/docs/IovDto.md).
 
 ```
        all_params = [ 'since', 'until', 'snapshot', 'type' ]
 ```
 
  * _getSize(self,tagname,**kwargs)_ : gets back the number of iovs in a tag. Additional parameter can be provided to select only iovs which were inserted before the given date.
  
  ```
        all_params = [ 'snapshot' ]
  ```
  * _selectGroups(self,tagname,**kwargs)_ : select, for the given tag, iov groups (pages) which can later be used to load the real iovs. Additional parameter can be provided to select only iovs which were inserted before the given date. Returns a list of [GroupDto](../client/docs/GroupDto.md).

  ```
        all_params = [ 'snapshot' ]
  ```
  
  * _storeObject(self,tagname,since,data,**kwargs)_ : store a file (blob) in a given tag and with a given time. Additional parameters can be used to provide meta informations on the file itself.
  
  ```
        all_params = ['tag_name', 'since', 'insertion_time', 'payload_hash', 'streamer_info', 'object_type', 'version', 'dataflag' ]
  ```
  * _getPayload(self, phash)_ : it retrieves a file from the CrestDB using the _hash_ of the payload that has been provided via the retrieval of the IovDto object. Returns a [PayloadDto](../client/docs/PayloadDto.md)._
 
### Crest Cool API: class CoolCMApi
We define here a set of functions which have been taken from the COOL API (missing a document link here ???). Here we have only low level functions, used to extract informations from the CrestDB.
For each method we have translated there is a sort of pseudo code using the previous functions. Some additional COOL API features are implemented in the package `../cool/ConditionsFolder.py`. A [README](../cool/README.md) file contains further informations on COOL interfaces like the `IFolder` for example. 

  * _browseObjects(self,since,until,tagname)_ : this methods allow to retrieve data for a given tag and in a given range. As such, it is a mixture of `listIovs` and `getPayload`. We have chosen to only retrieve IovDtos here, and to load the real payload when accessing the data.
  
  ```
        snapshot=DefaultSnapshot*1000
        self.__iovlist = self.listIovs(tagname,since=since, until=until,snapshot=snapshot)
        return self.__iovlist 
  ```
  
  * _getObject(self,iov,channelSel=None)_ : for a given IovDto, this method retrieves the payload (for a given channel if asked).
  
  ```
          if iov is not None:
            pyld = self.getPayload(iov.payload_hash)
  
  		.... channel selection available only if we assume to know the payload format....
  ```
  
  * _findObject(self,stime,tag=None)_ : it retrieves a single iov for the given tag, and retrieves the payload.
  
  ```
  ...
          self.__tag = self.getTag(tag)
  ...
          self.__iovlist = self.selectSnapshot(tagname...)
  ...        
          (before,after)=self.takeClosest(stimelist,search_params['since'])
  ...
  		  selobjiov= self.listIovs(tagname,since=before, until=after)
  ...
          for aniov in selobjiov:
            pyld = self.getPayload(aniov.payload_hash)
  ```
 
  