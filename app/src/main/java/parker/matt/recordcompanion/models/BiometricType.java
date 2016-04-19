package parker.matt.recordcompanion.models;

/**
 * Data model for Biometric Types.
 *
 * Biometric types can be considered to be a static set defined in the database.
 * @see Class: database.DatabaseHelper
 *
 */
public class BiometricType {

    public int id;
    public String name;
    public String units;

    public BiometricType(int id, String name, String units) {
        this.id = id;
        this.name = name;
        this.units = units;
    }

    /**
     * Return this record as a string
     */
    public String toString() {
        return String.format("BiometricType(%d, %s, %s)",
                id, name, units
        );
    }
}
