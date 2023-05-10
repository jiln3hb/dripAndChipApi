package wonderland.DripAndChipApi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED) //401
public class UnauthorizedException extends RuntimeException{
}
