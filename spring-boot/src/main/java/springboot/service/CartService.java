//CartService

package springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springboot.Cart;
import springboot.Product;
import springboot.database.Setup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
//Changed imports
@Service
public class CartService {

    public static double getPriceCart(){
        return Cart.calculateTotalAmount();
    }



    //Where the Checkout Magic Happens
    public static String shopCheckout(){
        String result = "";
        result = parseStockResult(checkAndUpdateStock());
        return result;

    }


    //This will not be able to check Stock
    public String addToCart(String item){
        String result = "";
        Product addedItem = createProductItem(item);
        result = Cart.addItemS(addedItem);
        return result;

    }

    // Method to check if a product is in stock and create a product item for the first one found in stock
    private Product createProductItem(String itemName) {
        Connection conn = Setup.getDBConnection();
        String selectSQL = "SELECT * FROM PRODUCTS WHERE productName = ? AND productStock > 0";
        Product product = null;

        try (PreparedStatement statement = conn.prepareStatement(selectSQL)) {
            statement.setString(1, itemName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int productId = resultSet.getInt("productId");
                String productName = resultSet.getString("productName");
                int productStock = resultSet.getInt("productStock");
                String productDescription = resultSet.getString("productDescription");
                double productPrice = resultSet.getDouble("productPrice");
                String imageURL = resultSet.getString("imageURL");
                String category = resultSet.getString("category");
                // Create the product object
                product = new Product(productId, productName, productStock, productDescription, productPrice, imageURL, category);
            } else {
                System.out.println("Item '" + itemName + "' is not in stock or does not exist.");
            }
        } catch (SQLException e) {
            System.out.println("Error accessing database: " + e.getMessage());
        }

        return product;
    }



    // Method to check if a product is in stock and create product items for those in stock
    private List<Product> createProductItems(String[] itemNames) {
        List<springboot.Product> products = new ArrayList<>();
        Connection conn = Setup.getDBConnection();

        String selectSQL = "SELECT * FROM PRODUCTS WHERE productName = ? AND productStock > 0";

        try (PreparedStatement statement = conn.prepareStatement(selectSQL)) {
            for (String itemName : itemNames) {
                statement.setString(1, itemName);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    int productId = resultSet.getInt("productId");
                    String productName = resultSet.getString("productName");
                    int productStock = resultSet.getInt("productStock");
                    String productDescription = resultSet.getString("productDescription");
                    double productPrice = resultSet.getDouble("productPrice");
                    String imageURL = resultSet.getString("imageURL");
                    String category = resultSet.getString("category");
                    //String productId, String productName, int productStock, String productDescription, double productPrice, String imageURL, String category
                    springboot.Product product = new Product(productId,productName, productStock, productDescription, productPrice, imageURL, category);
                    products.add(product);
                } else {
                    System.out.println("Item '" + itemName + "' is not in stock or does not exist.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error accessing database: " + e.getMessage());
        }

        return products;
    }

    public static String checkAndUpdateStock() {
        Connection conn = Setup.getDBConnection();
        List<Product> cartItems = Cart.getItems();

        String updateSQL = "UPDATE PRODUCTS SET productStock = productStock - 1 WHERE productName = ? AND productStock > 0";

        try (PreparedStatement statement = conn.prepareStatement(updateSQL)) {
            for (Product item : cartItems) {
                statement.setString(1, item.getProductName());
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated == 0) {
                    System.out.println("Item '" + item.getProductName() + "' is not in stock or does not exist.");
                    return "f " + item.getProductName();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error accessing database: " + e.getMessage());
            return "f";
        }

        return "t";
    }


    public static String parseStockResult(String result) {
        //Returns T if in stock returns item out of stock if not
        String successFactor = "";

        if (result.startsWith("t")) {
            successFactor = "t";
        } else if (result.startsWith("f")) {
            successFactor = result.substring(2);;
        } else {
            // Invalid result format
            return null;
        }

        return successFactor;
    }


}
