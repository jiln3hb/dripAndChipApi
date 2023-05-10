package wonderland.DripAndChipApi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN) //403
public class ForbiddenException extends RuntimeException{
}
