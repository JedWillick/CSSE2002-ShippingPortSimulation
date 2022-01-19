package portsim.util;

/**
 * Exception thrown when a piece of cargo is requested by a given ID but
 * no cargo with that ID exists.
 * <p>
 * See {@link portsim.cargo.Cargo#getCargoById(int)}.
 * <p>
 * Also thrown when a ship that is already unloaded is attempted to be unloaded.
 * See {@link portsim.ship.BulkCarrier#unloadCargo()} or
 * {@link portsim.ship.ContainerShip#unloadCargo()}
 */
public class NoSuchCargoException extends Exception {
    /**
     * Constructs a new NoSuchCargoException with no detail message or cause.
     *
     * @see Exception#Exception()
     */
    public NoSuchCargoException() {
        super();
    }

    /**
     * Constructs a NoSuchCargoException that contains a helpful detail message explaining why
     * the exception occurred.
     *
     * @param message detail message
     * @see Exception#Exception(String)
     */
    public NoSuchCargoException(String message) {
        super(message);
    }

    /**
     * Constructs a NoSuchCargoException that stores the underlying cause of the exception.
     *
     * @param cause throwable that caused this exception
     * @see Exception#Exception(Throwable)
     */
    public NoSuchCargoException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a NoSuchCargoException that contains a helpful detail message explaining why the
     * exception occurred and the underlying cause of the exception.
     *
     * @param message detail message
     * @param cause   throwable that caused this exception
     * @see Exception#Exception(String, Throwable)
     */
    public NoSuchCargoException(String message, Throwable cause) {
        super(message, cause);
    }
}
