package Kevin.Peyton.Game.Platform.Demo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record LoginRequest(
    @NotBlank
    @NotNull
    String username,

    @NotBlank
    @NotNull
    String password
) {
}
