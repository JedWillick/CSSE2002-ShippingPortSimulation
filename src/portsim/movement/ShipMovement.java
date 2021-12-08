package portsim.movement;

import portsim.ship.Ship;
import portsim.util.BadEncodingException;
import portsim.util.NoSuchShipException;

import java.util.function.Function;

/**
 * The movement of a ship coming into or out of the port.
 */
public class ShipMovement extends Movement {

    /**
     * The ship entering of leaving the Port
     */
    private Ship ship;

    /**
     * Creates a new ship movement with the given action time and direction
     * to be undertaken with the given ship.
     *
     * @param time      the time the movement should occur
     * @param direction the direction of the movement
     * @param ship      the ship which that is waiting to move
     * @throws IllegalArgumentException if time &lt; 0
     */
    public ShipMovement(long time, MovementDirection direction, Ship ship)
            throws IllegalArgumentException {
        super(time, direction);
        this.ship = ship;
    }

    /**
     * Creates a ship movement from a string encoding.
     * <p>
     * The format of the string should match the encoded representation of a
     * ship movement, as described in {@link #encode()}.
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <ul>
     * <li>The number of colons (<code>:</code>) detected was more/fewer than expected.</li>
     * <li>The time is not a long (i.e. cannot be parsed by {@link Long#parseLong(String)}).</li>
     * <li>The time is less than zero (0).</li>
     * <li>The movementDirection is not one of the valid directions (See
     * {@link MovementDirection}</li>
     * <li>The imoNumber is not a long (i.e. cannot be parsed by
     * {@link Long#parseLong(String)}).</li>
     * <li>There is no ship that exists with the specified imoNumber.</li>
     * </ul>
     *
     * @param string string containing the encoded ShipMovement
     * @return decoded ShipMovement instance
     * @throws BadEncodingException if the format of the given string is invalid according to the
     *                              rules above
     */
    public static ShipMovement fromString(String string) throws BadEncodingException {
        assertTrue("Encoding was null!", string != null);

        String[] parts = string.split(":", -1);
        assertTrue("Expected 4 parts but got " + parts.length, parts.length == 4);

        assertTrue("Expected ShipMovement but got " + parts[0], "ShipMovement".equals(parts[0]));

        // Trying to parse the time, direction and imoNumber
        long time = parseEncoding(parts[1], Long::parseLong);
        MovementDirection direction = parseEncoding(parts[2], MovementDirection::valueOf);
        long imoNumber = parseEncoding(parts[3], Long::parseLong);

        Ship ship;
        try {
            ship = Ship.getShipByImoNumber(imoNumber);
        } catch (NoSuchShipException noSuchShipException) {
            throw new BadEncodingException("There is no ship that exists with the "
                    + "specified imoNumber!", noSuchShipException);
        }

        try {
            return new ShipMovement(time, direction, ship);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new BadEncodingException("Invalid Argument(s) for ShipMovement!",
                    illegalArgumentException);
        }
    }

    /**
     * Helper method that asserts a condition is true by throwing a {@link BadEncodingException}.
     *
     * @param message   the message for the BadEncodingException
     * @param condition the condition being asserted
     * @throws BadEncodingException if the condition returns false (i.e. not the expected value)
     */
    private static void assertTrue(String message, boolean condition) throws BadEncodingException {
        if (!condition) {
            throw new BadEncodingException("ShipMovement: " + message);
        }
    }

    /**
     * Helper method that wraps a function that parses a value with a {@link BadEncodingException}
     * if the value cannot be parsed.
     *
     * @param encoding the encoding being parsed.
     * @param parser   the function being used to parse
     * @param <R>      the return type of the parsed value
     * @return the parsed value
     * @throws BadEncodingException if the encoding cannot be parsed with the specified function.
     * @require parser != null
     */
    private static <R> R parseEncoding(String encoding, Function<String, R> parser)
            throws BadEncodingException {
        try {
            return parser.apply(encoding);

        } catch (NumberFormatException numberFormatException) {
            throw new BadEncodingException("ShipMovement: " + encoding
                    + " cannot be parsed as a number!", numberFormatException);

        } catch (IllegalArgumentException illegalArgumentException) {
            throw new BadEncodingException("ShipMovement: " + encoding
                    + " cannot be parsed as an Enum!", illegalArgumentException);
        }
    }

    /**
     * Returns the ship undertaking the movement.
     *
     * @return movements ship
     */
    public Ship getShip() {
        return ship;
    }

    /**
     * Returns the human-readable string representation of this ShipMovement.
     * <p>
     * The format of the string to return is
     * <pre>
     * DIRECTION ShipMovement to occur at time involving the ship name </pre>
     * Where:
     * <ul>
     *   <li>{@code DIRECTION} is the direction of the movement </li>
     *   <li>{@code time} is the time the movement is meant to occur </li>
     *   <li>{@code name} is the name of the ship that is being moved</li>
     * </ul>
     * For example:
     * <pre>
     * OUTBOUND ShipMovement to occur at 135 involving the ship Voyager </pre>
     *
     * @return string representation of this ShipMovement
     */
    @Override
    public String toString() {
        return String.format("%s involving the ship %s", super.toString(), ship.getName());
    }

    /**
     * Returns the machine-readable string representation of this ship movement.
     * <p>
     * The format of the string to return is
     * <pre>ShipMovement:time:direction:imoNumber</pre>
     * Where:
     * <ul>
     * <li><code>time</code> is the time that the movement will be actioned</li>
     * <li><code>direction</code> is the direction of the movement</li>
     * <li><code>imoNumber</code> is the imoNumber of the ship that is moving</li>
     * </ul>
     * For example:
     * <pre>ShipMovement:120:INBOUND:1258691</pre>
     *
     * @return encoded string representation of this ShipMovement
     */
    @Override
    public String encode() {
        return String.format("%s:%s", super.encode(), ship.getImoNumber());
    }
}
