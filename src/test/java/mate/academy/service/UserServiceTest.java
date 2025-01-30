package mate.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import mate.academy.dto.user.UpdateUserProfileDto;
import mate.academy.dto.user.UserRegistrationRequestDto;
import mate.academy.dto.user.UserResponseDto;
import mate.academy.dto.user.UserRoleUpdateDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.exception.RegistrationException;
import mate.academy.mapper.UserMapper;
import mate.academy.model.User;
import mate.academy.repository.UserRepository;
import mate.academy.service.user.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Register user with valid data")
    void register_ValidData_ShouldReturnUserResponseDto() throws RegistrationException {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("bob@gmail.com");
        requestDto.setPassword("password");
        requestDto.setRepeatPassword("password");
        requestDto.setFirstName("Bob");
        requestDto.setLastName("Snow");

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(requestDto.getPassword());
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setEmail(user.getEmail());
        responseDto.setFirstName(user.getFirstName());
        responseDto.setLastName(user.getLastName());

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(userMapper.toEntity(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(responseDto);

        UserResponseDto actualResponse = userService.register(requestDto);

        assertThat(actualResponse).isEqualTo(responseDto);

        verify(userRepository, times(1)).existsByEmail(requestDto.getEmail());
        verify(userMapper, times(1)).toEntity(requestDto);
        verify(passwordEncoder, times(1)).encode(requestDto.getPassword());
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toUserResponse(user);
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Register user when email already exists")
    void register_EmailAlreadyExists_ShouldThrowException() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("bob@gmail.com");
        requestDto.setPassword("password");
        requestDto.setRepeatPassword("password");
        requestDto.setFirstName("Bob");
        requestDto.setLastName("Snow");

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(requestDto))
                .isInstanceOf(RegistrationException.class)
                .hasMessageContaining("User with email " + requestDto.getEmail()
                        + " already exists.");

        verify(userRepository, times(1)).existsByEmail(requestDto.getEmail());
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Get user by valid id")
    void getUserById_ValidId_ShouldReturnUserResponseDto() {
        Long validId = 1L;

        User user = new User();
        user.setId(validId);
        user.setEmail("bob@gmail.com");
        user.setFirstName("Bob");
        user.setLastName("Snow");

        UserResponseDto responseDto = new UserResponseDto();

        responseDto.setId(user.getId());
        responseDto.setEmail(user.getEmail());
        responseDto.setFirstName(user.getFirstName());
        responseDto.setLastName(user.getLastName());

        when(userRepository.findById(validId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(responseDto);

        UserResponseDto actualResponseDto = userService.getUserById(validId);

        assertThat(actualResponseDto).isEqualTo(responseDto);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Get user by invalid id")
    void getUserById_InvalidId_ShouldThrowException() {
        Long invalidId = 99L;

        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserById(invalidId)
        );

        assertThat(exception.getMessage()).isEqualTo("User not found.");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Update user's role when user not found")
    void updateRole_NotFoundUser_ShouldThrowException() {
        Long userId = 1L;

        UserRoleUpdateDto updateDto = new UserRoleUpdateDto();

        updateDto.setRole("CUSTOMER");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserById(userId)
        );

        assertThat(exception.getMessage()).isEqualTo("User not found.");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Update user's role, when role is invalid")
    void updateRole_InvalidRole_ShouldThrowException() {
        Long userId = 1L;

        UserRoleUpdateDto updateDto = new UserRoleUpdateDto();
        updateDto.setRole("INVALID_ROLE");

        User user = new User();
        user.setId(userId);
        user.setRole(User.Role.CUSTOMER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateRole(updateDto, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid role specified.");

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Update user's role when role is valid")
    void updateRole_ValidRole_ShouldReturnUserResponseDto() {
        Long userId = 1L;

        UserRoleUpdateDto updateDto = new UserRoleUpdateDto();
        updateDto.setRole("MANAGER");

        User user = new User();

        user.setId(userId);
        user.setEmail("bob@gmail.com");
        user.setFirstName("Bob");
        user.setLastName("Snow");
        user.setRole(User.Role.CUSTOMER);

        UserResponseDto responseDto = new UserResponseDto();

        responseDto.setEmail(user.getEmail());
        responseDto.setFirstName(user.getFirstName());
        responseDto.setLastName(user.getLastName());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(responseDto);

        UserResponseDto actualResponse = userService.updateRole(updateDto, userId);

        assertThat(actualResponse).isEqualTo(responseDto);
        assertThat(user.getRole()).isEqualTo(User.Role.MANAGER);

        verifyNoMoreInteractions(userMapper, userRepository);
    }

    @Test
    @DisplayName("Update user with valid id and dto")
    void updateUserProfile_ValidIdAndDto_ShouldUpdateUserAndSave() {
        Long userId = 1L;
        UpdateUserProfileDto updateUserProfileDto = new UpdateUserProfileDto();
        updateUserProfileDto.setFirstName("John");
        updateUserProfileDto.setLastName("Doe");

        User user = new User();
        user.setId(userId);
        user.setFirstName("OldName");
        user.setLastName("OldLastName");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        doAnswer(invocation -> {
            User updatedUser = invocation.getArgument(1);
            updatedUser.setFirstName(updateUserProfileDto.getFirstName());
            updatedUser.setLastName(updateUserProfileDto.getLastName());
            return null;
        }).when(userMapper).updateUserProfile(updateUserProfileDto, user);

        when(userRepository.save(user)).thenReturn(user);

        userService.updateUserProfile(userId, updateUserProfileDto);

        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).updateUserProfile(updateUserProfileDto, user);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Update user with an invalid id")
    void updateUserProfile_InvalidId_ShouldThrowException() {
        Long invalidId = 99L;

        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserById(invalidId)
        );

        assertThat(exception.getMessage()).isEqualTo("User not found.");

        verifyNoMoreInteractions(userRepository);
    }
}
