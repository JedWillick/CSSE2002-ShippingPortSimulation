package portsim.cargo;

import portsim.util.BadEncodingException;
import portsim.util.Encodable;
import portsim.util.NoSuchCargoException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static portsim.util.BadEncodingException.*;

/**
 * Denotes a cargo whose function is to be transported via a Ship or land
 * transport.
 * <p>
 * Cargo is kept track of via its ID.
 */
public abstract class Cargo implements Encodable {

    /**
     * Database of all cargo currently active in the simulation
     */
    private static Map<Integer, Cargo> cargoRegistry = new HashMap<>();

    /**
     * The ID of the cargo instance
     */
    private int id;

    /**
     * Destination for this cargo
     */
    private String destination;

    /**
     * Creates a new Cargo with the given ID and destination port.
     * <p>
     * When a new piece of cargo is created, it should be added to the cargo registry.
     *
     * @param id          cargo ID
     * @param destination destination port
     * @throws IllegalArgumentException if a cargo already exists with the
     *                                  given ID or ID &lt; 0
     */
    public Cargo(int id, String destination) throws IllegalArgumentException {
        if (id < 0) {
            throw new IllegalArgumentException("Cargo ID must be greater than"
                    + " or equal to 0: " + id);
        }

        if (cargoExists(id)) {
            throw new IllegalArgumentException("Cargo ID already exists in the registry: " + id);
        }

        this.id = id;
        this.destination = destination;

        // Adding cargo to the registry
        cargoRegistry.put(this.id, this);
    }

    /**
     * Reads a piece of cargo from its encoded representation in the given
     * string.
     * <p>
     * The format of the given string should match the encoded representation of a
     * Cargo, as described in {@link #encode()} (and subclasses).
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <ul>
     * <li>The number of colons (<code>:</code>) detected was more/fewer than expected.</li>
     * <li>The cargo's type specified is not one of {@link Container} or {@link BulkCargo}</li>
     * <li>The cargo id is not an integer (i.e. cannot be parsed by
     * {@link Integer#parseInt(String)}).</li>
     * <li>The cargo id is less than zero (0).</li>
     * <li>A piece of cargo with the specified ID already exists</li>
     * <li>The cargo type specified is not one of {@link BulkCargoType}
     * or {@link ContainerType}</li>
     * <li>If the cargo type is a BulkCargo:
     *     <ol>
     *     <li>The cargo weight in tonnes is not an integer (i.e. cannot be parsed by
     *      {@link Integer#parseInt(String)}).</li>
     *     <li>The cargo weight in tonnes is less than zero (0).</li>
     *     </ol>
     * </li>
     * </ul>
     *
     * @param string string containing the encoded cargo
     * @return decoded cargo instance
     * @throws BadEncodingException if the format of the given string is invalid according to the
     *                              rules above
     */
    public static Cargo fromString(String string) throws BadEncodingException {
        assertNotNull(string);

        String[] parts = string.split(":", -1);

        assertTrue("Expected at least 4 parts but got " + parts.length, parts.length >= 4);

        int id = parseEncoding(parts[1], Integer::parseInt);

        String destination = parts[2];
        Cargo decodedCargo;

        // Checking that the type of cargo is valid and calling the appropriate helper method.
        if ("BulkCargo".equals(parts[0])) {
            decodedCargo = bulkCargoFromString(id, destination, parts);

        } else if ("Container".equals(parts[0])) {
            decodedCargo = containerFromString(id, destination, parts);

        } else {
            throw new BadEncodingException("The encoded string has an invalid CargoClass: "
                    + parts[0]);
        }

        return decodedCargo;
    }

    /**
     * Private helper method for {@link #fromString(String)} that creates a bulkCargo from an
     * encoding.
     *
     * @param id          The id of the BulkCargo
     * @param destination The destination of the BulkCargo
     * @param parts       The encoding of the BulkCargo split into parts.
     * @return The decoded BulkCargo
     * @throws BadEncodingException If the format of the encoding is invalid according to
     *                              {@link #fromString(String)}
     * @require parts != null
     */
    private static BulkCargo bulkCargoFromString(int id, String destination, String[] parts)
            throws BadEncodingException {

        assertEqual(5, parts.length);

        BulkCargoType type = parseEncoding(parts[3], BulkCargoType::valueOf);
        int tonnage = parseEncoding(parts[4], Integer::parseInt);

        try {
            return new BulkCargo(id, destination, tonnage, type);
        } catch (IllegalArgumentException e) {
            throw new BadEncodingException("Invalid argument(s) for BulkCargo!", e);
        }
    }

