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

public class MainPageController {
    @FXML
    private VBox cardContainer;

    private DatabaseConnection dbConnection = new DatabaseConnection();

    @FXML
    public void initialize() {
        loadProducts("");
    }

    private void loadProducts(String search) {
        String query = "SELECT p.name, p.description, p.price, p.image_url, " +
                "m.name as manufacturer_name, m.country, m.website " +
                "FROM Products p " +
                "LEFT JOIN Manufacturers m ON p.manufacturer_id = m.manufacturer_id " +
                "WHERE p.name LIKE ?";


        List<Product> products = new ArrayList<>();

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + search + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(new Product(
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getString("image_url"),
                        rs.getString("manufacturer_name"),
                        rs.getString("country"),
                        rs.getString("website")
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
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

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

        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> modalStage.close());

        modalRoot.getChildren().addAll(
                productImage, nameLabel, descriptionLabel, priceLabel,
                manufacturerLabel, manufacturerCountryLabel, manufacturerWebsiteLabel, closeButton
        );

        Scene modalScene = new Scene(modalRoot, 400, 500);
        modalStage.setScene(modalScene);
        modalStage.show();
    }

    public void setUserId(int userId, String userRole) {
    }
}
