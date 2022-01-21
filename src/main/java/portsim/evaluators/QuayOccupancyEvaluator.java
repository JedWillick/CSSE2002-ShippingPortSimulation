package portsim.evaluators;

import portsim.movement.Movement;
import portsim.port.Port;
import portsim.port.Quay;

/**
 * Evaluator to monitor how many quays are currently occupied at the port.
 */
public class QuayOccupancyEvaluator extends StatisticsEvaluator {

    /**
     * The port being monitored.
     */
    private Port port;

    /**
     * Constructs a new QuayOccupancyEvaluator.
     *
     * @param port port to monitor quays
     */
    public QuayOccupancyEvaluator(Port port) {
        super();
        this.port = port;
    }

    /**
     * Return the number of quays that are currently occupied.
     * <p>
     * A quay is occupied if {@link Quay#isEmpty()} returns false.
     *
     * @return number of quays occupied.
     */
    public int getQuaysOccupied() {
        // Counting the number of quays that are not empty.
        return (int) port.getQuays().stream().filter(quay -> !quay.isEmpty()).count();
    }

    /**
     * QuayOccupancyEvaluator does not make use of onProcessMovement(), so this method can be
     * left empty.
     * <p>
     * Does nothing. This method is not used by this evaluator.
     *
     * @param movement movement to read
     */
    @Override
    public void onProcessMovement(Movement movement) {

    }
}
