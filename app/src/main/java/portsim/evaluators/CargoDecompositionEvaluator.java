package portsim.evaluators;

import portsim.cargo.*;
import portsim.movement.CargoMovement;
import portsim.movement.Movement;
import portsim.movement.MovementDirection;
import portsim.movement.ShipMovement;
import portsim.ship.BulkCarrier;
import portsim.ship.ContainerShip;
import portsim.ship.Ship;

import java.util.HashMap;
import java.util.Map;

/**
 * Collects data on what types of cargo are passing through the port. Gathers data on all
 * derivatives of the cargo class.
 * <p>
 * The data gathered is a count of how many times each type of cargo has entered the port.
 * This includes a count of how many times the port has received {@link BulkCargo}
 * or {@link Container} class cargo. As well as a count of how many times the port has seen each
 * cargo subclass type ({@link ContainerType} and {@link BulkCargoType}).
 */
public class CargoDecompositionEvaluator extends StatisticsEvaluator {

    /**
     * The distribution of which cargo types that have entered the port.
     */
    private Map<String, Integer> cargoDistribution;

    /**
     * The distribution of bulk cargo types that have entered the port.
     */
    private Map<BulkCargoType, Integer> bulkCargoDistribution;

    /**
     * The distribution of container cargo types that have entered the port.
     */
    private Map<ContainerType, Integer> containerDistribution;

    /**
     * Constructs a new CargoDecompositionEvaluator.
     */
    public CargoDecompositionEvaluator() {
        super();
        cargoDistribution = new HashMap<>();
        bulkCargoDistribution = new HashMap<>();
        containerDistribution = new HashMap<>();
    }

    /**
     * Returns the distribution of bulk cargo types that have entered the port.
     *
     * @return BulkCargo distribution map
     */
    public Map<BulkCargoType, Integer> getBulkCargoDistribution() {
        return bulkCargoDistribution;
    }

    /**
     * Returns the distribution of which cargo types that have entered the port.
     *
     * @return cargo distribution map
     */
    public Map<String, Integer> getCargoDistribution() {
        return cargoDistribution;
    }

    /**
     * Returns the distribution of container cargo types that have entered the port.
     *
     * @return Container distribution map
     */
    public Map<ContainerType, Integer> getContainerDistribution() {
        return containerDistribution;
    }

    /**
     * Updates the internal distributions of cargo types using the given movement.
     * <p>
     * If the movement is not an {@link MovementDirection#INBOUND} movement,
     * this method returns immediately without taking any action.
     * <p>
     * If the movement is an {@link MovementDirection#INBOUND} movement,
     * do the following:
     * <ul>
     * <li>If the movement is a {@link ShipMovement},
     * Retrieve the cargo from the ships and for each piece of cargo:
     * <ol>
     * <li>If the cargo class ({@link Container} / {@link BulkCargo}) has been seen before
     * (simple name exists as a key in the cargo map) -&gt; increment that number</li>
     * <li>If the cargo class has not been seen before then add its class simple name as a key in
     * the map with a corresponding value of 1</li>
     * <li>If the cargo type (Value of {@link ContainerType} / {@link BulkCargoType}) for the given
     * cargo class has been seen before (exists as a key in the map) increment that number</li>
     * <li>If the cargo type (Value of {@link ContainerType} / {@link BulkCargoType}) for the given
     * cargo class has not been seen before add as a key in the map with a corresponding value of 1
     * </li></ol></li>
     * <li>If the movement is a {@link CargoMovement},
     * Retrieve the cargo from the movement. For the cargo retrieved:
     * <ol>
     * <li>Complete steps 1-4 as given above for ShipMovement</li>
     * </ol></li>
     * </ul>
     *
     * @param movement movement to read
     */
    @Override
    public void onProcessMovement(Movement movement) {
        // Only taking action for INBOUND Movements!
        if (movement == null || movement.getDirection() != MovementDirection.INBOUND) {
            return;
        }

        if (movement instanceof ShipMovement) {
            Ship ship = ((ShipMovement) movement).getShip();

            if (ship instanceof BulkCarrier) {
                BulkCargo cargo = ((BulkCarrier) ship).getCargo();
                updateDistribution(bulkCargoDistribution, cargo.getType(), cargo);

            } else if (ship instanceof ContainerShip) {
                // Updating the distributions for every container.
                for (Container container : ((ContainerShip) ship).getCargo()) {
                    updateDistribution(containerDistribution, container.getType(), container);
                }
            }

        } else if (movement instanceof CargoMovement) {
            // Updating the distributions for every cargo in the movement.
            for (Cargo cargo : ((CargoMovement) movement).getCargo()) {
                if (cargo instanceof BulkCargo) {
                    updateDistribution(bulkCargoDistribution, ((BulkCargo) cargo).getType(), cargo);

                } else if (cargo instanceof Container) {
                    updateDistribution(containerDistribution, ((Container) cargo).getType(), cargo);
                }
            }
        }
    }

    /**
     * Helper method that updates the cargoDistribution and another given distribution as
     * described in {@link #onProcessMovement(Movement)}.
     *
     * @param distribution The distribution being updated
     * @param type         The cargo type used as the key in the distribution
     * @param cargo        The cargo being processed
     * @param <K>          The type of the key for the distribution.
     */
    private <K> void updateDistribution(Map<K, Integer> distribution, K type, Cargo cargo) {
        // Adding the key and 1 to the distributions or incrementing it by 1 if it already exists.
        cargoDistribution.merge(cargo.getClass().getSimpleName(), 1, Integer::sum);
        distribution.merge(type, 1, Integer::sum);
    }
}
