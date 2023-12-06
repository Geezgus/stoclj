FROM clojure:lein-2.10.0-alpine AS build
WORKDIR /usr/src/app
COPY project.clj .
RUN lein deps
COPY . .
CMD lein ring server-headless