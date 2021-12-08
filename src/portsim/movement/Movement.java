package portsim.movement;

import portsim.util.Encodable;

/**
 * The movement of ships or cargo coming into or out of the port from land or sea.
 */
public abstract class Movement implements Encodable {

    /**
     * The time in minutes that the movement should be completed
     */
    private long time;

    /**
     * The direction of the movement in relation to the port
     */
    private MovementDirection direction;

    /**
     * Creates a new movement with the given action time and direction.
     *
     * @param time      the time the movement should occur
     * @param direction the direction of the movement
     * @throws IllegalArgumentException if time &lt; 0
     */
    public Movement(long time, MovementDirection direction) throws IllegalArgumentException {
        if (time < 0) {
            throw new IllegalArgumentException("Time must be greater than"
                    + " or equal to 0: " + time);
        }
        this.time = time;
        this.direction = direction;
    }

    /**
     * Returns the direction of the movement.
     *
     * @return movement direction
     */
    public MovementDirection getDirection() {
        return direction;
    }

    /**
     * Returns the time the movement should be actioned.
     *
     * @return movement time
     */
    public long getTime() {
        return time;
    }

    /**
     * Returns the human-readable string representation of this Movement.
     * <p>
     * The format of the string to return is
     * <pre>DIRECTION MovementClass to occur at time</pre>
     * Where:
     * <ul>
     *   <li>{@code DIRECTION} is the direction of the movement </li>
     *   <li>{@code MovementClass} is the Movement class name</li>
     *   <li>{@code time} is the time the movement is meant to occur </li>
     * </ul>
     * For example: <pre>INBOUND Movement to occur at 120</pre>
     *
     * @return string representation of this Movement
     */
    @Override
    public String toString() {
        return String.format("%s %s to occur at %d", direction, getClass().getSimpleName(), time);
    }

    /**
     * Returns the machine-readable string representation of this movement.
     * <p>
     * The format of the string to return is
     * <pre>MovementClass:time:direction</pre>
     * Where:
     * <ul>
     * <li><code>MovementClass</code> is the Movement class name</li>
     * <li><code>time</code> is the time that the movement will be actioned</li>
     * <li><code>direction</code> is the direction of the movement</li>
     * </ul>
     * For example:
     * <pre>CargoMovement:120:INBOUND</pre>
     *
     * @return encoded string representation of this movement
     */
    @Override
    public String encode() {
        return String.format("%s:%d:%s", getClass().getSimpleName(), time, direction);
    }
}
