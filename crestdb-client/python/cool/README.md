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
2. [ConditionsFolder](#conditionsfolder)

## Description
This python package contains cool wrappers for accessing the CrestDB server in a similar manner respect to COOL API.

 * _ConditionsFolder_ :
 	This file contains classes handling a cool folder, iov and payload functions.
 * _ConditionsCoolPayload_ :
 	This file contains a preliminary version of COOL data in the form of a generic attribute list. This format has been the first attempt to provide common migration tools for data. Other tools are in development today, and probably more complete formats should be implemented at the level of this class.
 	
## ConditionsFolder
We describe below specific classes contained in this file.

### Cool Folder: class coolfolder
Emulator for cool folder.
 * _init(folder,tag)_ : ctor is taking information related to tag name. A tag in Crest is similar to a folder in COOL. In reality COOL folders could belong to the mapping between tags and global tags. 
 * _description(self)_ : return the tag description.
 * _specification(self)_ : return the tag specifications (object type).
 * _tag(self)_ : returns the tag name.
 * _timetype(self)_ : returns the tag time type.
 * _resolveTag(self,globaltagname)_ : to be implemented. Should use a label in the mapping.
 * _findObject(self, stime,channelSel=None,tag=None)_ : retrieve an object from CrestDB at a given time, for the specified tag and channel selection (here we assume that we know the payload format to be the one defined in `ConditionsCoolPayload`. It returns a single payload.
 * _browseObjects(self,since,until,channelSel,tag)_ : similar as the method before, but instead of returning a single payload we return here an iov container (see `iovcontainer` in this file).
 

### Iov container: class iovcontainer
This class allows to handle iovs retrieved from Crest and load transparently the payload when accessed.
 * _init(iovlist,channelSel)_: ctor takes the iov list in argument. Also a channel selection can be done at this level.
 * _size(self)_ : return the iov list length.
 * _next(self)_ : check if the next entry exists.
 * _gotToNext(self)_: increment the cursor.
 