## 1. Create volume
```
mkdir clojure_bot_db
```
## 2. Run container with mysql
```
docker run --name mysql -p 3306:3306 -v /path/to/clojure_bot_db:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -d mysql
```
## 3. Run bot
```
lein start
```