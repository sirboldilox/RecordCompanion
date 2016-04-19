package parker.matt.recordcompanion.models;

/**
 * Data model for ECG records
 *
 * @param id:                   Unique identifier
 * @param patient_id:           Patient this ECG record belongs to
 * @param data_id:              ECGData file this ECG record belongs to
 * @param sampling_freq:        Frequency the recordings was sampled at
 * @param timestamp:            Timestamp of when the ECG was taken
 */
public class ECG {

    public int id;
    public int patient_id;
    public int data_id;
    public float sampling_freq;
    public String timestamp;

    /**
     *  Construct an ECG record without a blank ID entry
     */
    public ECG(int patient_id, float sampling_freq, int data_id, String timestamp) {
        this.patient_id = patient_id;
        this.sampling_freq = sampling_freq;
        this.data_id = data_id;
        this.timestamp = timestamp;
    }

    /**
     *  Construct an ECG recrod with ID entry
     */
    public ECG(int id, int patient_id, float sampling_freq, int data_id, String timestamp) {
        this.id = id;
        this.patient_id = patient_id;
        this.sampling_freq = sampling_freq;
        this.data_id = data_id;
        this.timestamp = timestamp;
    }

    /**
     * Return this record as a string
     */
    public String toString() {
        return String.format("ECG(%d, %d, %f, %d, %s)",
                id, patient_id, sampling_freq, data_id, timestamp
        );
    }
}
