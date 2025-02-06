package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.car.CarDto;
import mate.academy.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarDto toDto(Car car);

    Car toModel(CarDto carDto);

    void updateCarFromDto(CarDto carDto, @MappingTarget Car car);
}
