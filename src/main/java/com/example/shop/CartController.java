package com.example.shop;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class CartController {
    @FXML
    private VBox cartContainer;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Button checkoutButton;

    private ShoppingCart shoppingCart = new ShoppingCart();

    @FXML
    public void initialize() {
        updateTotalPrice();
    }

    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
        loadCartItems();
    }

    private void loadCartItems() {
        cartContainer.getChildren().clear();
        Map<Product, Integer> items = shoppingCart.getItemsWithQuantity();
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();

            VBox itemBox = new VBox();
            itemBox.setSpacing(5);

            Label nameLabel = new Label(product.getName());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label priceLabel = new Label("Цена: " + product.getPrice() + " ₽");
            priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #d32f2f;");

            Label quantityLabel = new Label("Количество: " + quantity);
            quantityLabel.setStyle("-fx-font-size: 14px;");

            itemBox.getChildren().addAll(nameLabel, priceLabel, quantityLabel);
            cartContainer.getChildren().add(itemBox);
        }
    }

    private void updateTotalPrice() {
        double totalPrice = shoppingCart.getTotalPrice();
        totalPriceLabel.setText("Итого: " + totalPrice + " ₽");
    }

    @FXML
    private void checkout() {
        // Логика оформления заказа
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Оформление заказа");
        alert.setHeaderText(null);
        alert.setContentText("Спасибо за покупку! Ваш заказ оформлен.");
        alert.showAndWait();

        // Очистка корзины после оформления заказа
        shoppingCart.clear();
        loadCartItems();
        updateTotalPrice();
    }
}
