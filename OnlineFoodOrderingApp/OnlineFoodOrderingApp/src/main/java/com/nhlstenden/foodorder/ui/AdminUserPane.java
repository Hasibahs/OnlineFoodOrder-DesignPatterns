package com.nhlstenden.foodorder.ui;

import com.nhlstenden.foodorder.command.*;
import com.nhlstenden.foodorder.model.User;
import com.nhlstenden.foodorder.persistence.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Stack;

public class AdminUserPane {
    private final UserRepository userRepo = new DBUserRepository();
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final Stack<UserCommand> undoStack = new Stack<>();
    private final Stack<UserCommand> redoStack = new Stack<>();

    public Parent getView() {
        users.setAll(userRepo.findAll());

        TableView<User> table = new TableView<>(users);
        TableColumn<User, String> colUsername = new TableColumn<>("Username");
        TableColumn<User, String> colRole = new TableColumn<>("Role");
        colUsername.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getUsername()));
        colRole.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getRole()));
        table.getColumns().setAll(colUsername, colRole);

        Button btnDelete = new Button("Delete");
        Button btnChangeRole = new Button("Change Role");
        Button btnUndo = new Button("Undo");
        Button btnRedo = new Button("Redo");

        btnDelete.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                UserCommand cmd = new DeleteUserCommand(userRepo, sel);
                cmd.execute();
                undoStack.push(cmd);
                redoStack.clear();
                refresh();
            }
        });

        btnChangeRole.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                ChoiceDialog<String> dialog = new ChoiceDialog<>(sel.getRole(), "user", "admin");
                dialog.setHeaderText("Select new role for " + sel.getUsername());
                dialog.showAndWait().ifPresent(newRole -> {
                    if (!newRole.equals(sel.getRole())) {
                        UserCommand cmd = new ChangeUserRoleCommand(userRepo, sel, newRole);
                        cmd.execute();
                        undoStack.push(cmd);
                        redoStack.clear();
                        refresh();
                    }
                });
            }
        });

        btnUndo.setOnAction(e -> {
            if (!undoStack.isEmpty()) {
                UserCommand cmd = undoStack.pop();
                cmd.undo();
                redoStack.push(cmd);
                refresh();
            }
        });

        btnRedo.setOnAction(e -> {
            if (!redoStack.isEmpty()) {
                UserCommand cmd = redoStack.pop();
                cmd.execute();
                undoStack.push(cmd);
                refresh();
            }
        });

        HBox buttons = new HBox(10, btnDelete, btnChangeRole, btnUndo, btnRedo);
        buttons.setAlignment(Pos.CENTER);
        VBox root = new VBox(10, new Label("User Management"), table, buttons);
        root.setPadding(new Insets(10));
        return root;
    }

    private void refresh() {
        users.setAll(userRepo.findAll());
    }
}
