package Kevin.Peyton.Game.Platform.Demo.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import Kevin.Peyton.Game.Platform.Demo.service.UserService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import Kevin.Peyton.Game.Platform.Demo.dto.users.UserCreateRequest;
import Kevin.Peyton.Game.Platform.Demo.dto.users.UserResponse;

@Validated
@RestController
@RequestMapping("/API/users")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint to retrieve a list of all users.
     * @return A list of all users.
     */
    @GetMapping
    public List<UserResponse> list() {
        var users = userService.getAllUsers();
        return users.stream().map(UserResponse::fromEntity).toList();
    }

    /**
     * Endpoint to retrieve a specific user by ID.
     * @param id The ID of the user to retrieve.
     * @return The user with the specified ID.
     */
    @GetMapping("/{id}")
    public UserResponse get(@PathVariable @Positive @NotNull Integer id) {
        var user = userService.getUserById(id);
        return UserResponse.fromEntity(user);
    }

    /**
     * Endpoint to retrieve a specific user by username.
     * @param username The username of the user to retrieve.
     * @return The user with the specified username.
     */
    @GetMapping("/username/{username}")
    public UserResponse getByUsername(@PathVariable @NotNull String username) {
        var user = userService.getUserByUsername(username);
        return UserResponse.fromEntity(user);
    }


    /**
     * Endpoint to retrieve a specific user by email.
     * @param email The email of the user to retrieve.
     * @return The user with the specified email.
     */
    @GetMapping("/email/{email}")
    public UserResponse getByEmail(@PathVariable @NotNull String email) {
        var user = userService.getUserByEmail(email);
        return UserResponse.fromEntity(user);
    }

    /**
     * Endpoint to retrieve a specific user by ID, including their emails and addresses.
     * @param id The ID of the user to retrieve.
     * @return The user with the specified ID, including their emails and addresses.
     */
    @GetMapping("/{id}/details")
    public UserResponse getWithEmailsAndAddresses(@PathVariable @Positive @NotNull Integer id) {
        var user = userService.getUserByIdWithEmailsAndAddresses(id);
        return UserResponse.fromEntity(user);
    }

     /**
     * Endpoint to retrieve a specific user by ID, including their emails.
     * @param id The ID of the user to retrieve.
     * @return The user with the specified ID, including their emails.
     */
    @GetMapping("/{id}/emails")
    public UserResponse getWithEmails(@PathVariable @Positive @NotNull Integer id) {
        var user = userService.getUserByIdWithEmails(id);
        return UserResponse.fromEntity(user);
    }

    // Additional endpoints for creating, updating, and deleting users

    /**
     * Endpoint to create a user
     * @Param request The request containing the details of the user
     */
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request){
        
        var created = userService.registerUser(request.username(), request.password(), request.birthDate());
        var body = UserResponse.fromEntity(created);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(body);
    }

}
