#include "Rest/object.h"

namespace Rest {

    bool object::from_json(cJSON* json) {
        bool is_success = true;

        if (!json) {
            return false;
        }

        for (std::map<std::string, stub*>::iterator it = m_stubs.begin();
             it != m_stubs.end(); ++it) {
            it->second->from_json(json);
        }

        return is_success;
    }

    bool object::show() {
        for (std::map<std::string, stub*>::iterator it = m_stubs.begin();
             it != m_stubs.end(); ++it) {
            std::cout << it->first
                      << ": "
                      << it->second->val_str()
                      << std::endl;
        }
        return true;
    }

}
