package mate.academy.service.car;

import lombok.RequiredArgsConstructor;
import mate.academy.dto.car.CarDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.CarMapper;
import mate.academy.model.Car;
import mate.academy.repository.CarRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    @Transactional
    public CarDto save(CarDto carDto) {
        Car car = carMapper.toModel(carDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public Page<CarDto> findAll(Pageable pageable) {
        Page<Car> cars = carRepository.findAll(pageable);
        return cars.map(carMapper::toDto);
    }

    @Override
    public CarDto getById(Long id) {
        
        Car car = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't get car by id: " + id)
        );
        return carMapper.toDto(car);
    }

    @Override
    @Transactional
    public CarDto update(CarDto carDto, Long id) {
        Car existingCar = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't get car by id: " + id)
        );
        carMapper.updateCarFromDto(carDto, existingCar);
        return carMapper.toDto(carRepository.save(existingCar));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null!");
        }
        carRepository.deleteById(id);
    }
}
