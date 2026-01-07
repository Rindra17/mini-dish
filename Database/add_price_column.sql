
\c mini_dish_db

do $$
Begin
    if not exists (
            select 1 from information_schema.columns
            where table_name = 'dish'
            and column_name = 'price'
        )
        then
            alter table dish add column price numeric(10, 2);
        end if;
end $$;

UPDATE dish SET price = 2000.00 WHERE name = 'Salade fraîche';
UPDATE dish SET price = 6000.00 WHERE name = 'Poulet grillé';
UPDATE dish SET price = NULL WHERE name = 'Riz aux légumes';
UPDATE dish SET price = NULL WHERE name = 'Gâteau au chocolat';
UPDATE dish SET price = NULL WHERE name = 'Salade de fruits';