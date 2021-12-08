package portsim.cargo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import portsim.ship.Ship;

import static org.junit.Assert.assertEquals;

public class BulkCargoTest {
    private BulkCargo minBulkCargo;
    private BulkCargo maxBulkCargo;
    private BulkCargo normalBulkCargo;

    @Before
    public void setUp() throws Exception {
        Ship.resetShipRegistry();
        Cargo.resetCargoRegistry();

        minBulkCargo = new BulkCargo(0, "", 0, BulkCargoType.GRAIN);
        maxBulkCargo = new BulkCargo(Integer.MAX_VALUE, "The United Kingdom of Great Britain and Northern Ireland", Integer.MAX_VALUE, BulkCargoType.MINERALS);
        normalBulkCargo = new BulkCargo(21, "Brazil", 420, BulkCargoType.COAL);
    }

    /**
     * Testing with negative tonnage.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorTest1() {
        new BulkCargo(1, "AU", -1, BulkCargoType.OTHER);
    }

    /**
     * Testing with negative id.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorTest2() {
        new BulkCargo(-1, "USA", 1, BulkCargoType.COAL);
    }

    /**
     * Testing valid lower bounds.
     */
    @Test
    public void constructorTest3() {
        Cargo.resetCargoRegistry();
        BulkCargo bulkCargo = new BulkCargo(0, "", 0, BulkCargoType.MINERALS);
        assertEquals("Invalid ID", 0, bulkCargo.getId());
        assertEquals("Invalid Destination", "", bulkCargo.getDestination());
        assertEquals("Invalid tonnage", 0, bulkCargo.getTonnage());
        assertEquals("Invalid Type", BulkCargoType.MINERALS, bulkCargo.getType());
    }

    /**
     * Testing valid upper bounds.
     */
    @Test
    public void constructorTest4() {
        Cargo.resetCargoRegistry();
        BulkCargo bulkCargo = new BulkCargo(Integer.MAX_VALUE, "The United Kingdom of Great Britain and Northern Ireland",
                Integer.MAX_VALUE, BulkCargoType.OIL);
        assertEquals("Invalid ID", Integer.MAX_VALUE, bulkCargo.getId());
        assertEquals("Invalid Destination", "The United Kingdom of Great Britain and Northern Ireland", bulkCargo.getDestination());
        assertEquals("Invalid tonnage", Integer.MAX_VALUE, bulkCargo.getTonnage());
        assertEquals("Invalid Type", BulkCargoType.OIL, bulkCargo.getType());
    }

    /**
     * Testing normal case.
     */
    @Test
    public void constructorTest5() {
        BulkCargo bulkCargo = new BulkCargo(69, "New Zealand", 100, BulkCargoType.OTHER);
        assertEquals("Invalid ID", 69, bulkCargo.getId());
        assertEquals("Invalid Destination", "New Zealand", bulkCargo.getDestination());
        assertEquals("Invalid tonnage", 100, bulkCargo.getTonnage());
        assertEquals("Invalid Type", BulkCargoType.OTHER, bulkCargo.getType());
    }

    /**
     * Testing tonnage getter min
     */
    @Test
    public void getTonnageTest1() {
        assertEquals("The tonnage was incorrect.", 0, minBulkCargo.getTonnage());
    }

    /**
     * Testing tonnage getter max
     */
    @Test
    public void getTonnageTest2() {
        assertEquals("The tonnage was incorrect.", Integer.MAX_VALUE, maxBulkCargo.getTonnage());
    }

    /**
     * Testing tonnage getter normal
     */
    @Test
    public void getTonnageTest3() {
        assertEquals("The tonnage was incorrect.", 420, normalBulkCargo.getTonnage());
    }

    /**
     * Testing type getter min.
     */
    @Test
    public void getTypeTest1() {
        assertEquals("The type was incorrect.", BulkCargoType.GRAIN, minBulkCargo.getType());
    }

    /**
     * Testing type getter max.
     */
    @Test
    public void getTypeTest2() {
        assertEquals("The type was incorrect.", BulkCargoType.MINERALS, maxBulkCargo.getType());
    }
    /**
     * Testing type getter normal.
     */
    @Test
    public void getTypeTest3() {
        assertEquals("The type was incorrect.", BulkCargoType.COAL, normalBulkCargo.getType());
    }

    /**
     * Testing toString representation min case
     */
    @Test
    public void toStringTest1() {
        assertEquals("The toString representation was incorrect.",
                "BulkCargo 0 to  [GRAIN - 0]", minBulkCargo.toString());
    }

    /**
     * Testing toString representation max case
     */
    @Test
    public void toStringTest2() {
        assertEquals("The toString representation was incorrect.",
                String.format("BulkCargo %d to The United Kingdom of Great Britain and Northern Ireland [MINERALS - %d]", Integer.MAX_VALUE, Integer.MAX_VALUE), maxBulkCargo.toString());
    }

    /**
     * Testing toString representation with normal case
     */
    @Test
    public void toStringTest3() {
        assertEquals("The toString representation was incorrect.",
                "BulkCargo 21 to Brazil [COAL - 420]", normalBulkCargo.toString());
    }
}