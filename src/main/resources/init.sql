-- docker run --name pg -e POSTGRES_PASSWORD=test -d -p 5432:5432 postgres:15.3
create table product(
    id serial primary key not null
);

create table product_recommendation(
    id serial primary key not null,
    product_id1 integer references product not null,
    product_id2 integer references product not null
);
