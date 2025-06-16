# Social-Media-Backend
social-media-authentication port:8080
social-media-friend port:8081

social-media-discovery-server port:8761
api-gateway port:9191
kafka:9092

docker tag xuannam01/discovery-server:0.0.1 xuannam01/lineta-service:discovery-server-0.0.1
docker tag xuannam01/friend-service xuannam01/ lineta-service:friend-service-0.0.1
docker tag xuannam01/auth-service xuannam01/ lineta-service:auth-service-0.0.1
docker tag xuannam01/interaction-service xuannam01/ lineta-service:interaction-service-0.0.1
docker tag xuannam01/notification-service xuannam01/ lineta-service:notification-service-0.0.1

docker image push xuannam01/discovery-server:0.0.1
docker image push xuannam01/friend-service:0.0.1
docker image push xuannam01/auth-service:0.0.1
docker image push xuannam01/interaction-service:0.0.1
docker image push xuannam01/notification-service:0.0.1
docker image push xuannam01/message-service:0.0.1
docker image push xuannam01/api-gateway:0.0.1
