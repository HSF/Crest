#!/usr/bin/env python
#-*- coding: utf-8 -*-
# 
# This script will setup a fake crest api server.
# Both GET or POST data

import BaseHTTPServer
import json

class CrestRequestHandler(BaseHTTPServer.BaseHTTPRequestHandler):

    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()

        data = self.path_dispatch()
        if data is None:
            data = "{}"
        self.wfile.write(json.dumps(data))

    def do_POST(self):

        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()

        # print(dir(self.rfile))
        length = int(self.headers.getheader('content-length'))
        rawinput = ""
        if length:
            # print("start rfile read.")
            rawinput = self.rfile.read(length)

        # print("hello")

        data = self.path_dispatch()
        if data is None:
            data = "{}"
        self.wfile.write(json.dumps(data))
        # print("END")

    def path_dispatch(self):

        r = self.path.split('/')

        # "/"              -> ["", ""]
        # "/person"        -> ["", "person"]
        # "/person/lintao" -> ["", "person", "lintao"]

        length = len(r)
        key = None
        value = None

        if length == 1:
            return
        elif length == 2:
            key = r[1]
            value = None
        elif length == 3:
            key = r[1]
            value = r[2]


        if key == "person" and value:
            data = {}
            data["name"] = value
            data["age"] = len(value)
            return data

        elif key == "person":
            # return list of objects
            result = []
            
            for i in range(3):
                d = {}
                d["name"] = "user%d"%i
                d["age"] = i*3
                result.append(d)

            return result

def run(server_class=BaseHTTPServer.HTTPServer,
        handler_class=CrestRequestHandler):
    server_address = ('', 8000)
    httpd = server_class(server_address, handler_class)
    httpd.serve_forever()

if __name__ == "__main__":
    run()
