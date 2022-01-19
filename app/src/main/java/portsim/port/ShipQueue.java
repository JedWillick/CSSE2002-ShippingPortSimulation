package portsim.port;

import portsim.ship.ContainerShip;
import portsim.ship.NauticalFlag;
import portsim.ship.Ship;
import portsim.util.BadEncodingException;
import portsim.util.Encodable;
import portsim.util.NoSuchShipException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static portsim.util.BadEncodingException.*;

/**
 * Queue of ships waiting to enter a Quay at the port. Ships are chosen based on their priority.
 */
public class ShipQueue implements Encodable {

    /**
     * A list containing all the ships currently stored in this ShipQueue
     */
    private List<Ship> shipQueue;

    /**
     * Constructs a new ShipQueue with an initially empty queue of ships.
     */
    public ShipQueue() {
        shipQueue = new ArrayList<>();
    }

    /**
     * Creates a ship queue from a string encoding.
     * <p>
     * The format of the string should match the encoded representation of a
     * ship queue, as described in {@link #encode()}.
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <ul>
     * <li>The number of colons (<code>:</code>) detected was more/fewer than expected.</li>
     * <li>The string does not start with the literal string <code>"ShipQueue"</code></li>
     * <li>The number of ships in the shipQueue is not an integer (i.e. cannot
     * be parsed by {@link Integer#parseInt(String)}).</li>
     * <li>The number of ships in the shipQueue does not match the number specified.</li>
     * <li>The imoNumber of the ships in the shipQueue are not valid longs.
     * (i.e. cannot be parsed by {@link Long#parseLong(String)}).</li>
     * <li>Any imoNumber read does not correspond to a valid ship in the
     * simulation</li>
     * </ul>
     *
     * @param string string containing the encoded ShipQueue
     * @return decoded ShipQueue instance
     * @throws BadEncodingException if the format of the given string is invalid according to the
     *                              rules above
     */
    public static ShipQueue fromString(String string) throws BadEncodingException {
        assertNotNull(string);

        String[] parts = string.split(":", -1);
        assertEqual(3, parts.length);

        assertEqual("ShipQueue", parts[0]);

        int numShips = parseEncoding(parts[1], Integer::parseInt);

        ShipQueue queue = new ShipQueue();

        // Exiting method if there are no ships to add.
        if (numShips == 0 && "".equals(parts[2])) {
            return queue;
        }

        String[] imoNumbers = parts[2].split(",", -1);
        assertEqual(numShips, imoNumbers.length);

        for (String imoNumber : imoNumbers) {
            addShipsFromString(queue, imoNumber);
        }

        return queue;
    }

    /**
     * Private helper method for {@link #fromString(String)} that adds ships to the ShipQueue
     * from an encoded imoNumber.
     *
     * @param queue            the ShipQueue that the ship is being added too.
     * @param encodedImoNumber the encoded imoNumber pulled from the ShipQueue encoding.
     * @throws BadEncodingException If the imoNumber isn't a long, or the ship doesn't exist.
     * @require queue != null
     */
    private static void addShipsFromString(ShipQueue queue, String encodedImoNumber)
            throws BadEncodingException {

        long imoNumber = parseEncoding(encodedImoNumber, Long::parseLong);

        Ship ship;
        try {
            ship = Ship.getShipByImoNumber(imoNumber);
        } catch (NoSuchShipException e) {
            throw new BadEncodingException("The imoNumber does not correspond to a valid ship "
                    + "in the simulation!", e);
        }

        queue.add(ship);
    }

    /**
     * Returns a list containing all the ships currently stored in this ShipQueue.
     * <p>
     * The order of the ships in the returned list should be the order in which the ships were
     * added to the queue.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return ships in queue
     */
    public List<Ship> getShipQueue() {
        return new ArrayList<>(shipQueue);
    }

