package portsim.display;

import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import portsim.cargo.BulkCargo;
import portsim.cargo.Container;
import portsim.evaluators.*;
import portsim.port.Port;
import portsim.ship.BulkCarrier;
import portsim.ship.ContainerShip;
import portsim.ship.Ship;
import portsim.util.BadEncodingException;

import java.io.*;
import java.util.List;
import java.util.StringJoiner;

/**
 * View model for the Port Simulation GUI.
 * <p>
 * Everything in this class was provided except for:
 * <ul>
 *     <li>{@link #getShipContentsHandler()}</li>
 *     <li>{@link #updateEvaluatorText()}</li>
 *     <li>{@link #saveAs(Writer)}</li>
 * </ul>
 */
public class ViewModel {
    /**
     * Port  model containing ships and quays
     */
    private final Port port;

    /**
     * Whether the state of the model has changed
     */
    private final BooleanProperty changed = new SimpleBooleanProperty(false);

    /**
     * Contents of ship information text box
     */
    private final StringProperty shipInfoText = new SimpleStringProperty(
            "No ship selected");

    /**
     * Whether the simulation is paused or not
     */
    private final BooleanProperty paused = new SimpleBooleanProperty(true);

    /**
     * Text appended to window title when the simulation is paused
     */
    private final StringProperty pausedStatusText = new SimpleStringProperty(" (Paused)");

    /**
     * Text displayed in the "toggle pause" menu item
     */
    private final StringProperty pauseMenuText = new SimpleStringProperty("Un_pause");

    /**
     * Number of quays managed by the port
     */
    private final IntegerProperty numQuays = new SimpleIntegerProperty();

    /**
     * Text displayed in the label showing the cargo on ships
     */
    private final StringProperty cargoManifestText = new SimpleStringProperty(
            "Cargo Manifest");

    /**
     * Text displayed in the time label
     */
    private final StringProperty timeText = new SimpleStringProperty(
            "Time: --:--");

    /**
     * Text displayed in the label showing the evaluator statistics
     */
    private final StringProperty evaluatorsText = new SimpleStringProperty(
            "Evaluators Text");

    /**
     * The currently selected (clicked) ships
     */
    private final ObjectProperty<Ship> selectedShip =
            new SimpleObjectProperty<>();

    /**
     * File path of the port file that we loaded from
     */
    private final String defaultPortSaveLocation;

