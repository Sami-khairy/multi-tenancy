package ma.khairy.t_enant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// En annotant avec @ResponseStatus, Spring saura renvoyer un 404 si cette exception n'est pas gérée ailleurs.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(String message) {
        super(message);
    }
}