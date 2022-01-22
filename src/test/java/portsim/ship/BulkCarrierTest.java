package portsim.ship;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import portsim.cargo.*;
import portsim.port.BulkQuay;
import portsim.port.ContainerQuay;
import portsim.util.NoSuchCargoException;

import static org.junit.Assert.*;

public class BulkCarrierTest {
    private BulkCarrier minBulkCarrier;
    private BulkCarrier maxBulkCarrier;
    private BulkCarrier normalBulkCarrier1;
    private BulkCarrier normalBulkCarrier2;

    private BulkCargo minBulkCargo;
    private BulkCargo maxBulkCargo;
    private BulkCargo normalBulkCargo1;
    private BulkCargo normalBulkCargo2;

    @Before
    public void setUp() throws Exception {
        minBulkCarrier = new BulkCarrier(1000000, "", "", NauticalFlag.HOTEL, 0);
        minBulkCargo = new BulkCargo(0, "", 0, BulkCargoType.OIL);

        maxBulkCarrier = new BulkCarrier(9999999,
                "This is a very very very very very very very very very long and dumb Bulk Carrier name.",
                "The United Kingdom of Great Britain and Northern Ireland", NauticalFlag.BRAVO, Integer.MAX_VALUE);
        maxBulkCargo = new BulkCargo(Integer.MAX_VALUE,
                "The United Kingdom of Great Britain and Northern Ireland",
                Integer.MAX_VALUE, BulkCargoType.MINERALS);

        normalBulkCarrier1 = new BulkCarrier(1234567, "Voyager", "AU", NauticalFlag.WHISKEY, 100);
        normalBulkCargo1 = new BulkCargo(1, "AU", 100, BulkCargoType.GRAIN);

        normalBulkCarrier2 = new BulkCarrier(7654321, "Joe's Sheepy Ship", "New Zealand", NauticalFlag.NOVEMBER, 500);
        normalBulkCargo2 = new BulkCargo(21, "New Zealand", 222, BulkCargoType.OTHER);
    }

    @After
    public void tearDown() throws Exception {
        Ship.resetShipRegistry();
        Cargo.resetCargoRegistry();
    }

    /**
     * Testing imoNumber exception lower bounds.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorTest1() {
        new BulkCarrier(999999, "Voyager", "AU", NauticalFlag.WHISKEY, 100);
    }

    /**
     * Testing imoNumber exception upper bounds.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorTest2() {
        new BulkCarrier(10000000, "Voyager", "AU", NauticalFlag.WHISKEY, 100);
    }

    /**
     * Testing exception negative imoNumber with String length 7
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorTest3() {
        new BulkCarrier(-111111, "Voyager", "AU", NauticalFlag.WHISKEY, 100);

    }

    /**
     * Testing negative capacity
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorTest4() {
        new BulkCarrier(1234567, "Voyager", "AU", NauticalFlag.WHISKEY, -1);
    }

    /**
     * Testing min edge case valid params.
     */
    @Test
    public void constructorTest5() {
        Ship.resetShipRegistry();
        BulkCarrier bulkCarrier = new BulkCarrier(1000000, "", "", NauticalFlag.HOTEL, 0);
        assertEquals("Incorrect ImoNumber", 1000000, bulkCarrier.getImoNumber());
        assertEquals("Incorrect name", "", bulkCarrier.getName());
        assertEquals("Incorrect originFlag", "", bulkCarrier.getOriginFlag());
        assertEquals("Incorrect flag", NauticalFlag.HOTEL, bulkCarrier.getFlag());
    }

    /**
     * Testing max edge case valid params.
     */
    @Test
    public void constructorTest6() {
        Ship.resetShipRegistry();
        BulkCarrier bulkCarrier = new BulkCarrier(9999999,
                "This is a very very very very very very very very very long and dumb Bulk Carrier name.",
                "The United Kingdom of Great Britain and Northern Ireland", NauticalFlag.BRAVO, Integer.MAX_VALUE);
        assertEquals("Incorrect ImoNumber", 9999999, bulkCarrier.getImoNumber());
        assertEquals("Incorrect name",
                "This is a very very very very very very very very very long and dumb Bulk Carrier name.", bulkCarrier.getName());
        assertEquals("Incorrect originFlag",
                "The United Kingdom of Great Britain and Northern Ireland", bulkCarrier.getOriginFlag());
        assertEquals("Incorrect flag", NauticalFlag.BRAVO, bulkCarrier.getFlag());
    }

