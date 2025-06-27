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

public class RegisterPane {
    private final UserRepository repo = new DBUserRepository();

    public void show(Stage stage, Runnable onSuccess, Consumer<User> onLogin) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Register");
        TextField tfUsername = new TextField();
        tfUsername.setPromptText("Username");

        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText("Password");

        PasswordField pfConfirm = new PasswordField();
        pfConfirm.setPromptText("Confirm Password");

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: red");

        Button btnRegister = new Button("Register");
        Button btnBack = new Button("Back");

        btnRegister.setOnAction(e -> {
            String username = tfUsername.getText().trim();
            String password = pfPassword.getText().trim();
            String confirm = pfConfirm.getText().trim();

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                errorLbl.setText("All fields are required.");
                return;
            }

            if(password.length() < 6 || password.length() > 16) {
                errorLbl.setText("Password must be between 6 and 16 characters.");
                return;
            }

            if (!password.equals(confirm)) {
                errorLbl.setText("Passwords do not match.");
                return;
            }

            if (repo.findByUsername(username) != null) {
                errorLbl.setText("Username already exists.");
                return;
            }

            String passwordHash = AppUtils.hashPassword(password);
            User newUser = new User(username, passwordHash, "user"); // Only "user" role allowed during registration
            repo.save(newUser);

            System.out.println("Registration successful: " + username);
            onLogin.accept(newUser); // Pass user to main app
            onSuccess.run();         // Show main app
        });

        btnBack.setOnAction(e -> {
            new LoginPane().show(stage, onSuccess, onLogin);  // Fix: no createPane, just show
        });

        root.getChildren().addAll(title, tfUsername, pfPassword, pfConfirm, errorLbl, btnRegister, btnBack);
        Scene scene = new Scene(root, 300, 300);
        stage.setScene(scene);
        stage.setTitle("Register");
        stage.show();
    }
}
