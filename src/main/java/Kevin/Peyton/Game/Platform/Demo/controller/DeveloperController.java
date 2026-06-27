package Kevin.Peyton.Game.Platform.Demo.controller;

import java.util.List;

import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import Kevin.Peyton.Game.Platform.Demo.dto.errors.ApiErrorResponse;
import Kevin.Peyton.Game.Platform.Demo.dto.developers.DeveloperResponse;

import Kevin.Peyton.Game.Platform.Demo.service.DeveloperService;

@Tag(name = "Developers", description = "Browse the game catalog and manage developer metadata")
@Validated
@RestController
@RequestMapping("/API/developers")
public class DeveloperController {
    
    private final DeveloperService developerService;

    public DeveloperController(DeveloperService developerService) {
        this.developerService = developerService;
    }

    @Operation(summary = "List all developers")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeveloperResponse.class))))
    @GetMapping
    public List<DeveloperResponse> list() {
        var developers = developerService.getAllDevelopers();
        return developers.stream()
                .map(developer -> {
                    var games = developerService.getGamesByDeveloperId(developer.getId());
                    return DeveloperResponse.fromEntity(developer, games);
                })
                .toList();
    }

    @Operation(summary = "Get developer by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = DeveloperResponse.class))),
        @ApiResponse(responseCode = "404", description = "Developer not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeveloperResponse> getById(@Parameter(description = "Developer ID") @PathVariable @Positive Integer id) {
        var developer = developerService.getDeveloperById(id);
        var games = developerService.getGamesByDeveloperId(developer.getId());
        return ResponseEntity.ok(DeveloperResponse.fromEntity(developer, games));
    }

}
