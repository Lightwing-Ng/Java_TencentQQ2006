CREATE DATABASE IF NOT EXISTS TencentQQ;

USE TencentQQ;

/* 用户表 */
CREATE TABLE IF NOT EXISTS `user` (
    user_id VARCHAR(80) NOT NULL, /* 用户Id  */
    user_pwd VARCHAR(25) NOT NULL, /* 用户密码 */
    user_name VARCHAR(80) NOT NULL, /* 用户名 */
    user_icon VARCHAR(100) NOT NULL, /* 用户头像 */
    PRIMARY KEY (user_id)
);


/* 用户好友表Id1和Id2互为好友 */
CREATE TABLE IF NOT EXISTS `friend` (
    user_id1 VARCHAR(80) NOT NULL, /* 用户Id1  */
    user_id2 VARCHAR(80) NOT NULL, /* 用户Id2  */
    PRIMARY KEY (user_id1, user_id2)
);

/* 用户表数据 */
INSERT INTO user VALUES ('111', '123', '关东升', '28');
INSERT INTO user VALUES ('222', '123', '赵1', '30');
INSERT INTO user VALUES ('333', '123', '赵2', '52');
INSERT INTO user VALUES ('888', '123', '赵3', '53');

/* 用户好友表Id1和Id2互为好友 */
INSERT INTO friend VALUES ('111', '222');
INSERT INTO friend VALUES ('111', '333');
INSERT INTO friend VALUES ('888', '111');
INSERT INTO friend VALUES ('222', '333');