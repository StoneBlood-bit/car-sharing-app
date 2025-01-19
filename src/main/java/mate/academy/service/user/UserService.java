package mate.academy.service.user;

import mate.academy.dto.user.UserRegistrationRequestDto;
import mate.academy.dto.user.UserResponseDto;
import mate.academy.dto.user.UserRoleUpdateDto;
import mate.academy.exception.RegistrationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserResponseDto getUserById(Long id);

    Page<UserResponseDto> findAll(Pageable pageable);

    UserResponseDto update(UserRoleUpdateDto updateDto, Long id);

    void deleteUserById(Long id);
}
