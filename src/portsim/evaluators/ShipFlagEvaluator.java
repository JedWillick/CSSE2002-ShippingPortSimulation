package portsim.evaluators;

import portsim.movement.Movement;
import portsim.movement.MovementDirection;
import portsim.movement.ShipMovement;

import java.util.HashMap;
import java.util.Map;

/**
 * Gathers data on how many ships each country has sent to this port.
 * <p>
 * Stores a mapping of country-of-origin flags to the number of times that flag has been seen in
 * inbound movements.
 */
public class ShipFlagEvaluator extends StatisticsEvaluator {

    /**
     * The flag distribution seen at the port.
     */
    private Map<String, Integer> originFlagDistribution;

    /**
     * Constructs a new ShipFlagEvaluator.
     */
    public ShipFlagEvaluator() {
        super();
        originFlagDistribution = new HashMap<>();
    }

    /**
     * Return the flag distribution seen at this port.
     *
     * @return flag distribution
     */
    public Map<String, Integer> getFlagDistribution() {
        return originFlagDistribution;
    }

    /**
     * Return the number of times the given flag has been seen at the port.
     *
     * @param flag country flag to find in the mapping
     * @return number of times flag seen or 0 if not seen
     */
    public int getFlagStatistics(String flag) {
        return originFlagDistribution.getOrDefault(flag, 0);
    }

    /**
     * Updates the internal mapping of ship country flags using the given movement.
     * <p>
     * If the movement is not an {@link MovementDirection#INBOUND} movement, this method returns
     * immediately without taking any action.
     * <p>
     * If the movement is not a {@link ShipMovement} this method returns
     * immediately without taking any action.
     * <p>
     * If the movement is {@link MovementDirection#INBOUND} and a {@link ShipMovement}, do the
     * following:
     * <ul>
     *     <li>If the flag has been seen before (exists as a key in the map)
     *     increment that number
     *     </li>
     *     <li>If the flag has not been seen before add as a key in the map
     *     with a corresponding value of 1</li>
     * </ul>
     *
     * @param movement movement to read
     */
    @Override
    public void onProcessMovement(Movement movement) {
        if (!(movement instanceof ShipMovement)) {
            return;
        }

        if (movement.getDirection() != MovementDirection.INBOUND) {
            return;
        }

        // Updating the distribution with the originFlag from the ship involved in the movement.
        String originFlag = ((ShipMovement) movement).getShip().getOriginFlag();
        originFlagDistribution.merge(originFlag, 1, Integer::sum);
    }
}
