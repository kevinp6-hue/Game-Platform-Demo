package Kevin.Peyton.Game.Platform.Demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import Kevin.Peyton.Game.Platform.Demo.repository.UserRepository;
import Kevin.Peyton.Game.Platform.Demo.repository.RoleRepository;
import Kevin.Peyton.Game.Platform.Demo.repository.UserRoleRepository;
import Kevin.Peyton.Game.Platform.Demo.entity.User;
import Kevin.Peyton.Game.Platform.Demo.entity.UserRole;
import Kevin.Peyton.Game.Platform.Demo.entity.Role;
import Kevin.Peyton.Game.Platform.Demo.entity.UserRoleId;
import Kevin.Peyton.Game.Platform.Demo.exception.ConflictException;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder
        ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserByIdWithEmailsAndAddresses(Integer id) {
        return userRepository.findWithEmailsAndAddressesById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserByIdWithEmails(Integer id) {
        return userRepository.findWithEmailsById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserById(Integer id) {
        return requireUser(id);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findWithEmailsByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        return userRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRoleName(String roleName) {
        var role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with name: " + roleName));
        return userRoleRepository.findByRole_Id(role.getId())
                .stream()
                .map(UserRole::getUser)
                .toList();
    }

    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User registerUser(String username, String rawPassword, LocalDate birthDate) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ConflictException("Username already exists");
        }
        User newUser = new User();
        var encodedPassword = passwordEncoder.encode(rawPassword);
        newUser.setUsername(username);
        newUser.setPasswordHash(encodedPassword);
        newUser.setBirthDate(birthDate);

        var createdUser = userRepository.save(newUser);

        // Assign the default "player" role to the new user
        var defaultRole = roleRepository.findByName("player")
                .orElseThrow(() -> new EntityNotFoundException("Default role 'player' not found"));
        
        UserRole userRole = new UserRole();
        userRole.setId(new UserRoleId(createdUser.getId(), defaultRole.getId()));
        userRole.setUser(createdUser);
        userRole.setRole(defaultRole);
        userRoleRepository.save(userRole);


        return createdUser;
    }

    @Transactional
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserRole assignRoleToUser(Integer userId, Integer roleId) {
        var user = requireUser(userId);
        var role = requireRole(roleId);

        if (userRoleRepository.existsByUser_IdAndRole_Id(user.getId(), role.getId())) {
            throw new ConflictException("User already has the specified role assigned");
        }

        UserRole userRole = new UserRole();
        userRole.setId(new UserRoleId(user.getId(), role.getId()));
        userRole.setUser(user);
        userRole.setRole(role);
        return userRoleRepository.save(userRole);
    }

    @Transactional
    public void removeRoleFromUser(Integer userId, Integer roleId) {
        var user = requireUser(userId);
        var role = requireRole(roleId);

        UserRole userRole = userRoleRepository.findByUser_IdAndRole_Id(user.getId(), role.getId());
        if (userRole == null) {
            throw new EntityNotFoundException("User does not have the specified role assigned");
        }
        userRoleRepository.delete(userRole);
    }

    @Transactional(readOnly = true)
    public List<Role> getUserRoles(Integer userId) {
        var user = requireUser(userId);
        return userRoleRepository.findByUser_Id(user.getId())
                .stream()
                .map(UserRole::getRole)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRoleId(Integer roleId) {
        var role = requireRole(roleId);
        return userRoleRepository.findByRole_Id(role.getId())
                .stream()
                .map(UserRole::getUser)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean userHasRole(Integer userId, Integer roleId) {
        var user = requireUser(userId);
        var role = requireRole(roleId);

        return userRoleRepository.existsByUser_IdAndRole_Id(user.getId(), role.getId());
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<UserRole> getAllUserRoles() {
        return userRoleRepository.findAll();
    }

    @Transactional
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(Integer roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new EntityNotFoundException("Role not found with id: " + roleId);
        }
        roleRepository.deleteById(roleId);
    }

    @Transactional
    public User updateUser(Integer userId, User updatedUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setPasswordHash(updatedUser.getPasswordHash());
        existingUser.setEmails(updatedUser.getEmails());
        existingUser.setAddresses(updatedUser.getAddresses());
        existingUser.setIsActive(updatedUser.getIsActive());
        existingUser.setBirthDate(updatedUser.getBirthDate());
        existingUser.setLastLogin(updatedUser.getLastLogin());

        return userRepository.save(existingUser);
    }

    private User requireUser(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    private Role requireRole(Integer id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
    }
}
