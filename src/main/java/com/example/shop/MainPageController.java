package com.example.shop;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainPageController {
    @FXML
    private VBox cardContainer;
    @FXML
    private Button refactorBut;
    @FXML
    private TextField searchField; // Поле для ввода поискового запроса
    @FXML
    private ComboBox<String> manufacturerFilter; // Фильтр по производителю
    @FXML
    private ComboBox<String> sportFilter; // Фильтр по категории (спорту)

    private DatabaseConnection dbConnection = new DatabaseConnection();

    public int userId = -1;
    public String userRole = "";

    @FXML
    public void initialize() {
        loadProducts("");
        loadManufacturers();
        loadSports();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadProducts(newValue));
        manufacturerFilter.setOnAction(e -> loadProducts(searchField.getText()));
        sportFilter.setOnAction(e -> loadProducts(searchField.getText()));
    }

    public void setUserId(int userId, String userRole) {
        this.userId = userId;
        this.userRole = userRole;

        refactorBut.setOnAction(e -> openManageProducts());

        refactorBut.setVisible(!Objects.equals(userRole, "client"));
    }

    public void loadProducts(String search) {
        String query = "SELECT p.product_id, p.name, p.description, p.price, p.image_url, " +
                "m.name as manufacturer_name, m.country, m.website, s.name as sport_name, p.stock_quantity " +
                "FROM Products p " +
                "LEFT JOIN Manufacturers m ON p.manufacturer_id = m.manufacturer_id " +
                "LEFT JOIN Sports s ON p.sport_id = s.sport_id " +
                "WHERE p.name LIKE ? ";

        if (manufacturerFilter.getValue() != null && !manufacturerFilter.getValue().equals("Все производители")) {
            query += "AND m.name = ? ";
        }

        if (sportFilter.getValue() != null && !sportFilter.getValue().equals("Все категории")) {
            query += "AND s.name = ? ";
        }

        List<Product> products = new ArrayList<>();

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + search + "%");
            int paramIndex = 2;
            if (manufacturerFilter.getValue() != null && !manufacturerFilter.getValue().equals("Все производители")) {
                stmt.setString(paramIndex++, manufacturerFilter.getValue());
            }
            if (sportFilter.getValue() != null && !sportFilter.getValue().equals("Все категории")) {
                stmt.setString(paramIndex++, sportFilter.getValue());
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(new Product(
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

        } catch (SQLException e) {
            System.err.println("Ошибка при загрузке товаров: " + e.getMessage());
            return;
        }

        cardContainer.getChildren().clear();
        for (Product product : products) {
            HBox productCard = createProductCard(product);
            cardContainer.getChildren().add(productCard);
        }
    }

    private HBox createProductCard(Product product) {
        HBox card = new HBox();
        card.setSpacing(10);
        card.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5;");

        ImageView productImage = new ImageView();
        productImage.setFitWidth(100);
        productImage.setFitHeight(100);
        try {
            productImage.setImage(new Image(product.getImageUrl()));
        } catch (Exception e) {
            productImage.setImage(new Image("default.png"));
        }

        VBox textContainer = new VBox();
        textContainer.setSpacing(5);

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #000;");

        Label priceLabel = new Label("Цена: " + product.getPrice() + " ₽");
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #d32f2f;");

        textContainer.getChildren().addAll(nameLabel, priceLabel);
        card.getChildren().addAll(productImage, textContainer);

        card.setOnMouseClicked(event -> openProductModal(product));

        return card;
    }

    private void openProductModal(Product product) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Информация о товаре");

        VBox modalRoot = new VBox();
        modalRoot.setSpacing(10);
        modalRoot.setStyle("-fx-padding: 20;");

        ImageView productImage = new ImageView(new Image(product.getImageUrl()));
        productImage.setFitWidth(200);
        productImage.setFitHeight(200);

        Label nameLabel = new Label("Название: " + product.getName());
        Label descriptionLabel = new Label("Описание: " + product.getDescription());
        Label priceLabel = new Label("Цена: " + product.getPrice() + " ₽");
        Label manufacturerLabel = new Label("Производитель: " + product.getManufacturerName());
        Label manufacturerCountryLabel = new Label("Страна: " + product.getManufacturerCountry());
        Label manufacturerWebsiteLabel = new Label("Сайт: " + product.getManufacturerWebsite());
        Label sportLabel = new Label("Категория: " + product.getSportName());

        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> modalStage.close());

        modalRoot.getChildren().addAll(
                productImage, nameLabel, descriptionLabel, priceLabel,
                manufacturerLabel, manufacturerCountryLabel, manufacturerWebsiteLabel, sportLabel, closeButton
        );

        Scene modalScene = new Scene(modalRoot, 400, 500);
        modalStage.setScene(modalScene);
        modalStage.show();
    }

    private void loadManufacturers() {
        String query = "SELECT name FROM Manufacturers";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            manufacturerFilter.getItems().add("Все производители");
            while (rs.next()) {
                manufacturerFilter.getItems().add(rs.getString("name"));
            }
            manufacturerFilter.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSports() {
        String query = "SELECT name FROM Sports";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            sportFilter.getItems().add("Все категории");
            while (rs.next()) {
                sportFilter.getItems().add(rs.getString("name"));
            }
            sportFilter.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openManageProducts() {
        ManageProductsController.openManager(this);
    }
}
