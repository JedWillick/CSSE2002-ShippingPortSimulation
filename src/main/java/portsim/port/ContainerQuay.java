package portsim.port;

import java.util.Objects;

/**
 * A Container Quay is a type of quay specifically designed for the unloading of
 * Container Ship vessels.
 */
public class ContainerQuay extends Quay {

    /**
     * The maximum number of containers that the quay can handle
     */
    private int maxContainers;

    /**
     * Creates a new Container Quay with the given ID and maximum number of containers.
     *
     * @param id            quay ID
     * @param maxContainers maximum number of containers the quay can handle
     * @throws IllegalArgumentException if ID or maxContainers &lt; 0
     */
    public ContainerQuay(int id, int maxContainers) throws IllegalArgumentException {
        super(id);
        if (maxContainers < 0) {
            throw new IllegalArgumentException("maxContainers must be greater than"
                    + " or equal to 0: " + maxContainers);
        }
        this.maxContainers = maxContainers;
    }

    /**
     * Returns the maximum number of containers of this quay can process at once.
     *
     * @return maxContainers
     */
    public int getMaxContainers() {
        return maxContainers;
    }

    /**
     * Returns the hash code of this ContainerQuay.
     * <p>
     * Two ContainerQuays that are equal according to the {@link #equals(Object)} method should
     * have the same hash code.
     *
     * @return hash code of this ContainerQuay.
     */
    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(maxContainers);
    }

    /**
     * Returns true if and only if this ContainerQuay is equal to the other given ContainerQuay.
     * <p>
     * For two ContainerQuays to be equal, they must have the same ID, ship docked status
     * (must either both be empty or both be occupied) and same container capacity.
     *
     * @param o other object to check equality
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        boolean isCapacityEqual = false;

        if (o instanceof ContainerQuay) {
            isCapacityEqual = ((ContainerQuay) o).maxContainers == this.maxContainers;
        }

        return super.equals(o) && isCapacityEqual;
    }

    /**
     * Returns the human-readable string representation of this ContainerQuay.
     * <p>
     * The format of the string to return is
     * <pre>ContainerQuay id [Ship: imoNumber] - maxContainers</pre>
     * Where:
     * <ul>
     * <li>{@code id} is the ID of this quay</li>
     * <li>{@code imoNumber} is the IMO number of the ship docked at this
     * quay, or {@code None} if the quay is unoccupied.</li>
     * <li>{@code maxContainers} is the number of containers this quay can
     * take.</li>
     * </ul>
     * <p>
     * For example: <pre>ContainerQuay 3 [Ship: 2125622] - 32</pre>
     *
     * @return string representation of this ContainerQuay
     */
    @Override
    public String toString() {
        return String.format("%s - %s", super.toString(), maxContainers);
    }

    /**
     * Returns the machine-readable string representation of this ContainerQuay.
     * <p>
     * The format of the string to return is
     * <pre>ContainerQuay:id:imoNumber:maxContainers</pre>
     * Where:
     * <ul>
     *   <li><code>id</code> is the ID of this quay </li>
     *   <li><code>imoNumber</code> is the IMO number of the ship docked at this
     *   quay, or <code>None</code> if the quay is unoccupied.</li>
     *   <li><code>maxContainers</code> is the maximum number of containers this quay can
     *   handle</li>
     * </ul>
     * For example:
     * <pre>ContainerQuay:3:1258691:100</pre> or
     * <pre>ContainerQuay:3:None:100</pre>
     *
     * @return encoded string representation of this ContainerQuay.
     */
    @Override
    public String encode() {
        return String.format("%s:%s", super.encode(), maxContainers);
    }
}
