
\c mini_dish_db

alter table if exists ingredient
    drop constraint if exists ingredient_id_dish_fkey;

alter table if exists ingredient
    drop column if exists id_dish;

do $$
    begin
        if not exists (select 1 from pg_type where typname = 'unit_type') then
            create type unit_type as enum ('PCS', 'KG', 'L');
        end if;
        if not exists(select 1 from pg_type where typname = 'movement_type') then
            create type movement_type as enum ('IN', 'OUT');
        end if;
    end $$;

create table if not exists DishIngredient (
    id serial primary key,
    dish_id int references dish(id) on delete cascade not null,
    ingredient_id int references ingredient(id) on delete cascade not null,
    quantity decimal(10,2) not null,
    unit unit_type not null
);

create table if not exists StockMovement (
    id serial primary key,
    id_ingredient int references  ingredient(id) on delete cascade not null,
    quantity numeric(10,2),
    type movement_type,
    unit unit_type,
    creation_datetime timestamp
);