package com.example.shop;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class CartController {
    @FXML
    private VBox cartContainer;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Button checkoutButton;

    private ShoppingCart shoppingCart = new ShoppingCart();
    private DatabaseConnection dbConnection = new DatabaseConnection();
    private int selectedClientId = -1;

    @FXML
    public void initialize() {
        updateTotalPrice();
        updateCheckoutButtonState();
    }

    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
        loadCartItems();
        updateTotalPrice(); // Добавляем обновление цены
    }

    private void loadCartItems() {
        cartContainer.getChildren().clear();
        Map<Product, Integer> items = shoppingCart.getItemsWithQuantity();

        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();

            HBox itemBox = new HBox();
            itemBox.setSpacing(10);

            Label nameLabel = new Label(product.getName());
            Label priceLabel = new Label("Цена: " + product.getPrice() + " ₽");
            Label quantityLabel = new Label("Количество: " + quantity);

            Button removeButton = new Button("Удалить");
            removeButton.setOnAction(e -> {
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Подтверждение удаления");
                confirmationAlert.setHeaderText(null);
                confirmationAlert.setContentText("Вы уверены, что хотите удалить этот товар из корзины?");

                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response.getButtonData().isDefaultButton()) {
                        shoppingCart.removeItem(product);
                        loadCartItems();
                        updateTotalPrice();
                        updateCheckoutButtonState();
                    }
                });
            });


            itemBox.getChildren().addAll(nameLabel, priceLabel, quantityLabel, removeButton);
            cartContainer.getChildren().add(itemBox);
        }

        updateTotalPrice();
        updateCheckoutButtonState();
    }

    private void updateCheckoutButtonState() {
        boolean cartIsEmpty = shoppingCart.getItemsWithQuantity().isEmpty();
        checkoutButton.setDisable(cartIsEmpty);
    }

    private void updateTotalPrice() {
        double totalPrice = shoppingCart.getTotalPrice();
        totalPriceLabel.setText("Итого: " + totalPrice + " ₽");
    }

    @FXML
    private void clearCart() {
        shoppingCart.clear();
        loadCartItems();
        updateTotalPrice();
        updateCheckoutButtonState();
    }

    @FXML
    private void checkout() {
        openClientSearchModal();
    }

    private boolean areAllItemsAvailable(Map<Product, Integer> items) {
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            if (product.getStockQuantity() < quantity) {
                return false;
            }
        }
        return true;
    }

    private void placeOrder() {
        String orderQuery = "INSERT INTO Orders (user_id, order_date, total_price, status) VALUES (?, CURRENT_TIMESTAMP, ?, 'pending')";
        String orderItemQuery = "INSERT INTO orderitems (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        String updateStockQuery = "UPDATE Products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement orderStmt = conn.prepareStatement(orderQuery, PreparedStatement.RETURN_GENERATED_KEYS);
             PreparedStatement orderItemStmt = conn.prepareStatement(orderItemQuery);
             PreparedStatement updateStockStmt = conn.prepareStatement(updateStockQuery)) {

            // 1. Создаём заказ
            orderStmt.setInt(1, selectedClientId);
            orderStmt.setDouble(2, shoppingCart.getTotalPrice());
            orderStmt.executeUpdate();

            // 2. Получаем ID созданного заказа
            ResultSet generatedKeys = orderStmt.getGeneratedKeys();
            int orderId;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Ошибка: Не удалось получить ID созданного заказа.");
            }

            // 3. Заполняем orderitems
            Map<Product, Integer> items = shoppingCart.getItemsWithQuantity();
            for (Map.Entry<Product, Integer> entry : items.entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue();

                orderItemStmt.setInt(1, orderId);
                orderItemStmt.setInt(2, product.getId());
                orderItemStmt.setInt(3, quantity);
                orderItemStmt.setDouble(4, product.getPrice());
                orderItemStmt.addBatch();

                // 4. Обновляем количество на складе
                updateStockStmt.setInt(1, quantity);
                updateStockStmt.setInt(2, product.getId());
                updateStockStmt.addBatch();

                // 5. Обновляем объект Product
                product.setStockQuantity(product.getStockQuantity() - quantity);
            }

            orderItemStmt.executeBatch();
            updateStockStmt.executeBatch();

            // 6. Показываем уведомление об успешном заказе
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Оформление заказа");
            alert.setHeaderText(null);
            alert.setContentText("Спасибо за покупку! Ваш заказ оформлен.");
            alert.showAndWait();

            // 7. Очищаем корзину
            shoppingCart.clear();
            loadCartItems();
            updateTotalPrice();
            updateCheckoutButtonState(); // обновляем кнопку

        } catch (SQLException e) {
            System.err.println("Ошибка при оформлении заказа: " + e.getMessage());
        }
    }


    private void addToWishlist() {
        Map<Product, Integer> items = shoppingCart.getItemsWithQuantity();
        String query = "INSERT INTO Wishlist (user_id, product_id, added_at) VALUES (?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (Map.Entry<Product, Integer> entry : items.entrySet()) {
                Product product = entry.getKey();
                stmt.setInt(1, selectedClientId);
                stmt.setInt(2, product.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Лист ожидания");
            alert.setHeaderText(null);
            alert.setContentText("Товары добавлены в лист ожидания.");
            alert.showAndWait();

            shoppingCart.clear();
            loadCartItems();
            updateTotalPrice();
            updateCheckoutButtonState(); // обновляем кнопку
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении товаров в лист ожидания: " + e.getMessage());
        }
    }

    @FXML
    private void openClientSearchModal() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Выбор клиента");

        VBox modalRoot = new VBox();
        modalRoot.setSpacing(10);
        modalRoot.setStyle("-fx-padding: 20;");

        TextField searchField = new TextField();
        searchField.setPromptText("Введите имя клиента");

        Button searchButton = new Button("Поиск");
        searchButton.setOnAction(e -> searchClients(searchField.getText(), modalRoot, modalStage));

        VBox clientList = new VBox();
        clientList.setSpacing(5);

        modalRoot.getChildren().addAll(searchField, searchButton, clientList);

        Scene modalScene = new Scene(modalRoot, 400, 300);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    private void searchClients(String query, VBox clientList, Stage modalStage) {
        String sqlQuery = "SELECT user_id, username FROM Users WHERE role = 'client' AND username LIKE ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {

            stmt.setString(1, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();

            clientList.getChildren().clear();

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String clientName = rs.getString("username");

                Label clientLabel = new Label(clientName);
                clientLabel.setOnMouseClicked(e -> {
                    selectedClientId = userId;
                    clientList.getChildren().clear();
                    clientList.getChildren().add(new Label("Выбран клиент: " + clientName));

                    Button confirmButton = new Button("Оформить заказ");
                    confirmButton.setOnAction(event -> {
                        if (areAllItemsAvailable(shoppingCart.getItemsWithQuantity())) {
                            placeOrder();
                        } else {
                            addToWishlist();
                        }
                        modalStage.close();
                    });

                    clientList.getChildren().add(confirmButton);
                });

                clientList.getChildren().add(clientLabel);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске клиентов: " + e.getMessage());
        }
    }
}
