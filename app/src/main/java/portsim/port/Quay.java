package portsim.port;

import portsim.ship.Ship;
import portsim.util.BadEncodingException;
import portsim.util.Encodable;
import portsim.util.NoSuchShipException;

import java.util.Objects;

import static portsim.util.BadEncodingException.*;


/**
 * Quay is a platform lying alongside or projecting into the water where
 * ships are moored for loading or unloading.
 */
public abstract class Quay implements Encodable {

    /**
     * The ID of the quay
     */
    private int id;

    /**
     * The ship currently in the Quay
     */
    private Ship ship;

    /**
     * Creates a new Quay with the given ID, with no ship docked at the quay.
     *
     * @param id quay ID
     * @throws IllegalArgumentException if ID &lt; 0
     */
    public Quay(int id) throws IllegalArgumentException {
        if (id < 0) {
            throw new IllegalArgumentException("Quay ID must be greater than or equal to 0: " + id);
        }
        this.id = id;
        this.ship = null;
    }

    /**
     * Reads a Quay from its encoded representation in the given
     * string.
     * <p>
     * The format of the string should match the encoded representation of a
     * Quay, as described in {@link #encode()} (and subclasses).
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <ul>
     * <li>The number of colons (<code>:</code>) detected was more/fewer than expected.</li>
     * <li>The quay id is not a integer (i.e. cannot be parsed by
     * {@link Integer#parseInt(String)}).</li>
     * <li>The quay id is less than zero (0).</li>
     * <li>The quay type specified is not one of {@link BulkQuay} or {@link ContainerQuay} </li>
     * <li>If the encoded ship is not <code>None</code> then the ship must exist
     * and the imoNumber specified must be a long (i.e. can be parsed by
     * {@link Long#parseLong(String)}).
     * </li>
     * <li>The quay capacity is not an integer (i.e. cannot be parsed by
     * {@link Integer#parseInt(String)}).</li>
     * <li>Any of the parsed values given to a subclass constructor causes an
     * {@link IllegalArgumentException}</li>
     * </ul>
     *
     * @param string string containing the encoded Quay
     * @return decoded Quay instance
     * @throws BadEncodingException if the format of the given string is invalid according to
     *                              the rules above
     */
    public static Quay fromString(String string) throws BadEncodingException {
        assertNotNull(string);

        String[] parts = string.split(":", -1);
        assertEqual(4, parts.length);

        int id = parseEncoding(parts[1], Integer::parseInt);
        int capacity = parseEncoding(parts[3], Integer::parseInt);

        // Attempting to create a BulkQuay or ContainerQuay
        Quay decodedQuay;
        if ("BulkQuay".equals(parts[0])) {
            try {
                decodedQuay = new BulkQuay(id, capacity);
            } catch (IllegalArgumentException e) {
                throw new BadEncodingException("Invalid Argument(s) for BulkQuay", e);
            }

        } else if ("ContainerQuay".equals(parts[0])) {
            try {
                decodedQuay = new ContainerQuay(id, capacity);
            } catch (IllegalArgumentException e) {
                throw new BadEncodingException("Invalid Argument(s) for ContainerQuay", e);
            }

        } else {
            throw new BadEncodingException("Invalid Quay type: " + parts[0]);
        }

        // Docking a ship if needed.
        if (!"None".equals(parts[2])) {
            shipArrivesFromString(decodedQuay, parts[2]);
        }

        return decodedQuay;
    }

    /**
     * Private helper method for {@link #fromString(String)} that validates and docks the ship at
     * the specified Quay.
     *
     * @param quay             the Quay to dock the ship at.
     * @param encodedImoNumber the String encoded IMO number of the ship.
     * @throws BadEncodingException If the imoNumber is not a long, or the ship doesn't exist.
     * @require quay != null
     */
    private static void shipArrivesFromString(Quay quay, String encodedImoNumber)
            throws BadEncodingException {

        long imoNumber = parseEncoding(encodedImoNumber, Long::parseLong);

        Ship ship;
        try {
            ship = Ship.getShipByImoNumber(imoNumber);
        } catch (NoSuchShipException e) {
            throw new BadEncodingException("The ship doesn't exist in the registry!", e);
        }
        quay.shipArrives(ship);
    }

    /**
     * Get the id of this quay
     *
     * @return quay id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the ship currently docked at the quay.
     *
     * @return ship at quay or null if no ship is docked
     */
    public Ship getShip() {
        return ship;
    }

    /**
     * Returns whether a ship is currently docked at this quay.
     *
     * @return true if there is no ship docked else false
     */
    public boolean isEmpty() {
        return ship == null;
    }

    /**
     * Docks the given ship at the Quay so that the quay becomes occupied.
     *
     * @param ship ship to dock to the quay
     */
    public void shipArrives(Ship ship) {
        this.ship = ship;
    }

    /**
     * Removes the current ship docked at the quay.
     * The current ship should be set to {@code null}.
     *
     * @return the current ship or null if quay is empty.
     */
    public Ship shipDeparts() {
        Ship current = ship;
        ship = null;
        return current;
    }

    /**
     * Returns the hash code of this Quay.
     * <p>
     * Two Quays that are equal according to the {@link #equals(Object)} method should have the
     * same hash code.
     *
     * @return hash code of this Quay.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, isEmpty());
    }

    /**
     * Returns true if and only if this Quay is equal to the other given Quay.
     * <p>
     * For two Quays to be equal, they must have the same ID and ship docked status
     * (must either both be empty or both be occupied).
     *
     * @param o other object to check equality
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        boolean isIdEqual = false;
        boolean isShipStatusEqual = false;

        if (o instanceof Quay) {
            Quay other = (Quay) o;

            isIdEqual = other.id == this.id;
            isShipStatusEqual = other.isEmpty() == this.isEmpty();
        }
        return isIdEqual && isShipStatusEqual;
    }

    /**
     * Returns the human-readable string representation of this quay.
     * <p>
     * The format of the string to return is
     * <pre>QuayClass id [Ship: imoNumber]</pre>
     * Where:
     * <ul>
     * <li>{@code id} is the ID of this quay</li>
     * <li>{@code imoNumber} is the IMO number of the ship docked at this
     * quay, or {@code None} if the quay is unoccupied.</li>
     * </ul>
     * <p>
     * For example: <pre>BulkQuay 1 [Ship: 2313212]</pre> or
     * <pre>ContainerQuay 3 [Ship: None]</pre>
     *
     * @return string representation of this quay
     */
    @Override
    public String toString() {
        return String.format("%s %d [Ship: %s]", getClass().getSimpleName(), id,
                (ship != null ? ship.getImoNumber() : "None"));
    }

    /**
     * Returns the machine-readable string representation of this Quay.
     * <p>
     * The format of the string to return is
     * <pre>QuayClass:id:imoNumber</pre>
     * Where:
     * <ul>
     *   <li><code>QuayClass</code> is the Quay class name</li>
     *   <li><code>id</code> is the ID of this quay </li>
     *   <li><code>imoNumber</code> is the IMO number of the ship docked at this
     *   quay, or <code>None</code> if the quay is unoccupied.</li>
     * </ul>
     * For example:
     * <pre>BulkQuay:3:1258691</pre> or <pre>ContainerQuay:3:None</pre>
     *
     * @return encoded string representation of this Quay
     */
    @Override
    public String encode() {
        return String.format("%s:%d:%s", getClass().getSimpleName(), id,
                (ship != null ? ship.getImoNumber() : "None"));
    }
}