    /**
     * Gets the next ship to enter the port and removes it from the queue.
     * <p>
     * The same rules as described in {@link #peek()} should be used for determining which ship to
     * remove and return.
     *
     * @return next ship to dock
     */
    public Ship poll() {
        Ship ship = peek();
        if (ship != null) {
            shipQueue.remove(ship);
        }
        return ship;
    }

    /**
     * Returns the next ship waiting to enter the port.
     * The queue should not change.
     * <p>
     * The rules for determining which ship in the queue should be returned
     * next are as follows:
     * <ol>
     *     <li> If a ship is carrying dangerous cargo, it should be
     *     returned. If more than one ship is carrying dangerous cargo
     *     return the one added to the queue first. </li>
     *     <li> If a ship requires medical assistance, it should be
     *     returned. If more than one ship requires medical assistance,
     *     return the one added to the queue first. </li>
     *     <li> If a ship is ready to be docked, it should be returned. If
     *     more than one ship is ready to be docked, return the one added
     *     to the queue first. </li>
     *     <li> If there is a container ship in the queue, return the one
     *     added to the queue first. </li>
     *     <li> If this point is reached and no ship has been returned,
     *     return the ship that was added to the queue first.</li>
     *     <li> If there are no ships in the queue, return null.</li>
     * </ol>
     *
     * @return next ship in queue
     */
    public Ship peek() {
        // Searching for ship's carrying dangerous cargo and returning.
        for (Ship ship : shipQueue) {
            if (ship.getFlag() == NauticalFlag.BRAVO) {
                return ship;
            }
        }

        // Searching for ship's requiring medical assistance and returning.
        for (Ship ship : shipQueue) {
            if (ship.getFlag() == NauticalFlag.WHISKEY) {
                return ship;
            }
        }

        // Searching for ship's ready to be docked and returning.
        for (Ship ship : shipQueue) {
            if (ship.getFlag() == NauticalFlag.HOTEL) {
                return ship;
            }
        }

        // Searching for ContainerShip's and returning.
        for (Ship ship : shipQueue) {
            if (ship instanceof ContainerShip) {
                return ship;
            }
        }

        return (shipQueue.isEmpty() ? null : shipQueue.get(0));
    }

    /**
     * Adds the specified ship to the queue.
     *
     * @param ship to be added to queue
     */
    public void add(Ship ship) {
        shipQueue.add(ship);
    }

    /**
     * Returns the hash code of this ship queue.
     * <p>
     * Two ShipQueue's that are equal according to the {@link #equals(Object)} method should
     * have the same hash code.
     *
     * @return hash code of this ShipQueue.
     */
    @Override
    public int hashCode() {
        return Objects.hash(shipQueue);
    }

    /**
     * Returns true if and only if this ShipQueue is equal to the other given ShipQueue.
     * <p>
     * For two ShipQueue's to be equal, they must have the same ships in the queue,
     * in the same order.
     *
     * @param o other object to check equality
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ShipQueue)) {
            return false;
        }

        return Objects.equals(this.shipQueue, ((ShipQueue) o).shipQueue);
    }

    /**
     * Returns the machine-readable string representation of this ShipQueue.
     * <p>
     * The format of the string to return is
     * <pre>ShipQueue:numShipsInQueue:shipID,shipID,...</pre>
     * Where:
     * <ul>
     *   <li>numShipsInQueue is the total number of ships in the ship queue
     *   in the port</li>
     *   <li>If present (numShipsInQueue &gt; 0): shipID is each ship's ID in the aforementioned
     *   queue</li>
     * </ul>
     * For example:
     * <pre>ShipQueue:0:</pre> or <pre>ShipQueue:2:3456789,1234567</pre>
     *
     * @return encoded string representation of this ShipQueue
     */
    @Override
    public String encode() {
        StringJoiner imoNumbers = new StringJoiner(",");

        for (Ship ship : shipQueue) {
            imoNumbers.add("" + ship.getImoNumber());
        }
        return String.format("ShipQueue:%d:%s", shipQueue.size(), imoNumbers);
    }
}
