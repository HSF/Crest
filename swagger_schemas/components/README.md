#       RestSwagger  / Schemes / components 

#### Author: L. Michel

## Description
- This folder contains the serialization of all the flat components of the JSON messages shared by different modules of the FSC
- Serializations are available in both JSON and YAML formats, but the reference is always the JOSN file
- The conversion from JSON to YAM is achieved by the JAXB/Jackson chain run by the command XXX

## Usage
The JSON files must be compliant with the JSON schema standard
1. [Web site](http://json-schema.org/)
2. [Tutorial](https://spacetelescope.github.io/understanding-json-schema/)

The conversion from JSON to YAML must be operated by hand. 
1. Some script sample are provided in /scripts
2. There are also lots of [converters](https://www.json2yaml.com/) on line
