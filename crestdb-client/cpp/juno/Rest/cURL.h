#ifndef Rest_cURL_h
#define Rest_cURL_h

#include <stdio.h>
#include <curl/curl.h>
#include <iostream>

namespace Rest {
struct cURL {
    cURL();
    ~cURL();

    // request a URL:
    bool request(const std::string& uri);
    // get the result of request:
    std::string result();

private:
    static  size_t write_data(void *buffer, size_t size, size_t nmemb, void *userp);
    CURL *curl;
    std::string m_result;

};

}

#endif
