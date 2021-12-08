package portsim.display;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import portsim.cargo.*;
import portsim.evaluators.*;
import portsim.movement.CargoMovement;
import portsim.movement.Movement;
import portsim.movement.ShipMovement;
import portsim.port.BulkQuay;
import portsim.port.ContainerQuay;
import portsim.port.Quay;
import portsim.ship.BulkCarrier;
import portsim.ship.ContainerShip;
import portsim.ship.NauticalFlag;
import portsim.ship.Ship;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;

/**
 * View for the Port Simulation GUI.
 *
 * @given
 */
public class View {
    /**
     * Maximum number of ports that can be displayed each side
     */
    private static final int MAX_QUAYS = 8;
    /**
     * Stage containing the application scene
     */
    private final Stage stage;
    /**
     * ViewModel that manages interaction with the model
     */
    private final ViewModel viewModel;
    /**
     * Time interval between ticks of the view model
     */
    private final IntegerProperty secondsPerMinute = new SimpleIntegerProperty(5);
    /**
     * Custom canvas that represents the state of the simulation graphically
     */
    private PortCanvas canvas;
    /**
     * Last recorded time in nanoseconds
     */
    private long lastNanoTime;
    /**
     * Time spent un-paused since last minute, in nanoseconds
     */
    private long timeSpentUnpaused = 0;

    /**
     * Creates a new view for the given view model and adds the associated GUI elements to the given
     * stage.
     *
     * @param stage     stage to add GUI elements to
     * @param viewModel view model to display
     * @given
     */
    public View(Stage stage, ViewModel viewModel) {
        this.stage = stage;
        this.viewModel = viewModel;

        stage.setResizable(false);

        stage.titleProperty().bind(Bindings.concat("Port Simulation: "
                        + this.viewModel.getPort().getName(),
                viewModel.getPausedStatusText()));

        Scene rootScene = new Scene(createWindow());
        stage.setScene(rootScene);

    }

    /* Creates the root window containing all GUI elements */
    private Pane createWindow() {
        this.canvas = new PortCanvas(viewModel, 1100, 680);
        BorderPane.setAlignment(canvas, Pos.CENTER_RIGHT);

        var space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        VBox.setVgrow(space, Priority.ALWAYS);

        var buttons = new HBox();
        buttons.setPadding(new Insets(10, 10, 10, 10));
        buttons.setSpacing(10);
        var cargoManifestButton = new Button("Dump Cargo Manifest");
        cargoManifestButton.setOnAction(viewModel.getShipContentsHandler());

        var time = new Text();
        time.textProperty().bind(viewModel.getTimeText());

        buttons.getChildren().add(cargoManifestButton);
        buttons.getChildren().add(space);
        buttons.getChildren().add(time);

        var middleInfoBox = createInfoBox(viewModel.getCargoManifestText(), 10);
        var scroll = new ScrollPane();
        scroll.setContent(middleInfoBox);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);

        var leftPanel = new VBox();
        var topInfoBox = createInfoBox(viewModel.getShipInfoText(), 8);
        leftPanel.getChildren().add(topInfoBox);
        leftPanel.getChildren().add(buttons);
        leftPanel.getChildren().add(scroll);
        leftPanel.getChildren().add(space);
        var bottomInfoBox = createInfoBox(viewModel.getEvaluatorsText(), 11);
        var scroll2 = new ScrollPane();
        scroll2.setContent(bottomInfoBox);
        scroll2.setFitToHeight(true);
        scroll2.setFitToWidth(true);
        leftPanel.getChildren().add(scroll2);

        var bottomPanel = new HBox();
        bottomPanel.getChildren().add(leftPanel);
        bottomPanel.getChildren().add(canvas);

