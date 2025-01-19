package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.user.UserRegistrationRequestDto;
import mate.academy.dto.user.UserResponseDto;
import mate.academy.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto toUserResponse(User user);

    User toEntity(UserRegistrationRequestDto requestDto);

    void updateUser(UserRegistrationRequestDto requestDto, @MappingTarget User user);
}
