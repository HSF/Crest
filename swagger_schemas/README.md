#       RestSwagger  / Schemes  

#### Author: A. Formica

## Description
- This folder contains the Crest API description and the messages description to exchange data from client and server. 

## Usage
The JSON files must be compliant with the JSON schema standard
1. [Web site](http://json-schema.org/)

The conversion from JSON to YAML must be operated by hand. 
1. Some script sample are provided in /scripts
2. There are also lots of [converters](https://www.json2yaml.com/) on line

The generation of Crest server stubs and DTO objects (Data Transfer Object) is done via gradle plugins from the root directory of the project:
```
gradle generateSwaggerCode
```
The output is put in `<rootdir>/build`.