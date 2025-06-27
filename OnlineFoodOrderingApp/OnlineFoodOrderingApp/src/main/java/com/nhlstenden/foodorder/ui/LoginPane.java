package com.nhlstenden.foodorder.ui;

import com.nhlstenden.foodorder.model.User;
import com.nhlstenden.foodorder.persistence.DBUserRepository;
import com.nhlstenden.foodorder.persistence.UserRepository;
import com.nhlstenden.foodorder.util.AppUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class LoginPane {
    private final UserRepository repo = new DBUserRepository();

    public void show(Stage stage, Runnable onSuccess, Consumer<User> onLogin) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Login");
        TextField tfUsername = new TextField();
        tfUsername.setPromptText("Username");

        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText("Password");

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: red");

        Button btnLogin = new Button("Login");
        Button btnRegister = new Button("Register");

        btnLogin.setOnAction(e -> {
            String username = tfUsername.getText().trim();
            String password = pfPassword.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                errorLbl.setText("Enter username and password.");
                return;
            }

            User user = repo.findByUsername(username);
            String hash = AppUtils.hashPassword(password);

            if (user == null || !user.getPasswordHash().equals(hash)) {
                errorLbl.setText("Invalid username or password.");
                return;
            }

            System.out.println("Login successful: " + username + " (" + user.getRole() + ")");
            onLogin.accept(user);   // Pass user to MainApp
            onSuccess.run();        // Show main app
        });

        btnRegister.setOnAction(e -> {
            new RegisterPane().show(stage, onSuccess, onLogin);
        });

        root.getChildren().addAll(title, tfUsername, pfPassword, errorLbl, btnLogin, btnRegister);
        Scene scene = new Scene(root, 300, 280);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }
}
