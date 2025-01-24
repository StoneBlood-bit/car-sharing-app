package mate.academy.service.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import mate.academy.model.Rental;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {
    @Override
    public Session createSession(
            Rental rental, String type, BigDecimal amount,
            UriComponentsBuilder uriComponentsBuilder
    ) throws StripeException {
        String successUrl = uriComponentsBuilder.path("/payments/success").toUriString();
        String cancelUrl = uriComponentsBuilder.path("/payment/cancel").toUriString();

        return Session.create(
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(successUrl)
                        .setCancelUrl(cancelUrl)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("usd")
                                                        .setUnitAmount(amount
                                                                .multiply(BigDecimal.valueOf(100))
                                                                .longValue())
                                                        .setProductData(
                                                                SessionCreateParams.LineItem
                                                                        .PriceData.ProductData
                                                                        .builder()
                                                                        .setName(type
                                                                                .equalsIgnoreCase(
                                                                                        "FINE"
                                                                                )
                                                                                ? "Fine for delay"
                                                                                : "Rent a car")
                                                                        .build())
                                                        .build())
                                        .build())
                        .build());
    }
}
