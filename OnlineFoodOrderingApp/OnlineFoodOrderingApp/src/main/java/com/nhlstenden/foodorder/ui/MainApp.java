package com.nhlstenden.foodorder.ui;

import com.nhlstenden.foodorder.command.*;
import com.nhlstenden.foodorder.factory.FactoryProducer;
import com.nhlstenden.foodorder.model.*;
import com.nhlstenden.foodorder.observer.Cart;
import com.nhlstenden.foodorder.observer.CartListener;
import com.nhlstenden.foodorder.payment.*;
import com.nhlstenden.foodorder.persistence.*;
import com.nhlstenden.foodorder.receipt.*;
import com.nhlstenden.foodorder.util.AppUtils;
import com.nhlstenden.foodorder.util.MenuEventManager;
import com.nhlstenden.foodorder.util.ToppingEventManager;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.*;

public class MainApp extends Application implements CartListener {
    private Stage stage;
    private User loggedInUser;

    private Cart cart;
    private Order order;

    private final OrderRepository orderRepo = new DBOrderRepository();
    private final MenuRepository menuRepo = new DBMenuRepository();
    private final DBToppingRepository toppingRepo = new DBToppingRepository();

    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();

    private ObservableList<FoodItem> cartList;
    private Label totalLbl;
    private RadioButton rbCash, rbCard;
    private TextField tfCard;
    private Label cardError;

