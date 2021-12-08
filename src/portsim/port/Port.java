package portsim.port;

import portsim.cargo.Cargo;
import portsim.cargo.Container;
import portsim.evaluators.*;
import portsim.movement.CargoMovement;
import portsim.movement.Movement;
import portsim.movement.MovementDirection;
import portsim.movement.ShipMovement;
import portsim.ship.BulkCarrier;
import portsim.ship.ContainerShip;
import portsim.ship.Ship;
import portsim.util.BadEncodingException;
import portsim.util.Encodable;
import portsim.util.NoSuchCargoException;
import portsim.util.Tickable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static portsim.util.BadEncodingException.*;

/**
 * A place where ships can come and dock with Quays to load / unload their
 * cargo.
 * <p>
 * Ships can enter a port through its queue. Cargo is stored within the port at warehouses.
 */
public class Port implements Tickable, Encodable {

    /**
     * The name of this port used for identification
     */
    private String name;

    /**
     * The quays associated with this port
     */
    private List<Quay> quays;

    /**
     * The cargo currently stored at the port at warehouses. Cargo unloaded from trucks / ships
     */
    private List<Cargo> storedCargo;

    /**
     * The time since the simulation was started.
     */
    private long time;

    /**
     * The statistic evaluators associated with the port.
     */
    private List<StatisticsEvaluator> evaluators;

    /**
     * The queue of ships waiting to enter the port.
     */
    private ShipQueue shipQueue;

    /**
     * The queue of movements waiting to be processed. Ordered by the time of the movement
     * according to {@link Movement#getTime()}.
     */
    private PriorityQueue<Movement> movements;

