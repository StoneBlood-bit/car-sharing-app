package mate.academy.service.user;

import mate.academy.dto.user.UpdateUserProfileDto;
import mate.academy.dto.user.UserRegistrationRequestDto;
import mate.academy.dto.user.UserResponseDto;
import mate.academy.dto.user.UserRoleUpdateDto;
import mate.academy.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserResponseDto getUserById(Long id);

    UserResponseDto updateRole(UserRoleUpdateDto updateDto, Long id);

    void updateUserProfile(Long userId, UpdateUserProfileDto updateUserProfileDto);
}
