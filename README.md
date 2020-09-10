## 1. Run bot with docker-compose
```
docker-compose up
```
## OR
## 2. Run container with mysql
```
docker run --name mysql -p 3306:3306 -v /path/to/clojure_bot_db:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -d mysql
```
## 3. Run migrations
## 4. Run bot
```
lein start
```
## 5. Run test
```
lein test
```