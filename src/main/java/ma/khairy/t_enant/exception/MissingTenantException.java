package ma.khairy.t_enant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MissingTenantException extends RuntimeException {
    public MissingTenantException(String message) {
        super(message);
    }
}