    /**
     * Testing normal case.
     */
    @Test
    public void constructorTest7() {
        BulkCarrier bulkCarrier = new BulkCarrier(5000000, "Joe's Ship", "Brazil", NauticalFlag.NOVEMBER, 500);
        assertEquals("Incorrect ImoNumber", 5000000, bulkCarrier.getImoNumber());
        assertEquals("Incorrect name", "Joe's Ship", bulkCarrier.getName());
        assertEquals("Incorrect originFlag", "Brazil", bulkCarrier.getOriginFlag());
        assertEquals("Incorrect flag", NauticalFlag.NOVEMBER, bulkCarrier.getFlag());
    }

    /**
     * Testing first condition with a ContainerQuay.
     * "Quay must be a BulkQuay"
     */
    @Test
    public void canDockTest1() {
        assertFalse("Quay must be a BulkQuay",
                normalBulkCarrier1.canDock(new ContainerQuay(10, 10)));
    }

    /**
     * Testing first condition with null.
     * "Quay must be a BulkQuay"
     */
    @Test
    public void canDockTest2() {
        assertFalse("Quay must be a BulkQuay", normalBulkCarrier2.canDock(null));
    }

    /**
     * Testing second condition with no cargo. i.e. ship's cargo weight = 0
     * "The quay's maximum cargo weight must be &ge; this ship's cargo weight in tonnes.".
     */
    @Test
    public void canDockTest3() {
        assertTrue("Ship can dock.",
                normalBulkCarrier2.canDock(new BulkQuay(10, 10)));
    }

    /**
     * Testing second condition with normal case (Quays max < ships cargo)
     * "The quay's maximum cargo weight must be &ge; this ship's cargo weight in tonnes.".
     */
    @Test
    public void canDockTest4() {
        normalBulkCarrier1.loadCargo(normalBulkCargo1); // 100 tonnes loaded.
        assertFalse("Ship can't dock.",
                normalBulkCarrier1.canDock(new BulkQuay(10, 99)));
    }

    /**
     * Testing second condition with the minimum values (equals)
     * "The quay's maximum cargo weight must be &ge; this ship's cargo weight in tonnes.".
     */
    @Test
    public void canDockTest5() {
        minBulkCarrier.loadCargo(minBulkCargo); // 0 tonnes loaded.
        assertTrue("Ship can dock.",
                minBulkCarrier.canDock(new BulkQuay(10, 0)));
    }

    /**
     * Testing second condition with the maximum values (equals)
     * "The quay's maximum cargo weight must be &ge; this ship's cargo weight in tonnes.".
     */
    @Test
    public void canDockTest6() {
        maxBulkCarrier.loadCargo(maxBulkCargo); // Integer.MAX_VALUE tonnes loaded.
        assertTrue("Ship can dock.",
                maxBulkCarrier.canDock(new BulkQuay(10, Integer.MAX_VALUE)));
    }

    /**
     * Testing second condition with normal case. (Quays max > ships cargo)
     * "The quay's maximum cargo weight must be &ge; this ship's cargo weight in tonnes.".
     */
    @Test
    public void canDockTest7() {
        normalBulkCarrier2.loadCargo(normalBulkCargo2); // 222 tonnes loaded.
        assertTrue("Ship can dock.",
                normalBulkCarrier2.canDock(new BulkQuay(21, 223)));
    }

    /**
     * Testing second condition with normal case. (Quays max = ships cargo)
     * "The quay's maximum cargo weight must be &ge; this ship's cargo weight in tonnes.".
     */
    @Test
    public void canDockTest8() {
        normalBulkCarrier1.loadCargo(normalBulkCargo1); // 100 tonnes loaded.
        assertTrue("Ship can dock.",
                normalBulkCarrier1.canDock(new BulkQuay(21, 100)));
    }

    /**
     * Testing fist condition, "The ship does not have any cargo on board".
     */
    @Test
    public void canLoadTest1() {
        normalBulkCarrier2.loadCargo(normalBulkCargo2); // Loading 222 ton cargo
        assertFalse("The ship already has cargo!",
                normalBulkCarrier2.canLoad(new BulkCargo(3, "New Zealand", 50, BulkCargoType.OIL)));
    }

