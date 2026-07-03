package in.codekerdos.ems.service;

/**
 * Week 3 Exercise A — file pointer only.
 * Real implementation: {@link in.codekerdos.ems.exception.ResourceNotFoundException}
 */
@Deprecated
public class ResourceNotFoundException extends in.codekerdos.ems.exception.ResourceNotFoundException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