    /**
     * Private helper method for {@link #fromString(String)} that creates a Container from an
     * encoding.
     *
     * @param id          The id of the Container
     * @param destination The destination of the Container
     * @param parts       The encoding of the Container split into parts
     * @return The decoded Container
     * @throws BadEncodingException If the format of the encoding is invalid according to
     *                              {@link #fromString(String)}
     * @require parts != null
     */
    private static Container containerFromString(int id, String destination, String[] parts)
            throws BadEncodingException {

        assertEqual(4, parts.length);

        ContainerType type = parseEncoding(parts[3], ContainerType::valueOf);

        try {
            return new Container(id, destination, type);
        } catch (IllegalArgumentException e) {
            throw new BadEncodingException("Invalid argument(s) for Container!", e);
        }
    }

    /**
     * Resets the global cargo registry.
     * This utility method is for the testing suite.
     *
     * @given
     */
    public static void resetCargoRegistry() {
        Cargo.cargoRegistry = new HashMap<>();
    }

    /**
     * Checks if a cargo exists in the simulation using its ID.
     *
     * @param id unique key to identify cargo
     * @return true if there is a cargo stored in the registry with key id; false otherwise
     */
    public static boolean cargoExists(int id) {
        return cargoRegistry.containsKey(id);
    }

    /**
     * Returns the cargo specified by the given ID.
     *
     * @param id unique key to identify cargo
     * @return cargo specified by the id
     * @throws NoSuchCargoException if the cargo does not exist in the registry
     */
    public static Cargo getCargoById(int id) throws NoSuchCargoException {
        if (!cargoExists(id)) {
            throw new NoSuchCargoException("Cargo ID " + id + " does not exist in the registry.");
        }

        return cargoRegistry.get(id);
    }

    /**
     * Returns the global registry of all pieces of cargo, as a mapping from cargo IDs to Cargo
     * instances.
     * <p>
     * Adding or removing elements from the returned map should not affect the original map.
     *
     * @return cargo registry
     */
    public static Map<Integer, Cargo> getCargoRegistry() {
        return new HashMap<>(cargoRegistry);
    }

    /**
     * Retrieve the destination of this piece of cargo.
     *
     * @return the cargo's destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Retrieve the ID of this piece of cargo.
     *
     * @return the cargo's ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the hash code of this cargo.
     * <p>
     * Two cargo are equal according to {@link #equals(Object)} method should have the same
     * hash code.
     *
     * @return hash code of this cargo.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, destination);
    }

    /**
     * Returns true if and only if this cargo is equal to the other given cargo.
     * <p>
     * For two cargo to be equal, they must have the same ID and destination.
     *
     * @param o other object to check equality
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        boolean isIdEqual = false;
        boolean isDestinationEqual = false;

        if (o instanceof Cargo) {
            Cargo other = (Cargo) o;
            isIdEqual = other.id == this.id;
            isDestinationEqual = Objects.equals(other.destination, this.destination);
        }
        return isIdEqual && isDestinationEqual;
    }

    /**
     * Returns the human-readable string representation of this cargo.
     * <p>
     * The format of the string to return is
     * <pre>CargoClass id to destination</pre>
     * Where:
     * <ul>
     *   <li>{@code CargoClass} is the cargo class name</li>
     *   <li>{@code id} is the id of this cargo </li>
     *   <li>{@code destination} is the destination of the cargo </li>
     * </ul>
     * <p>
     * For example: <pre>Container 55 to New Zealand</pre>
     *
     * @return string representation of this Cargo
     */
    @Override
    public String toString() {
        return String.format("%s %d to %s", getClass().getSimpleName(), id, destination);
    }

    /**
     * Returns the machine-readable string representation of this Cargo.
     * <p>
     * The format of the string to return is
     * <pre>CargoClass:id:destination</pre>
     * Where:
     * <ul>
     *   <li><code>CargoClass</code> is the Cargo class name</li>
     *   <li><code>id</code> is the id of this cargo </li>
     *   <li><code>destination</code> is the destination of this cargo </li>
     * </ul>
     * <p>
     * For example:
     * <pre>Container:3:Australia</pre> OR <pre>BulkCargo:2:France</pre>
     *
     * @return encoded string representation of this Cargo
     */
    @Override
    public String encode() {
        return String.format("%s:%d:%s", getClass().getSimpleName(), id, destination);
    }
}