    private ObservableList<OrderRecord> histData;
    private FilteredList<FoodItem> filteredMenu;
    private ObservableList<FoodItem> masterMenu;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        showLogin();
    }

    private void showLogin() {
        new LoginPane().show(stage, this::showMainApp, user -> this.loggedInUser = user);
    }

    private void showMainApp() {
        this.cart = new Cart();
        this.order = new Order(cart);
        this.histData = FXCollections.observableArrayList();
        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Shop", buildShopPane()));
        tabs.getTabs().add(new Tab("History", buildHistoryPane()));
        if ("admin".equalsIgnoreCase(loggedInUser.getRole())) {
            tabs.getTabs().add(new Tab("Items", new AdminPane().getView()));
            tabs.getTabs().add(new Tab("Toppings", new AdminToppingPane().getView()));
            tabs.getTabs().add(new Tab("Users", new AdminUserPane().getView()));

        }
        Tab logoutTab = new Tab("Logout");
        logoutTab.setClosable(false);
        logoutTab.setOnSelectionChanged(e -> {
            if (logoutTab.isSelected()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Are you sure you want to logout?",
                        ButtonType.YES, ButtonType.CANCEL);
                alert.setHeaderText(null);
                if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.YES) {
                    showLogin();
                } else {
                    tabs.getSelectionModel().selectFirst();
                }
            }
        });
        tabs.getTabs().add(logoutTab);
        tabs.getTabs().forEach(t -> t.setClosable(false));
        cart.addListener(this);
        refreshHistory();
        stage.setScene(new Scene(tabs, 950, 550));
        stage.setTitle("Online Food Ordering App");
        stage.show();
    }

    private Parent buildShopPane() {
        masterMenu = FXCollections.observableArrayList();
        filteredMenu = new FilteredList<>(masterMenu, p -> true);
        refreshMenuItems();
        MenuEventManager.subscribe(this::refreshMenuItems);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Search...");
        ChoiceBox<String> cbCat = new ChoiceBox<>(FXCollections.observableArrayList("All", "Pizza", "Drink"));
        cbCat.setValue("All");
        txtSearch.textProperty().addListener((o, ov, nv) -> applyFilter(txtSearch, cbCat));
        cbCat.valueProperty().addListener((o, ov, nv) -> applyFilter(txtSearch, cbCat));

        HBox filterBar = new HBox(10, new Label("Filter:"), txtSearch, cbCat);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(5));

        TableView<FoodItem> availTbl = new TableView<>(filteredMenu);
        TableColumn<FoodItem, String> colName = new TableColumn<>("Name");
        TableColumn<FoodItem, Number> colPrice = new TableColumn<>("Price");
        colName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
        colPrice.setCellValueFactory(cd -> new ReadOnlyDoubleWrapper(cd.getValue().getPrice()));
        availTbl.getColumns().setAll(colName, colPrice);
        availTbl.setPrefWidth(260);

        cartList = FXCollections.observableArrayList();
        TableView<FoodItem> cartTbl = new TableView<>(cartList);
        TableColumn<FoodItem, String> cartName = new TableColumn<>("Name");
        TableColumn<FoodItem, Number> cartPrice = new TableColumn<>("Price");
        cartName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
        cartPrice.setCellValueFactory(cd -> new ReadOnlyDoubleWrapper(cd.getValue().getPrice()));
        cartTbl.getColumns().setAll(cartName, cartPrice);
        cartTbl.setPrefWidth(260);

        VBox toppingBox = new VBox(5);
        toppingBox.setPadding(new Insets(5));
        ScrollPane toppingScroll = new ScrollPane(toppingBox);
        toppingScroll.setFitToWidth(true);
        toppingScroll.setPrefHeight(100);
        Map<ToppingRecord, CheckBox> toppingCheckMap = new HashMap<>();

        Runnable refreshToppings = () -> {
            toppingBox.getChildren().clear();
            toppingCheckMap.clear();
            for (ToppingRecord tr : toppingRepo.findAll()) {
                CheckBox cb = new CheckBox(tr.getName() + " ($" + tr.getPrice() + ")");
                toppingBox.getChildren().add(cb);
                toppingCheckMap.put(tr, cb);
            }
        };
        refreshToppings.run(); // initial load
        ToppingEventManager.subscribe(refreshToppings);

        toppingScroll = new ScrollPane(toppingBox);
        toppingScroll.setFitToWidth(true);
        toppingScroll.setPrefHeight(100);

        Button btnAdd = new Button("Add to Cart");
        Button btnRemove = new Button("Remove Selected");
        btnAdd.disableProperty().bind(availTbl.getSelectionModel().selectedItemProperty().isNull());
        btnRemove.disableProperty().bind(cartTbl.getSelectionModel().selectedItemProperty().isNull());

        btnAdd.setOnAction(e -> {
            FoodItem sel = availTbl.getSelectionModel().getSelectedItem();
            if (sel == null) return;

            String key = sel.getName().split(" ")[0].toLowerCase();
            FoodItem item = FactoryProducer.getFactory(
                    (sel instanceof Pizza) ? "pizza" : "drink"
            ).createFoodItem(key, sel.getName(), sel.getPrice());

            if (item instanceof Pizza) {
                for (Map.Entry<ToppingRecord, CheckBox> entry : toppingCheckMap.entrySet()) {
                    if (entry.getValue().isSelected()) {
                        ToppingRecord tr = entry.getKey();
                        item = new DynamicTopping(item, tr.getName(), tr.getPrice());
                    }
                }
            }

            Command cmd = new AddItemCommand(cart, item);
            cmd.execute();
            undoStack.push(cmd);
            redoStack.clear();
        });

        btnRemove.setOnAction(e -> {
            FoodItem sel = cartTbl.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            Command cmd = new RemoveItemCommand(cart, sel);
            cmd.execute();
            undoStack.push(cmd);
            redoStack.clear();
        });

        Button btnUndo = new Button("Undo");
        Button btnRedo = new Button("Redo");

        btnUndo.setOnAction(e -> {
            if (!undoStack.isEmpty()) {
                Command cmd = undoStack.pop();
                cmd.undo();
                redoStack.push(cmd);
            }
        });

        btnRedo.setOnAction(e -> {
            if (!redoStack.isEmpty()) {
                Command cmd = redoStack.pop();
                cmd.execute();
                undoStack.push(cmd);
            }
        });

        totalLbl = new Label("Total: $0.00");
        rbCash = new RadioButton("Cash");
        rbCard = new RadioButton("Card");
        ToggleGroup pg = new ToggleGroup();
        rbCash.setToggleGroup(pg);
        rbCard.setToggleGroup(pg);
        rbCash.setSelected(true);

        tfCard = new TextField();
        tfCard.setPromptText("16-digit");
        tfCard.setDisable(true);
        cardError = new Label("Enter 16 digits");
        cardError.setStyle("-fx-text-fill:red;");
        cardError.setVisible(false);

        rbCard.setOnAction(e -> tfCard.setDisable(false));
        rbCash.setOnAction(e -> {
            tfCard.clear();
            tfCard.setDisable(true);
            tfCard.setStyle("");
            cardError.setVisible(false);
        });

        tfCard.textProperty().addListener((o, ov, nv) -> {
            if (rbCard.isSelected()) {
                boolean ok = nv.matches("\\d{16}");
                tfCard.setStyle(ok ? "" : "-fx-border-color:red;");
                cardError.setVisible(!ok);
            }
        });

        Button btnPlace = new Button("Place Order");
        btnPlace.disableProperty().bind(Bindings.createBooleanBinding(
                () -> cart.getTotalCost() <= 0 || (rbCard.isSelected() && !tfCard.getText().matches("\\d{16}")),
                cartList, rbCard.selectedProperty(), tfCard.textProperty()
        ));
        btnPlace.setOnAction(e -> confirmAndPlace());

        HBox topPane = new HBox(20,
                new VBox(5, new Label("Available"), filterBar, availTbl),
                new VBox(10, toppingScroll, btnAdd, btnRemove, btnUndo, btnRedo),
                new VBox(5, new Label("Cart"), cartTbl, totalLbl)
        );
        topPane.setPadding(new Insets(10));

        HBox bottom = new HBox(10, rbCash, rbCard, tfCard, cardError, btnPlace);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setCenter(topPane);
        root.setBottom(bottom);
        return root;
    }

    private void applyFilter(TextField txt, ChoiceBox<String> cat) {
        String f = txt.getText().toLowerCase().trim();
        String c = cat.getValue();
        filteredMenu.setPredicate(item ->
                item.getName().toLowerCase().contains(f)
                        && (c.equals("All")
                        || (c.equals("Pizza") && item instanceof Pizza)
                        || (c.equals("Drink") && item instanceof Drink))
        );
    }

    private void confirmAndPlace() {
        List<FoodItem> snapshot = new ArrayList<>(cart.getItems());
        StringBuilder conf = new StringBuilder("You have ")
                .append(snapshot.size()).append(" items:\n");
        snapshot.forEach(fi -> conf.append("- ").append(fi.getName()).append("\n"));

        double total = cart.getTotalCost();
        conf.append(String.format("\nTotal: $%.2f\nPay by %s\n",
                total,
                rbCash.isSelected() ? "Cash" : "Card"));

        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setHeaderText(null);
        c.setContentText(conf.toString());
        if (c.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        PaymentStrategy ps = rbCash.isSelected()
                ? new CashPayment()
                : new CardPayment(tfCard.getText().trim());
        order.setPaymentStrategy(ps);
        order.placeOrder();

        OrderRecord rec = new OrderRecord(
                LocalDateTime.now(),
                AppUtils.itemsAsCsv(snapshot),
                total,
                total,
                rbCash.isSelected() ? "Cash" : "Card",
                loggedInUser.getUsername()
        );
        orderRepo.save(rec);
        refreshHistory();

        ReceiptBuilder r = new TextReceiptBuilder();
        r.reset();
        r.addHeader("🍕 My Pizza Shop 🍹");
        r.addDateTime(LocalDateTime.now());
        for (FoodItem fi : snapshot) {
            Map<String, Double> tmap = AppUtils.collectToppings(fi);
            double base = fi.getPrice() - tmap.values().stream()
                    .mapToDouble(Double::doubleValue).sum();
            r.addItemLine(fi.getName(), base, tmap);
        }
        r.addSubtotal(total);
        r.addTotal(total);
        r.addPaymentMethod(rec.getPaymentMethod());

        Alert receiptAlert = new Alert(Alert.AlertType.INFORMATION);
        receiptAlert.setTitle("Your Receipt");
        TextArea area = new TextArea(r.build());
        area.setEditable(false);
        receiptAlert.getDialogPane().setContent(area);
        receiptAlert.showAndWait();

        cart.getItems().forEach(cart::removeItem);
    }

    private Parent buildHistoryPane() {
        histData = FXCollections.observableArrayList();
        refreshHistory(); // Load based on role (admin vs user)

        TableView<OrderRecord> tbl = new TableView<>(histData);

        if ("admin".equalsIgnoreCase(loggedInUser.getRole())) {
            TableColumn<OrderRecord, String> colUser = new TableColumn<>("User");
            colUser.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getUsername()));
            tbl.getColumns().add(colUser);
        }

        var colDate = new TableColumn<OrderRecord, String>("Date");
        var colItems = new TableColumn<OrderRecord, String>("Items");
        var colRaw = new TableColumn<OrderRecord, Number>("Raw");
        var colFin = new TableColumn<OrderRecord, Number>("Final");
        var colVia = new TableColumn<OrderRecord, String>("Paid Via");

        colDate.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDateTime().toString()));
        colItems.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getItemsCsv()));
        colRaw.setCellValueFactory(c -> new ReadOnlyDoubleWrapper(c.getValue().getRawTotal()));
        colFin.setCellValueFactory(c -> new ReadOnlyDoubleWrapper(c.getValue().getFinalTotal()));
        colVia.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getPaymentMethod()));

        tbl.getColumns().addAll(colDate, colItems, colRaw, colFin, colVia);
        tbl.setPrefHeight(350);

        Button btnDeleteSelected = new Button("Delete Selected");

        btnDeleteSelected.setOnAction(e -> {
            OrderRecord selected = tbl.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Are you sure you want to " +
                                ("admin".equalsIgnoreCase(loggedInUser.getRole()) ? "permanently delete" : "hide") +
                                " this order?",
                        ButtonType.OK, ButtonType.CANCEL);
                confirm.setHeaderText(null);
                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    if ("admin".equalsIgnoreCase(loggedInUser.getRole())) {
                        orderRepo.deleteById(selected.getId()); // permanently delete
                    } else {
                        orderRepo.softDeleteById(selected.getId(), loggedInUser.getUsername()); // soft delete
                    }
                    refreshHistory();
                }
            }
        });

        HBox buttons = new HBox(10, btnDeleteSelected);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(5));

        return new VBox(10, new Label("Order History"), tbl, buttons);
    }


    private void refreshMenuItems() {
        masterMenu.clear();
        for (MenuEntry e : menuRepo.findAll()) {
            try {
                masterMenu.add(FactoryProducer.getFactory(e.getType())
                        .createFoodItem(e.getKey(), e.getName(), e.getPrice()));
            } catch (Exception ex) {
                System.out.println("Error loading menu entry: " + e.getKey());
            }
        }
    }

    private void refreshHistory() {
        try {
            List<OrderRecord> orders;
            if ("admin".equalsIgnoreCase(loggedInUser.getRole())) {
                orders = orderRepo.findAll();
            } else {
                orders = orderRepo.findAllVisibleToUser(loggedInUser.getUsername());
            }
            histData.setAll(orders != null ? orders : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            histData.clear();
        }
    }

    @Override
    public void onItemAdded(FoodItem item) {
        cartList.add(item);
        totalLbl.setText(String.format("Total: $%.2f", cart.getTotalCost()));
    }

    @Override
    public void onItemRemoved(FoodItem item) {
        cartList.remove(item);
        totalLbl.setText(String.format("Total: $%.2f", cart.getTotalCost()));
    }
}
