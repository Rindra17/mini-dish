package com.school.hei.repository;

import com.school.hei.DataBase.DBConnection;
import com.school.hei.model.Dish;
import com.school.hei.model.Ingredient;
import com.school.hei.type.CategoryEnum;
import com.school.hei.type.DishTypeEnum;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataRetriever {
    public final DBConnection dbConnection = new DBConnection();

    public Dish findDishById(Integer id) {
        String dishSql = """
           select d.id as dish_id, d.name as dish_name, d.dish_type
           from dish d
           where id = ?
        """;

        String ingredientSql = """
            select i.id as ing_id, i.name as ing_name, i.price as ing_price, i.category as ing_category
            from ingredient i
            where i.id_dish = ?
            order by i.id
        """;

        Connection con;
        PreparedStatement dishStmt;
        ResultSet dishRs;
        PreparedStatement ingredientStmt;
        ResultSet ingredientRs;

        try {
           con = dbConnection.getDBConnection();
           dishStmt = con.prepareStatement(dishSql);
           dishStmt.setInt(1, id);
           dishRs = dishStmt.executeQuery();
           if (!dishRs.next()) {
               throw new RuntimeException("Dish with ID " + id +" not found");
           }

           Dish dish = new Dish();
           dish.setId(dishRs.getInt("dish_id"));
           dish.setName(dishRs.getString("dish_name"));
           dish.setDishType(DishTypeEnum.valueOf(dishRs.getString("dish_type")));

           ingredientStmt = con.prepareStatement(ingredientSql);
           ingredientStmt.setInt(1, id);
           ingredientRs = ingredientStmt.executeQuery();

           List<Ingredient> ingredients = new ArrayList<>();
           while (ingredientRs.next()) {
               Ingredient ingredient = new Ingredient();
               ingredient.setId(ingredientRs.getInt("ing_id"));
               ingredient.setName(ingredientRs.getString("ing_name"));
               ingredient.setPrice(ingredientRs.getDouble("ing_price"));
               ingredient.setCategory(CategoryEnum.valueOf(ingredientRs.getString("ing_category")));

               ingredients.add(ingredient);
           }
           dish.setIngredients(ingredients);
           dbConnection.closeDBConnection(con);
           return dish;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {
        String sql = """
            select i.id as ing_id, i.name as ing_name, i.price as ing_price, i.category as ing_category, i.id_dish
            from ingredient i
            order by i.id
            limit ? offset ?
        """;

        int offset = (page - 1) * size;
        Connection con;
        PreparedStatement ingredientStmt;
        ResultSet ingredientRs;

        try {
            con = dbConnection.getDBConnection();
            ingredientStmt = con.prepareStatement(sql);
            ingredientStmt.setInt(1, size);
            ingredientStmt.setInt(2, offset);
            ingredientRs = ingredientStmt.executeQuery();
            List<Ingredient> ingredients = new ArrayList<>();
            while (ingredientRs.next()) {
                ingredients.add(resultsetToIngredient(ingredientRs));
            }
            dbConnection.closeDBConnection(con);
            return ingredients;
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            throw new IllegalArgumentException("Ingredients list cannot be empty");
        }

        for (Ingredient newIngredient : newIngredients) {
            if (newIngredient == null) {
                throw new IllegalArgumentException("Ingredients list cannot contain null values");
            }
            if (newIngredient.getName() == null || newIngredient.getName().isEmpty()
                    || newIngredient.getCategory() == null) {
                throw new IllegalArgumentException("Ingredients name or category cannot be empty or null");
            }
            if (newIngredient.getPrice() < 0 || newIngredient.getPrice() == null) {
                throw new IllegalArgumentException("Ingredients price cannot be empty or null");
            }
        }

        Set<String> ingredientsName = new HashSet<>();
        for (Ingredient newIngredient : newIngredients) {
            if (!ingredientsName.add(newIngredient.getName().toLowerCase())) {
                throw new IllegalArgumentException("Duplicate ingredient provided in list: " + newIngredient.getName());
            }
        }

        String insertSql =
        """
            insert into ingredient (name, price, category, id_dish) values (?, ?, ?::categories, ?)
        """;

        String searchSql =
        """
            select i.id as ing_id, i.name as ing_name from ingredient i where lower(i.name) = lower(?)
        """;

        Connection con = null;
        PreparedStatement insertStm;
        PreparedStatement searchStm;
        ResultSet insertRs;
        ResultSet searchRs;

        try {
            con = dbConnection.getDBConnection();
            con.setAutoCommit(false);
            searchStm = con.prepareStatement(searchSql);
            ResultSet generatedKeys;

            for (Ingredient newIngredient : newIngredients) {
                searchStm.setString(1, newIngredient.getName());
                searchRs = searchStm.executeQuery();
                if (searchRs.next()) {
                    con.rollback();
                    throw new RuntimeException("Ingredient with name " + newIngredient.getName() + " already exists");
                }
            }

            insertStm = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            for (Ingredient newIngredient : newIngredients) {
                insertStm.setString(1, newIngredient.getName());
                insertStm.setDouble(2, newIngredient.getPrice());
                insertStm.setString(3, newIngredient.getCategory().name());

                if (newIngredient.getDish() != null) {
                    insertStm.setInt(4, newIngredient.getDish().getId());
                }
                else {
                    insertStm.setNull(4, Types.INTEGER);
                }
                insertStm.addBatch();
            }
            int[] batchResults = insertStm.executeBatch();

            for (int i = 0; i < batchResults.length; i++) {
                if (batchResults[i] == Statement.EXECUTE_FAILED) {
                    con.rollback();
                    throw new RuntimeException("Error while creating: " + newIngredients.get(i).getName() + ". \nNo ingredients inserted");
                }
            }

            generatedKeys = insertStm.getGeneratedKeys();
            List<Ingredient> createdIngredients = new ArrayList<>();
            int index = 0;

            while (generatedKeys.next()) {
                Ingredient createdIngredient = new Ingredient();
                createdIngredient.setId(generatedKeys.getInt(1));
                createdIngredient.setName(newIngredients.get(index).getName());
                createdIngredient.setPrice(newIngredients.get(index).getPrice());
                createdIngredient.setCategory(newIngredients.get(index).getCategory());
                createdIngredients.add(createdIngredient);
                index++;
            }
            con.commit();
            dbConnection.closeDBConnection(con);
            return createdIngredients;
        }
        catch (SQLException e) {
            try {
                if (!con.isClosed()) {
                    con.rollback();
                    System.out.println("Error, rolling back");
                }
            }
            catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    public Dish saveDish(Dish dishToSave) {
        if (dishToSave == null) {
            throw new IllegalArgumentException("Dish cannot be null");
        }

        if (dishToSave.getName() == null || dishToSave.getName().isEmpty()
                || dishToSave.getDishType() == null) {
            throw new IllegalArgumentException("Dish name or type cannot be empty or null");
        }
        if (dishToSave.getId() != null && dishToSave.getId() <= 0) {
            throw new IllegalArgumentException("Dish id cannot be negative");
        }

        String searchSql =
        """
            select id as dish_id from dish where id = ?
        """;

        String updateDishSql =
        """
            update dish set name = ?, dish_type = ?::dish_types where id = ?
        """;

        String createDishSql =
        """
            insert into dish (name, dish_type) values (?, ?::dish_types) returning id
       """;

        String dissociateSql =
        """
            update ingredient set id_dish = null where id_dish = ?
        """;

        String associateSql =
        """
            update ingredient set id_dish = ? where id = ?
        """;

        Connection con = null;
        PreparedStatement searchDishStm;
        PreparedStatement updateStm;
        PreparedStatement createStm;
        PreparedStatement associateStm;
        PreparedStatement dissociateStm;
        ResultSet searchDishRs;
        ResultSet createDishRs;

        try {
            con = dbConnection.getDBConnection();
            con.setAutoCommit(false);
            Integer dishId = null;
            boolean isUpdate = false;

            if (dishToSave.getId() != null) {
                searchDishStm = con.prepareStatement(searchSql);
                searchDishStm.setInt(1, dishToSave.getId());
                searchDishRs = searchDishStm.executeQuery();
                if (searchDishRs.next()) {
                    dishId = searchDishRs.getInt("dish_id");
                    isUpdate = true;
                }
            }

            if (isUpdate) {
                updateStm = con.prepareStatement(updateDishSql);
                updateStm.setString(1, dishToSave.getName());
                updateStm.setString(2, dishToSave.getDishType().name());
                updateStm.setInt(3, dishToSave.getId());
                int result = updateStm.executeUpdate();
                if (result == 0) {
                    throw new RuntimeException("Error while updating: " + dishToSave.getName());
                }
            }
            else {
                createStm = con.prepareStatement(createDishSql, Statement.RETURN_GENERATED_KEYS);
                createStm.setString(1, dishToSave.getName());
                createStm.setString(2, dishToSave.getDishType().name());
                createStm.executeUpdate();
                createDishRs = createStm.getGeneratedKeys();
                if (createDishRs.next()) {
                    dishId = createDishRs.getInt(1);
                }
                else {
                    throw new RuntimeException("Error while creating: " + dishToSave.getName());
                }
            }

            if (isUpdate) {
                dissociateStm = con.prepareStatement(dissociateSql);
                dissociateStm.setInt(1, dishId);
                dissociateStm.executeUpdate();
            }

            if (dishToSave.getIngredients() != null && !dishToSave.getIngredients().isEmpty()) {
                associateStm = con.prepareStatement(associateSql);
                for (Ingredient ingredient : dishToSave.getIngredients()) {
                    associateStm.setInt(1, dishId);
                    associateStm.setInt(2, ingredient.getId());
                    associateStm.addBatch();
                }

                int[] batchResults = associateStm.executeBatch();
                for (int res : batchResults) {
                    if (res == Statement.EXECUTE_FAILED) {
                        throw new RuntimeException("Error while associating ingredient to the dish: " + dishToSave.getName());
                    }
                }
            }

            con.commit();
            dbConnection.closeDBConnection(con);
            return findDishById(dishId);
        }
        catch (SQLException e) {
            try {
                if (!con.isClosed()) {
                    con.rollback();
                    System.out.println("Error, rolling back");
                }
            }
            catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    public Ingredient findIngredientByName(String ingredientName) {
        String searchSql =
        """
            select i.id as ing_id, i.name as ing_name, i.name as ing_name, i.price as ing_price, i.category as ing_category, i.id_dish as id_dish
            from ingredient i
            where lower(i.name) = lower(?)
            order by ing_id
        """;

        Connection con = null;
        PreparedStatement searchStm;
        ResultSet searchRs;

        try {
            con = dbConnection.getDBConnection();
            searchStm = con.prepareStatement(searchSql);
            searchStm.setString(1, ingredientName);
            searchRs = searchStm.executeQuery();
            if (!searchRs.next()) {
                return null;
            }
            dbConnection.closeDBConnection(con);
            return resultsetToIngredient(searchRs);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<Dish> findDishesByIngredientName(String ingredientName) {
        String searchSql =
                """
                    select d.id as dish_id, d.name as dish_name, d.dish_type, i.name as ing_name
                    from Dish d
                    join Ingredient i on d.id = i.id_dish
                    where i.name ilike ?
                """;

        Connection con = null;
        PreparedStatement searchStm;
        ResultSet searchRs;

        try {
            con = dbConnection.getDBConnection();
            searchStm = con.prepareStatement(searchSql);
            searchStm.setString(1, "%" + ingredientName + "%");
            searchRs = searchStm.executeQuery();
            List<Dish> dishes = new ArrayList<>();
            while (searchRs.next()) {
                dishes.add(findDishById(searchRs.getInt("dish_id")));
            }
            dbConnection.closeDBConnection(con);
            return dishes;
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) {
        StringBuilder searchSql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        searchSql.append(
        """
            select i.id as ing_id, i.name as ing_name, i.price as ing_price,
                i.category as ing_category, i.id_dish, d.name as dish_name
            from ingredient i
            join public.dish d on d.id = i.id_dish
            where 1=1
        """);

        if (ingredientName != null && !ingredientName.isEmpty()) {
            searchSql.append("and i.name ilike ? ");
            params.add("%" + ingredientName + "%");
        }

        if (category != null) {
            searchSql.append("and i.category = ?::categories ");
            params.add(category.name());
        }

        if (dishName != null && !dishName.isEmpty()) {
            searchSql.append("and d.name ilike ? ");
            params.add("%" + dishName + "%");
        }

        if (page > 0 && size > 0) {
            int offset = (page - 1) * size;
            searchSql.append("limit ? offset ? ");
            params.add(size);
            params.add(offset);
        }
        else {
            throw new IllegalArgumentException("Page and size must be greater than 0");
        }

        Connection con = null;
        PreparedStatement searchStm;
        ResultSet searchRs;

        try {
            con = dbConnection.getDBConnection();
            searchStm = con.prepareStatement(searchSql.toString());
            int paramIndex = 1;
            for (Object param : params) {
                searchStm.setObject(paramIndex, param);
                paramIndex++;
            }
            searchRs = searchStm.executeQuery();
            List<Ingredient> ingredients = new ArrayList<>();
            while (searchRs.next()) {
                ingredients.add(resultsetToIngredient(searchRs));
            }
            dbConnection.closeDBConnection(con);
            return ingredients;
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Ingredient resultsetToIngredient(ResultSet ingredientRs) throws SQLException {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(ingredientRs.getInt("ing_id"));
        ingredient.setName(ingredientRs.getString("ing_name"));
        ingredient.setPrice(ingredientRs.getDouble("ing_price"));
        ingredient.setCategory(CategoryEnum.valueOf(ingredientRs.getString("ing_category")));
        if (ingredientRs.getInt("id_dish") > 0) {
            ingredient.setDish(findDishById(ingredientRs.getInt("id_dish")));
        }
        return ingredient;
    }
}
