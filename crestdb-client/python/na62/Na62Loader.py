#!/usr/local/bin/python2.7
# encoding: utf-8
'''
utils.Na62Loader -- shortdesc

utils.Na62Loader is a description

It defines classes_and_methods

@author:     user_name

@copyright:  2017 organization_name. All rights reserved.

@license:    license

@contact:    user_email
@deffield    updated: Updated
'''

import sys
import os

from argparse import ArgumentParser
from argparse import RawDescriptionHelpFormatter
from os import walk
sys.path.append(os.path.join(sys.path[0],'..'))
from utils.ConditionsManagementApi import CMApi

__all__ = []
__version__ = 0.1
__date__ = '2017-09-20'
__updated__ = '2017-09-20'

DEBUG = 0
TESTRUN = 0
PROFILE = 1

class CLIError(Exception):
    '''Generic exception to raise and log different fatal errors.'''
    def __init__(self, msg):
        super(CLIError).__init__(type(self))
        self.msg = "E: %s" % msg
    def __str__(self):
        return self.msg
    def __unicode__(self):
        return self.msg

def main(argv=None): # IGNORE:C0111
    '''Command line options.'''

    if argv is None:
        argv = sys.argv
    else:
        sys.argv.extend(argv)

    program_name = os.path.basename(sys.argv[0])
    program_version = "v%s" % __version__
    program_build_date = str(__updated__)
    program_version_message = '%%(prog)s %s (%s)' % (program_version, program_build_date)
    program_shortdesc = __import__('__main__').__doc__.split("\n")[1]
    program_license = '''%s

  Created by user_name on %s.
  Copyright 2017 organization_name. All rights reserved.

  Licensed under the Apache License 2.0
  http://www.apache.org/licenses/LICENSE-2.0

  Distributed on an "AS IS" basis without warranties
  or conditions of any kind, either express or implied.

USAGE
''' % (program_shortdesc, str(__date__))
    ###print program_license
    try:
        # Setup argument parser
        parser = ArgumentParser(description=program_license, formatter_class=RawDescriptionHelpFormatter)
        parser.add_argument("-r", "--recursive", dest="recurse", action="store_true", help="recurse into subfolders [default: %(default)s]")
        parser.add_argument("-v", "--verbose", dest="verbose", action="count", help="set verbosity level [default: %(default)s]")
        parser.add_argument("-i", "--include", dest="include", help="only include paths matching this regex pattern. Note: exclude is given preference over include. [default: %(default)s]", metavar="RE" )
        parser.add_argument("-e", "--exclude", dest="exclude", help="exclude paths matching this regex pattern. [default: %(default)s]", metavar="RE" )
        parser.add_argument('-V', '--version', action='version', version=program_version_message)
        parser.add_argument("-t", "--tagext", dest="tagext",help="tag extension string [default: %(default)s]", default='HEAD')
        parser.add_argument(dest="paths", help="paths to folder(s) with source file(s) [default: %(default)s]", metavar="path", nargs='+')

        # Process arguments
        args = parser.parse_args()

        paths = args.paths
        verbose = args.verbose
        recurse = args.recurse
        inpat = args.include
        expat = args.exclude
        tagext = args.tagext

        if verbose > 0:
            print("Verbose mode on")
            if recurse:
                print("Recursive mode on")
            else:
                print("Recursive mode off")

        if inpat and expat and inpat == expat:
            raise CLIError("include and exclude pattern are equal! Nothing will be processed.")

        gtagarr = paths[0].split('/')
        gtagname = gtagarr[len(gtagarr)-1]
        print 'creating gtag ',gtagname
        iovlist=[]
        taglist=[]
        for inpath in paths:
            ### do something with inpath ###
            print(inpath)
            for (dirpath, dirnames, filenames) in walk(inpath):
                print dirpath,dirnames,filenames
                if dirpath == inpath and len(dirnames)>0:
                    iovlist.extend(dirnames)
                if len(filenames)>0:
                    taglist.extend( [ x.split('.')[0] for x in filenames])
        
            print iovlist
            print taglist
        cresapi = CMApi()
        print 'Creating global tag ',gtagname
        cresapi.createGlobalTag(gtagname)
        
        for atag in taglist:
            tagname = '%s-%s' % (atag,tagext)
            print 'Creating tag ',tagname
            cresapi.createTag(tagname,time_type='run')
            print 'Creating map for ',tagname,' with label ',atag
            cresapi.createGlobalTagMap(gtagname, tagname,label=atag)
            
        
        for aniov in iovlist:
            since = int(aniov)
            iovpath = '%s/%s' % (inpath,aniov)
            for atag in taglist:
                tagname = '%s-%s' % (atag,tagext)
                tmplist = []
                for (dirpath, dirnames, filenames) in walk(iovpath):
                    if len(filenames)>0:
                        tmplist = [ x.split('.')[0] for x in filenames]
                        if atag in tmplist:
                            datafile = '%s/%s.dat' % (iovpath,atag)
                            print 'Storing ',datafile
                            # insert the iov in the tag...
                            cresapi.storeObject(tagname, since, datafile,dataflag='fromfile')
                        
        return 0
    except KeyboardInterrupt:
        ### handle keyboard interrupt ###
        return 0
    except Exception, e:
        if DEBUG or TESTRUN:
            raise(e)
        indent = len(program_name) * " "
        sys.stderr.write(program_name + ": " + repr(e) + "\n")
        sys.stderr.write(indent + "  for help use --help")
        return 2

if __name__ == "__main__":
    if DEBUG:
        sys.argv.append("-h")
        sys.argv.append("-v")
        sys.argv.append("-r")
    if TESTRUN:
        import doctest
        doctest.testmod()
    if PROFILE:
        import cProfile
        import pstats
        profile_filename = 'utils.Na62Loader_profile.txt'
        cProfile.run('main()', profile_filename)
        statsfile = open("profile_stats.txt", "wb")
        p = pstats.Stats(profile_filename, stream=statsfile)
        stats = p.strip_dirs().sort_stats('cumulative')
        stats.print_stats()
        statsfile.close()
        sys.exit(0)
    sys.exit(main())
