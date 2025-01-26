package mate.academy.repository;

import java.util.Optional;
import mate.academy.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmail(String email);

}
