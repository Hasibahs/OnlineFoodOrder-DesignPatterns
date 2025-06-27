package com.nhlstenden.foodorder.ui;

import com.nhlstenden.foodorder.model.MenuEntry;
import com.nhlstenden.foodorder.persistence.DBMenuRepository;
import com.nhlstenden.foodorder.persistence.MenuRepository;
import com.nhlstenden.foodorder.util.MenuEventManager;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminPane {
    private final MenuRepository menuRepo = new DBMenuRepository();
    private final ObservableList<MenuEntry> data =
            FXCollections.observableArrayList(menuRepo.findAll());

    public Parent getView() {
        TableView<MenuEntry> table = new TableView<>(data);
        var colType = new TableColumn<MenuEntry, String>("Type");
        var colKey = new TableColumn<MenuEntry, String>("Key");
        var colName = new TableColumn<MenuEntry, String>("Name");
        var colPrice = new TableColumn<MenuEntry, Number>("Price");

        colType.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getType()));
        colKey.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getKey()));
        colName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));
        colPrice.setCellValueFactory(c -> new ReadOnlyDoubleWrapper(c.getValue().getPrice()));

        table.getColumns().setAll(colType, colKey, colName, colPrice);
        table.setPrefHeight(300);

        var tfType = new TextField();
        var tfKey = new TextField();
        var tfName = new TextField();
        var tfPrice = new TextField();

        Button btnSave = new Button("Add/Update");
        Button btnDelete = new Button("Delete");

        btnSave.setOnAction(e -> {
            String t = tfType.getText().trim();
            String k = tfKey.getText().trim();
            String n = tfName.getText().trim();
            String ps = tfPrice.getText().trim();

            if (t.isEmpty() || k.isEmpty() || n.isEmpty() || ps.isEmpty()) {
                showError("All fields are required");
                return;
            }

            double p;
            try {
                p = Double.parseDouble(ps);
                if (p < 0) {
                    showError("Price must be non-negative");
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("Price must be a valid number");
                return;
            }

            if (!t.equalsIgnoreCase("pizza") && !t.equalsIgnoreCase("drink")) {
                showError("Type must be either “pizza” or “drink”");
                return;
            }

            if (!k.matches("[A-Za-z0-9_-]+")) {
                showError("Key can only contain letters, digits, _ or -");
                return;
            }

            boolean isAdding = !tfKey.isDisabled();

            if (isAdding && data.stream().anyMatch(me -> me.getKey().equalsIgnoreCase(k))) {
                showError("An item with key “" + k + "” already exists");
                return;
            }

            boolean dupName = data.stream().anyMatch(me ->
                    me.getName().equalsIgnoreCase(n)
                            && (isAdding || !me.getKey().equalsIgnoreCase(k))
            );
            if (dupName) {
                showError("An item with name “" + n + "” already exists");
                return;
            }

            var me = new MenuEntry(t, k, n, p);
            menuRepo.save(me);
            refresh();
            MenuEventManager.fire(); // 🔄 notify Shop tab

            tfType.clear(); tfType.setDisable(false);
            tfKey.clear(); tfKey.setDisable(false);
            tfName.clear();
            tfPrice.clear();
        });

        btnDelete.setOnAction(e -> {
            var sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                menuRepo.deleteByKey(sel.getKey());
                refresh();
                MenuEventManager.fire(); // 🔄 notify Shop tab
            }
        });

        table.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (nv != null) {
                tfType.setText(nv.getType());
                tfKey.setText(nv.getKey());
                tfName.setText(nv.getName());
                tfPrice.setText(String.valueOf(nv.getPrice()));
                tfType.setDisable(true);
                tfKey.setDisable(true);
            }
        });

        HBox form = new HBox(10,
                new VBox(new Label("Type"), tfType),
                new VBox(new Label("Key"), tfKey),
                new VBox(new Label("Name"), tfName),
                new VBox(new Label("Price"), tfPrice),
                btnSave, btnDelete
        );
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(10));

        return new VBox(10, table, form);
    }

    private void refresh() {
        data.setAll(menuRepo.findAll());
    }

    private void showError(String msg) {
        var a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }
}