    /**
     * Creates a new view model and constructs a port by reading from the given filenames.
     *
     * @param filename filename specifying the path to: the port file
     * @throws IOException          if loading from the file specified generates an
     *                              IOException
     * @throws BadEncodingException if the file is invalid according to
     *                              {@link Port#initialisePort(Reader)}
     * @require filename != null &amp;&amp; filenames.size() == 1
     * @given
     */
    public ViewModel(String filename) throws IOException, BadEncodingException {
        this.defaultPortSaveLocation = filename;

        this.port = Port.initialisePort(new FileReader(filename));

        this.numQuays.set(port.getQuays().size());

        this.selectedShip.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                shipInfoText.set("No ship selected");
            } else {
                shipInfoText.set(generateShipInfoText(newValue));
            }
        });

    }

    /**
     * Returns the property storing the contents of the selected ships cargo info text box.
     *
     * @return cargo contents info text box
     * @given
     */
    public StringProperty getCargoManifestText() {
        return cargoManifestText;
    }

    /**
     * Returns the property storing the contents of the selected ships cargo info text box.
     *
     * @return evaluators info text box
     * @given
     */
    public StringProperty getEvaluatorsText() {
        return evaluatorsText;
    }

    /**
     * Returns the property storing the number of quays managed by the port.
     *
     * @return number of quays property
     * @given
     */
    public IntegerProperty getNumQuays() {
        return numQuays;
    }

    /**
     * Returns the property storing the text shown for the pause/unpause menu item.
     *
     * @return pause/unpause menu item text property
     * @given
     */
    public StringProperty getPauseMenuText() {
        return pauseMenuText;
    }

    /**
     * Returns the property storing whether the simulation is paused.
     *
     * @return paused property
     * @given
     */
    public BooleanProperty getPaused() {
        return paused;
    }

    /**
     * Returns the property storing the text appended to window title when the simulation is paused.
     *
     * @return paused status text property
     * @given
     */
    public StringProperty getPausedStatusText() {
        return pausedStatusText;
    }

    /**
     * Returns the port linked to this view model.
     *
     * @return port
     * @given
     */
    public Port getPort() {
        return port;
    }

    /**
     * Returns the property storing the currently selected ships; or null if no
     * ship is selected.
     *
     * @return currently selected ship property
     * @given
     */
    public ObjectProperty<Ship> getSelectedShip() {
        return selectedShip;
    }

    /**
     * Returns an event handler for when the "Dump Cargo Manifest"
     * button is clicked.
     * <p>
     * The overall purpose of this event handler is to update the {@code cargoManifestText} string
     * property with the string representation of the  cargo on the currently selected ship, as
     * returned by {@link BulkCarrier#getCargo()} or {@link ContainerShip#getCargo()}.
     * <p>
     * The event handler should perform the following actions:
     * <ol>
     * <li>If no ship is currently selected (i.e. {@link #getSelectedShip()} ()} is storing
     * null), then the event handler should return immediately without taking any further action.
     * </li>
     * <li>If calling {@link BulkCarrier#getCargo()} or {@link ContainerShip#getCargo()} for the
     * currently selected ship returns a value indicating there is no cargo on board , then the
     * {@code cargoManifestText} property should be set to {@code "No cargo on board."}.</li>
     * <li>If the ship currently selected  is a {@link BulkCarrier}, the {@code
     * cargoManifestText} property should be set to the {@link BulkCargo#toString()} representation
     * of the cargo onboard. <br> For Example:<pre>BulkCargo 42 to Brazil [OIL - 420]</pre></li>
     * <li>If the ship currently selected  is a {@link ContainerShip}, the {@code
     * cargoManifestText} property should be set to a String containing the
     * {@link Container#toString()} representation of each of the cargo onboard separated by a
     * comma and {@link System#lineSeparator()}.
     * <br><b>Note: The last line should not have a
     * trailing comma</b><br> For Example:
     * <pre>Container 43 to Australia [OTHER],</pre>
     * <pre>Container 66 to Australia [OPEN_TOP],</pre>
     * <pre>Container 92 to Australia [OPEN_TOP]</pre></li>
     * </ol>
     *
     * @return event handler for "Show Cargo Manifest for Selected Ship" button
     */
    public EventHandler<ActionEvent> getShipContentsHandler() {
        return actionEvent -> {
            if (getSelectedShip() == null) {
                return; // Exiting method as there is no ship selected.
            }

            Ship ship = getSelectedShip().get();

            if (ship instanceof BulkCarrier) {
                BulkCargo cargo = ((BulkCarrier) ship).getCargo();

                if (cargo == null) {
                    cargoManifestText.set("No cargo on board.");
                } else {
                    cargoManifestText.set(cargo.toString());
                }

            } else if (ship instanceof ContainerShip) {
                List<Container> cargo = ((ContainerShip) ship).getCargo();

                if (cargo.isEmpty()) {
                    cargoManifestText.set("No cargo on board.");
                } else {
                    // Joining the containers and updating the cargoManifest.
                    StringJoiner containers = new StringJoiner("," + System.lineSeparator());
                    for (Container container : cargo) {
                        containers.add(container.toString());
                    }
                    cargoManifestText.set(containers.toString());
                }
            }
        };
    }

    /**
     * Returns the property storing the contents of the ship info text box.
     *
     * @return ship info text box
     * @given
     */
    public StringProperty getShipInfoText() {
        return shipInfoText;
    }

    /**
     * Returns the property storing the contents of the time info text box.
     *
     * @return time info text box
     * @given
     */
    public StringProperty getTimeText() {
        return timeText;
    }

    /**
     * Returns whether or not the state of the model has changed since it was last checked for a
     * change.
     *
     * @return has the model changed since last check
     * @given
     */
    public boolean isChanged() {
        return changed.get();
    }

    /**
     * The purpose of this method is to update the {@code evaluatorsText} string.
     * Called each {@link #elapseOneMinute()}
     * <p>
     * The string should be set to the following with each line separated by
     * {@link System#lineSeparator()}.
     * <ul>
     * <li>If there are no evaluators at the port then the evaluatorsText should be set to
     * {@code "No Evaluators Present"}.
     * </li>
     * <li>If there are evaluators present their contents should be added in the order that they
     * appear in the list of evaluator in port ({@link Port#getEvaluators()}).
     * The contents of each evaluator is as follows with each bullet point indicating a new line.
     * <ol>
     * <li>If the port has a {@link QuayOccupancyEvaluator}:
     * <ul>
     *     <li>The name of the evaluator</li>
     *     <li>The current number of Quay's occupied should be printed as the following format
     *     <pre>num Quay(s) currently occupied</pre> where num is the number of occupied quays</li>
     * </ul></li>
     * <li>If the port has a {@link ShipFlagEvaluator}:
     * <ul>
     *     <li>The name of the evaluator</li>
     *     <li>For each unique Country flag that the evaluator has seen it should print that
     *     statistic in the following format
     *     <pre>country : num</pre> where country is the name of the country flag and num is the
     *     number of times that flag has been seen</li>
     * </ul></li>
     * <li>If the port has a {@link ShipThroughputEvaluator}:
     * <ul>
     *     <li>The name of the evaluator</li>
     *     <li>The current number of ships that have passed through the port in the last hour should
     *     be printed as the following format
     *     <pre>num Ships passed in the last hour</pre> where num is the number of ships
     *     through the port in the last hour</li>
     * </ul></li>
     * <li>If the port has a {@link CargoDecompositionEvaluator}:
     * <ul>
     *     <li>The name of the evaluator</li>
     *     <li>For each unique cargo type that the evaluator has seen it should print that
     *     statistic in the following format
     *     <pre>cargo : num</pre> where cargo is the name of the cargo class and num is the
     *     number of times that flag has been seen</li>
     * </ul></li>
     * </ol></li></ul>
     * For example:
     * <pre>
     * QuayOccupancyEvaluator
     * 4 Quay(s) currently occupied
     * ShipFlagEvaluator
     * New Zealand : 2
     * Australia : 4
     * ShipThroughputEvaluator
     * 2 Ships passed in the last hour
     * CargoDecompositionEvaluator
     * Container : 7
     * BulkCargo : 12
     * </pre>
     */
    public void updateEvaluatorText() {
        List<StatisticsEvaluator> evaluators = port.getEvaluators();

        if (evaluators.isEmpty()) {
            evaluatorsText.set("No Evaluators Present");
            return;
        }

        StringJoiner joiner = new StringJoiner(System.lineSeparator());

        for (StatisticsEvaluator evaluator : evaluators) {

            // Adding the name of the evaluator
            joiner.add(evaluator.getClass().getSimpleName());

            if (evaluator instanceof QuayOccupancyEvaluator) {
                joiner.add(((QuayOccupancyEvaluator) evaluator).getQuaysOccupied()
                        + " Quay(s) currently occupied");

            } else if (evaluator instanceof ShipFlagEvaluator) {
                // Adding each country flag seen and their respective count
                ((ShipFlagEvaluator) evaluator).getFlagDistribution()
                        .forEach((key, value) -> joiner.add(key + " : " + value));

            } else if (evaluator instanceof ShipThroughputEvaluator) {
                joiner.add(((ShipThroughputEvaluator) evaluator).getThroughputPerHour()
                        + " Ships passed in the last hour");

            } else if (evaluator instanceof CargoDecompositionEvaluator) {
                // Adding each Cargo class seen and their respective count.
                ((CargoDecompositionEvaluator) evaluator).getCargoDistribution()
                        .forEach((key, value) -> joiner.add(key + " : " + value));
            }
        }
        evaluatorsText.set(joiner.toString());
    }

    /**
     * Saves the current state of the port simulation to the given writer.
     * <p>
     * The writer should be written to in the following format:
     * <pre>
     * Name
     * Time
     * numCargo
     * EncodedCargo
     * EncodedCargo...
     * numShips
     * EncodedShip
     * EncodedShip...
     * numQuays
     * EncodedQuay
     * EncodedQuay...
     * ShipQueue:numShipsInQueue:shipID,shipID,...
     * StoredCargo:numCargo:cargoID,cargoID,...
     * Movements:numMovements
     * EncodedMovement
     * EncodedMovement...
     * Evaluators:numEvaluators:EvaluatorSimpleName,EvaluatorSimpleName,...
     * </pre>
     * Where:
     * <ul>
     *   <li>Name is the name of the Port</li>
     *   <li>Time is the time elapsed since the simulation started</li>
     *   <li>numCargo is the total number of cargo in the simulation</li>
     *   <li>EncodedCargo is the encoded representation of each individual cargo in the
     *   simulation</li>
     *   <li>numShips is the total number of ships in the simulation</li>
     *   <li>EncodedShip is the encoded representation of each individual ship encoding in the
     *   simulation</li>
     *   <li>numQuays is the total number of quays in the Port</li>
     *   <li>EncodedQuay is the encoded representation of each individual quay in the
     *   simulation</li>
     *   <li>numShipsInQueue is the total number of ships in the ship queue
     *   in the port</li>
     *   <li>shipID is each ship's ID in the aforementioned queue</li>
     *   <li>numCargo is the total amount of stored cargo in the Port</li>
     *   <li>cargoID is each cargo's ID in the stored cargo list of Port</li>
     *   <li>numMovements is the number of movements in the queue of movements
     *   in Port</li>
     *   <li>EncodedMovement is the encoded representation of each individual Movement in the
     *   aforementioned list</li>
     *   <li>numEvaluators is the number of statistics evaluators in the Port</li>
     *   <li>EvaluatorSimpleName is the name given by {@link Class#getSimpleName()} for
     *   each evaluator in the aforementioned list</li>
     * </ul>
     * <p>
     * After all the data has been written, the writer should be closed.
     *
     * @param portWriter writer to which the port will be written
     * @throws IOException if an IOException occurs when writing to the writer
     * @see Port#encode()
     */
    public void saveAs(Writer portWriter) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(portWriter)) {
            writer.write(getPort().encode());
        }
    }

    /**
     * Elapses one minute in the model and updates the state of the GUI.
     *
     * @given
     */
    public void elapseOneMinute() {
        port.elapseOneMinute();
        timeText.set(String.format("Time: %02d:%02d", port.getTime() / 60, port.getTime() % 60));
        updateEvaluatorText();
        if (selectedShip.isNotNull().get()) {
            this.shipInfoText.set(generateShipInfoText(selectedShip.get()));
        }
        registerChange();
    }

    /* Generates the formatted information text for the given ship */
    private String generateShipInfoText(Ship ship) {
        StringJoiner lineJoiner = new StringJoiner(System.lineSeparator());
        lineJoiner.add("Name:\t\t" + ship.getName());
        lineJoiner.add("imoNumber\t" + ship.getImoNumber());
        lineJoiner.add("Ship type:\t\t" + ship.getClass().getSimpleName());
        lineJoiner.add("IMO number:\t" + ship.getImoNumber());
        lineJoiner.add("Nautical Flag: \t" + ship.getFlag());
        lineJoiner.add("Port of origin:\t" + ship.getOriginFlag());
        if (ship instanceof BulkCarrier) {
            BulkCargo c = ((BulkCarrier) ship).getCargo();
            lineJoiner.add("Carrying:      \t" + (c == null ? "Nothing" : c.getType()));
        } else if (ship instanceof ContainerShip) {
            lineJoiner.add("Carrying:      \t" + ((ContainerShip) ship).getCargo().size()
                    + " containers");
        }
        return lineJoiner.toString();
    }

    /**
     * Toggles whether the simulation is paused.
     *
     * @given
     */
    public void togglePaused() {
        this.paused.setValue(!this.paused.getValue());
        if (this.paused.get()) {
            this.pausedStatusText.setValue(" (Paused)");
            this.pauseMenuText.setValue("Un_pause");
        } else {
            this.pausedStatusText.setValue("");
            this.pauseMenuText.setValue("_Pause");
        }
    }

    /**
     * Saves the current state of the port simulation to the same file it was loaded
     * from when the application was launched.
     *
     * @throws IOException if an IOException occurs when writing to the file
     * @given
     */
    public void save() throws IOException {
        saveAs(new FileWriter(this.defaultPortSaveLocation));
    }

    /**
     * Acknowledges the model has changed, and sets the changed status to false.
     *
     * @given
     */
    public void notChanged() {
        changed.setValue(false);
    }

    /**
     * Registers that the model has changed, and the view needs to be updated.
     *
     * @given
     */
    public void registerChange() {
        changed.setValue(true);
    }

    /**
     * Creates and shows an error dialog.
     *
     * @param headerText  text to show in the dialog header
     * @param contentText text to show in the dialog content box
     * @given
     */
    public void createErrorDialog(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

    /**
     * Creates and shows a success dialog.
     *
     * @param headerText  text to show in the dialog header
     * @param contentText text to show in the dialog content box
     * @given
     */
    public void createSuccessDialog(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }
}
