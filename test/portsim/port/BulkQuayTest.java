package portsim.port;

import org.junit.Before;
import org.junit.Test;
import portsim.cargo.Cargo;
import portsim.ship.BulkCarrier;
import portsim.ship.NauticalFlag;
import portsim.ship.Ship;

import static org.junit.Assert.assertEquals;

public class BulkQuayTest {
    private BulkQuay minBulkQuay;
    private BulkQuay maxBulkQuay;
    private BulkQuay normalBulkQuay1;

    private BulkCarrier bulkCarrier;

    @Before
    public void setUp() throws Exception {
        Ship.resetShipRegistry();
        Cargo.resetCargoRegistry();

        minBulkQuay = new BulkQuay(0, 0);
        maxBulkQuay = new BulkQuay(Integer.MAX_VALUE, Integer.MAX_VALUE);
        normalBulkQuay1 = new BulkQuay(420, 500);

        bulkCarrier = new BulkCarrier(4204201, "Enterprise", "USA",
                NauticalFlag.WHISKEY, 100);
    }

    /**
     * Testing with negative id
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorTest1() {
        new BulkQuay(-1, 1);
    }

    /**
     * Testing with negative maxTonnage
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorTest2() {
        new BulkQuay(1, -1);
    }

    /**
     * Testing with maximum numbers.
     */
    @Test
    public void constructorTest3() {
        BulkQuay bulkQuay = new BulkQuay(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals("Incorrect ID", Integer.MAX_VALUE, bulkQuay.getId());
        assertEquals("Incorrect maxTonnage", Integer.MAX_VALUE, bulkQuay.getMaxTonnage());
    }

    /**
     * Testing with minimum numbers.
     */
    @Test
    public void constructorTest4() {
        BulkQuay bulkQuay = new BulkQuay(0, 0);
        assertEquals("Incorrect ID", 0, bulkQuay.getId());
        assertEquals("Incorrect maxTonnage", 0, bulkQuay.getMaxTonnage());
    }

    /**
     * Testing normal case.
     */
    @Test
    public void constructorTest5() {
        BulkQuay bulkQuay = new BulkQuay(420, 100);
        assertEquals("Incorrect ID", 420, bulkQuay.getId());
        assertEquals("Incorrect maxTonnage", 100, bulkQuay.getMaxTonnage());
    }

    /**
     * Testing maxTonnage getter min.
     */
    @Test
    public void getMaxTonnageTest1() {
        assertEquals("The maxTonnage was incorrect.", 0,
                minBulkQuay.getMaxTonnage());
    }

    /**
     * Testing maxTonnage getter max.
     */
    @Test
    public void getMaxTonnageTest2() {
        assertEquals("The maxTonnage was incorrect.", Integer.MAX_VALUE,
                maxBulkQuay.getMaxTonnage());
    }

    /**
     * Testing maxTonnage getter normal.
     */
    @Test
    public void getMaxTonnageTest3() {
        assertEquals("The maxTonnage was incorrect.", 500,
                normalBulkQuay1.getMaxTonnage());
    }

    /**
     * Testing when the quay is empty min case.
     */
    @Test
    public void toStringTest1() {
        assertEquals("The toString representation was incorrect.",
                "BulkQuay 0 [Ship: None] - 0", minBulkQuay.toString());
    }

    /**
     * Testing when the quay has a ship min case.
     */
    @Test
    public void toStringTest2() {
        minBulkQuay.shipArrives(bulkCarrier);
        assertEquals("The toString representation was incorrect.",
                "BulkQuay 0 [Ship: 4204201] - 0", minBulkQuay.toString());
    }

    /**
     * Testing when the quay is empty max case.
     */
    @Test
    public void toStringTest3() {
        assertEquals("The toString representation was incorrect.",
                String.format("BulkQuay %d [Ship: None] - %d", Integer.MAX_VALUE, Integer.MAX_VALUE), maxBulkQuay.toString());
    }

    /**
     * Testing when the quay has a ship max case.
     */
    @Test
    public void toStringTest4() {
        maxBulkQuay.shipArrives(bulkCarrier);
        assertEquals("The toString representation was incorrect.",
                String.format("BulkQuay %d [Ship: 4204201] - %d", Integer.MAX_VALUE, Integer.MAX_VALUE), maxBulkQuay.toString());
    }

    /**
     * Testing when the quay is empty normal case.
     */
    @Test
    public void toStringTest5() {
        assertEquals("The toString representation was incorrect.",
                "BulkQuay 420 [Ship: None] - 500", normalBulkQuay1.toString());
    }

    /**
     * Testing when the quay has a ship normal case.
     */
    @Test
    public void toStringTest6() {
        normalBulkQuay1.shipArrives(bulkCarrier);
        assertEquals("The toString representation was incorrect.",
                "BulkQuay 420 [Ship: 4204201] - 500", normalBulkQuay1.toString());
    }


    /**
     * Testing that the toString updates accordingly when a ship departs.
     */
    @Test
    public void toStringTest7() {
        normalBulkQuay1.shipArrives(bulkCarrier);
        normalBulkQuay1.shipDeparts();
        assertEquals("shipArrives allows any ship to dock.",
                "BulkQuay 420 [Ship: None] - 500", normalBulkQuay1.toString());
    }
}