    /**
     * Creates a new port with the given name.
     * <p>
     * The time since the simulation was started should be initialised as 0.
     * <p>
     * The list of quays in the port, stored cargo (warehouses) and statistics evaluators should be
     * initialised as empty lists.
     * <p>
     * An empty ShipQueue should be initialised, and a {@link PriorityQueue} should be initialised
     * to store movements ordered by the time of the movement (see {@link Movement#getTime()}).
     *
     * @param name name of the port
     */
    public Port(String name) {
        this(name, 0, new ShipQueue(), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Creates a new port with the given name, time elapsed, ship queue, quays and stored cargo.
     * <p>
     * The list of statistics evaluators should be initialised as an empty list.
     * <p>
     * A {@link PriorityQueue} should be initialised to store movements ordered by the time of the
     * movement (see {@link Movement#getTime()}).
     *
     * @param name        name of the port
     * @param time        number of minutes since simulation started
     * @param shipQueue   ships waiting to enter the port
     * @param quays       the port's quays
     * @param storedCargo the cargo stored at the port
     * @throws IllegalArgumentException if time &lt; 0
     */
    public Port(String name, long time, ShipQueue shipQueue, List<Quay> quays,
                List<Cargo> storedCargo) throws IllegalArgumentException {
        // Time cant be negative.
        if (time < 0) {
            throw new IllegalArgumentException("The time can't be less than zero: " + time);
        }

        this.name = name;
        this.time = time;
        this.shipQueue = shipQueue;
        this.quays = quays;
        this.storedCargo = storedCargo;
        this.evaluators = new ArrayList<>();

        // Storing movements ordered by the time of the movement
        this.movements = new PriorityQueue<>(Comparator.comparingLong(Movement::getTime));
    }

    /**
     * Creates a port instance by reading various {@link Ship}, {@link Quay}, {@link Cargo},
     * {@link Movement} and {@link StatisticsEvaluator} entities from the given reader.
     * <p>
     * The provided file should be in the format:
     * <pre>
     *  Name
     *  Time
     *  numCargo
     *  EncodedCargo
     *  EncodedCargo...
     *  numShips
     *  EncodedShip
     *  EncodedShip...
     *  numQuays
     *  EncodedQuay
     *  EncodedQuay...
     *  ShipQueue:NumShipsInQueue:shipID,shipId
     *  StoredCargo:numCargo:cargoID,cargoID
     *  Movements:numMovements
     *  EncodedMovement
     *  EncodedMovement...
     *  Evaluators:numEvaluators:EvaluatorSimpleName,EvaluatorSimpleName
     *  </pre>
     * As specified by {@link #encode()}
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <ul>
     * <li>The time is not a valid long (i.e. cannot be parsed by
     * {@link Long#parseLong(String)}).</li>
     * <li>The number of cargo is not an integer (i.e. cannot be parsed by
     * {@link Integer#parseInt(String)}).</li>
     * <li>The number of cargo to be read in does not match the number
     * specified above. (ie. too many / few encoded cargo following the
     * number)</li>
     * <li>An encoded cargo line throws a {@link BadEncodingException}</li>
     * <li>The number of ships is not an integer (i.e. cannot be parsed by
     * {@link Integer#parseInt(String)}).</li>
     * <li>The number of ship to be read in does not match the number
     * specified above. (ie. too many / few encoded ships following the
     * number)</li>
     * <li>An encoded ship line throws a {@link BadEncodingException}</li>
     * <li>The number of quays is not an integer (i.e. cannot be parsed by
     * {@link Integer#parseInt(String)}).</li>
     * <li>The number of quays to be read in does not match the number
     * specified above. (ie. too many / few encoded quays following the
     * number)</li>
     * <li>An encoded quay line throws a {@link BadEncodingException}</li>
     * <li>The shipQueue does not follow the last encoded quay</li>
     * <li>The number of ships in the shipQueue is not an integer (i.e. cannot
     * be parsed by {@link Integer#parseInt(String)}).</li>
     * <li>The imoNumber of the ships in the shipQueue are not valid longs.
     * (i.e. cannot be parsed by {@link Long#parseLong(String)}).</li>
     * <li>Any imoNumber read does not correspond to a valid ship in the
     * simulation</li>
     * <li>The storedCargo does not follow the encoded shipQueue</li>
     * <li>The number of cargo in the storedCargo is not an integer (i.e. cannot
     * be parsed by {@link Integer#parseInt(String)}).</li>
     * <li>The id of the cargo in the storedCargo are not valid Integers.
     * (i.e. cannot be parsed by {@link Integer#parseInt(String)}).</li>
     * <li>Any cargo id read does not correspond to a valid cargo in the
     * simulation</li>
     * <li>The movements do not follow the encoded storedCargo</li>
     * <li>The number of movements is not an integer (i.e. cannot be parsed
     * by {@link Integer#parseInt(String)}).</li>
     * <li>The number of movements to be read in does not match the number
     * specified above. (ie. too many / few encoded movements following the
     * number)</li>
     * <li>An encoded movement line throws a {@link BadEncodingException}</li>
     * <li>The evaluators do not follow the encoded movements</li>
     * <li>The number of evaluators is not an integer (i.e. cannot be parsed
     * by {@link Integer#parseInt(String)}).</li>
     * <li>The number of evaluators to be read in does not match the number
     * specified above. (ie. too many / few encoded evaluators following the
     * number)</li>
     * <li>An encoded evaluator name does not match any of the possible {@link StatisticsEvaluator}
     * classes</li>
     * <li>If any of the following lines are missing:
     *      <ol>
     *          <li>Name</li>
     *          <li>Time</li>
     *          <li>Number of Cargo</li>
     *          <li>Number of Ships</li>
     *          <li>Number of Quays</li>
     *          <li>ShipQueue</li>
     *          <li>StoredCargo</li>
     *          <li>Movements</li>
     *          <li>Evaluators</li>
     *      </ol>
     * </li>
     * </ul>
     *
     * @param reader reader from which to load all info
     * @return port created by reading from given reader
     * @throws IOException          if an IOException is encountered when reading from the reader
     * @throws BadEncodingException if the reader reads a line that does not adhere to the rules
     *                              above indicating that the contents of the reader are invalid
     */
    public static Port initialisePort(Reader reader) throws IOException, BadEncodingException {
        assertNotNull(reader);

        BufferedReader buffered = new BufferedReader(reader);

        String name = buffered.readLine();
        long time = parseEncoding(buffered.readLine(), Long::parseLong);

        // Decoding Cargos
        int numCargo = parseEncoding(buffered.readLine(), Integer::parseInt);
        for (int i = 0; i < numCargo; i++) {
            Cargo.fromString(buffered.readLine());
        }

        // Decoding Ships
        int numShips = parseEncoding(buffered.readLine(), Integer::parseInt);
        for (int i = 0; i < numShips; i++) {
            Ship.fromString(buffered.readLine());
        }

        // Decoding Quays
        int numQuays = parseEncoding(buffered.readLine(), Integer::parseInt);
        List<Quay> quays = new ArrayList<>();
        for (int i = 0; i < numQuays; i++) {
            quays.add(Quay.fromString(buffered.readLine()));
        }

        // Decoding the shipQueue and storedCargo
        ShipQueue shipQueue = ShipQueue.fromString(buffered.readLine());
        List<Cargo> storedCargo = storedCargoFromString(buffered.readLine());

        Port decodedPort;
        try {
            decodedPort = new Port(name, time, shipQueue, quays, storedCargo);
        } catch (IllegalArgumentException e) {
            throw new BadEncodingException("Invalid Argument(s) for Port!", e);
        }
        // Adding the movements and evaluators to the port.
        addMovementsFromString(buffered, decodedPort);
        addEvaluatorsFromString(buffered.readLine(), decodedPort);

        assertTrue("Expected EOF!", buffered.readLine() == null);

        return decodedPort;
    }

    /**
     * Private helper method for {@link #initialisePort(Reader)} that creates and adds the encoded
     * movements to the port.
     *
     * @param reader The reader that is being used to initialise the port
     * @param port   The port being initialised
     * @throws IOException          If an IOException is encountered when reading from the reader
     * @throws BadEncodingException If any of the conditions outlined in
     *                              {@link #initialisePort(Reader)} are violated
     * @require reader != null && port != null
     */
    private static void addMovementsFromString(BufferedReader reader, Port port)
            throws IOException, BadEncodingException {
        // Splitting the encoding and ensuring it is valid with 2 parts.
        String[] movementParts = encodingSplit(reader.readLine(), ":", 2);
        assertEqual("Movements", movementParts[0]);

        int numMovements = parseEncoding(movementParts[1], Integer::parseInt);

        // Decoding each Movement and adding it to the port
        for (int i = 0; i < numMovements; i++) {
            String encodedMovement = reader.readLine();
            assertNotNull(encodedMovement);

            if (encodedMovement.startsWith("CargoMovement")) {
                try {
                    port.addMovement(CargoMovement.fromString(encodedMovement));
                } catch (IllegalArgumentException e) {
                    throw new BadEncodingException(encodedMovement + " occurs before the port time",
                            e);
                }

            } else if (encodedMovement.startsWith("ShipMovement")) {
                try {
                    port.addMovement(ShipMovement.fromString(encodedMovement));
                } catch (IllegalArgumentException e) {
                    throw new BadEncodingException(encodedMovement + " occurs before the port time",
                            e);
                }

            } else {
                throw new BadEncodingException("Invalid Movement type: " + encodedMovement);
            }
        }
    }

    /**
     * Private helper method for {@link #initialisePort(Reader)} that creates and adds evaluators
     * from an encoded string to a ports control.
     *
     * @param encoding The encoded evaluators as a string.
     * @param port     The port that the evaluators are being added to.
     * @throws BadEncodingException If any of the conditions outlined in
     *                              {@link #initialisePort(Reader)} are violated
     * @require port != null
     */
    private static void addEvaluatorsFromString(String encoding, Port port)
            throws BadEncodingException {
        // Splitting the encoding and ensuring it is valid with 3 parts.
        String[] evaluatorParts = encodingSplit(encoding, ":", 3);
        assertEqual("Evaluators", evaluatorParts[0]);

        int numEvaluators = parseEncoding(evaluatorParts[1], Integer::parseInt);

        // Exiting method if there are no evaluators to add!
        if (numEvaluators == 0 && "".equals(evaluatorParts[2])) {
            return;
        }

        String[] evaluators = encodingSplit(evaluatorParts[2], ",", numEvaluators);

        // Decoding each evaluator and adding it to the port
        for (int i = 0; i < numEvaluators; i++) {
            if ("CargoDecompositionEvaluator".equals(evaluators[i])) {
                port.addStatisticsEvaluator(new CargoDecompositionEvaluator());

            } else if ("QuayOccupancyEvaluator".equals(evaluators[i])) {
                port.addStatisticsEvaluator(new QuayOccupancyEvaluator(port));

            } else if ("ShipFlagEvaluator".equals(evaluators[i])) {
                port.addStatisticsEvaluator(new ShipFlagEvaluator());

            } else if ("ShipThroughputEvaluator".equals(evaluators[i])) {
                port.addStatisticsEvaluator(new ShipThroughputEvaluator());

            } else {
                throw new BadEncodingException("Invalid StatisticEvaluator name: " + evaluators[i]);
            }
        }
    }

    /**
     * Private helper method for {@link #initialisePort(Reader)} that creates a list of storedCargo
     * from an encoded string.
     *
     * @param encoding The encoded string of stored cargo.
     * @return The decoded list of storedCargo.
     * @throws BadEncodingException If any of the conditions outlined in
     *                              {@link #initialisePort(Reader)} are violated
     */
    private static List<Cargo> storedCargoFromString(String encoding) throws BadEncodingException {
        // Splitting the encoding and ensuring it is valid with 3 parts.
        String[] storedCargoParts = encodingSplit(encoding, ":", 3);
        assertEqual("StoredCargo", storedCargoParts[0]);

        int numStoredCargo = parseEncoding(storedCargoParts[1], Integer::parseInt);

        // Exiting method if there is no cargo to add!
        if (numStoredCargo == 0 && "".equals(storedCargoParts[2])) {
            return new ArrayList<>();
        }

        String[] storedCargoIds = encodingSplit(storedCargoParts[2], ",", numStoredCargo);

        // Decoding each cargo and adding it to the list of storedCargo
        List<Cargo> storedCargo = new ArrayList<>();
        for (int i = 0; i < numStoredCargo; i++) {
            int id = parseEncoding(storedCargoIds[i], Integer::parseInt);

            Cargo cargo;
            try {
                cargo = Cargo.getCargoById(id);
            } catch (NoSuchCargoException e) {
                throw new BadEncodingException("Cargo ID " + id + " does not exist!", e);
            }
            storedCargo.add(cargo);
        }

        return storedCargo;
    }

    /**
     * Private helper method that splits an encoding into its parts, and checks the expected number.
     *
     * @param encoding       The encoding being split
     * @param delimiter      The delimiter to split at
     * @param expectedLength The expected length of the split encoding
     * @return the split encoding
     * @throws BadEncodingException If the encoding is null, or the expected length != the actual.
     * @require delimiter != null
     * @ensure the encoding has the expected number of parts.
     */
    private static String[] encodingSplit(String encoding, String delimiter,
                                          int expectedLength) throws BadEncodingException {
        assertNotNull(encoding);
        String[] parts = encoding.split(delimiter, -1);
        assertEqual(expectedLength, parts.length);

        return parts;
    }

    /**
     * Returns the cargo stored in warehouses at this port.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return port cargo
     */
    public List<Cargo> getCargo() {
        return new ArrayList<>(storedCargo);
    }

    /**
     * Returns the list of evaluators at the port.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return the ports evaluators
     */
    public List<StatisticsEvaluator> getEvaluators() {
        return new ArrayList<>(evaluators);
    }

    /**
     * Returns the queue of movements waiting to be processed.
     *
     * @return The queue of movements.
     */
    public PriorityQueue<Movement> getMovements() {
        return movements;
    }

    /**
     * Returns the name of this port.
     *
     * @return port's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a list of all quays associated with this port.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     * <p>
     * The order in which quays appear in this list should be the same as
     * the order in which they were added by calling {@link #addQuay(Quay)}.
     *
     * @return all quays
     */
    public List<Quay> getQuays() {
        return new ArrayList<>(quays);
    }

    /**
     * Returns the queue of ships waiting to be docked at this port.
     *
     * @return port's queue of ships
     */
    public ShipQueue getShipQueue() {
        return shipQueue;
    }

    /**
     * Returns the time since simulation started.
     *
     * @return time in minutes
     */
    public long getTime() {
        return time;
    }

    /**
     * Adds a movement to the PriorityQueue of movements.
     *
     * @param movement movement to add.
     * @throws IllegalArgumentException If the given movement's action time is less than the
     *                                  current number of minutes elapsed
     */
    public void addMovement(Movement movement) throws IllegalArgumentException {
        if (movement.getTime() < time) {
            throw new IllegalArgumentException("The given movement's action time is less than "
                    + "the current time.");
        }
        movements.add(movement);
    }

    /**
     * Processes a movement.
     * <p>
     * The action taken depends on the type of movement to be processed.
     * <p>
     * If the movement is a ShipMovement:
     * <ul>
     * <li>If the movement direction is {@link MovementDirection#INBOUND} then the ship should be
     * added to the ship queue.</li>
     * <li>If the movement direction is {@link MovementDirection#INBOUND} then any cargo stored in
     * the port whose destination is the ship's origin port should be added to the ship according
     * to {@link Ship#canLoad(Cargo)}.
     * Next, the ship should be removed from the quay it is currently docked in (if any).</li>
     * </ul>
     * If the movement is a CargoMovement:
     * <ul>
     * <li>If the movement direction is {@link MovementDirection#INBOUND} then all of the cargo
     * that is being moved should be added to the port's stored cargo.</li>
     * <li>If the movement direction is {@link MovementDirection#INBOUND} then all cargo with the
     * given IDs should be removed from the port's stored cargo.</li>
     * </ul>
     * <p>
     * Finally, the movement should be forwarded onto each statistics evaluator stored by the port
     * by calling {@link StatisticsEvaluator#onProcessMovement(Movement)}.
     *
     * @param movement movement to execute
     */
    public void processMovement(Movement movement) {
        // Processing the movement.
        if (movement instanceof ShipMovement) {
            processShipMovement((ShipMovement) movement);

        } else if (movement instanceof CargoMovement) {
            processCargoMovement(((CargoMovement) movement));
        }

        // Passing the movement to the evaluators.
        evaluators.forEach(evaluator -> evaluator.onProcessMovement(movement));
    }

    /**
     * Processes a ShipMovement as described in {@link #processMovement(Movement)}
     *
     * @param movement the ShipMovement being processed
     * @require movement != null
     */
    private void processShipMovement(ShipMovement movement) {
        Ship ship = movement.getShip();

        if (movement.getDirection() == MovementDirection.INBOUND) {
            shipQueue.add(ship);

        } else if (movement.getDirection() == MovementDirection.OUTBOUND) {
            // Adding all cargo that can be loaded by the ship to the ship.
            for (Cargo cargo : getCargo()) {
                if (ship.canLoad(cargo)) {
                    ship.loadCargo(cargo);
                    storedCargo.remove(cargo);
                }
            }
            // Searching for the ship and removing it from the Quay it's docked at.
            for (Quay quay : quays) {
                if (quay.getShip() == ship) {
                    quay.shipDeparts();
                    break; // The ship should only be at one Quay.
                }
            }
        }
    }

    /**
     * Processes a CargoMovement as described in {@link #processMovement(Movement)}
     *
     * @param movement the CargoMovement being processed
     * @require movement != null
     */
    private void processCargoMovement(CargoMovement movement) {
        if (movement.getDirection() == MovementDirection.INBOUND) {
            storedCargo.addAll(movement.getCargo());

        } else if (movement.getDirection() == MovementDirection.OUTBOUND) {
            storedCargo.removeAll(movement.getCargo());
        }
    }

    /**
     * Adds the given statistics evaluator to the port's list of evaluators.
     * <p>
     * If the port already has an evaluator of that type, no action should be taken.
     *
     * @param eval statistics evaluator to add to the port
     */
    public void addStatisticsEvaluator(StatisticsEvaluator eval) {
        // Ensuring the evaluator type doesn't already exist
        for (StatisticsEvaluator evaluator : evaluators) {
            if (evaluator.getClass().equals(eval.getClass())) {
                return;
            }
        }
        evaluators.add(eval);
    }

    /**
     * Adds a quay to the ports control.
     *
     * @param quay the quay to add
     */
    public void addQuay(Quay quay) {
        quays.add(quay);
    }

    /**
     * Advances the simulation by one minute.
     * <p>
     * On each call to <code>elapseOneMinute()</code>, the following actions should be completed by
     * the port in order:
     * <ol>
     * <li>Advance the simulation time by 1</li>
     * <li>If the time is a multiple of 10, attempt to bring a ship from the
     * ship queue to any empty quay that matches the requirements from {@link Ship#canDock(Quay)}.
     * The ship should only be docked to one quay.
     * </li>
     * <li>If the time is a multiple of 5, all quays must unload the cargo from ships
     * docked (if any) and add it to warehouses at the port (the Port's list of stored cargo)</li>
     * <li>All movements stored in the queue whose action time is equal to the current time
     * should be processed by {@link #processMovement(Movement)}</li>
     * <li>Call {@link StatisticsEvaluator#elapseOneMinute()} on all statistics evaluators</li>
     * </ol>
     */
    @Override
    public void elapseOneMinute() {
        time++;

        // Handling events that occur when time is a multiple of 10.
        if (time % 10 == 0) {
            dockShipFromQueue();
        }

        // Handling events that occur when time is a multiple of 5.
        if (time % 5 == 0) {
            unloadShipsDocked();
        }

        // Processing all movements occurring at the current time, if any.
        while (movements.peek() != null && movements.peek().getTime() == time) {
            processMovement(movements.poll());
        }

        // Updating the time of all evaluators.
        evaluators.forEach(StatisticsEvaluator::elapseOneMinute);
    }

    /**
     * Attempts to dock a ship from the ship queue to any empty quay that matches the requirements
     * from {@link Ship#canDock(Quay)}. The ship should only be docked to one quay.
     */
    private void dockShipFromQueue() {
        if (shipQueue.peek() == null) {
            return; // No ship to dock.
        }
        for (Quay quay : quays) {
            // Quay must be empty and the ship must be able to dock it.
            if (quay.isEmpty() && shipQueue.peek().canDock(quay)) {
                quay.shipArrives(shipQueue.poll());
                break; // Only docking once.
            }
        }
    }

    /**
     * All quays must unload the cargo from ships docked (if any) and add it to warehouses at the
     * port (the Port's list of stored cargo).
     */
    private void unloadShipsDocked() {
        for (Quay quay : quays) {
            Ship ship = quay.getShip();

            if (ship instanceof BulkCarrier) {
                try {
                    Cargo cargo = ((BulkCarrier) ship).unloadCargo();
                    storedCargo.add(cargo);
                } catch (NoSuchCargoException ignore) {
                    // No cargo to unload, moving onto next Quay
                }

            } else if (ship instanceof ContainerShip) {
                try {
                    List<Container> cargo = ((ContainerShip) ship).unloadCargo();
                    storedCargo.addAll(cargo);
                } catch (NoSuchCargoException ignore) {
                    // No cargo to unload, moving onto next Quay
                }
            }
        }
    }

    /**
     * Returns the machine-readable string representation of this Port.
     * <p>
     * The format of the string to return is
     * <pre>
     *  Name
     *  Time
     *  numCargo
     *  EncodedCargo
     *  EncodedCargo...
     *  numShips
     *  EncodedShip
     *  EncodedShip...
     *  numQuays
     *  EncodedQuay
     *  EncodedQuay...
     *  ShipQueue:numShipsInQueue:shipID,shipID,...
     *  StoredCargo:numCargo:cargoID,cargoID,...
     *  Movements:numMovements
     *  EncodedMovement
     *  EncodedMovement...
     *  Evaluators:numEvaluators:EvaluatorSimpleName,EvaluatorSimpleName,...
     *  </pre>
     * Where:
     * <ul>
     *   <li>Name is the name of the Port</li>
     *   <li>Time is the time elapsed since the simulation started</li>
     *   <li>numCargo is the total number of cargo in the simulation</li>
     *   <li>If present (numCargo &gt; 0): EncodedCargo is the encoded representation of each
     *   individual cargo in the simulation </li>
     *   <li>numShips is the total number of ships in the simulation</li>
     *   <li>If present (numShips &gt; 0): EncodedShip is the encoded representation of each
     *   individual ship encoding in the simulation</li>
     *   <li>numQuays is the total number of quays in the Port</li>
     *   <li>If present (numQuays &gt; 0): EncodedQuay is the encoded representation of each
     *   individual quay in the simulation</li>
     *   <li>numShipsInQueue is the total number of ships in the ship queue
     *   in the port</li>
     *   <li>If present (numShipsInQueue &gt; 0): shipID is each ship's ID in the aforementioned
     *   queue</li>
     *   <li>numCargo is the total amount of stored cargo in the Port</li>
     *   <li>If present (numCargo &gt; 0): cargoID is each cargo's ID in the stored cargo list of
     *   Port</li>
     *   <li>numMovements is the number of movements in the list of movements
     *   in Port</li>
     *   <li>If present (numMovements &gt; 0): EncodedMovement is the encoded representation of each
     *   individual Movement in the aforementioned list</li>
     *   <li>numEvaluators is the number of statistics evaluators in the Port evaluators list</li>
     *   <li>If present (numEvaluators &gt; 0): EvaluatorSimpleName is the name given by
     *   {@link Class#getSimpleName()} for each evaluator in the aforementioned list separated
     *   by a comma</li>
     *   <li>Each line is separated by a {@link System#lineSeparator()}</li>
     * </ul>
     * <p>
     * For example the minimum / default encoding would be:
     * <pre>
     * PortName
     * 0
     * 0
     * 0
     * 0
     * ShipQueue:0:
     * StoredCargo:0:
     * Movements:0
     * Evaluators:0:
     * </pre>
     *
     * @return encoded string representation of this Port
     */
    @Override
    public String encode() {
        StringJoiner encodedPort = new StringJoiner(System.lineSeparator());
        encodedPort.add(name);
        encodedPort.add("" + time);

        // Encoding all the cargo, ships and quays in the simulation
        encodeCollection(encodedPort, Cargo.getCargoRegistry().values(), "");
        encodeCollection(encodedPort, Ship.getShipRegistry().values(), "");
        encodeCollection(encodedPort, quays, "");

        encodedPort.add(shipQueue.encode());

        // Joining all the ID's of the stored cargo and encoding.
        StringJoiner storedCargoIds = new StringJoiner(",");
        for (Cargo cargo : storedCargo) {
            storedCargoIds.add("" + cargo.getId());
        }
        encodedPort.add(String.format("StoredCargo:%d:%s", storedCargo.size(), storedCargoIds));

        // Encoding all the movements currently at the port.
        encodeCollection(encodedPort, movements, "Movements:");

        // Joining all the evaluator names and encoding.
        StringJoiner evaluatorNames = new StringJoiner(",");
        for (StatisticsEvaluator evaluator : evaluators) {
            evaluatorNames.add(evaluator.getClass().getSimpleName());
        }
        encodedPort.add(String.format("Evaluators:%d:%s", evaluators.size(), evaluatorNames));

        return encodedPort.toString();
    }

    /**
     * Private helper method for {@link #encode()} which takes a Collection of {@link Encodable}
     * objects and encodes and adds them individually one after the other to the Port encoding.
     *
     * @param encoding  The encoding that is being added to.
     * @param values    The Collection of Encodable objects.
     * @param extraInfo Extra information needed to be displayed in the encoding (i.e. Movements:)
     * @require encoding != null && values != null
     */
    private void encodeCollection(StringJoiner encoding, Collection<? extends Encodable> values,
                                  String extraInfo) {
        int numToAdd = values.size();
        encoding.add(extraInfo + numToAdd);

        // Adding all the encoded values, if any, to the encoding.
        if (numToAdd > 0) {
            for (Encodable value : values) {
                encoding.add(value.encode());
            }
        }
    }
}
