package academy.carol.portal.repository;

import academy.carol.portal.domain.Role;
import academy.carol.portal.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    long countByRole(Role role);
    List<UserAccount> findByRoleOrderByDisplayNameAsc(Role role);
    List<UserAccount> findByEnabledFalseAndRoleInOrderByDisplayNameAsc(List<Role> roles);
    void deleteByUsername(String username);
}
