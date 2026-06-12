package Kevin.Peyton.Game.Platform.Demo.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

import Kevin.Peyton.Game.Platform.Demo.service.UserService;
import Kevin.Peyton.Game.Platform.Demo.dto.users.UserCreateRequest;
import Kevin.Peyton.Game.Platform.Demo.dto.users.UserResponse;
import Kevin.Peyton.Game.Platform.Demo.dto.errors.ApiErrorResponse;

@Tag(name = "Users", description = "User registration and profile lookup")
@Validated
@RestController
@RequestMapping("/API/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get currently logged-in user profile", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/me")
    public UserResponse getMe(Principal principal) {
        var user = userService.getUserByUsername(principal.getName());
        return UserResponse.fromEntity(user);
    }

    @Operation(summary = "List all users", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))))
    @GetMapping
    public List<UserResponse> list() {
        var users = userService.getAllUsers();
        return users.stream().map(UserResponse::fromEntity).toList();
    }

    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public UserResponse get(@Parameter(description = "User ID") @PathVariable @Positive @NotNull Integer id) {
        var user = userService.getUserById(id);
        return UserResponse.fromEntity(user);
    }

    @Operation(summary = "Get user by username", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/username/{username}")
    public UserResponse getByUsername(@Parameter(description = "Username") @PathVariable @NotNull String username) {
        var user = userService.getUserByUsername(username);
        return UserResponse.fromEntity(user);
    }

    @Operation(summary = "Get user by email", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/email/{email}")
    public UserResponse getByEmail(@Parameter(description = "Email address") @PathVariable @NotNull String email) {
        var user = userService.getUserByEmail(email);
        return UserResponse.fromEntity(user);
    }

    @Operation(summary = "Get user with emails and addresses", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}/details")
    public UserResponse getWithEmailsAndAddresses(@Parameter(description = "User ID") @PathVariable @Positive @NotNull Integer id) {
        var user = userService.getUserByIdWithEmailsAndAddresses(id);
        return UserResponse.fromEntity(user);
    }

    @Operation(summary = "Get user with emails", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}/emails")
    public UserResponse getWithEmails(@Parameter(description = "User ID") @PathVariable @Positive @NotNull Integer id) {
        var user = userService.getUserByIdWithEmails(id);
        return UserResponse.fromEntity(user);
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account. Returns 201 with Location header.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Username or email already taken",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        var created = userService.registerUser(request.username(), request.password(), request.birthDate());
        var body = UserResponse.fromEntity(created);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(body);
    }
}
