package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.payment.PaymentDtoOverview;
import mate.academy.dto.payment.PaymentRequestDto;
import mate.academy.dto.payment.PaymentResponseDto;
import mate.academy.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    PaymentResponseDto toDto(Payment payment);

    Payment toModel(PaymentRequestDto requestDto);

    @Mapping(target = "rentalId", source = "rental.id")
    PaymentDtoOverview toDtoOverview(Payment payment);
}
