create table product
(
    id          serial       not null primary key,
    name        varchar(255) not null,
    category_id integer      not null
);

create table hotel
(
    id          serial       not null primary key,
    name        varchar(255) not null,
    rating      integer      not null,
    description varchar      not null
);
