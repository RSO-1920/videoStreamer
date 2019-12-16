# videoStreamer
video streamer for rso1920


## API
* GET stream
```$xslt
http://<ip>:8084/v1/stream
```


## DOCKER RUN

```docker run -d --name rso1920-video-streaming-api --network rso1920 -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://etcd:2379 -p 8084:8084 rso1920/video-streamer:1.0.0-SNAPSHOT```


## OPENApi
```
http://localhost:8084/api-specs/v1/openapi.json
```
```
http://localhost:8084/api-specs/ui
```