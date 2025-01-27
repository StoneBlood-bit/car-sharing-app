package mate.academy.service.user;

import lombok.RequiredArgsConstructor;
import mate.academy.dto.user.UpdateUserProfileDto;
import mate.academy.dto.user.UserRegistrationRequestDto;
import mate.academy.dto.user.UserResponseDto;
import mate.academy.dto.user.UserRoleUpdateDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.exception.RegistrationException;
import mate.academy.mapper.UserMapper;
import mate.academy.model.User;
import mate.academy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException(
                    "User with email " + requestDto.getEmail()
                            + " already exists."
            );
        }
        User user = userMapper.toEntity(requestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.CUSTOMER);

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateRole(UserRoleUpdateDto updateDto, Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id: " + id)
        );
        try {
            User.Role newRole = User.Role.valueOf(updateDto.getRole().toUpperCase());
            user.setRole(newRole);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role specified.");
        }
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void updateUserProfile(Long userId, UpdateUserProfileDto updateUserProfileDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id: " + userId)
        );

        userMapper.updateUserProfile(updateUserProfileDto, user);
        userRepository.save(user);
    }
}
