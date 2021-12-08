package portsim.util;

import portsim.cargo.BulkCargoType;

import java.util.Objects;
import java.util.function.Function;

/**
 * Exception thrown when an encoded string is not correct according to the appropriate fromString()
 * method.
 * <br>
 * Also contains a handful of useful static methods used within the fromStrings.
 */
public class BadEncodingException extends Exception {
    /**
     * Constructs a new BadEncodingException with no detail message or cause.
     *
     * @see Exception#Exception()
     */
    public BadEncodingException() {
        super();
    }

    /**
     * Constructs a BadEncodingException that contains a helpful detail message explaining why
     * the exception occurred.
     *
     * @param message detail message
     * @see Exception#Exception(String)
     */
    public BadEncodingException(String message) {
        super(message);
    }

    /**
     * Constructs a BadEncodingException that stores the underlying cause of the exception.
     *
     * @param cause throwable that caused this exception
     * @see Exception#Exception(Throwable)
     */
    public BadEncodingException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a BadEncodingException that contains a helpful detail message explaining why the
     * exception occurred and the underlying cause of the exception.
     *
     * @param message detail message
     * @param cause   throwable that caused this exception
     * @see Exception#Exception(String, Throwable)
     */
    public BadEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Wraps a {@link Number} or {@link Enum} parsing method (such as
     * {@link Integer#parseInt(String)} or {@link BulkCargoType#valueOf(String)})
     * with a {@link BadEncodingException}
     *
     * @param encoding the encoding being parsed.
     * @param parser   the function being used to parse
     * @param <R>      the return type of the parsed value
     * @return the parsed value
     * @throws BadEncodingException if the encoding cannot be parsed with the specified function.
     * @require parser != null
     */
    public static <R> R parseEncoding(String encoding, Function<String, R> parser)
            throws BadEncodingException {
        try {
            return parser.apply(encoding);

        } catch (NumberFormatException e) {
            throw new BadEncodingException(encoding + " cannot be parsed as a number!", e);

        } catch (IllegalArgumentException e) {
            throw new BadEncodingException(encoding + " cannot be parsed as an Enum!", e);
        }
    }

    /**
     * Asserts that the provided object is not null with a {@link BadEncodingException}
     *
     * @param object the object being asserted
     * @throws BadEncodingException if the object == null
     * @ensure object != null
     */
    public static void assertNotNull(Object object) throws BadEncodingException {
        if (object == null) {
            throw new BadEncodingException("Object was null :(");
        }
    }

    /**
     * Asserts that two generic objects with the same type are equal with a
     * {@link BadEncodingException}
     *
     * @param expected the expected value
     * @param actual   the actual value
     * @param <T>      the type of the values
     * @throws BadEncodingException if the expected != actual.
     * @ensure expected == actual
     */
    public static <T> void assertEqual(T expected, T actual) throws BadEncodingException {
        if (!Objects.equals(expected, actual)) {
            throw new BadEncodingException(
                    String.format("Expected %s but got %s", expected, actual));
        }
    }

    /**
     * Asserts that a condition is true with a {@link BadEncodingException}
     *
     * @param message   the exception message
     * @param condition the condition being asserted
     * @throws BadEncodingException if the condition fails
     * @ensure condition == true
     */
    public static void assertTrue(String message, boolean condition) throws BadEncodingException {
        if (!condition) {
            throw new BadEncodingException(message);
        }
    }
}
