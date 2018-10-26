#include "Rest/cURL.h"

namespace Rest {

    cURL::cURL() {
        /* In windows, this will init the winsock stuff */ 
        curl_global_init(CURL_GLOBAL_ALL);
 
        /* get a curl handle */ 
        curl = curl_easy_init();
    }

    cURL::~cURL() {
        if (curl) {
            curl_easy_cleanup(curl);
            curl = NULL;
        }

        curl_global_cleanup();

    }

    bool cURL::request(const std::string& uri) {
        if (!curl) { return false; }

        curl_easy_setopt(curl, CURLOPT_URL, uri.c_str());
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_data);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, this); 

        /* Perform the request, res will get the return code */ 
        CURLcode res = curl_easy_perform(curl);
        if(res != CURLE_OK) {
            fprintf(stderr, "curl_easy_perform() failed: %s\n",
                    curl_easy_strerror(res));
        }

        return true;
    }

    std::string cURL::result() {
        return m_result;
    }

    size_t cURL::write_data(void *buffer, size_t size, size_t nmemb, void *userp) {
        cURL* self = (cURL*)(userp);

        size_t realsize = size * nmemb;
        self->m_result.append((char*)buffer, realsize);

        return realsize;
    }
}