        var pane = new VBox();
        pane.getChildren().add(createMenuBar());
        pane.getChildren().add(bottomPanel);
        return pane;
    }

    /* Creates a menu bar that allows actions to be taken within the GUI */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) {
            menuBar.useSystemMenuBarProperty().set(true);
        }

        MenuItem save = new MenuItem("_Save");
        save.setMnemonicParsing(true);
        save.setOnAction(event -> {
            try {
                viewModel.save();
            } catch (IOException e) {
                viewModel.createErrorDialog("Error saving to file",
                        e.getMessage());
                return;
            }
            viewModel.createSuccessDialog("Saved successfully",
                    "Saved to default provided file locations successfully.");
        });

        MenuItem exit = new MenuItem("_Exit");
        exit.setMnemonicParsing(true);
        exit.setOnAction(event -> System.exit(0));
        exit.setAccelerator(KeyCombination.keyCombination("Shortcut+Q"));

        Menu menuFile = new Menu("_File");
        menuFile.setMnemonicParsing(true);
        menuFile.getItems().add(save);
        menuFile.getItems().add(createSaveAsMenuItem());
        menuFile.getItems().add(new SeparatorMenuItem());
        menuFile.getItems().add(exit);

        Menu add = new Menu("_Add");
        add.setMnemonicParsing(true);
        add.getItems().add(createAddShipMenu());
        add.getItems().add(createAddCargoMenu());
        add.getItems().add(createAddQuayMenu());

        Menu movements = new Menu("_Movements");
        movements.setMnemonicParsing(true);
        movements.getItems().add(createAddMovementMenu("ShipMovement"));
        movements.getItems().add(createAddMovementMenu("CargoMovement"));

        Menu evaluators = new Menu("_Evaluators");
        evaluators.setMnemonicParsing(true);
        evaluators.getItems().add(createAddEvaluatorMenu("QuayOccupancyEvaluator"));
        evaluators.getItems().add(createAddEvaluatorMenu("ShipThroughputEvaluator"));
        evaluators.getItems().add(createAddEvaluatorMenu("ShipFlagEvaluator"));
        evaluators.getItems().add(createAddEvaluatorMenu("CargoDecompositionEvaluator"));

        Menu menuActions = new Menu("_Actions");
        menuActions.setMnemonicParsing(true);
        menuActions.getItems().add(add);
        menuActions.getItems().add(movements);
        menuActions.getItems().add(evaluators);

        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(createSimMenu());
        menuBar.getMenus().add(menuActions);
        return menuBar;
    }

    /* Creates a menu containing actions related to controlling the simulation */
    private Menu createSimMenu() {
        MenuItem pause = new MenuItem();
        pause.setMnemonicParsing(true);
        pause.textProperty().bind(viewModel.getPauseMenuText());
        pause.setOnAction(event -> viewModel.togglePaused());
        pause.setAccelerator(KeyCombination.keyCombination("Shortcut+P"));
        MenuItem lowSpeed = new MenuItem("_5 seconds per minute");
        lowSpeed.setMnemonicParsing(true);
        lowSpeed.setOnAction(e -> secondsPerMinute.set(5));
        lowSpeed.disableProperty().bind(secondsPerMinute.isEqualTo(5));
        MenuItem medSpeed = new MenuItem("_3 seconds per minute");
        medSpeed.setMnemonicParsing(true);
        medSpeed.setOnAction(e -> secondsPerMinute.set(3));
        medSpeed.disableProperty().bind(secondsPerMinute.isEqualTo(3));
        MenuItem highSpeed = new MenuItem("_1 second per minute");
        highSpeed.setMnemonicParsing(true);
        highSpeed.setOnAction(e -> secondsPerMinute.set(1));
        highSpeed.disableProperty().bind(secondsPerMinute.isEqualTo(1));
        Menu menuSim = new Menu("_Simulation");
        menuSim.setMnemonicParsing(true);
        Menu speed = new Menu("_Speed");
        speed.setMnemonicParsing(true);
        speed.getItems().add(lowSpeed);
        speed.getItems().add(medSpeed);
        speed.getItems().add(highSpeed);
        menuSim.getItems().add(pause);
        menuSim.getItems().add(speed);
        return menuSim;
    }

    /* Creates a menu item that, when clicked, prompts for a new ship to be added */
    private MenuItem createAddShipMenu() {
        MenuItem addShip = new MenuItem("New _ship...");
        addShip.setMnemonicParsing(true);
        Random random = new Random();
        addShip.setOnAction(event -> {
            var validShipTypes = List.of("BulkCarrier", "ContainerShip");
            var shipType = getChoice("Add Ship",
                    "Please choose the ship's type", "Ship type:",
                    validShipTypes.get(0),
                    validShipTypes.toArray(new String[0]));
            if (shipType.isEmpty()) {
                return;
            }

            var defaultShipNumber = 1000000 + Ship.getShipRegistry().size();
            var imoNumber = getResponse("Add Ship",
                    "Please enter the ship IMO number",
                    "IMO number:",
                    defaultShipNumber);
            // fail if it fails the bounds specified in Ship#Constructor
            if (imoNumber.isEmpty() || imoNumber.get() < 1000000 || imoNumber.get() > 9999999) {
                return;
            }
            /* Can't create a new ship with the same IMO number as an existing one */
            if (Ship.shipExists(imoNumber.get())) {
                viewModel.createErrorDialog("Cannot create ship",
                        "Ship with IMO number " + imoNumber.get() + " already exists");
                return;
            }

            var defaultShipNames = List.of("Serenity", "Freedom", "Osprey", "Destiny",
                    "Odyssey", "Tranquility");
            var shipName = getResponse("Add Ship",
                    "Please enter the ship name",
                    "Ship Name:",
                    defaultShipNames.get(random.nextInt(defaultShipNames.size())));
            if (shipName.isEmpty()) {
                return;
            }

            var defaultShipOrigin = List.of("Australia", "New Zealand", "Japan", "USA",
                    "United Kingdom", "China");
            var shipOrigin = getResponse("Add Ship",
                    "Please enter the ship's origin country",
                    "Ship Origin:",
                    defaultShipOrigin.get(random.nextInt(defaultShipOrigin.size())));
            if (shipOrigin.isEmpty()) {
                return;
            }

            var validNauticalFlags = NauticalFlag.values();
            var shipNauticalFlag = getChoice("Add Ship",
                    "Please choose the ship's nautical flag", "Ship's flag:",
                    NauticalFlag.NOVEMBER,
                    validNauticalFlags);
            if (shipNauticalFlag.isEmpty()) {
                return;
            }

            var defaultShipCapacity = 100;
            var capacity = getResponse("Add Ship",
                    "Please enter the ship's cargo capacity",
                    "Ship Capacity:",
                    defaultShipCapacity);
            // fail if it fails the bounds specified in Ship#Constructor
            if (capacity.isEmpty() || capacity.get() < 0) {
                return;
            }

            Ship newShip;
            if (shipType.get().equals("BulkCarrier")) {
                newShip = new BulkCarrier(imoNumber.get(), shipName.get(), shipOrigin.get(),
                        shipNauticalFlag.get(), capacity.get());
            } else {
                newShip = new ContainerShip(imoNumber.get(), shipName.get(), shipOrigin.get(),
                        shipNauticalFlag.get(), capacity.get());
            }
            //  Quay (If valid)
            var choice = chooseShipLocation("Add Ship to Quay or save for later",
                    "Please choose either a quay or save for later", newShip);
            if (choice.isEmpty()) {
                return;
            }
            if (choice.get() instanceof Quay) {
                Quay q = ((Quay) choice.get());
                // if another ship has docked since choice was made (if sim is running)
                if (q.isEmpty()) {
                    q.shipArrives(newShip);
                } else {
                    viewModel.createErrorDialog("Cannot add ship to Quay",
                            "Since selecting that Quay (" + q + ") it has become occupied. "
                                    +
                                    "Try pausing the simulation first.");
                    return;
                }
            }
            viewModel.registerChange();
        });
        return addShip;
    }

    /* Creates a menu item that, when clicked, prompts for a new cargo to be added */
    private MenuItem createAddCargoMenu() {
        MenuItem addCargo = new MenuItem("New _cargo...");
        addCargo.setMnemonicParsing(true);
        Random random = new Random();
        addCargo.setOnAction(e -> {
            var validShipTypes = List.of("BulkCargo", "Container");
            var shipType = getChoice("Add Cargo",
                    "Please choose the cargo's type", "Cargo type:",
                    validShipTypes.get(0),
                    validShipTypes.toArray(new String[0]));
            if (shipType.isEmpty()) {
                return;
            }

            var defaultCargoNumber = 1 + Cargo.getCargoRegistry().size();
            var cargoId = getResponse("Add Cargo",
                    "Please enter the cargo ID",
                    "cargo ID:",
                    defaultCargoNumber);
            // fail if it fails the bounds specified in Cargo#Constructor
            if (cargoId.isEmpty() || cargoId.get() < 1) {
                return;
            }
            /* Can't create a new ship with the same ID as an existing one */
            if (Cargo.cargoExists(cargoId.get())) {
                viewModel.createErrorDialog("Cannot create cargo",
                        "Cargo with cargo ID " + cargoId.get() + " already exists");
                return;
            }


            var defaultDestination = List.of("Australia", "New Zealand", "Japan", "USA",
                    "United Kingdom", "China");
            var cargoDestination = getResponse("Add Cargo",
                    "Please enter the cargo's destination country",
                    "Cargo destination:",
                    defaultDestination.get(random.nextInt(defaultDestination.size())));
            if (cargoDestination.isEmpty()) {
                return;
            }
            var validCargoTypes = shipType.get().equals("BulkCargo") ? BulkCargoType.values() :
                    ContainerType.values();
            var cargoType = getChoice("Add Cargo",
                    "Please choose the cargo type", "Cargo type:",
                    validCargoTypes[0],
                    validCargoTypes);
            if (cargoType.isEmpty()) {
                return;
            }

            Cargo newCargo;
            if (shipType.get().equals("BulkCargo")) {
                var cargoTonnage = getResponse("Add Cargo",
                        "Please enter the cargo weight in tonnes",
                        "cargo weight:",
                        100);
                // fail if it fails the bounds specified in Cargo#Constructor
                if (cargoTonnage.isEmpty() || cargoTonnage.get() < 1) {
                    return;
                }
                newCargo = new BulkCargo(cargoId.get(), cargoDestination.get(), cargoTonnage.get(),
                        (BulkCargoType) cargoType.get());
            } else {
                newCargo = new Container(cargoId.get(), cargoDestination.get(),
                        (ContainerType) cargoType.get());
            }
            viewModel.registerChange();
        });
        return addCargo;
    }

    /* Creates a menu item that, when clicked, prompts for a new quay to be added */
    private MenuItem createAddQuayMenu() {
        MenuItem addQuay = new MenuItem("New _quay...");
        addQuay.setMnemonicParsing(true);
        Random random = new Random();
        addQuay.setOnAction(e -> {
            var validQuayTypes = List.of("BulkQuay", "ContainerQuay");
            var quayType = getChoice("Add Quay",
                    "Please choose the quays's type", "Quay type:",
                    validQuayTypes.get(0),
                    validQuayTypes.toArray(new String[0]));
            if (quayType.isEmpty()) {
                return;
            }

            var defaultQuayNumber = 1 + viewModel.getPort().getQuays().size();
            var quayNum = getResponse("Add Quay",
                    "Please enter the quay ID",
                    "quay ID:",
                    defaultQuayNumber);
            // fail if it fails the bounds specified in Quay#Constructor
            if (quayNum.isEmpty() || quayNum.get() < 1) {
                return;
            }

            var cargoCapacity = getResponse("Add Quay",
                    "Please enter the maximum amount of cargo this quay can handle",
                    "Max cargo:",
                    100);
            // fail if it fails the bounds specified in Quay#Constructor
            if (cargoCapacity.isEmpty() || cargoCapacity.get() < 1) {
                return;
            }

            Quay newQuay;
            if (quayType.get().equals("BulkQuay")) {
                newQuay = new BulkQuay(quayNum.get(), cargoCapacity.get());
            } else {
                newQuay = new ContainerQuay(quayNum.get(), cargoCapacity.get());
            }

            viewModel.getPort().addQuay(newQuay);
            viewModel.getNumQuays().set(viewModel.getNumQuays().get() + 1);
            viewModel.registerChange();
        });
        addQuay.disableProperty().bind(Bindings.greaterThan(viewModel.getNumQuays(),
                MAX_QUAYS - 1));
        return addQuay;
    }

    /* Creates a menu item that, when clicked, prompts for a new movement to be added */
    private MenuItem createAddMovementMenu(String type) {
        MenuItem addMovement = new MenuItem("New _" + type + "...");
        addMovement.setMnemonicParsing(true);
        addMovement.setOnAction(e -> {
            if (!(type.equals("CargoMovement") || type.equals("ShipMovement"))) {
                // can't create generic movement
                return;
            }
            var encoding = getResponse("Add " + type,
                    "Please enter the encoding of the movement that you would like to add",
                    "Encoding:",
                    "");
            // fail if it fails the bounds specified in Movement#Constructor
            if (encoding.isEmpty()) {
                return;
            }
            Movement newMovement;
            try {
                if (type.equals("CargoMovement")) {
                    newMovement = CargoMovement.fromString(encoding.get());
                } else {
                    newMovement = ShipMovement.fromString(encoding.get());
                }
                viewModel.getPort().addMovement(newMovement);
            } catch (Exception exception) {
                viewModel.createErrorDialog("Error creating movement",
                        exception.getMessage());
                return;
            }
            viewModel.registerChange();
        });
        return addMovement;
    }

    /* Creates a menu item that, when clicked, prompts for a new evaluator to be added */
    private MenuItem createAddEvaluatorMenu(String type) {
        MenuItem addEval = new MenuItem("New _" + type + "...");
        addEval.setMnemonicParsing(true);
        addEval.setOnAction(e -> {
            StatisticsEvaluator ev = null;
            switch (type) {
                case "QuayOccupancyEvaluator":
                    ev = new QuayOccupancyEvaluator(viewModel.getPort());
                    break;
                case "ShipThroughputEvaluator":
                    ev = new ShipThroughputEvaluator();
                    break;
                case "CargoDecompositionEvaluator":
                    ev = new CargoDecompositionEvaluator();
                    break;
                case "ShipFlagEvaluator":
                    ev = new ShipFlagEvaluator();
                    break;
                default:
                    return;
            }
            viewModel.getPort().addStatisticsEvaluator(ev);

            viewModel.registerChange();
        });
        return addEval;
    }

    /* Creates a menu item that, when clicked, prompts for the state of the model to be saved */
    private MenuItem createSaveAsMenuItem() {
        MenuItem saveAs = new MenuItem("Save _As...");
        saveAs.setMnemonicParsing(true);
        saveAs.setOnAction(event -> {
            var filename = getResponse("Save to port file",
                    "Please enter the path of the file to save to", "port file name", "");
            if (filename.isEmpty()) {
                return;
            }
            try {
                viewModel.saveAs(new FileWriter(filename.get()));
            } catch (IOException e) {
                viewModel.createErrorDialog("Error saving to file",
                        e.getMessage());
                return;
            }
            viewModel.createSuccessDialog("Saved files successfully",
                    "Saved to \"" + filename.get() + "\" successfully.");
        });
        saveAs.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
        return saveAs;
    }

    /* Prompts the user to choose a quay from a list of all the port's quays or to add later */
    private Optional<Object> chooseShipLocation(String title, String header, Ship ship) {
        var options = new TreeMap<String, Object>();
        for (Quay quay : viewModel.getPort().getQuays()) {
            if (quay.isEmpty() && ship.canDock(quay)) {
                options.put(quay.toString(), quay);
            }
        }
        options.put("Add ship later", "");
        var choice = getChoice(title, header, "Location:",
                options.keySet().toArray(new String[0])[0],
                options.keySet().toArray(new String[0]));
        if (choice.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(options.get(choice.get()));
    }


    /* Creates a non-editable text area to display some text information */
    private TextArea createInfoBox(StringProperty contents, int rowCount) {
        var infoBox = new TextArea();
        infoBox.textProperty().bind(contents);
        infoBox.setEditable(false);
        infoBox.setFocusTraversable(false);
        infoBox.setWrapText(false);
        infoBox.setFont(Font.font(14));
        infoBox.setPrefRowCount(rowCount);
        infoBox.setPrefWidth(200);
        return infoBox;
    }

    /***
     * Prompts the user for a textual response via a dialog box.
     *
     * @param title title of dialog box window
     * @param header header text of dialog box
     * @param label label text to display beside input box
     * @param defaultValue initial contents of the input box
     * @return value entered by the user
     * @given
     */
    public Optional<String> getResponse(String title, String header,
                                        String label, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(label);
        dialog.setGraphic(null);
        return dialog.showAndWait();
    }

    /***
     * Prompts the user for a numeric response via a dialog box.
     *
     * @param title title of dialog box window
     * @param header header text of dialog box
     * @param label label text to display beside input box
     * @param defaultValue initial contents of the input box
     * @return value entered by the user
     * @given
     */
    public Optional<Integer> getResponse(String title, String header,
                                         String label, int defaultValue) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(defaultValue));
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(label);
        dialog.setGraphic(null);
        // Only allow numeric values to be entered
        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                dialog.getEditor().setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        return dialog.showAndWait().map(Integer::valueOf);
    }

    /* Prompts the user for a choice from a list of options */
    @SafeVarargs
    private <T> Optional<T> getChoice(String title, String header, String label,
                                      T defaultChoice, T... choices) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(defaultChoice, choices);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(label);
        dialog.setGraphic(null);
        return dialog.showAndWait();
    }

    /**
     * Initialises the view and begins the timer responsible for performing ticks
     *
     * @given
     */
    public void run() {
        final long nanosPerSecond = 1000000000;

        new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                if (viewModel.isChanged()) {
                    viewModel.notChanged();
                    canvas.draw();
                }

                if (viewModel.getPaused().get()) {
                    lastNanoTime = currentNanoTime;
                    return;
                }

                timeSpentUnpaused += currentNanoTime - lastNanoTime;
                lastNanoTime = currentNanoTime;

                if (timeSpentUnpaused > secondsPerMinute.get() * nanosPerSecond) {
                    timeSpentUnpaused = 0;
                    viewModel.elapseOneMinute();
                }
            }
        }.start();

        this.stage.show();
        this.canvas.draw();
    }
}
