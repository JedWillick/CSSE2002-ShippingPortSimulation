package portsim.ship;

import portsim.cargo.Cargo;
import portsim.port.Quay;
import portsim.util.BadEncodingException;
import portsim.util.Encodable;
import portsim.util.NoSuchCargoException;
import portsim.util.NoSuchShipException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static portsim.util.BadEncodingException.*;

/**
 * Represents a ship whose movement is managed by the system.
 * <p>
 * Ships store various types of cargo which can be loaded and unloaded at a port.
 */
public abstract class Ship implements Encodable {

    /**
     * Database of all ships currently active in the simulation
     */
    private static Map<Long, Ship> shipRegistry = new HashMap<>();

    /**
     * Name of the ship
     */
    private String name;

    /**
     * Unique 7 digit identifier to identify this ship (no leading zero's [0])
     */
    private long imoNumber;

    /**
     * Port of origin of ship
     */
    private String originFlag;

    /**
     * Maritime flag designated for use on this ship
     */
    private NauticalFlag flag;

    /**
     * Creates a new ship with the given
     * <a href="https://en.wikipedia.org/wiki/IMO_number">IMO number</a>,
     * name, origin port flag and nautical flag.
     * <p>
     * Finally, the ship should be added to the ship registry with the
     * IMO number as the key.
     *
     * @param imoNumber  unique identifier
     * @param name       name of the ship
     * @param originFlag port of origin
     * @param flag       the nautical flag this ship is flying
     * @throws IllegalArgumentException if a ship already exists with the given
     *                                  imoNumber, imoNumber &lt; 0 or imoNumber is not 7 digits
     *                                  long (no leading zero's [0])
     */
    public Ship(long imoNumber, String name, String originFlag,
                NauticalFlag flag) throws IllegalArgumentException {

        // IMO Number must be a positive 7-digit number with no leading zeros.
        // Thus, between 1000000 and 9999999 (inclusive).
        if (imoNumber < 1000000 || imoNumber > 9999999) {
            throw new IllegalArgumentException("Illegal imoNumber: " + imoNumber);
        }

        if (shipExists(imoNumber)) {
            throw new IllegalArgumentException("IMO number already exists in the registry: "
                    + imoNumber);
        }

        this.imoNumber = imoNumber;
        this.name = name;
        this.originFlag = originFlag;
        this.flag = flag;

        // Adding the ship to the registry
        shipRegistry.put(this.imoNumber, this);
    }

    /**
     * Checks if a ship exists in the simulation using its IMO number.
     *
     * @param imoNumber unique key to identify ship
     * @return true if there is a ship with key imoNumber else false
     */
    public static boolean shipExists(long imoNumber) {
        return shipRegistry.containsKey(imoNumber);
    }

    /**
     * Returns the ship specified by the IMO number.
     *
     * @param imoNumber unique key to identify ship
     * @return Ship specified by the given IMO number
     * @throws NoSuchShipException if the ship does not exist
     */
    public static Ship getShipByImoNumber(long imoNumber) throws NoSuchShipException {
        if (!shipExists(imoNumber)) {
            throw new NoSuchShipException("Ship " + imoNumber + " does not exist in the registry!");
        }
        return shipRegistry.get(imoNumber);
    }

