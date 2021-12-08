package portsim.movement;

import portsim.cargo.Cargo;
import portsim.util.BadEncodingException;
import portsim.util.NoSuchCargoException;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static portsim.util.BadEncodingException.*;

/**
 * The movement of cargo coming into or out of the port.
 */
public class CargoMovement extends Movement {

    /**
     * The cargo that will be involved in the movement
     */
    private List<Cargo> cargo;

    /**
     * Creates a new cargo movement with the given action time and direction
     * to be undertaken with the given cargo.
     *
     * @param time      the time the movement should occur
     * @param direction the direction of the movement
     * @param cargo     the cargo to be moved
     * @throws IllegalArgumentException if time &lt; 0
     */
    public CargoMovement(long time, MovementDirection direction,
                         List<Cargo> cargo) throws IllegalArgumentException {
        super(time, direction);
        this.cargo = cargo;
    }

    /**
     * Creates a cargo movement from a string encoding.
     * <p>
     * The format of the string should match the encoded representation of a
     * cargo movement, as described in {@link #encode()}.
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <ul>
     * <li>The number of colons (<code>:</code>) detected was more/fewer than expected.</li>
     * <li>The given string is not a CargoMovement encoding</li>
     * <li>The time is not a long (i.e. cannot be parsed by {@link Long#parseLong(String)}).</li>
     * <li>The time is less than zero (0).</li>
     * <li>The movementDirection is not one of the valid directions (See
     * {@link MovementDirection}).</li>
     * <li>The number of ids is not a int (i.e. cannot be parsed by
     * {@link Integer#parseInt(String)}).</li>
     * <li>The number of ids is less than one (1).</li>
     * <li>An id is not a int (i.e. cannot be parsed by {@link Integer#parseInt(String)}).</li>
     * <li>An id is less than zero (0).</li>
     * <li>There is no cargo that exists with a specified id.</li>
     * <li>The number of id's does not match the number specified.</li>
     * </ul>
     *
     * @param string string containing the encoded CargoMovement
     * @return decoded CargoMovement instance
     * @throws BadEncodingException if the format of the given string is invalid according to the
     *                              rules above
     */
    public static CargoMovement fromString(String string) throws BadEncodingException {
        assertNotNull(string);

        String[] parts = string.split(":", -1);
        assertEqual(5, parts.length);

        assertEqual("CargoMovement", parts[0]);

        long time = parseEncoding(parts[1], Long::parseLong);
        MovementDirection direction = parseEncoding(parts[2], MovementDirection::valueOf);

        int numCargo = parseEncoding(parts[3], Integer::parseInt);
        assertTrue("numCargo can't be less than one!", numCargo >= 1);

        String[] cargoIds = parts[4].split(",", -1);
        assertEqual(numCargo, cargoIds.length);

        List<Cargo> cargo = new ArrayList<>();

        // Decoding each cargo ID and adding it to the list of cargo
        for (int i = 0; i < numCargo; i++) {
            int id = parseEncoding(cargoIds[i], Integer::parseInt);
            try {
                cargo.add(Cargo.getCargoById(id));
            } catch (NoSuchCargoException e) {
                throw new BadEncodingException(String.format("Cargo id %s does not exist!", id), e);
            }
        }

        try {
            return new CargoMovement(time, direction, cargo);
        } catch (IllegalArgumentException e) {
            throw new BadEncodingException("Invalid Argument(s) for CargoMovement!", e);
        }
    }

    /**
     * Returns the cargo that will be moved.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all cargo in the movement
     */
    public List<Cargo> getCargo() {
        return new ArrayList<>(cargo);
    }

    /**
     * Returns the human-readable string representation of this CargoMovement.
     * <p>
     * The format of the string to return is
     * <pre>
     * DIRECTION CargoMovement to occur at time involving num piece(s) of cargo </pre>
     * Where:
     * <ul>
     *   <li>{@code DIRECTION} is the direction of the movement </li>
     *   <li>{@code time} is the time the movement is meant to occur </li>
     *   <li>{@code num} is the number of cargo pieces that are being moved</li>
     * </ul>
     * <p>
     * For example: <pre>
     * OUTBOUND CargoMovement to occur at 135 involving 5 piece(s) of cargo </pre>
     *
     * @return string representation of this CargoMovement
     */
    @Override
    public String toString() {
        return String.format("%s involving %d piece(s) of cargo", super.toString(), cargo.size());
    }

    /**
     * Returns the machine-readable string representation of this movement.
     * <p>
     * The format of the string to return is
     * <pre>CargoMovement:time:direction:numCargo:ID1,ID2,...</pre>
     * Where:
     * <ul>
     * <li><code>time</code> is the time that the movement will be actioned</li>
     * <li><code>direction</code> is the direction of the movement</li>
     * <li><code>numCargo</code> is the number of the cargo in the movement</li>
     * <li><code>ID1,ID2,...</code> are the IDs of the cargo in the movement separated by a
     * comma ','. There should be no trailing comma after the last ID.</li>
     * </ul>
     * For example:
     * <pre>CargoMovement:120:INBOUND:3:22,23,12</pre>
     *
     * @return encoded string representation of this CargoMovement
     */
    @Override
    public String encode() {
        StringJoiner cargoIds = new StringJoiner(",");

        for (Cargo item : cargo) {
            cargoIds.add("" + item.getId());
        }

        return String.format("%s:%d:%s", super.encode(), cargo.size(), cargoIds);
    }
}
