package com.example.shop;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class EditProductController {
    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField imageUrlField;
    @FXML
    private TextField stockQuantityField;
    @FXML
    private ComboBox<String> manufacturerComboBox;
    @FXML
    private ComboBox<String> sportComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;

    private Product product;
    private Stage stage;
    private DatabaseConnection dbConnection = new DatabaseConnection();
    private MainPageController mainPageController; // Ссылка на главную страницу

    public void setProduct(Product product, Stage stage, MainPageController mainPageController) {
        this.product = product;
        this.stage = stage;
        this.mainPageController = mainPageController;

        if (product != null) {
            nameField.setText(product.getName());
            descriptionField.setText(product.getDescription());
            priceField.setText(String.valueOf(product.getPrice()));
            imageUrlField.setText(product.getImageUrl());
            stockQuantityField.setText(String.valueOf(product.getStockQuantity()));
        } else {
            deleteButton.setDisable(true);
        }

        loadManufacturers();
        loadSports();

        // Устанавливаем выбранные значения в ComboBox
        if (product != null) {
            manufacturerComboBox.setValue(product.getManufacturerName());
            sportComboBox.setValue(product.getSportName());
        }
    }

    private void loadManufacturers() {
        String query = "SELECT name FROM Manufacturers";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            manufacturerComboBox.getItems().clear();
            while (rs.next()) {
                manufacturerComboBox.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSports() {
        String query = "SELECT name FROM Sports";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            sportComboBox.getItems().clear();
            while (rs.next()) {
                sportComboBox.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveProduct() {
        if (validateFields()) {
            String name = nameField.getText();
            String description = descriptionField.getText();
            double price = Double.parseDouble(priceField.getText());
            String imageUrl = imageUrlField.getText();
            int stockQuantity = Integer.parseInt(stockQuantityField.getText());
            String manufacturerName = manufacturerComboBox.getValue();
            String sportName = sportComboBox.getValue();

            int manufacturerId = getManufacturerIdByName(manufacturerName);
            int sportId = getSportIdByName(sportName);

            try (Connection conn = dbConnection.connect()) {
                if (product == null) {
                    String insertQuery = "INSERT INTO Products (name, description, price, image_url, stock_quantity, manufacturer_id, sport_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(insertQuery);
                    stmt.setString(1, name);
                    stmt.setString(2, description);
                    stmt.setDouble(3, price);
                    stmt.setString(4, imageUrl);
                    stmt.setInt(5, stockQuantity);
                    stmt.setInt(6, manufacturerId);
                    stmt.setInt(7, sportId);
                    stmt.executeUpdate();
                } else {
                    String updateQuery = "UPDATE Products SET name = ?, description = ?, price = ?, image_url = ?, stock_quantity = ?, manufacturer_id = ?, sport_id = ? WHERE product_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(updateQuery);
                    stmt.setString(1, name);
                    stmt.setString(2, description);
                    stmt.setDouble(3, price);
                    stmt.setString(4, imageUrl);
                    stmt.setInt(5, stockQuantity);
                    stmt.setInt(6, manufacturerId);
                    stmt.setInt(7, sportId);
                    stmt.setInt(8, product.getId());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            mainPageController.loadProducts(""); // Обновление главной страницы
            stage.close();
        }
    }

    private boolean validateFields() {
        if (nameField.getText().isEmpty()) {
            showAlert("Пожалуйста, введите название товара.");
            return false;
        }
        if (descriptionField.getText().isEmpty()) {
            showAlert("Пожалуйста, введите описание товара.");
            return false;
        }
        if (priceField.getText().isEmpty()) {
            showAlert("Пожалуйста, введите цену товара.");
            return false;
        }
        if (imageUrlField.getText().isEmpty()) {
            showAlert("Пожалуйста, введите URL изображения товара.");
            return false;
        }
        if (stockQuantityField.getText().isEmpty()) {
            showAlert("Пожалуйста, введите количество товара на складе.");
            return false;
        }
        if (manufacturerComboBox.getValue() == null) {
            showAlert("Пожалуйста, выберите производителя.");
            return false;
        }
        if (sportComboBox.getValue() == null) {
            showAlert("Пожалуйста, выберите категорию.");
            return false;
        }
        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private int getManufacturerIdByName(String name) {
        String query = "SELECT manufacturer_id FROM Manufacturers WHERE name = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("manufacturer_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Возвращаем -1, если производитель не найден
    }

    private int getSportIdByName(String name) {
        String query = "SELECT sport_id FROM Sports WHERE name = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("sport_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Возвращаем -1, если категория не найдена
    }

    @FXML
    private void deleteProduct() {
        if (product != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText(null);
            alert.setContentText("Вы уверены, что хотите удалить этот товар?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (Connection conn = dbConnection.connect()) {
                    String deleteQuery = "DELETE FROM Products WHERE product_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                    stmt.setInt(1, product.getId());
                    stmt.executeUpdate();
                }
                catch (SQLIntegrityConstraintViolationException e) {
                    AlertManager.showErrorAlert("Ошибка удаления", "Нельзя удалить товар, так как он есть в заказах или в листе ожидания.");
                }
                catch (SQLException e) {
                    AlertManager.showErrorAlert("Ошибка базы данных", "Не удалось удалить товар: " + e.getMessage());
                }

                mainPageController.loadProducts(""); // Обновление главной страницы
                stage.close();
            }
        }
    }

    public static void openEditor(Product product, Stage stage, MainPageController mainPageController) {
        try {
            FXMLLoader loader = new FXMLLoader(EditProductController.class.getResource("edit_product.fxml"));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(product == null ? "Добавить товар" : "Редактировать товар");
            stage.setScene(new Scene(loader.load()));

            EditProductController controller = loader.getController();
            controller.setProduct(product, stage, mainPageController); // Передаём ссылку на контроллер

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
