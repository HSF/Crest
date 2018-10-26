#include <iostream>
#include "Rest/cURL.h"
#include "Rest/object.h"

struct Params {
    bool parse(int argc, char* argv[]) {

        for (int i = 1; i < argc; ++i) {
            std::string opt = argv[i];
            if (opt=="--uri") {
                if (++i>=argc) { return false; }
                uri = argv[i];
            } else if (opt=="--type") {
                if (++i>=argc) { return false; }
                type = argv[i];
            } else {
                std::cerr << "unknown options: " << opt << std::endl;
		return false;
	    }
        }

        return true;
    }

    std::string uri;

    std::string type; // load data type
};

// Data Model
struct Person: Rest::object{
    std::string name;
    int age;

    Person() {
        decl("name", name = "Unknown");
        decl("age", age = 0);
    }

    bool show() {
        std::cout << "I am " << name << ". "
                  << "I am " << age << " years old."
                  << std::endl;
        return true;
    }

};

struct GlobalTags: Rest::object {
    std::string name;
    double validity;
    std::string description;
    std::string release;
    std::string insert_time;

    GlobalTags() {
        decl("name", name);
        decl("validity", validity);
        decl("description", description);
        decl("release", release);
        decl("insertionTime", insert_time);
    }
};

struct Tag: Rest::object {
    std::string name;
    std::string description;

    Tag() {
        decl("name", name);
        decl("description", description);
    }
};

// payload
// {"hash":"aeiou","version":"aeiou","objectType":"aeiou","data":"","streamerInfo":"","insertionTime":"2018-05-24"}
struct Payload: Rest::object {
    std::string hash;
    std::string version;
    std::string object_type;
    std::string data;
    std::string streamer_info;
    std::string insertion_time;

    Payload() {
        decl("hash", hash);
        decl("version", version);
        decl("objectType", object_type);
        decl("data", data);
        decl("streamerInfo", streamer_info);
        decl("insertionTime", insertion_time);
    }
};

int main(int argc, char* argv[]) {
    Params param;
    bool st = param.parse(argc, argv);
    if (!st) {
        return -1;
    }

    Rest::cURL curlobj;

    if (curlobj.request(param.uri)) {
        std::cout << curlobj.result() << std::endl;
        cJSON* json = cJSON_Parse(curlobj.result().c_str());

        bool is_array = false;
        if (json->type==cJSON_Array) {
            std::cout << "It's array!" << std::endl;
            is_array = true;
        } else {
            std::cout << "It's object!" << std::endl;
        }
        // if it's an array, we access child, otherwise, using it self.
        for (cJSON* elem = (is_array) ? json->child: json; 
             elem != NULL; elem = elem->next) {

            if (param.type == "person") {
                Person p;
                p.from_json(elem);
                p.show();
            } else if (param.type == "globaltags") {
                GlobalTags gt;
                gt.from_json(elem);
                gt.show();
            } else if (param.type == "tags") {
                Tag t;
                t.from_json(elem);
                t.show();
            } else if (param.type == "payload") {
                Payload p;
                p.from_json(elem);
                p.show();
            } else {
                std::cout << "Unkown type: " << param.type << std::endl;
                break;
            }

            if (!is_array) { break; }
        }
        
    }


    
}
