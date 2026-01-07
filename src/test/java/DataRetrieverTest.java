import com.school.hei.model.Dish;
import com.school.hei.model.Ingredient;
import com.school.hei.repository.DataRetriever;
import com.school.hei.type.CategoryEnum;
import com.school.hei.type.DishTypeEnum;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataRetrieverTest {

    private static  DataRetriever dataRetriever;

    @BeforeAll
    public static void setupClass() {
        dataRetriever = new DataRetriever();
        resetAndInitializeDatabase();
    }

    @AfterAll
    public static void tearDownClass() {
        resetAndInitializeDatabase();
    }

    private static void resetAndInitializeDatabase() {
        Connection connection = null;
        try {
            connection = dataRetriever.dbConnection.getDBConnection();
            connection.setAutoCommit(false);

            connection.prepareStatement("delete from ingredient").executeUpdate();
            connection.prepareStatement("delete from dish").executeUpdate();

            insertInitialData(connection);
            connection.commit();
            connection.close();
            System.out.println("Database reset successfully");
        }
        catch (SQLException e) {
            try {
                connection.rollback();
                System.out.println("Error resetting database, rolled back");
            }
            catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    private static void insertInitialData(Connection connection) throws SQLException {
        String insertDishSql = "insert into dish (id, name, dish_type) values (?, ?, ?::dish_types)";
        PreparedStatement dishStmt = connection.prepareStatement(insertDishSql);

        dishStmt.setInt(1, 1);
        dishStmt.setString(2, "Salade fraîche");
        dishStmt.setString(3, "START");
        dishStmt.executeUpdate();

        dishStmt.setInt(1, 2);
        dishStmt.setString(2, "Poulet grillé");
        dishStmt.setString(3, "MAIN");
        dishStmt.executeUpdate();

        dishStmt.setInt(1, 3);
        dishStmt.setString(2, "Riz aux légumes");
        dishStmt.setString(3, "MAIN");
        dishStmt.executeUpdate();

        dishStmt.setInt(1, 4);
        dishStmt.setString(2, "Gâteau au chocolat");
        dishStmt.setString(3, "DESSERT");
        dishStmt.executeUpdate();

        dishStmt.setInt(1, 5);
        dishStmt.setString(2, "Salade de fruits");
        dishStmt.setString(3, "DESSERT");
        dishStmt.executeUpdate();

        String insertIngredientSql =
                "insert into ingredient (id, name, price, category, id_dish) values (?, ?, ?, ?::categories, ?)";
        PreparedStatement ingStmt = connection.prepareStatement(insertIngredientSql);

        ingStmt.setInt(1, 1);
        ingStmt.setString(2, "Laitue");
        ingStmt.setDouble(3, 800.00);
        ingStmt.setString(4, "VEGETABLE");
        ingStmt.setInt(5, 1);
        ingStmt.executeUpdate();

        ingStmt.setInt(1, 2);
        ingStmt.setString(2, "Tomate");
        ingStmt.setDouble(3, 600.00);
        ingStmt.setString(4, "VEGETABLE");
        ingStmt.setInt(5, 1);
        ingStmt.executeUpdate();

        ingStmt.setInt(1, 3);
        ingStmt.setString(2, "Poulet");
        ingStmt.setDouble(3, 4500.00);
        ingStmt.setString(4, "ANIMAL");
        ingStmt.setInt(5, 2);
        ingStmt.executeUpdate();

        ingStmt.setInt(1, 4);
        ingStmt.setString(2, "Chocolat");
        ingStmt.setDouble(3, 3000.00);
        ingStmt.setString(4, "OTHER");
        ingStmt.setInt(5, 4);
        ingStmt.executeUpdate();

        ingStmt.setInt(1, 5);
        ingStmt.setString(2, "Beurre");
        ingStmt.setDouble(3, 2500.00);
        ingStmt.setString(4, "DAIRY");
        ingStmt.setInt(5, 4);
        ingStmt.executeUpdate();

        connection.prepareStatement("SELECT setval('dish_id_seq', 5, true)").execute();
        connection.prepareStatement("SELECT setval('ingredient_id_seq', 5, true)").execute();
    }

    @Test
    @Order(1)
    @DisplayName("a) findDishById with id=1 should return Salade fraîche with 2 ingredients (Laitue et Tomate)")
    public void testFindDishById_1() {
        Dish dish = dataRetriever.findDishById(1);

        assertNotNull(dish);
        assertEquals("Salade fraîche", dish.getName());
        assertNotNull(dish.getIngredients());
        assertEquals(2, dish.getIngredients().size());

        List<String> ingredientNames = dish.getIngredients().stream()
                .map(Ingredient::getName)
                .toList();

        System.out.println("Test a) PASSED\nDish: " + dish.getName() + "\nIngredients: " + ingredientNames);
    }

    @Test
    @Order(2)
    @DisplayName("b) findDishById with id=999 should throw RuntimeException")
    public void testFindDishById_999() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> dataRetriever.findDishById(999));

        assertNotNull(exception);
        System.out.println("Test b) PASSED\nRuntimeException thrown - " + exception.getMessage());
    }

    @Test
    @Order(3)
    @DisplayName("c) findIngredients with page=2, size=2 should return Poulet and Chocolat")
    public void testFindIngredients_P2S2() {
        List<Ingredient> ingredients = dataRetriever.findIngredients(2, 2);

        assertNotNull(ingredients);
        assertEquals(2, ingredients.size());

        List<String> ingredientNames = ingredients.stream()
                .map(Ingredient::getName)
                .toList();
        assertTrue(ingredientNames.contains("Poulet"));
        assertTrue(ingredientNames.contains("Chocolat"));

        System.out.println("Test c) PASSED\nIngredients: " + ingredientNames);
    }

    @Test
    @Order(4)
    @DisplayName("d) findIngredients with page=3, size=5 should return empty list")
    public void testFindIngredients_P3S5() {
        List<Ingredient> ingredients = dataRetriever.findIngredients(3, 5);

        assertNotNull(ingredients);
        assertTrue(ingredients.isEmpty());

        System.out.println("Test d) PASSED. Empty list returned");
    }

    @Test
    @Order(5)
    @DisplayName("e) findDishesByIngredientName with 'eur' should return Gâteau au chocolat")
    public void testFindDishesByIngredientName() {
        List<Dish> dishes = dataRetriever.findDishesByIngredientName("eur");

        assertNotNull(dishes);
        assertFalse(dishes.isEmpty());
        assertEquals(1, dishes.size());

        Dish dish = dishes.getFirst();
        assertEquals("Gâteau au chocolat", dish.getName());
        assertEquals(DishTypeEnum.DESSERT, dish.getDishType());

        System.out.println("Test e) PASSED\nDish: " + dish.getName());
    }

    @Test
    @Order(6)
    @DisplayName("f) findIngredientsByCriteria with category=VEGETABLE should return Laitue and Tomate")
    public void testFindIngredientsByCriteria_OnlyCategory() {
        List<Ingredient> ingredients = dataRetriever.findIngredientsByCriteria(
                null, CategoryEnum.VEGETABLE, null, 1, 10);

        assertNotNull(ingredients);
        assertEquals(2, ingredients.size());

        List<String> ingredientNames = ingredients.stream()
                .map(Ingredient::getName)
                .toList();
        assertTrue(ingredientNames.contains("Laitue"));
        assertTrue(ingredientNames.contains("Tomate"));

        System.out.println("Test f) PASSED\nIngredients: " + ingredientNames);
    }

    @Test
    @Order(7)
    @DisplayName("g) findIngredientsByCriteria with ingredientName='cho' and dishName='Sal' should return empty list")
    public void testFindIngredientsByCriteria_ChoAndSal() {
        List<Ingredient> ingredients = dataRetriever.findIngredientsByCriteria(
                "cho", null, "Sal", 1, 10);

        assertNotNull(ingredients);
        assertTrue(ingredients.isEmpty());

        System.out.println("Test g) PASSED. Empty list returned");
    }

    @Test
    @Order(8)
    @DisplayName("h) findIngredientsByCriteria with ingredientName='cho' and dishName='gâteau' should return Chocolat")
    public void testFindIngredientsByCriteria_ChoAndGateau() {
        List<Ingredient> ingredients = dataRetriever.findIngredientsByCriteria(
                "cho", null, "gâteau", 1, 10);

        assertNotNull(ingredients);
        assertEquals(1, ingredients.size());
        assertEquals("Chocolat", ingredients.getFirst().getName());
        assertEquals(CategoryEnum.OTHER, ingredients.getFirst().getCategory());

        System.out.println("Test h) PASSED\nIngredients: " + ingredients.getFirst().getName());
    }

    @Test
    @Order(9)
    @DisplayName("i) createIngredients with Fromage and Oignon should create both successfully")
    public void testCreateIngredients_FromageAndOignon() {
        Ingredient fromage = new Ingredient("Fromage", 1200.0, CategoryEnum.DAIRY);
        Ingredient oignon = new Ingredient("Oignon", 500.0, CategoryEnum.VEGETABLE);

        List<Ingredient> createdIngredients = dataRetriever.createIngredients(
                new ArrayList<>(Arrays.asList(fromage, oignon)));

        assertNotNull(createdIngredients);
        assertEquals(2, createdIngredients.size());

        List<String> ingredientNames = createdIngredients.stream()
                .map(Ingredient::getName)
                .toList();
        assertTrue(ingredientNames.contains("Fromage"));
        assertTrue(ingredientNames.contains("Oignon"));

        System.out.println("Test i) PASSED\nIngredients created: " + createdIngredients.stream().map(Ingredient::getName).toList());
    }

    @Test
    @Order(10)
    @DisplayName("j) createIngredients with Carotte and Laitue should throw exception (Laitue exists)")
    public void testCreateIngredients_CarotteAndLaitue() {
        Ingredient carotte = new Ingredient("Carotte", 2000.0, CategoryEnum.VEGETABLE);
        Ingredient laitue = new Ingredient("Laitue", 2000.0, CategoryEnum.VEGETABLE);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            dataRetriever.createIngredients(new ArrayList<>(Arrays.asList(carotte, laitue))));

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Laitue") ||
                exception.getMessage().contains("already exists"));

        System.out.println("Test j) PASSED\nException thrown - " + exception.getMessage());
    }

    @Test
    @Order(11)
    @DisplayName("k) saveDish with new dish 'Soupe de légumes' and Oignon ingredient")
    public void testSaveDish_NewDish() {
        Ingredient oignon = dataRetriever.findIngredientByName("Oignon");
        Dish newDish = new Dish(
                "Soupe de légumes",
                DishTypeEnum.START,
                new ArrayList<>(Collections.singletonList(oignon)));

        Dish savedDish = dataRetriever.saveDish(newDish);

        assertNotNull(savedDish);
        assertNotNull(savedDish.getId());
        assertEquals("Soupe de légumes", savedDish.getName());
        assertEquals(DishTypeEnum.START, savedDish.getDishType());
        assertNotNull(savedDish.getIngredients());
        assertEquals(1, savedDish.getIngredients().size());
        assertEquals("Oignon", savedDish.getIngredients().getFirst().getName());

        System.out.println("Test k) PASSED\nDish saved: " + savedDish.getName() + "\nIngredients: " + savedDish.getIngredients().getFirst().getName());
    }

    @Test
    @Order(12)
    @DisplayName("l) saveDish update dish id=1 with 4 ingredients")
    public void testSaveDish_UpdateWithFourIngredients() {
        Ingredient oignon = dataRetriever.findIngredientByName("Oignon");
        Ingredient laitue = dataRetriever.findIngredientByName("Laitue");
        Ingredient tomate = dataRetriever.findIngredientByName("Tomate");
        Ingredient fromage = dataRetriever.findIngredientByName("Fromage");

        Dish updatedDish = new Dish(
                1,
                "Salade fraîche",
                DishTypeEnum.START,
                new ArrayList<>(Arrays.asList(oignon, laitue, tomate, fromage)));

        Dish savedDish = dataRetriever.saveDish(updatedDish);

        assertNotNull(savedDish);
        assertEquals(1, savedDish.getId());
        assertEquals("Salade fraîche", savedDish.getName());
        assertEquals(DishTypeEnum.START, savedDish.getDishType());
        assertNotNull(savedDish.getIngredients());
        assertEquals(4, savedDish.getIngredients().size());

        List<String> ingredientNames = savedDish.getIngredients().stream()
                .map(Ingredient::getName)
                .toList();
        assertTrue(ingredientNames.contains("Oignon"));
        assertTrue(ingredientNames.contains("Laitue"));
        assertTrue(ingredientNames.contains("Tomate"));
        assertTrue(ingredientNames.contains("Fromage"));

        System.out.println("Test k) PASSED\nDish saved: " + savedDish.getName() +
                "\nIngredients: " + savedDish.getIngredients().stream().map(Ingredient::getName).toList());
    }

    @Test
    @Order(13)
    @DisplayName("m) saveDish update dish id=1 with only Fromage ingredient")
    public void testSaveDish_UpdateWithOnlyFromage() {
        Ingredient fromage = dataRetriever.findIngredientByName("Fromage");

        Dish updatedDish = new Dish(
                1,
                "Salade de fromage",
                DishTypeEnum.START,
                new ArrayList<>(Collections.singletonList(fromage)));

        Dish savedDish = dataRetriever.saveDish(updatedDish);

        assertNotNull(savedDish);
        assertEquals(1, savedDish.getId());
        assertEquals("Salade de fromage", savedDish.getName());
        assertEquals(DishTypeEnum.START, savedDish.getDishType());
        assertNotNull(savedDish.getIngredients());
        assertEquals(1, savedDish.getIngredients().size());
        assertEquals("Fromage", savedDish.getIngredients().getFirst().getName());

        System.out.println("Test k) PASSED\nDish saved: " + savedDish.getName() +
                "\nIngredients: " + savedDish.getIngredients().stream().map(Ingredient::getName).toList());
    }
}
