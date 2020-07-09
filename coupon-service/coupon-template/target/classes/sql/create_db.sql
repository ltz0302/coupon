-- 登录 MySQL 服务器
mysql -hlocalhost -uroot -ppassword

-- 创建数据库 coupon
CREATE DATABASE IF NOT EXISTS coupon;

-- 登录 MySQL 服务器, 并进入到 imooc_coupon_data 数据库中
mysql -hlocalhost -uroot -ppassword -Dcoupon

--启动redis
redis-server.exe redis.windows.conf