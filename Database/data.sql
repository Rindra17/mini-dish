
\c mini_dish_db

insert into Dish (id, name, dish_type) 
values  (1, 'Salade fraîche', 'START'),
        (2, 'Poulet grillé', 'MAIN'),
        (3, 'Riz aux légumes', 'MAIN'),
        (4, 'Gâteau au chocolat', 'DESSERT'),
        (5, 'Salade de fruits', 'DESSERT');

insert into Ingredient (id, name, price, category)
VALUES  (1, 'Laitue', 800.00, 'VEGETABLE'),
        (2, 'Tomate', 600.00, 'VEGETABLE'),
        (3, 'Poulet', 4500.00, 'ANIMAL'),
        (4, 'Chocolat', 3000.00, 'OTHER'),
        (5, 'Beurre', 2500.00, 'DAIRY');

SELECT setval('dish_id_seq', (SELECT MAX(id) FROM Dish));
SELECT setval('ingredient_id_seq', (SELECT MAX(id) FROM Ingredient));

update dish
    set price = 2000.0 where id = 1;

update dish
    set price = 6000.0 where id = 2;

insert into dishingredient (id, dish_id, ingredient_id, quantity, unit)
    VALUES (1, 1, 1, 0.20, 'KG'),
           (2, 1, 2, 0.15, 'KG'),
           (3, 2, 3, 1.00, 'KG'),
           (4, 4, 4, 0.30, 'KG'),
           (5, 4, 5, 0.20, 'KG')

update dish
    set price = 3500.00 where id = 1;

update dish
    set price = 12000.00 where id = 2;

update dish
    set price = 8000.00 where id = 4;