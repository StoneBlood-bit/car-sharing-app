package mate.academy.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
        scripts = "classpath:database/create-user.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@Sql(
        scripts = "classpath:database/clear-users-table.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS
)
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Exists user by valid email")
    void existsByEmail_ValidEmail_ShouldReturnTrue() {
        String validEmail = "bob@gmail.com";

        assertTrue(userRepository.existsByEmail(validEmail));
    }

    @Test
    @DisplayName("Exists user by invalid email")
    void existsByEmail_InvalidEmail_ShouldReturnFalse() {
        String invalidEmail = "invalid@gmail.com";

        assertFalse(userRepository.existsByEmail(invalidEmail));
    }

    @Test
    @DisplayName("Find user by valid email")
    void findByEmail_ValidEmail_ShouldReturnUser() {
        String validEmail = "bob@gmail.com";

        User actualUser = userRepository.findByEmail(validEmail).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by email:" + validEmail)
        );

        assertEquals(1, actualUser.getId());
        assertEquals("bob@gmail.com", actualUser.getEmail());
        assertEquals("Bob", actualUser.getFirstName());
        assertEquals("Snow", actualUser.getLastName());
        assertEquals(User.Role.CUSTOMER, actualUser.getRole());
        assertEquals("1212", actualUser.getChatId());
        assertEquals("password", actualUser.getPassword());
    }

    @Test
    @DisplayName("Find user by invalid email")
    void findByEmail_InvalidEmail_ShouldReturnOptionalEmpty() {
        String invalidEmail = "invalid@gmail.com";

        assertEquals(Optional.empty(), userRepository.findByEmail(invalidEmail));
    }
}
