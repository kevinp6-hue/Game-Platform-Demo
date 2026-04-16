package Kevin.Peyton.Game.Platform.Demo.service;

import java.util.List;

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

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional(readOnly = true)
    public User findById( Integer id) {
        return userRepository.findWithEmailsAndAddressesById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User findByIdWithEmails( Integer id) {
        return userRepository.findWithEmailsById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User findByIdWithoutRelations( Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User findByEmail( String email) {
        return userRepository.findWithEmailsByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public User findByUsername( String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public boolean existsById( Integer id) {
        return userRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername( String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional(readOnly = true)
    public List<User> findByRole( String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with name: " + roleName));
        return userRoleRepository.findByRole_Id(role.getId())
                .stream()
                .map(UserRole::getUser)
                .toList();
    }

    @Transactional
    public User saveUser( User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser( Integer id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserRole assignRoleToUser( Integer userId, Integer roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

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
    public void removeRoleFromUser( Integer userId, Integer roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        UserRole userRole = userRoleRepository.findByUser_IdAndRole_Id(user.getId(), role.getId());
        if (userRole == null) {
            throw new EntityNotFoundException("User does not have the specified role assigned");
        }
        userRoleRepository.delete(userRole);
    }

    @Transactional(readOnly = true)
    public List<Role> getUserRoles( Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return userRoleRepository.findByUser_Id(user.getId())
                .stream()
                .map(UserRole::getRole)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole( Integer roleId) {
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
        return userRoleRepository.findByRole_Id(role.getId())
                .stream()
                .map(UserRole::getUser)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean userHasRole( Integer userId, Integer roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        return userRoleRepository.existsByUser_IdAndRole_Id(user.getId(), role.getId());
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<UserRole> findAllUserRoles() {
        return userRoleRepository.findAll();
    }

    @Transactional
    public Role saveRole( Role role) {
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole( Integer roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new EntityNotFoundException("Role not found with id: " + roleId);
        }
        roleRepository.deleteById(roleId);
    }

    @Transactional
    public User updateUser( Integer userId, User updatedUser) {
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



}
