FROM node:9.11.1-alpine as build-stage

# install simple http server for serving static content
# RUN npm install -g http-server

# make the 'app' folder the current working directory
WORKDIR /home/crestui

# copy both 'package.json' and 'package-lock.json' (if available)
COPY package*.json ./

# install project dependencies
RUN npm install

# copy project files and folders to the current working directory (i.e. 'app' folder)
COPY . .

# build app for production with minification
RUN npm run build

# production stage
FROM nginx:1.13.12-alpine as production-stage
COPY --from=build-stage /home/crestui/dist /usr/share/nginx/html/crestui
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

#EXPOSE 8080
#CMD [ "http-server", "dist" ]
