package portsim.cargo;

import java.util.Objects;

/**
 * Represents a shipping container, used for holding or transporting something.
 */
public class Container extends Cargo {

    /**
     * The type of the container
     */
    private ContainerType type;

    /**
     * Creates a new Container of the specified {@link ContainerType},
     * with the given ID and destination.
     *
     * @param id          cargo ID
     * @param destination destination port
     * @param type        type of container
     * @throws IllegalArgumentException if a cargo already exists with the
     *                                  given ID or ID &lt; 0
     */
    public Container(int id, String destination, ContainerType type)
            throws IllegalArgumentException {
        super(id, destination);
        this.type = type;
    }

    /**
     * Returns the type of this container.
     *
     * @return container type
     */
    public ContainerType getType() {
        return type;
    }

    /**
     * Returns the hash code of this Container.
     * <p>
     * Two cargo are equal according to {@link #equals(Object)} method should have the same
     * hash code.
     *
     * @return hash code of this cargo.
     */
    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(type);
    }

    /**
     * Returns true if and only if this Container is equal to the other given Container.
     * <p>
     * For two Containers to be equal, they must have the same ID, destination and type.
     *
     * @param o other object to check equality
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        boolean isTypeEqual = false;

        if (o instanceof Container) {
            isTypeEqual = Objects.equals(((Container) o).type, this.type);
        }

        return super.equals(o) && isTypeEqual;
    }

    /**
     * Returns the human-readable string representation of this Container.
     * <p>
     * The format of the string to return is
     * <pre>Container id to destination [type]</pre>
     * Where:
     * <ul>
     *   <li>{@code id} is the id of this cargo </li>
     *   <li>{@code destination} is the destination of the cargo </li>
     *   <li>{@code type} is the type of cargo</li>
     * </ul>
     * For example: <pre>Container 42 to Brazil [OTHER]</pre>
     *
     * @return string representation of this Container
     */
    @Override
    public String toString() {
        return String.format("%s [%s]", super.toString(), type);
    }

    /**
     * Returns the machine-readable string representation of this Container.
     * <p>
     * The format of the string to return is
     * <pre>Container:id:destination:type</pre>
     * Where:
     * <ul>
     * <li><code>id</code> is the id of this cargo </li>
     * <li><code>destination</code> is the destination of this cargo </li>
     * <li><code>type</code> is the container type</li>
     * </ul>
     * For example:
     * <pre>Container:3:Australia:OPEN_TOP</pre>
     *
     * @return encoded string representation of this Cargo
     */
    @Override
    public String encode() {
        return String.format("%s:%s", super.encode(), type);
    }
}
