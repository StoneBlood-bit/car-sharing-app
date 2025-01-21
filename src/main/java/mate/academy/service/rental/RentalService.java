package mate.academy.service.rental;

import java.util.List;
import mate.academy.dto.rental.RentalDetailDto;
import mate.academy.dto.rental.RentalFilterRequestDto;
import mate.academy.dto.rental.RentalRequestDto;
import mate.academy.model.User;

public interface RentalService {
    RentalDetailDto createRental(RentalRequestDto requestDto, String email);

    List<RentalDetailDto> getRentals(RentalFilterRequestDto filter, User currentUser);

    void completeRental(Long rentalId);
}
