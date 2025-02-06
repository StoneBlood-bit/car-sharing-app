package mate.academy.service.car;

import mate.academy.dto.car.CarDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {

    CarDto save(CarDto carDto);

    Page<CarDto> findAll(Pageable pageable);

    CarDto getById(Long id);

    CarDto update(CarDto carDto, Long id);

    void deleteById(Long id);
}
