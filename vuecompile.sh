#!/bin/sh
cd ./web-ui/crest-ui

npm install
npm rebuild node-sass
#npm install --save axios
#npm install --save buefy
npm update caniuse-lite browserslist
npm run build
