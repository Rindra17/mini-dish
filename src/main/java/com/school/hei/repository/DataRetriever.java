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
import java.util.stream.Collectors;

public class DataRetriever {
    public final DBConnection dbConnection = new DBConnection();

    public Dish findDishById(Integer id) {
        String sql = """
                select d.id as dish_id, d.name as dish_name, dish_type, d.price as dish_price
                from dish d where d.id = ?;
                """;
        Connection con = null;
        PreparedStatement dishStm;
        ResultSet dishRs;

        try {
            con = dbConnection.getDBConnection();
            dishStm = con.prepareStatement(sql);
            dishStm.setInt(1, id);
            dishRs = dishStm.executeQuery();

            Dish dish = new Dish();
            if (dishRs.next()) {
                dish.setId(dishRs.getInt("dish_id"));
                dish.setName(dishRs.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(dishRs.getString("dish_type")));
                dish.setPrice(dishRs.getObject("dish_price") == null
                        ? null : dishRs.getDouble("dish_price"));
                dish.setIngredients(findIngredientByDishId(id));
                return dish;
            }
            dbConnection.closeDBConnection(con);
            throw new RuntimeException("Dish not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Ingredient> findIngredientByDishId(Integer id) {
        String sql = """
                select i.id, i.name, i.price, i.category
                from ingredient i where id_dish = ?;
                """;
        Connection con = null;
        PreparedStatement statement;
        ResultSet resultSet;
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            con = dbConnection.getDBConnection();
            statement = con.prepareStatement(sql);
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
                ingredients.add(ingredient);
            }
            dbConnection.closeDBConnection(con);
            return ingredients;
        } catch (SQLException e) {
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return List.of();
        }
        List<Ingredient> savedIngredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection conn = dbConnection.getDBConnection();
        try {
            conn.setAutoCommit(false);
            String insertSql = """
                        INSERT INTO ingredient (id, name, category, price, required_quantity)
                        VALUES (?, ?, ?::categories, ?, ?)
                        RETURNING id
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Ingredient ingredient : newIngredients) {
                    if (ingredient.getId() != null) {
                        ps.setInt(1, ingredient.getId());
                    } else {
                        ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                    }
                    ps.setString(2, ingredient.getName());
                    ps.setString(3, ingredient.getCategory().name());
                    ps.setDouble(4, ingredient.getPrice());
                    if (ingredient.getQuantity() != null) {
                        ps.setDouble(5, ingredient.getQuantity());
                    }else {
                        ps.setNull(5, Types.DOUBLE);
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        int generatedId = rs.getInt(1);
                        ingredient.setId(generatedId);
                        savedIngredients.add(ingredient);
                    }
                }
                conn.commit();
                return savedIngredients;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeDBConnection(conn);
        }
    }

    public Dish saveDish(Dish toSave) {
        String upsertDishSql = """
                    INSERT INTO dish (id, price, name, dish_type)
                    VALUES (?, ?, ?, ?::dish_types)
                    ON CONFLICT (id) DO UPDATE
                    SET name = excluded.name,
                        dish_type = excluded.dish_type,
                        price = excluded.price
                    RETURNING id
                """;

        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);
            Integer dishId;
            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "dish", "id"));
                }
                if (toSave.getPrice() != null) {
                    ps.setDouble(2, toSave.getPrice());
                } else {
                    ps.setNull(2, Types.DOUBLE);
                }
                ps.setString(3, toSave.getName());
                ps.setString(4, toSave.getDishType().name());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishId = rs.getInt(1);
                }
            }

            List<Ingredient> newIngredients = toSave.getIngredients();
            detachIngredients(conn, dishId, newIngredients);
            attachIngredients(conn, dishId, newIngredients);

            conn.commit();
            return findDishById(dishId);
        } catch (SQLException e) {
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

        Connection con;
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Dish> findDishesByIngredientName(String ingredientName) {
        String searchSql =
                """
                            select d.id as dish_id, d.name as dish_name, d.dish_type, d.price, i.name as ing_name
                            from Dish d
                            join Ingredient i on d.id = i.id_dish
                            where i.name ilike ?
                        """;

        Connection con;
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
        } catch (SQLException e) {
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
        } else {
            throw new IllegalArgumentException("Page and size must be greater than 0");
        }

        Connection con;
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
        } catch (SQLException e) {
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

    private void detachIngredients(Connection con, Integer dishId, List<Ingredient> ingredients) throws SQLException{
        if (ingredients == null || ingredients.isEmpty()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE ingredient SET id_dish = NULL WHERE id_dish = ?")) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }
            return;
        }

        String baseSql = """
                    UPDATE ingredient
                    SET id_dish = NULL
                    WHERE id_dish = ? AND id NOT IN (%s)
                """;

        String inClause = ingredients.stream()
                .map(i -> "?")
                .collect(Collectors.joining(","));

        String sql = String.format(baseSql, inClause);

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            int index = 2;
            for (Ingredient ingredient : ingredients) {
                ps.setInt(index++, ingredient.getId());
            }
            ps.executeUpdate();
        }
    }

    private void attachIngredients(Connection con, Integer dishId, List<Ingredient> ingredients) throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }

        String attachSql = """
                    UPDATE ingredient
                    SET id_dish = ?
                    WHERE id = ?
                """;

        try (PreparedStatement ps = con.prepareStatement(attachSql)) {
            for (Ingredient ingredient : ingredients) {
                ps.setInt(1, dishId);
                ps.setInt(2, ingredient.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private int getNextSerialValue(Connection con, String tableName, String columnNane) throws SQLException{
        String sequenceName = getSerialSequenceName(con, tableName, columnNane);
        if (sequenceName == null) {
            throw new IllegalArgumentException(
                    "Any sequence found for " + tableName + "." + columnNane
            );
        }
        updateSeqenceNextValue(con, tableName, columnNane, sequenceName);

        String nextValSql = "select nextval(?)";
        try (PreparedStatement ps = con.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void updateSeqenceNextValue(Connection con, String tableName, String columnNane, String sequenceName) throws SQLException {
        String setValSql = String.format(
                "select setval('%s', (select coalesce(max(%s), 0) from %s))",
                sequenceName, columnNane, tableName
        );

        try (PreparedStatement ps = con.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }

    private String getSerialSequenceName(Connection con, String tableName, String columnNane) throws SQLException{
        String sql = "select pg_get_serial_sequence(?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnNane);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

}
