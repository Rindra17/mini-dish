
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
    end $$;

create table if not exists dish_ingredient (
    id serial primary key,
    dish_id int references dish(id) on delete cascade not null,
    ingredient_id int references public.ingredient(id) on delete cascade not null,
    quantity decimal(10,2) not null,
    unit unit_type not null
)