    /**
     * Testing second condition by trying to load a container.
     * "The cargo given is a BulkCargo".
     */
    @Test
    public void canLoadTest2() {
        assertFalse("BulkCarrier can only load BulkCargo!",
                normalBulkCarrier2.canLoad(new Container(19, "New Zealand", ContainerType.REEFER)));
    }

    /**
     * Testing second condition by trying to load null.
     * "The cargo given is a BulkCargo".
     */
    @Test
    public void canLoadTest3() {
        assertFalse("BulkCarrier can only load BulkCargo!",
                normalBulkCarrier1.canLoad(null));
    }

    /**
     * Testing third condition, "The cargo tonnage is less than or equal to the ship's tonnage
     * capacity".
     * Normal case greater than.
     */
    @Test
    public void canLoadTest4() {
        assertFalse("Cant load cargo over the max tonnage capacity!",
                normalBulkCarrier1.canLoad(new BulkCargo(33, "AU", 101, BulkCargoType.OIL)));
    }

    /**
     * Testing third condition, "The cargo tonnage is less than or equal to the ship's tonnage
     * capacity".
     * Maximum ship tonnage capacity and maximum cargo capacity (equals).
     */
    @Test
    public void canLoadTest5() {
        assertTrue("Can load cargo equal to max tonnage capacity!",
                maxBulkCarrier.canLoad(maxBulkCargo));
    }

    /**
     * Testing third condition, "The cargo tonnage is less than or equal to the ship's tonnage
     * capacity".
     * Minimum ship tonnage capacity and minimum cargo capacity (equals).
     */
    @Test
    public void canLoadTest6() {
        assertTrue("Can load cargo equal to max tonnage capacity!",
                minBulkCarrier.canLoad(minBulkCargo));
    }

    /**
     * Testing third condition, "The cargo tonnage is less than or equal to the ship's tonnage
     * capacity".
     * Normal case less than.
     */
    @Test
    public void canLoadTest7() {
        assertTrue("Can load cargo less than max tonnage capacity!",
                normalBulkCarrier2.canLoad(normalBulkCargo2));
    }

    /**
     * Testing third condition, "The cargo tonnage is less than or equal to the ship's tonnage
     * capacity".
     * Normal case equal too.
     */
    @Test
    public void canLoadTest8() {
        assertTrue("Can load cargo less than max tonnage capacity!",
                normalBulkCarrier1.canLoad(normalBulkCargo1));
    }

    /**
     * Testing last condition, "The cargo's destination is the same as the ship's origin country".
     * Empty string case not equal
     */
    @Test
    public void canLoadTest9() {
        assertFalse("Cargo's destination must be the same as the ship's origin!",
                minBulkCarrier.canLoad(new BulkCargo(33, "AU", 0, BulkCargoType.OIL)));
    }

    /**
     * Testing last condition, "The cargo's destination is the same as the ship's origin country".
     * Normal case not equal.
     */
    @Test
    public void canLoadTest10() {
        assertFalse("Cargo's destination must be the same as the ship's origin!",
                normalBulkCarrier2.canLoad(new BulkCargo(22, "Brazil", 100, BulkCargoType.COAL)));
    }

    /**
     * Testing min edge case.
     */
    @Test
    public void loadCargoTest1() {
        minBulkCarrier.loadCargo(minBulkCargo);
        assertEquals("BulkCarrier can load BulkCargo", minBulkCargo, minBulkCarrier.getCargo());
    }

    /**
     * Testing max edge case.
     */
    @Test
    public void loadCargoTest2() {
        maxBulkCarrier.loadCargo(maxBulkCargo);
        assertEquals("BulkCarrier can load BulkCargo", maxBulkCargo, maxBulkCarrier.getCargo());
    }

    /**
     * Testing normal case equal.
     */
    @Test
    public void loadCargoTest3() {
        normalBulkCarrier1.loadCargo(normalBulkCargo1);
        assertEquals("BulkCarrier can load BulkCargo", normalBulkCargo1, normalBulkCarrier1.getCargo());
    }

