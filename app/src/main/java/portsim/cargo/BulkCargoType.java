package portsim.cargo;

/**
 * Represents the possible types that Bulk Cargo can be.
 */
public enum BulkCargoType {
    /**
     * A combustible black or brownish-black used as a fuel source.
     */
    COAL,

    /**
     * A small hard seed.
     */
    GRAIN,

    /**
     * A solid compound, usually ores such as iron ore.
     */
    MINERALS,

    /**
     * A viscous liquid derived from petroleum, especially for use as a fuel or lubricant
     */
    OIL,

    /**
     * Another form of bulk cargo that does not fit into other categories.
     */
    OTHER
}
