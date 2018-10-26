

cmake_minimum_required(VERSION 2.8)

# add this options before PROJECT keyword
# disable in source build
set(CMAKE_DISABLE_SOURCE_CHANGES ON)
set(CMAKE_DISABLE_IN_SOURCE_BUILD ON)

project(Rest)

find_package(CURL REQUIRED)
if(CURL_FOUND)
        message("Found CURL: ${CURL_LIBRARIES}")
endif(CURL_FOUND)

# header files
include_directories (${CMAKE_CURRENT_SOURCE_DIR})
include_directories (${CMAKE_CURRENT_SOURCE_DIR}/contrib/cJSON)

set(SOURCE_FILES
        src/cURL.cc
        src/object.cc
        contrib/cJSON/src/cJSON.c
        contrib/cJSON/src/common.c
        contrib/cJSON/src/js.cc
)

# Rest library
add_library(Rest SHARED ${SOURCE_FILES})
target_link_libraries(Rest ${CURL_LIBRARIES})

# Application
add_executable(restapi tests/main.cc)
target_link_libraries(restapi Rest)

# Install
install(TARGETS Rest DESTINATION lib)
install(DIRECTORY
                Rest
                contrib/cJSON/cJSON
        DESTINATION include)
install(TARGETS restapi DESTINATION bin)

# Test
ENABLE_TESTING()

## copy the files into build directories
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/share/arr_data.json
               ${CMAKE_CURRENT_BINARY_DIR}/share/arr_data.json
               COPYONLY)
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/share/data.json
               ${CMAKE_CURRENT_BINARY_DIR}/share/data.json
               COPYONLY)

add_test(NAME TestRestapiSingle
         COMMAND
                restapi --uri file://${CMAKE_CURRENT_BINARY_DIR}/share/data.json --type person
)
add_test(NAME TestRestapiArray
         COMMAND
                restapi --uri file://${CMAKE_CURRENT_BINARY_DIR}/share/arr_data.json --type person
)
add_test(NAME TestRestapiSingleHTTP
         COMMAND
                restapi --uri http://localhost:8000/person/tao --type person
)
add_test(NAME TestRestapiArrayHTTP
         COMMAND
                restapi --uri http://localhost:8000/person --type person
)
