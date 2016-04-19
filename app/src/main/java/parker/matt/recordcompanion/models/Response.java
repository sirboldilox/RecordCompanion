package parker.matt.recordcompanion.models;

/**
 * Represents the JSON structure returned by all API calls
 *
 * {
 *     "response": data (T)
 * }
 */
public class Response<T> {
    public T response;
}
