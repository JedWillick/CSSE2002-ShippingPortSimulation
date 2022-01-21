package portsim.port;

import org.junit.Before;
import org.junit.Test;
import portsim.ship.BulkCarrier;
import portsim.ship.ContainerShip;
import portsim.ship.NauticalFlag;
import portsim.ship.Ship;
import portsim.util.BadEncodingException;
import portsim.util.Encodable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ShipQueueTest {

    private ShipQueue emptyShipQueue;
    private ShipQueue emptyShipQueueCopy;

    private ShipQueue populatedShipQueue;
    private ShipQueue populatedShipQueueCopy;

    private ShipQueue singleShipQueue;
    private ShipQueue singleShipQueueCopy;

    private ContainerShip containerShipDangerous;
    private BulkCarrier bulkCarrierDangerous;

    private ContainerShip containerShipMedical;
    private BulkCarrier bulkCarrierMedical;

    private ContainerShip containerShipReady;
    private BulkCarrier bulkCarrierReady;

    private ContainerShip containerShipDefault1;
    private ContainerShip containerShipDefault2;

    private BulkCarrier bulkCarrierDefault1;
    private BulkCarrier bulkCarrierDefault2;

    private List<Ship> ships;

    @Before
    public void setUp() throws Exception {
        Ship.resetShipRegistry();
        ships = new ArrayList<>();

        // Dangerous Ships - Bravo Flag
        containerShipDangerous = new ContainerShip(1000000, "containerShipDangerous", "AU", NauticalFlag.BRAVO, 100);
        bulkCarrierDangerous = new BulkCarrier(1000001, "bulkCarrierDangerous", "AU", NauticalFlag.BRAVO, 100);

        // Medical Assistance Ships - Whisky Flag
        containerShipMedical = new ContainerShip(1000002, "containerShipMedical", "AU", NauticalFlag.WHISKEY, 100);
        bulkCarrierMedical = new BulkCarrier(1000003, "bulkCarrierMedical", "AU", NauticalFlag.WHISKEY, 100);

        // Ready to Dock Ships - Hotel Flag
        containerShipReady = new ContainerShip(1000004, "containerShipReady", "AU", NauticalFlag.HOTEL, 100);
        bulkCarrierReady = new BulkCarrier(1000005, "bulkCarrierReady", "AU", NauticalFlag.HOTEL, 100);

        // Default ContainerShips - November Flag
        containerShipDefault1 = new ContainerShip(1000006, "containerShipDefault1", "AU", NauticalFlag.NOVEMBER, 100);
        containerShipDefault2 = new ContainerShip(1000007, "containerShipDefault2", "AU", NauticalFlag.NOVEMBER, 100);

        // Default BulkCarriers - November Flag
        bulkCarrierDefault1 = new BulkCarrier(1000008, "bulkCarrierDefault1", "AU", NauticalFlag.NOVEMBER, 100);
        bulkCarrierDefault2 = new BulkCarrier(1000009, "bulkCarrierDefault2", "AU", NauticalFlag.NOVEMBER, 100);

        // Adding the ships to the list of ships (Most prioritised being added last)
        ships.add(bulkCarrierDefault2);
        ships.add(bulkCarrierDefault1);
        ships.add(containerShipDefault2);
        ships.add(containerShipDefault1);
        ships.add(bulkCarrierReady);
        ships.add(containerShipReady);
        ships.add(bulkCarrierMedical);
        ships.add(containerShipMedical);
        ships.add(bulkCarrierDangerous);
        ships.add(containerShipDangerous);

        emptyShipQueue = new ShipQueue();
        emptyShipQueueCopy = new ShipQueue();

        populatedShipQueue = new ShipQueue();
        populatedShipQueueCopy = new ShipQueue();

        singleShipQueue = new ShipQueue();
        singleShipQueueCopy = new ShipQueue();

        try {
            singleShipQueue.add(containerShipDefault1);
            singleShipQueueCopy.add(containerShipDefault1);
            for (Ship ship : ships) {
                populatedShipQueue.add(ship);
                populatedShipQueueCopy.add(ship);
            }
        } catch (Exception ignore) {

        }
    }

    @Test
    public void ShipQueueTestImplementsEncodable() {
        ShipQueue shipQueue = new ShipQueue();
        assertTrue("ShipQueue should implement Encodable!", shipQueue instanceof Encodable);
    }

    @Test
    public void constructorTestShipQueueInitialised() {
        ShipQueue shipQueue = new ShipQueue();
        try {
            assertTrue("shipQueue should be initialised as empty!", shipQueue.getShipQueue().isEmpty());
        } catch (NullPointerException nullPointerException) {
            fail("shipQueue was not initialised!");
        }
    }

    @Test
    public void pollTestEmpty() {
        assertNull("Should return null when empty", emptyShipQueue.poll());
    }

    @Test
    public void pollTestRemovesPolled() {
        List<Ship> expected = new ArrayList<>(populatedShipQueue.getShipQueue());
        expected.remove(bulkCarrierDangerous);
        populatedShipQueue.poll();

        assertEquals("Polled ship was not removed!", expected, populatedShipQueue.getShipQueue());
    }

    @Test
    public void pollTestFullShipQueue() {
        assertEquals("Incorrect ship was polled!", bulkCarrierDangerous, populatedShipQueue.poll());
        assertEquals("Incorrect ship was polled!", containerShipDangerous, populatedShipQueue.poll());
        assertEquals("Incorrect ship was polled!", bulkCarrierMedical, populatedShipQueue.poll());
        assertEquals("Incorrect ship was polled!", containerShipMedical, populatedShipQueue.poll());
        assertEquals("Incorrect ship was polled!", bulkCarrierReady, populatedShipQueue.poll());
        assertEquals("Incorrect ship was polled!", containerShipReady, populatedShipQueue.poll());
        assertEquals("Incorrect ship was polled!", containerShipDefault2, populatedShipQueue.poll());
        assertEquals("Incorrect ship was polled!", containerShipDefault1, populatedShipQueue.poll());
        assertEquals("Incorrect ship was polled!", bulkCarrierDefault2, populatedShipQueue.poll());
        assertEquals("Incorrect ship was polled!", bulkCarrierDefault1, populatedShipQueue.poll());
        assertNull("Should return null when empty!", populatedShipQueue.poll());
        assertNull("Should still be empty and returning null!", populatedShipQueue.poll());
    }

    @Test
    public void pollTestSingleShip() {
        assertEquals("Incorrect ship was polled!", containerShipDefault1, singleShipQueue.poll());
    }

    @Test
    public void pollTestMultipleUnprioritised() {
        emptyShipQueue.add(bulkCarrierDefault1);
        emptyShipQueue.add(bulkCarrierDefault2);
        assertEquals("Incorrect ship was polled!", bulkCarrierDefault1, emptyShipQueue.poll());
    }

    @Test
    public void peekTestEmpty() {
        assertNull("Should return null when empty", emptyShipQueue.peek());
    }

    @Test
    public void peekTestSingleShip() {
        assertEquals("Incorrect ship was peeked!", containerShipDefault1, singleShipQueue.peek());
    }

    @Test
    public void peekTestDoesntModifyQueue() {
        List<Ship> before = populatedShipQueue.getShipQueue();
        populatedShipQueue.peek();
        // Checking queue is not changed.
        assertEquals("Queue should not change!", before, populatedShipQueue.getShipQueue());
    }

    @Test
    public void peekTestDangerous() {
        List<Ship> before = populatedShipQueue.getShipQueue();

        // populatedShipQueue has all conditions of ships.
        // Two Dangerous ships, bulkCarrierDangerous was added first.
        assertEquals("Ships carrying dangerous cargo are the most prioritised in this queue.",
                bulkCarrierDangerous, populatedShipQueue.peek());

        // Checking queue is not changed.
        assertEquals("Queue should not change!", before, populatedShipQueue.getShipQueue());
    }

    @Test
    public void peekTestMedical() {
        // Creating a ShipQueue where medical ships should return first.
        ShipQueue medical = new ShipQueue();
        for (Ship ship : ships) {
            if (ship.getFlag() != NauticalFlag.BRAVO) {
                medical.add(ship);
            }
        }
        List<Ship> before = medical.getShipQueue();

        // Two Medical ships, bulkCarrierMedical was added first.
        assertEquals("Ships requiring medical attention are the most prioritised in this queue.",
                bulkCarrierMedical, medical.peek());

        // Checking queue is not changed.
        assertEquals("Queue should not change!", before, medical.getShipQueue());
    }

    @Test
    public void peekTestReady() {
        // Creating a ShipQueue where ships ready to dock should return first.
        ShipQueue ready = new ShipQueue();
        for (Ship ship : ships) {
            if (ship.getFlag() != NauticalFlag.BRAVO && ship.getFlag() != NauticalFlag.WHISKEY) {
                ready.add(ship);
            }
        }
        List<Ship> before = ready.getShipQueue();

        // Two Ready ships, bulkCarrierReady was added first.
        assertEquals("Ships ready to dock are the most prioritised in this queue",
                bulkCarrierReady, ready.peek());

        // Checking queue is not changed.
        assertEquals("Queue should not change!", before, ready.getShipQueue());
    }

    @Test
    public void peekTestContainerShipDefault() {
        // Creating a ShipQueue where default ContainerShips should return first.
        ShipQueue containerShipDefault = new ShipQueue();
        for (Ship ship : ships) {
            if (ship.getFlag() == NauticalFlag.NOVEMBER) {
                containerShipDefault.add(ship);
            }
        }
        List<Ship> before = containerShipDefault.getShipQueue();

        // Two Default ContainerShips, containerShipDefault2 was added first.
        assertEquals("Default ContainerShips are the highest priority in this queue.",
                containerShipDefault2, containerShipDefault.peek());

        // Checking queue is not changed.
        assertEquals("Queue should not change!", before, containerShipDefault.getShipQueue());
    }

    @Test
    public void peekTestBulkCarrierDefault() {
        // Creating a ShipQueue where default BulkCarriers should return first.
        ShipQueue bulkCarrierDefault = new ShipQueue();
        for (Ship ship : ships) {
            if (ship instanceof BulkCarrier && ship.getFlag() == NauticalFlag.NOVEMBER) {
                bulkCarrierDefault.add(ship);
            }
        }
        List<Ship> before = bulkCarrierDefault.getShipQueue();

        // Two Default BulkCarrier's, bulkCarrierDefault2 was added first.
        assertEquals("No priority ships, first should should be returned",
                bulkCarrierDefault2, bulkCarrierDefault.peek());

        // Checking queue is not changed.
        assertEquals("Queue should not change!", before, bulkCarrierDefault.getShipQueue());
    }

    @Test
    public void addTestEmpty() {
        List<Ship> expected = new ArrayList<>();
        expected.add(containerShipDefault1);

        emptyShipQueue.add(containerShipDefault1);
        assertEquals("Ship was not added to the queue!", expected, emptyShipQueue.getShipQueue());

    }

    @Test
    public void addTestPopulated() {
        List<Ship> expected = new ArrayList<>(ships);
        expected.add(containerShipDefault1);

        populatedShipQueue.add(containerShipDefault1);
        assertEquals("Ship was not added to the queue!", expected, populatedShipQueue.getShipQueue());
    }

    @Test
    public void addTestMultipleAdds() {
        List<Ship> expected = new ArrayList<>();
        expected.add(containerShipDefault1); // in single already
        expected.add(bulkCarrierDangerous);
        expected.add(containerShipMedical);
        expected.add(bulkCarrierReady);

        singleShipQueue.add(bulkCarrierDangerous);
        singleShipQueue.add(containerShipMedical);
        singleShipQueue.add(bulkCarrierReady);
        assertEquals("Ships were not added to the queue properly!", expected, singleShipQueue.getShipQueue());
    }

    @Test
    public void getShipQueueTestEmpty() {
        List<Ship> expected = new ArrayList<>();
        assertEquals("Incorrect value returned!", expected, emptyShipQueue.getShipQueue());
    }

    @Test
    public void getShipQueueTestPopulated() {
        List<Ship> expected = new ArrayList<>(ships);

        assertEquals("Incorrect value returned!", expected, populatedShipQueue.getShipQueue());
    }

    @Test
    public void getShipQueueTestSingle() {
        List<Ship> expected = new ArrayList<>();
        expected.add(containerShipDefault1); // in single already

        assertEquals("Incorrect shipQueue returned!", expected, singleShipQueue.getShipQueue());
    }

    @Test
    public void getShipQueueTestReverseOrder() {
        List<Ship> expected = new ArrayList<>();
        for (int i = ships.size() - 1; i >= 0; i--) {
            expected.add(ships.get(i));
        }

        assertNotEquals("Incorrect value returned!", expected, populatedShipQueue.getShipQueue());
    }

    @Test
    public void getShipQueueTestReturnsNewReference() {
        assertNotSame("Each call should return a new reference in memory!",
                populatedShipQueue.getShipQueue(), populatedShipQueue.getShipQueue());
    }

    @Test
    public void getShipQueueTestRemovingReturned() {
        int sizeBefore = populatedShipQueue.getShipQueue().size();
        List<Ship> returned = populatedShipQueue.getShipQueue();
        returned.clear();

        assertEquals("Removing elements from returned list should not change original list!",
                sizeBefore, populatedShipQueue.getShipQueue().size());
        assertNotEquals("Removing elements from returned list should not change original list!",
                returned, populatedShipQueue.getShipQueue());
    }

    @Test
    public void getShipQueueTestAddingReturned() {
        int sizeBefore = populatedShipQueue.getShipQueue().size();
        List<Ship> returned = populatedShipQueue.getShipQueue();
        returned.add(containerShipDefault1);

        assertEquals("Adding elements from returned list should not change original list!",
                sizeBefore, populatedShipQueue.getShipQueue().size());
        assertNotEquals("Adding elements from returned list should not change original list!",
                returned, populatedShipQueue.getShipQueue());
    }


    @Test
    public void equalsTestBothEmpty() {
        assertTrue("Two empty queues are equal!", emptyShipQueue.equals(emptyShipQueueCopy));
    }

    @Test
    public void equalsTestPopulatedCopy() {
        assertTrue("The queues should be equal!", populatedShipQueue.equals(populatedShipQueueCopy));
    }

    @Test
    public void equalsTestSingleCopy() {
        assertTrue("The queues should be equal!", singleShipQueue.equals(singleShipQueueCopy));
    }

    @Test
    public void equalsTestSameLengthDifferentShips() {
        emptyShipQueue.add(containerShipDefault2); // singleShipQueue has containerShipDefault1
        assertFalse("The queues have different ships!", singleShipQueue.equals(emptyShipQueue));
    }

    @Test
    public void equalsTestClearlyNot() {
        assertFalse("Clearly not equal!", populatedShipQueue.equals(emptyShipQueue));
    }

    @Test
    public void equalsTestSameShipsReverseOrder() {
        ShipQueue populatedShipQueueReversed = new ShipQueue();
        for (int i = ships.size() - 1; i >= 0; i--) {
            populatedShipQueueReversed.add(ships.get(i));
        }
        assertFalse("Ships must be in the same order!", populatedShipQueue.equals(populatedShipQueueReversed));
    }

    @Test
    public void equalsTestWrongOrder() {
        ShipQueue wrong = new ShipQueue();
        wrong.add(containerShipDefault2);
        wrong.add(containerShipDefault1);
        wrong.add(bulkCarrierDangerous);

        emptyShipQueue.add(containerShipDefault1);
        emptyShipQueue.add(bulkCarrierDangerous);
        emptyShipQueue.add(containerShipDefault2);
        assertFalse("Ships must be in the same order!", emptyShipQueue.equals(wrong));
    }

    @Test
    public void equalsTestPopulatedNull() {
        try {
            assertFalse("Clearly not null!", populatedShipQueue.equals(null));
        } catch (NullPointerException nullPointerException) {
            fail("Cannot equate a ShipQueue and null!");
        }
    }

    @Test
    public void equalsTestEmptyNull() {
        try {
            assertFalse("Clearly not null!", emptyShipQueue.equals(null));
        } catch (NullPointerException nullPointerException) {
            fail("Cannot equate a ShipQueue and null!");
        }
    }

    @Test
    public void equalsTestNotAShipQueue() {
        try {
            assertFalse("Cant equate something that isnt a ShipQueue!", emptyShipQueue.equals(new ArrayList<>()));
        } catch (ClassCastException classCastException) {
            fail("Other object must be a ShipQueue!");
        }
    }

    @Test
    public void hashCodeTestEmptyEquals() {
        assertEquals("Two ship queue's that are equal according to the equals(Object) method should have the same hash code.",
                emptyShipQueue.hashCode(), emptyShipQueueCopy.hashCode());
    }

    @Test
    public void hashCodeTestPopulatedEquals() {
        assertEquals("Two ship queue's that are equal according to the equals(Object) method should have the same hash code.",
                populatedShipQueue.hashCode(), populatedShipQueueCopy.hashCode());
    }

    @Test
    public void hashCodeTestSingleEquals() {
        assertEquals("Two ship queue's that are equal according to the equals(Object) method should have the same hash code.",
                singleShipQueue.hashCode(), singleShipQueueCopy.hashCode());
    }

    @Test
    public void encodeTestEmpty() {
        assertEquals("Incorrect encoding.", "ShipQueue:0:", emptyShipQueue.encode());
    }

    @Test
    public void encodeTestPopulated() {
        assertEquals("Incorrect encoding.",
                "ShipQueue:10:1000009,1000008,1000007,1000006,1000005,1000004,1000003,1000002,1000001,1000000", populatedShipQueue.encode());
    }

    @Test
    public void encodeTestSingle() {
        assertEquals("Incorrect encoding.",
                "ShipQueue:1:1000006", singleShipQueue.encode());
    }

    @Test
    public void encodeTestEquals() {
        assertEquals("If two ShipQueue's are equal then there encoding should be the same!",
                populatedShipQueue.encode(), populatedShipQueueCopy.encode());
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestNullEncoding() throws BadEncodingException {
        ShipQueue.fromString(null);
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestEmptyString() throws BadEncodingException {
        ShipQueue.fromString("");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestJustShipQueue() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestTrailingColon() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:1:1000009:");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestLeadingColon() throws BadEncodingException {
        ShipQueue.fromString(":ShipQueue:1:1000009");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestTooFewColons() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:0");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestNotShipQueue() throws BadEncodingException {
        ShipQueue.fromString("ShipQueueI:1:1000009");
    }
    @Test(expected = BadEncodingException.class)
    public void fromStringTestDoesntStartWithShipQueue() throws BadEncodingException {
        ShipQueue.fromString("ShippingQueue:1:1000009");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestNumShipsNotValid() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:I:1000009");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestNumShipsNotInt() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:6.9:1000009");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestNumShipsZeroButShipGiven() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:0:1000009");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestNumShipsZeroButShipsGiven() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:0:1000009,1000007,1000001,1000003");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestNumShipsOneButNoShip() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:1:");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestNumShipsZeroButCommaGiven() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:0:,");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestNumShipsZeroButInvalidGiven() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:0:ABC");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestTooManyShips() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:3:1000009,1000007,1000001,1000003");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestTooFewShips() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:3:1000009,1000007");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestTrailingComma() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:3:1000009,1000007,1000001,");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestLeadingComma() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:3:,1000009,1000007,1000001");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestFirstImoNumberNotNumber() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:1:ABC");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestFirstImoNumberNotLong() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:1:11.11");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestFirstImoNumberNotExists() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:1:9000009");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestFirstImoNumberInvalid() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:1:420");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestAnyImoNumberNotNumber() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:4:1000009,1000007,ABC,1000003");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestAnyImoNumberNotLong() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:4:1000009,1000007,11.11,1000003");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestAnyImoNumberNotExists() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:4:1000009,1000007,9000009,1000003");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestAnyImoNumberInvalid() throws BadEncodingException {
        ShipQueue.fromString("ShipQueue:4:1000009,1000007,420,1000003");
    }

    @Test(expected = BadEncodingException.class)
    public void fromStringTestMultipleLines() throws BadEncodingException {
        ShipQueue.fromString(
                "ShipQueue:4:1000009,1000007,1000002,1000003"
                + System.lineSeparator()
                + "ShipQueue:10:1000009,1000008,1000007,1000006,1000005,1000004,1000003,1000002,1000001,1000000"
        );
    }

    @Test
    public void fromStringTestValidEmpty() {
        try {
            assertTrue("fromString did not construct the expected ShipQueue",
                    emptyShipQueue.equals(ShipQueue.fromString("ShipQueue:0:")));
        } catch (BadEncodingException badEncodingException) {
            fail("fromString threw a BadEncodingException when trying to construct a valid encoding!");
        }
    }

    @Test
    public void fromStringTestValidPopulated() {
        try {
            assertTrue("fromString did not construct the expected ShipQueue",
                    populatedShipQueue.equals(ShipQueue.fromString("ShipQueue:10:1000009,1000008,1000007,1000006,1000005,1000004,1000003,1000002,1000001,1000000")));
        } catch (BadEncodingException badEncodingException) {
            fail("fromString threw a BadEncodingException when trying to construct a valid encoding!");
        }
    }

    @Test
    public void fromStringTestValidSingle() {
        try {
            assertTrue("fromString did not construct the expected ShipQueue",
                    singleShipQueue.equals(ShipQueue.fromString("ShipQueue:1:1000006")));
        } catch (BadEncodingException badEncodingException) {
            fail("fromString threw a BadEncodingException when trying to construct a valid encoding!");
        }
    }
}