package Kevin.Peyton.Game.Platform.Demo.dto.users;

import java.time.LocalDate;


public record UserCreateRequest(
    String username,
    LocalDate birthDate,
    String password

)
 {
    
}
