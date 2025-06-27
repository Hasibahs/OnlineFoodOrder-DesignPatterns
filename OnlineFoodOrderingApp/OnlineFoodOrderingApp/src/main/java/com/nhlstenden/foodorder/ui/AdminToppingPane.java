package com.nhlstenden.foodorder.ui;

import com.nhlstenden.foodorder.persistence.*;
import com.nhlstenden.foodorder.util.ToppingEventManager;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminToppingPane {
    private final DBToppingRepository repo = new DBToppingRepository();
    private final ObservableList<ToppingRecord> data =
            FXCollections.observableArrayList(repo.findAll());

    public Parent getView() {
        TableView<ToppingRecord> table = new TableView<>(data);
        var colName = new TableColumn<ToppingRecord, String>("Name");
        var colPrice = new TableColumn<ToppingRecord, Number>("Price");

        colName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));
        colPrice.setCellValueFactory(c -> new ReadOnlyDoubleWrapper(c.getValue().getPrice()));

        table.getColumns().setAll(colName, colPrice);
        table.setPrefHeight(200);

        TextField tfName = new TextField();
        TextField tfPrice = new TextField();

        Button btnSave = new Button("Add/Update");
        Button btnDelete = new Button("Delete");

        btnSave.setOnAction(e -> {
            String name = tfName.getText().trim();
            double price;
            try {
                price = Double.parseDouble(tfPrice.getText().trim());
            } catch (Exception ex) {
                showError("Price must be numeric");
                return;
            }

            if (name.isEmpty()) {
                showError("Name required");
                return;
            }

            repo.save(new ToppingRecord(name, price));
            refresh();
            ToppingEventManager.fire(); // 🔁 notify Shop tab
        });

        btnDelete.setOnAction(e -> {
            var sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                repo.delete(sel.getName());
                refresh();
                ToppingEventManager.fire(); // 🔁 notify Shop tab
            }
        });

        table.getSelectionModel().selectedItemProperty().addListener((o, old, sel) -> {
            if (sel != null) {
                tfName.setText(sel.getName());
                tfPrice.setText(String.valueOf(sel.getPrice()));
            }
        });

        HBox form = new HBox(10,
                new VBox(new Label("Name"), tfName),
                new VBox(new Label("Price"), tfPrice),
                btnSave, btnDelete
        );
        form.setPadding(new Insets(10));
        form.setAlignment(Pos.CENTER);

        return new VBox(10, new Label("Manage Toppings"), table, form);
    }

    private void refresh() {
        data.setAll(repo.findAll());
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}
