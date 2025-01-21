package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.rental.RentalDetailDto;
import mate.academy.dto.rental.RentalRequestDto;
import mate.academy.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    @Mapping(target = "carModel", source = "rental.car.model")
    @Mapping(target = "userEmail", source = "rental.user.email")
    RentalDetailDto toDto(Rental rental);

    Rental toModel(RentalRequestDto requestDto);
}
