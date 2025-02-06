package mate.academy.service.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import mate.academy.model.Rental;
import org.springframework.web.util.UriComponentsBuilder;

public interface StripeService {
    Session createSession(Rental rental, String type, BigDecimal amount,
                          UriComponentsBuilder uriComponentsBuilder) throws StripeException;
}
