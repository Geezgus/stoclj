# stoclj

The [Clojure](https://github.com/clojure/clojure) Stock market

## Prerequisites

You will need either:
- [Leiningen][] 2.0.0 or above installed.
- [Docker + Docker Compose](https://docs.docker.com/get-docker/)

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

```bash
export BRAPI_API_TOKEN = "your brapi token"
lein ring server
```
If you want to use docker, instead, run:

```bash
touch .env
echo BRAPI_API_TOKEN = "your brapi token" >> .env
docker compose up
```

## License

Copyright © 2023 FIXME
