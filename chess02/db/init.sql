drop database if exists chess;
create database chess character set utf8mb4;

use chess;

create table user(
    id int primary key,
    password varchar(20) not null,
    integral int
);

insert into user(id, password, integral) values (1, '1', 3);
insert into user(id, password, integral) values (2, '2', 0);
insert into user(id, password, integral) values (3, '3', 4);
insert into user(id, password, integral) values (4, '4', 2);