    /**
     * Reads a Ship from its encoded representation in the given string.
     * <p>
     * The format of the string should match the encoded representation of a
     * Ship, as described in {@link #encode()} (and subclasses).
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <ul>
     * <li>The number of colons (<code>:</code>) detected was more/fewer than expected</li>
     * <li>The ship's IMO number is not a long (i.e. cannot be parsed by
     * {@link Long#parseLong(String)})</li>
     * <li>The ship's IMO number is valid according to the constructor</li>
     * <li>The ship's type specified is not one of {@link ContainerShip} or {@link BulkCarrier}</li>
     * <li>The encoded Nautical flag is not one of {@link NauticalFlag#values()}</li>
     * <li>The encoded cargo to add does not exist in the simulation according to
     * {@link Cargo#cargoExists(int)}</li>
     * <li>The encoded cargo can not be added to the ship according to {@link #canLoad(Cargo)}
     * <br>
     * <b>NOTE: Keep this in mind when making your own save files</b></li>
     * <li>Any of the parsed values given to a subclass constructor causes an
     * {@link IllegalArgumentException}.</li>
     * </ul>
     *
     * @param string string containing the encoded Ship
     * @return decoded ship instance
     * @throws BadEncodingException if the format of the given string is invalid according to the
     *                              rules above
     */
    public static Ship fromString(String string) throws BadEncodingException {
        assertNotNull(string);

        String[] parts = string.split(":", -1);
        assertTrue("Expected at least 6 parts but got " + parts.length, parts.length >= 6);

        long imoNumber = parseEncoding(parts[1], Long::parseLong);
        String name = parts[2];
        String originFlag = parts[3];
        NauticalFlag flag = parseEncoding(parts[4], NauticalFlag::valueOf);
        int capacity = parseEncoding(parts[5], Integer::parseInt);

        // Attempting to create a BulkCarrier or ContainerShip
        Ship ship;
        if ("BulkCarrier".equals(parts[0])) {
            assertEqual(7, parts.length);
            try {
                ship = new BulkCarrier(imoNumber, name, originFlag, flag, capacity);
            } catch (IllegalArgumentException e) {
                throw new BadEncodingException("One of the parsed values given to BulkCarrier's "
                        + "constructor caused an IllegalArgumentException!", e);
            }
            if (!"".equals(parts[6])) {
                addCargoFromString(ship, parts[6]);
            }

        } else if ("ContainerShip".equals(parts[0])) {
            assertEqual(8, parts.length);
            try {
                ship = new ContainerShip(imoNumber, name, originFlag, flag, capacity);
            } catch (IllegalArgumentException e) {
                throw new BadEncodingException("One of the parsed values given to ContainerShip's "
                        + "constructor caused an IllegalArgumentException!", e);
            }
            int numContainers = parseEncoding(parts[6], Integer::parseInt);

            if (numContainers != 0 || !"".equals(parts[7])) {
                String[] containers = parts[7].split(",", -1);
                assertEqual(numContainers, containers.length);

                for (int i = 0; i < numContainers; i++) {
                    addCargoFromString(ship, containers[i]);
                }
            }

        } else {
            throw new BadEncodingException("Invalid Ship type: " + parts[0]);
        }

        return ship;
    }


    /**
     * Private helper method to add cargo from a given string Cargo ID to a given ship.
     *
     * @param ship     the ship that the cargo is being loaded onto.
     * @param encoding the encoding being parsed as the cargo ID
     * @throws BadEncodingException If the format of the encoding is invalid according to
     *                              {@link #fromString(String)}
     * @require ship != null
     */
    private static void addCargoFromString(Ship ship, String encoding) throws BadEncodingException {
        int id = parseEncoding(encoding, Integer::parseInt);

        Cargo cargo;
        try {
            cargo = Cargo.getCargoById(id);
        } catch (NoSuchCargoException e) {
            throw new BadEncodingException("The encoded cargo does not exist!", e);
        }

        assertTrue(cargo + " could not be loaded onto " + ship, ship.canLoad(cargo));
        ship.loadCargo(cargo);
    }

    /**
     * Resets the global ship registry.
     * This utility method is for the testing suite.
     *
     * @given
     */
    public static void resetShipRegistry() {
        Ship.shipRegistry = new HashMap<>();
    }

    /**
     * Returns the database of ships currently active in the simulation as a mapping from the
     * ship's IMO number to its Ship instance.
     * <p>
     * Adding or removing elements from the returned map should not affect the original map.
     *
     * @return ship registry database
     */
    public static Map<Long, Ship> getShipRegistry() {
        return new HashMap<>(shipRegistry);
    }

