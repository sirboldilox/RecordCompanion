package parker.matt.recordcompanion.models;

/**
 * Data model for Biometrics
 *
 * @param id:                   Unique identifier
 * @param value:                Raw value
 * @param biometric_type_id:    Type of biometric this entry relates to
 * @param patient_id:           Patient this biometric belongs to
 * @param timestamp:            Timestamp of when the biometric was taken
 */
public class Biometric {

    public int id;
    public String value;
    public int biometric_type_id;
    public int patient_id;
    public String timestamp;

    /**
     *  Construct a biometric record without a blank ID entry
     */
    public Biometric(String value, int biometric_type_id, int patient_id, String timestamp) {
        this.value = value;
        this.biometric_type_id = biometric_type_id;
        this.patient_id = patient_id;
        this.timestamp = timestamp;
    }

    /**
     *  Construct a biometric recrod with ID entry
     */
    public Biometric(int id, String value, int biometric_type_id, int patient_id, String timestamp) {
        this.id = id;
        this.value = value;
        this.biometric_type_id = biometric_type_id;
        this.patient_id = patient_id;
        this.timestamp = timestamp;
    }

    /**
     * Return this record as a string
     */
    public String toString() {
        return String.format("Biometric(%d, %s, %d, %d, %s)",
                id, value, biometric_type_id, patient_id, timestamp
        );
    }
}
