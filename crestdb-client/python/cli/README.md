# Crest client documentation
This python client is an example to test CREST server capabilities. It can be used as a base to get familiar with the CREST API.

## Requirements
Need python >=3.5

## Installation
Can be installed via pip. 
`pip install . `
or 
`pip install . --user`
for local user installation.

## Usage examples
Launch the command line after the library has been installed.
```
python CliCrestConsole.py
```

```
(py36-venv) [lxplus775] hackathon/db-client-tools/crest % python CliCrestConsole.py -h
usage: CliCrestConsole.py [-h] [--host HOST] [--api API] [--port PORT]
                          [--socks] [--ssl]

Crest browser.

optional arguments:
  -h, --help   show this help message and exit
  --host HOST  Host of the Crest service (default: svomtest.svom.fr)
  --api API    Base name of the api (default: crestapi)
  --port PORT  Port of the Crest service (default: 8090)
  --socks      Activate socks (default: false)
  --ssl        Activate ssl (default: false)
(py36-venv) [lxplus775] hackathon/db-client-tools/crest % python CliCrestConsole.py --host crest-01.cern.ch --port 8080
INFO:__main__: The host is set to http://crest-01.cern.ch:8080/crestapi
INFO:__main__: Connected to http://crest-01.cern.ch:8080/crestapi
INFO:__main__: Start application
(Crest): help

Documented commands (type help <topic>):
========================================
connect  convert  create  get  help  iovs  ls  select  upload

Undocumented commands:
======================
exit  quit

(Crest): help ls
ls <datatype> [-t tag_name] [-T globaltag_name] [other options: --size, --page, --sort, --format]
        Search for data collection of different kinds: iovs, tags, globaltags.
        datatype: iovs, tags, globaltags, trace, backtrace
        Type ls -h for help on available options (not all will be appliable depending on the chosen datatype)

(Crest): ls tags
INFO:__main__: Searching tags
Retrieved 46 lines
```