    /**
     * Testing normal case less than.
     */
    @Test
    public void loadCargoTest4() {
        normalBulkCarrier2.loadCargo(normalBulkCargo2);
        assertEquals("BulkCarrier can load BulkCargo", normalBulkCargo2, normalBulkCarrier2.getCargo());
    }

    /**
     * Testing if NoSuchCargoException is thrown when the ship has no cargo.
     *
     * @throws NoSuchCargoException if the ship has no cargo.
     */
    @Test(expected = NoSuchCargoException.class)
    public void unloadCargoTest1() throws NoSuchCargoException {
        normalBulkCarrier1.unloadCargo();
    }

    /**
     * Testing if the unloaded cargo is returned.
     *
     * @throws NoSuchCargoException if the ship has no cargo.
     */
    @Test
    public void unloadCargoTest2() throws NoSuchCargoException {
        normalBulkCarrier2.loadCargo(normalBulkCargo2);
        assertEquals("Cargo being unloaded should be returned",
                normalBulkCargo2, normalBulkCarrier2.unloadCargo());
    }

    /**
     * Testing if the ship's cargo is set to null.
     *
     * @throws NoSuchCargoException if the ship has no cargo.
     */
    @Test
    public void unloadCargoTest3() throws NoSuchCargoException {
        normalBulkCarrier2.loadCargo(normalBulkCargo2);
        normalBulkCarrier2.unloadCargo();
        assertNull("Ship's cargo should be set to null once unloaded.",
                normalBulkCarrier2.getCargo());
    }

    /**
     * Testing that NoSuchCargoException is thrown if cargo is unloaded twice.
     *
     * @throws NoSuchCargoException if the ship has no cargo.
     */
    @Test(expected = NoSuchCargoException.class)
    public void unloadCargoTest4() throws NoSuchCargoException {
        normalBulkCarrier2.loadCargo(normalBulkCargo2);
        normalBulkCarrier2.unloadCargo();
        normalBulkCarrier2.unloadCargo();
    }

    /**
     * Testing with no cargo.
     */
    @Test
    public void getCargoTest1() {
        assertNull("getCargo should return null when no cargo onboard!",
                normalBulkCarrier1.getCargo());
    }

    /**
     * Testing with max values.
     */
    @Test
    public void getCargoTest2() {
        maxBulkCarrier.loadCargo(maxBulkCargo);
        assertEquals("Didn't return current cargo.", maxBulkCargo, maxBulkCarrier.getCargo());
    }

    /**
     * Testing with min values.
     */
    @Test
    public void getCargoTest3() {
        minBulkCarrier.loadCargo(minBulkCargo);
        assertEquals("Didn't return current cargo.", minBulkCargo, minBulkCarrier.getCargo());
    }

    /**
     * Testing normal case.
     */
    @Test
    public void getCargoTest4() {
        normalBulkCarrier2.loadCargo(normalBulkCargo2);
        assertEquals("Didn't return current cargo.", normalBulkCargo2, normalBulkCarrier2.getCargo());
    }


    /**
     * Testing normal case with no cargo.
     */
    @Test
    public void toStringTest1() {
        assertEquals("Incorrect toString representation.",
                "BulkCarrier Voyager from AU [WHISKEY] carrying nothing", normalBulkCarrier1.toString());
    }

    /**
     * Testing min values
     */
    @Test
    public void toStringTest2() {
        minBulkCarrier.loadCargo(minBulkCargo);
        assertEquals("Incorrect toString representation.",
                "BulkCarrier  from  [HOTEL] carrying OIL", minBulkCarrier.toString());
    }

    /**
     * Testing with max values.
     */
    @Test
    public void toStringTest3() {
        maxBulkCarrier.loadCargo(maxBulkCargo);
        assertEquals("Incorrect toString representation.",
                "BulkCarrier This is a very very very very very very very very very long and dumb Bulk Carrier name. from The United Kingdom of Great Britain and Northern Ireland [BRAVO] carrying MINERALS",
                maxBulkCarrier.toString());
    }

    /**
     * Testing normal case with cargo.
     */
    @Test
    public void toStringTest4() {
        normalBulkCarrier2.loadCargo(normalBulkCargo2);
        assertEquals("Incorrect toString representation.",
                "BulkCarrier Joe's Sheepy Ship from New Zealand [NOVEMBER] carrying OTHER", normalBulkCarrier2.toString());
    }
}