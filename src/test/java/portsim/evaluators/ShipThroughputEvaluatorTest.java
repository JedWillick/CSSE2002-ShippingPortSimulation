package portsim.evaluators;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import portsim.cargo.*;
import portsim.movement.CargoMovement;
import portsim.movement.MovementDirection;
import portsim.movement.ShipMovement;
import portsim.ship.BulkCarrier;
import portsim.ship.ContainerShip;
import portsim.ship.NauticalFlag;
import portsim.ship.Ship;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ShipThroughputEvaluatorTest {
    private ShipThroughputEvaluator emptyEvaluator;
    private ShipThroughputEvaluator populatedEvaluator;

    private ContainerShip containerShip1;
    private ContainerShip containerShip2;
    private BulkCarrier bulkCarrier1;
    private BulkCarrier bulkCarrier2;

    private ShipMovement shipMovement1;
    private ShipMovement shipMovement2;
    private ShipMovement shipMovement3;
    private ShipMovement shipMovement4;

    private List<Cargo> cargos;

    @Before
    public void setUp() throws Exception {
        containerShip1 = new ContainerShip(1234567, "containerShip1", "AU", NauticalFlag.BRAVO, 100);
        containerShip2 = new ContainerShip(1234568, "containerShip2", "AU", NauticalFlag.BRAVO, 100);
        bulkCarrier1 = new BulkCarrier(1234569, "bulkCarrier1", "AU", NauticalFlag.BRAVO, 100);
        bulkCarrier2 = new BulkCarrier(2234569, "bulkCarrier2", "AU", NauticalFlag.BRAVO, 100);

        shipMovement1 = new ShipMovement(1, MovementDirection.OUTBOUND, containerShip1);
        shipMovement2 = new ShipMovement(1, MovementDirection.OUTBOUND, containerShip2);
        shipMovement3 = new ShipMovement(1, MovementDirection.OUTBOUND, bulkCarrier1);
        shipMovement4 = new ShipMovement(1, MovementDirection.OUTBOUND, bulkCarrier2);

        cargos = new ArrayList<>();
        cargos.add(new BulkCargo(1, "AU", 10, BulkCargoType.OTHER));
        cargos.add(new BulkCargo(2, "AU", 10, BulkCargoType.OTHER));
        cargos.add(new Container(3, "AU", ContainerType.REEFER));


        emptyEvaluator = new ShipThroughputEvaluator();
        populatedEvaluator = new ShipThroughputEvaluator();

        try {
            populatedEvaluator.onProcessMovement(shipMovement1);
            populatedEvaluator.onProcessMovement(shipMovement2);
            populatedEvaluator.onProcessMovement(shipMovement3);
        } catch (Exception ignore) {

        }
    }

    @After
    public void tearDown() throws Exception {
        Ship.resetShipRegistry();
        Cargo.resetCargoRegistry();
    }

    @Test
    public void ShipThroughputEvaluatorTestExtendsStatisticsEvaluator() {
        ShipThroughputEvaluator shipThroughputEvaluator = new ShipThroughputEvaluator();
        assertTrue("ShipThroughputEvaluator should extend StatisticsEvaluator!",
                shipThroughputEvaluator instanceof StatisticsEvaluator);
    }

    @Test
    public void constructorTestTime() {
        ShipThroughputEvaluator shipThroughputEvaluator = new ShipThroughputEvaluator();
        assertEquals("Time should be initialised to 0 through super()!", 0, shipThroughputEvaluator.getTime());
    }

    @Test
    public void constructorTestThroughputPerHourInitialised() {
        ShipThroughputEvaluator shipThroughputEvaluator = new ShipThroughputEvaluator();
        assertEquals("Immediately after creating a new ShipThroughputEvaluator, getThroughputPerHour() should return 0!",
                0, shipThroughputEvaluator.getThroughputPerHour());
    }

    @Test
    public void getThroughputPerHourTestNoShips() {
        assertEquals("No ships have passed through! Should be 0.", 0, emptyEvaluator.getThroughputPerHour());
    }

    @Test
    public void getThroughputPerHourTestPopulated() {
        assertEquals("Three ships have passed through!", 3, populatedEvaluator.getThroughputPerHour());
    }

    @Test
    public void getThroughputPerHourTestNewShip() {
        populatedEvaluator.onProcessMovement(shipMovement4);
        assertEquals("Four ships have passed through!", 4, populatedEvaluator.getThroughputPerHour());
    }

    @Test
    public void getThroughputPerHourTestSameShip() {
        populatedEvaluator.onProcessMovement(shipMovement1);
        assertEquals("Four ships have passed through!", 4, populatedEvaluator.getThroughputPerHour());
    }

    @Test
    public void onProcessMovementTestINBOUNDEmpty() {
        ShipMovement shipMovement = new ShipMovement(1, MovementDirection.INBOUND, containerShip1);
        int beforeProcessMovement = emptyEvaluator.getThroughputPerHour();
        emptyEvaluator.onProcessMovement(shipMovement);
        assertEquals("INBOUND movements should return without taking action!",
                beforeProcessMovement, emptyEvaluator.getThroughputPerHour());
    }

    @Test
    public void onProcessMovementTestINBOUNDPopulated() {
        ShipMovement shipMovement = new ShipMovement(1, MovementDirection.INBOUND, containerShip1);
        int beforeProcessMovement = populatedEvaluator.getThroughputPerHour();
        populatedEvaluator.onProcessMovement(shipMovement);
        assertEquals("INBOUND movements should return without taking action!",
                beforeProcessMovement, populatedEvaluator.getThroughputPerHour());
    }

    @Test
    public void onProcessMovementTestCargoMovementEmpty() {
        CargoMovement cargoMovement = new CargoMovement(1, MovementDirection.OUTBOUND, cargos);
        int beforeProcessMovement = emptyEvaluator.getThroughputPerHour();
        emptyEvaluator.onProcessMovement(cargoMovement);
        assertEquals("CargoMovement should return without taking action!",
                beforeProcessMovement, emptyEvaluator.getThroughputPerHour());
    }

    @Test
    public void onProcessMovementTestCargoMovementPopulated() {
        CargoMovement cargoMovement = new CargoMovement(1, MovementDirection.OUTBOUND, cargos);
        int beforeProcessMovement = populatedEvaluator.getThroughputPerHour();
        populatedEvaluator.onProcessMovement(cargoMovement);
        assertEquals("CargoMovement should return without taking action!",
                beforeProcessMovement, populatedEvaluator.getThroughputPerHour());
    }

    @Test
    public void onProcessMovementTestNullEmpty() {
        int beforeProcessMovement = emptyEvaluator.getThroughputPerHour();
        try {
            emptyEvaluator.onProcessMovement(null);
            assertEquals("A null argument should return without taking action!",
                    beforeProcessMovement, emptyEvaluator.getThroughputPerHour());
        } catch (NullPointerException nullPointerException) {
            fail("onProcessMovement checks Direction before checking if its a valid Movement!");
        }
    }

    @Test
    public void onProcessMovementTestNullPopulated() {
        int beforeProcessMovement = populatedEvaluator.getThroughputPerHour();
        try {
            populatedEvaluator.onProcessMovement(null);
            assertEquals("A null argument should return without taking action!",
                    beforeProcessMovement, populatedEvaluator.getThroughputPerHour());
        } catch (NullPointerException nullPointerException) {
            fail("onProcessMovement checks Direction before checking if its a valid Movement!");
        }
    }

    @Test
    public void onProcessMovementTestValidEmpty() {
        int beforeProcessMovement = emptyEvaluator.getThroughputPerHour();
        emptyEvaluator.onProcessMovement(shipMovement4);
        assertEquals("OUTBOUND ShipMovement's should increase getThroughputPerHour by 1.",
                beforeProcessMovement + 1, emptyEvaluator.getThroughputPerHour());
    }

    @Test
    public void onProcessMovementTestValidPopulated() {
        int beforeProcessMovement = populatedEvaluator.getThroughputPerHour();
        populatedEvaluator.onProcessMovement(shipMovement4);
        assertEquals("OUTBOUND ShipMovement's should increase getThroughputPerHour by 1.",
                beforeProcessMovement + 1, populatedEvaluator.getThroughputPerHour());
    }

    @Test
    public void onProcessMovementTestSameShip() {
        int before = emptyEvaluator.getThroughputPerHour();
        emptyEvaluator.onProcessMovement(shipMovement1);
        assertEquals("OUTBOUND ShipMovement's should increase getThroughputPerHour by 1.",
                before + 1, emptyEvaluator.getThroughputPerHour());

        before = emptyEvaluator.getThroughputPerHour();
        emptyEvaluator.onProcessMovement(shipMovement1);
        assertEquals("OUTBOUND ShipMovement's should increase getThroughputPerHour by 1."
                ,before + 1, emptyEvaluator.getThroughputPerHour());
    }

    @Test
    public void onProcessMovementTestFull() {
        int before = emptyEvaluator.getThroughputPerHour();
        emptyEvaluator.onProcessMovement(shipMovement1);
        assertEquals("OUTBOUND ShipMovement's should increase getThroughputPerHour by 1.",
                before + 1, emptyEvaluator.getThroughputPerHour());

        before = emptyEvaluator.getThroughputPerHour();
        emptyEvaluator.onProcessMovement(shipMovement2);
        assertEquals("OUTBOUND ShipMovement's should increase getThroughputPerHour by 1.",
                before + 1, emptyEvaluator.getThroughputPerHour());

        before = emptyEvaluator.getThroughputPerHour();
        emptyEvaluator.onProcessMovement(shipMovement3);
        assertEquals("OUTBOUND ShipMovement's should increase getThroughputPerHour by 1.",
                before + 1, emptyEvaluator.getThroughputPerHour());

        before = emptyEvaluator.getThroughputPerHour();
        emptyEvaluator.onProcessMovement(shipMovement4);
        assertEquals("OUTBOUND ShipMovement's should increase getThroughputPerHour by 1.",
                before + 1, emptyEvaluator.getThroughputPerHour());
    }

    @Test
    public void elapseOneMinuteTestTimeIncremented() {
        long before = populatedEvaluator.getTime();
        populatedEvaluator.elapseOneMinute();
        assertEquals("Time wasn't incremented by one minute!",
                before + 1, populatedEvaluator.getTime());
    }

    @Test
    public void elapseOneMinuteTestRemainsFor60() {
        // populatedEvaluator's movements were all added at time = 0
        int before = populatedEvaluator.getThroughputPerHour();
        // Elapsing 60 minutes.
        for (int i = 0; i < 60; i++) {
            populatedEvaluator.elapseOneMinute();
        }
        assertEquals("Incorrect time", 60, populatedEvaluator.getTime());

        assertEquals("It has not been more than 60 minutes!",
                before, populatedEvaluator.getThroughputPerHour());
    }

    @Test
    public void elapseOneMinuteTestRemovesMoreThan60() {
        // Elapsing 61 minutes.
        for (int i = 0; i < 61; i++) {
            populatedEvaluator.elapseOneMinute();
        }
        assertEquals("Incorrect time", 61, populatedEvaluator.getTime());

        // populatedEvaluator's movements were all added at time = 0
        // So they should all be removed at time = 61.
        assertEquals("It has been more than 60 minutes!",
                0, populatedEvaluator.getThroughputPerHour());
    }

    @Test
    public void elapseOneMinuteTestRemovesMoreThan60SameShip() {
        populatedEvaluator.elapseOneMinute();
        populatedEvaluator.onProcessMovement(shipMovement1);
        // Elapsing 60 minutes.
        for (int i = 0; i < 60; i++) {
            populatedEvaluator.elapseOneMinute();
        }
        assertEquals("Incorrect time", 61, populatedEvaluator.getTime());

        assertEquals("Only ships that have been counted for more than 60 minutes should be removed",
                1, populatedEvaluator.getThroughputPerHour());
    }

    @Test
    public void elapseOneMinuteTestVaryingTimes() {
        emptyEvaluator.elapseOneMinute();
        assertEquals("Incorrect time", 1, emptyEvaluator.getTime());
        // Added at time = 1
        emptyEvaluator.onProcessMovement(shipMovement1);

        for (int i = 0; i < 10; i++) {
            emptyEvaluator.elapseOneMinute();
        }
        assertEquals("Incorrect time", 11, emptyEvaluator.getTime());
        // Added at time = 11
        emptyEvaluator.onProcessMovement(shipMovement2);

        for (int i = 0; i < 3; i++) {
            emptyEvaluator.elapseOneMinute();
        }
        assertEquals("Incorrect time", 14, emptyEvaluator.getTime());
        // Added at time = 14
        emptyEvaluator.onProcessMovement(shipMovement3);

        for (int i = 0; i < 29; i++) {
            emptyEvaluator.elapseOneMinute();
        }
        assertEquals("Incorrect time", 43, emptyEvaluator.getTime());
        // Added at time = 43
        emptyEvaluator.onProcessMovement(shipMovement4);

        // Elapse 19 minutes to remove first ship.
        for (int i = 0; i < 19; i++) {
            emptyEvaluator.elapseOneMinute();
        }
        assertEquals("Incorrect time", 62, emptyEvaluator.getTime());

        // First ship should no longer be counted.
        assertEquals("First ship exited more than 60 minutes ago!", 3, emptyEvaluator.getThroughputPerHour());

        // Elapse 10 minutes to remove next ship.
        for (int i = 0; i < 10; i++) {
            emptyEvaluator.elapseOneMinute();
        }
        assertEquals("Incorrect time", 72, emptyEvaluator.getTime());

        // Next ship should no longer be counted.
        assertEquals("A ship exited more than 60 minutes ago!", 2, emptyEvaluator.getThroughputPerHour());

        // Elapse 3 minutes to remove next ship.
        for (int i = 0; i < 3; i++) {
            emptyEvaluator.elapseOneMinute();
        }
        assertEquals("Incorrect time", 75, emptyEvaluator.getTime());

        // Next ship should no longer be counted.
        assertEquals("A ship exited more than 60 minutes ago!", 1, emptyEvaluator.getThroughputPerHour());

        // Elapse 29 minutes to remove next ship.
        for (int i = 0; i < 29; i++) {
            emptyEvaluator.elapseOneMinute();
        }
        assertEquals("Incorrect time", 104, emptyEvaluator.getTime());

        // No more ships in the evaluator
        assertEquals("A ship exited more than 60 minutes ago!", 0, emptyEvaluator.getThroughputPerHour());
    }
}