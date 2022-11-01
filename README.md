# json-posts project 
REST app exposing an endpoint allowing to take all posts from `https://jsonplaceholder.typicode.com/` service and save all of them to separate JSON files inside a configured directory.
File names are taken from a post id f.e. `1.json`

## Running a project
In order to build and run the app it's required to have installed:
* `java11`
* `sbt`

To run a project simply type `sbt run` in the terminal
The app should be accessible from `http://localhost:8080`

## Testing the app
As it's already mentioned, the app allows provides and endpoint to
save posts - with or without comments
### Endpoint
#### Method: POST
#### Path: `/post`
#### Body: `{ "withComments": <<Boolean>> }`
#### Example with curl:
```
curl --request POST 'localhost:8080/post' \
--header 'Content-Type: application/json' \
--data-raw '{
    "withComments": true
}'
```
Result with 200 code indicates success, otherwise it will fail with 500

## Additional routes
There are additional basic routes

| Path            | Method | Description     |
|:----------------|--------|-----------------|
| `/health/ready` | GET	   | Readiness check |
| `/health/live`  | GET	   | Liveness check  |
| `/metrics`      | GET	   | Apps metrics    |

