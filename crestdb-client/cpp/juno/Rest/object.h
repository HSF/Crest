#ifndef Rest_object_h
#define Rest_object_h

#include <stdio.h>
#include "cJSON/cJSON.h"
#include <map>
#include <iostream>
#include <sstream>
namespace Rest {
    struct stub {
        virtual bool from_json(cJSON* json) = 0;
        virtual std::string val_str() = 0;
    };

    template <class T>
    struct stubT: stub {
        stubT(const std::string& k, T& v)
            : key(k), val(v), type(cJSON_String) {
        }
        
        bool from_json(cJSON* json) {
            cJSON* json_value = cJSON_GetObjectItem(json, key.c_str());
            // check type
            val = json_value->valuestring;
            val_ = json_value->valuestring;
            return true;
        }

        std::string val_str() {
            return val_;
        }
        
        std::string key;
        T& val;
        std::string val_;
        int type; // for cJSON
    };

    template <>
    struct stubT<int>: stub {
    stubT(const std::string& k, int& v)
        : key(k), val(v), type(cJSON_Number) {
        }
        
        bool from_json(cJSON* json) {
            cJSON* json_value = cJSON_GetObjectItem(json, key.c_str());
            // check type
            val = json_value->valueint;
            std::stringstream ss; ss << val; ss >> val_;
            return true;
        }

        std::string val_str() {
            return val_;
        }
        
        std::string key;
        int& val;
        std::string val_;
        int type; // for cJSON

    };

    template <>
    struct stubT<double>: stub {
    stubT(const std::string& k, double& v)
        : key(k), val(v), type(cJSON_Number) {
        }
        
        bool from_json(cJSON* json) {
            cJSON* json_value = cJSON_GetObjectItem(json, key.c_str());
            // check type
            val = json_value->valuedouble;
            std::stringstream ss; ss << val; ss >> val_;
            return true;
        }

        std::string val_str() {
            return val_;
        }
        
        std::string key;
        double& val;
        std::string val_;
        int type; // for cJSON

    };


    struct object {

        template<class T>
        void decl(const std::string& key, T& val) {
            m_stubs[key] = new stubT<T>(key, val);
        }

        bool from_json(cJSON* json);
        virtual bool show();

        std::map<std::string, stub*> m_stubs;

    };

}

#endif
