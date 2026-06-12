package in.codekerdos.demo.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope("prototype")
public class RequestLogger {

    private final UUID id = UUID.randomUUID();

    public UUID getId() {
        return id;
    }
}
