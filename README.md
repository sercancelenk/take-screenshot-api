
**Build and Run**

```
    root> cd url-tracker-api
    root> mvn clean install -DskipTests
    root> cd ..
    root> docker-compose build
    root> docker-compose up
 ```


**Service**
- /take/screenshot/multiple/v1

**Request**

```
curl -X POST \
  http://localhost:8081/take/screenshot/multiple/v1 \
  -H 'Cache-Control: no-cache' \
  -H 'Postman-Token: 274b744f-becf-4596-9a40-5abde4798105' \
  -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  -F file=@/Users/byzasttech/Desktop/tmp/urls.txt
```

**Response**
  - ThreadId: String  

Service: /take/screenshot/single

**Request**

```
curl -X POST \
  http://localhost:8081/take/screenshot/single \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/json' \
  -H 'Postman-Token: 15b9f16b-231c-4b6e-87d9-1ac63d440f34' \
  -d '{
	"url":"http://www.google.com"
}'
```

**Response**
- ScreenshotFileName: String

**Service**
- /screenshots/{threadId}

**Request**
```
curl -X GET \
  http://localhost:8081/screenshots/e7852fb4-cb7b-44bf-b6dd-ec96eb293010 \
  -H 'Cache-Control: no-cache' \
  -H 'Postman-Token: {threadId}'
 ```
  
**Response**
- Screenshots: Array
    
**Service**
- /screenshot/{name}
 
**Method**
- GET

**Request**

```
curl -X GET \
  http://localhost:8081/screenshot/file.png \
  -H 'Cache-Control: no-cache' \
  -H 'Postman-Token: d2cfb101-e176-4954-828c-8a99f867c7cb'
 ```
 
**Response**
- image: IMAGE