package piecework;

/**
 * Marker interface for REST resources that will be exposed via API (instead of behind Single-Sign-On)
 * @author James Renfro
 */
public interface ApiResource {

    public String getVersion();

}
