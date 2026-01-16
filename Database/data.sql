
\c mini_dish_db

insert into Dish (id, name, dish_type) 
values  (1, 'Salade fraîche', 'START'),
        (2, 'Poulet grillé', 'MAIN'),
        (3, 'Riz aux légumes', 'MAIN'),
        (4, 'Gâteau au chocolat', 'DESSERT'),
        (5, 'Salade de fruits', 'DESSERT');

insert into Ingredient (id, name, price, category, id_dish)
VALUES  (1, 'Laitue', 800.00, 'VEGETABLE', 1),
        (2, 'Tomate', 600.00, 'VEGETABLE', 1),
        (3, 'Poulet', 4500.00, 'ANIMAL', 2),
        (4, 'Chocolat', 3000.00, 'OTHER', 4),
        (5, 'Beurre', 2500.00, 'DAIRY', 4);

SELECT setval('dish_id_seq', (SELECT MAX(id) FROM Dish));
SELECT setval('ingredient_id_seq', (SELECT MAX(id) FROM Ingredient));

update dish
    set price = 2000.0 where id = 1;

update dish
    set price = 6000.0 where id = 2;