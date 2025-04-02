package com.example.shop;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManageProductsController {
    @FXML
    private ListView<Product> productListView;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;

    private MainPageController mainPageController; // Добавляем ссылку на главный контроллер

    private DatabaseConnection dbConnection = new DatabaseConnection();
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadProducts("");

        productListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Настройка CellFactory для отображения названий товаров
        productListView.setCellFactory(new Callback<ListView<Product>, ListCell<Product>>() {
            @Override
            public ListCell<Product> call(ListView<Product> param) {
                return new ListCell<Product>() {
                    @Override
                    protected void updateItem(Product item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });

        editButton.setOnAction(e -> editSelectedProduct());
        addButton.setOnAction(e -> openEditor(null));
    }

    public ManageProductsController() {
        // Конструктор должен быть пустым
    }

    public void setMainPageController(MainPageController mainPageController) {
        this.mainPageController = mainPageController;
    }

    private void loadProducts(String search) {
        productList.clear();
        String query = "SELECT p.product_id, p.name, p.description, p.price, p.image_url, " +
                "m.name as manufacturer_name, m.country, m.website, s.name as sport_name, p.stock_quantity " +
                "FROM Products p " +
                "LEFT JOIN Manufacturers m ON p.manufacturer_id = m.manufacturer_id " +
                "LEFT JOIN Sports s ON p.sport_id = s.sport_id " +
                "WHERE p.name LIKE ? ";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + search + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                productList.add(new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getString("image_url"),
                        rs.getString("manufacturer_name"),
                        rs.getString("country"),
                        rs.getString("website"),
                        rs.getInt("stock_quantity"), // Добавляем stock_quantity
                        rs.getString("sport_name") // Добавляем категорию (спорт)
                ));
            }
            productListView.setItems(productList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editSelectedProduct() {
        Product selectedProduct = productListView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            openEditor(selectedProduct);
        }
    }

    private void openEditor(Product product) {
        Stage stage = new Stage();
        EditProductController.openEditor(product, stage, mainPageController);
        loadProducts(""); // Перезагрузка списка после редактирования
    }

    public static void openManager(MainPageController mainPageController) {
        try {
            FXMLLoader loader = new FXMLLoader(ManageProductsController.class.getResource("manage_products.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Управление товарами");
            stage.setScene(new Scene(loader.load()));

            ManageProductsController controller = loader.getController();
            controller.setMainPageController(mainPageController);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
