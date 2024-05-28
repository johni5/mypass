create schema mypass;

create table position
(
    id       INT primary key auto_increment not null,
    name     VARCHAR(200) not null,
    code     VARCHAR(200) not null,
    category VARCHAR(200),
    unique key name_uniq (name)
);