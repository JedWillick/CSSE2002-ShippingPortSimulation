package portsim.evaluators;

import portsim.movement.Movement;
import portsim.movement.MovementDirection;
import portsim.movement.ShipMovement;

import java.util.ArrayList;
import java.util.List;

/**
 * Gathers data on how many ships pass through the port over time.
 * <p>
 * This evaluator only counts ships that have passed through the port in the last hour (60 minutes)
 */
public class ShipThroughputEvaluator extends StatisticsEvaluator {

    /**
     * A list containing the exit times of the ships that have passed through the port.
     */
    private List<Long> exitTimes;

    /**
     * Constructs a new ShipThroughputEvaluator.
     * <p>
     * Immediately after creating a new ShipThroughputEvaluator, {@link #getThroughputPerHour()}
     * should return 0.
     */
    public ShipThroughputEvaluator() {
        super();
        exitTimes = new ArrayList<>();
    }

    /**
     * Return the number of ships that have passed through the port in the last 60 minutes.
     *
     * @return ships throughput
     */
    public int getThroughputPerHour() {
        return exitTimes.size();
    }

    /**
     * Updates the internal count of ships that have passed through the port using the given
     * movement.
     * <p>
     * If the movement is not an <code>OUTBOUND ShipMovement</code>, this method returns
     * immediately without taking any action.
     * <p>
     * Otherwise, the internal state of this evaluator should be modified such that
     * {@link #getThroughputPerHour()} should return a value 1 more than before this method was
     * called. e.g. If the following code and output occurred over a program execution:<br>
     * <table border="1"><caption>Example of behaviour</caption>
     *     <tr>
     *         <th>Java method call</th>
     *         <th>Returned value</th>
     *     </tr>
     *     <tr>
     *         <td><code>getThroughputPerHour()</code></td>
     *         <td>1</td>
     *     </tr>
     *     <tr>
     *         <td><code>onProcessMovement(validMovement)</code></td>
     *         <td>void</td>
     *     </tr>
     *     <tr>
     *         <td><code>getThroughputPerHour()</code></td>
     *         <td>2</td>
     *     </tr>
     * </table>
     * <p>
     * Where <code>validMovement</code> is an OUTBOUND ShipMovement.
     *
     * @param movement movement to read
     */
    @Override
    public void onProcessMovement(Movement movement) {
        // Only taking action for OUTBOUND ShipMovement's
        if ((movement instanceof ShipMovement)
                && (movement.getDirection() == MovementDirection.OUTBOUND)) {

            exitTimes.add(getTime());
        }
    }

    /**
     * Simulate a minute passing. The time since the evaluator was created should be incremented by
     * one.
     * <p>
     * If it has been more than 60 minutes since a ship exited the port, it should no longer be
     * counted towards the count returned by {@link #getThroughputPerHour()}.
     */
    @Override
    public void elapseOneMinute() {
        super.elapseOneMinute();

        // Removing times that have been counted for more than 60 minutes
        exitTimes.removeIf(time -> (getTime() > (time + 60)));
    }
}
