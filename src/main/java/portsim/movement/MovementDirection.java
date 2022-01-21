package portsim.movement;

/**
 * Represents the possible directions a ship or cargo can be moving.
 */
public enum MovementDirection {

    /**
     * Any cargo / ships that are coming to the port are considered inbound.
     */
    INBOUND,

    /**
     * Any cargo / ships that are moving away from the port are considered outbound.
     */
    OUTBOUND
}