    /**
     * Returns the nautical flag the ship is flying.
     *
     * @return flag
     */
    public NauticalFlag getFlag() {
        return flag;
    }

    /**
     * Returns this ship's IMO number.
     *
     * @return imoNumber
     */
    public long getImoNumber() {
        return imoNumber;
    }

    /**
     * Returns this ship's name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns this ship's flag denoting its origin.
     *
     * @return originFlag
     */
    public String getOriginFlag() {
        return originFlag;
    }

    /**
     * Check if this ship can dock with the specified quay according
     * to the conditions determined by the ships type.
     *
     * @param quay quay to be checked
     * @return true if the Quay satisfies the conditions else false
     */
    public abstract boolean canDock(Quay quay);

    /**
     * Checks if the specified cargo can be loaded onto the ship according
     * to the conditions determined by the ships type and contents.
     *
     * @param cargo cargo to be loaded
     * @return true if the Cargo satisfies the conditions else false
     */
    public abstract boolean canLoad(Cargo cargo);

    /**
     * Loads the specified cargo onto the ship.
     *
     * @param cargo cargo to be loaded
     * @require Cargo given is able to be loaded onto this ship according to
     * the implementation of {@link Ship#canLoad(Cargo)}
     */
    public abstract void loadCargo(Cargo cargo);

    /**
     * Returns the hash code of this ship.
     * <p>
     * Two ships that are equal according to the {@link #equals(Object)} method should have the
     * same hash code.
     *
     * @return hash code of this ship.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, flag, originFlag, imoNumber);
    }

    /**
     * Returns true if and only if this ship is equal to the other given ship.
     * <p>
     * For two ships to be equal, they must have the same name, flag, origin port, and IMO number.
     *
     * @param o other object to check equality
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        boolean isNameEqual = false;
        boolean isFlagEqual = false;
        boolean isOriginEqual = false;
        boolean isImoEqual = false;

        if (o instanceof Ship) {
            Ship other = (Ship) o;

            isNameEqual = Objects.equals(other.name, this.name);
            isFlagEqual = Objects.equals(other.flag, this.flag);
            isOriginEqual = Objects.equals(other.originFlag, this.originFlag);
            isImoEqual = other.imoNumber == this.imoNumber;
        }

        return isNameEqual && isFlagEqual && isOriginEqual && isImoEqual;
    }

    /**
     * Returns the human-readable string representation of this Ship.
     * <p>
     * The format of the string to return is
     * <pre>ShipClass name from origin [flag]</pre>
     * Where:
     * <ul>
     *   <li>{@code ShipClass} is the Ship class</li>
     *   <li>{@code name} is the name of this ship</li>
     *   <li>{@code origin} is the country of origin of this ship</li>
     *   <li>{@code flag} is the nautical flag of this ship</li>
     * </ul>
     * For example: <pre>BulkCarrier Evergreen from Australia [BRAVO]</pre>
     *
     * @return string representation of this Ship
     */
    @Override
    public String toString() {
        return String.format("%s %s from %s [%s]", getClass().getSimpleName(), name, originFlag,
                flag);
    }

    /**
     * Returns the machine-readable string representation of this Ship.
     * <p>
     * The format of the string to return is
     * <pre>ShipClass:imoNumber:name:origin:flag</pre>
     * Where:
     * <ul>
     *   <li><code>ShipClass</code> is the Ship class name</li>
     *   <li><code>imoNumber</code> is the IMO number of the ship</li>
     *   <li><code>name</code> is the name of this ship </li>
     *   <li><code>origin</code> is the country of origin of this ship </li>
     *   <li><code>flag</code> is the nautical flag of this ship </li>
     * </ul>
     * For example:
     * <pre>Ship:1258691:Evergreen:Australia:BRAVO</pre>
     *
     * @return encoded string representation of this Ship
     */
    @Override
    public String encode() {
        return String.format("%s:%d:%s:%s:%s", getClass().getSimpleName(), imoNumber, name,
                originFlag, flag);
    }
}
