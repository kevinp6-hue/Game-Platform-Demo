package Kevin.Peyton.Game.Platform.Demo.dto.users;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Password change request")
public record PasswordChangeRequest(
        @Schema(description = "Current password")
        @NotBlank
        String currentPassword,

        @Schema(description = "New password")
        @NotBlank
        String newPassword
) {
}
