package Kevin.Peyton.Game.Platform.Demo.controller;

import java.util.List;
import java.security.Principal;

import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import Kevin.Peyton.Game.Platform.Demo.service.LibraryService;
import Kevin.Peyton.Game.Platform.Demo.dto.library.LibraryEntryResponse;
import Kevin.Peyton.Game.Platform.Demo.dto.library.LibraryUpdateRequest;
import Kevin.Peyton.Game.Platform.Demo.dto.errors.ApiErrorResponse;
import Kevin.Peyton.Game.Platform.Demo.repository.UserRepository;

@Tag(name = "Library", description = "Manage the authenticated user's game library")
@Validated
@RestController
public class LibraryController {

    private final LibraryService libraryService;
    private final UserRepository userRepository;

    public LibraryController(LibraryService libraryService, UserRepository userRepository) {
        this.libraryService = libraryService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get my library", description = "Returns all library entries for the authenticated user.",
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = LibraryEntryResponse.class))))
    @GetMapping("/API/me/library")
    public List<LibraryEntryResponse> getMyLibrary(Principal principal) {
        var userId = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + principal.getName()))
                .getId();
        var libraryEntries = libraryService.getLibraryByUserId(userId);
        return libraryEntries.stream().map(LibraryEntryResponse::fromEntity).toList();
    }

    @Operation(summary = "Update a library entry", description = "Update playtime or install status for a game in the authenticated user's library.",
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Entry updated",
            content = @Content(schema = @Schema(implementation = LibraryEntryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Library entry not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/API/me/library/{gameId}")
    public ResponseEntity<LibraryEntryResponse> updateLibraryEntry(
            @Parameter(description = "Game ID") @PathVariable @Positive Integer gameId,
            @RequestBody LibraryUpdateRequest request,
            Principal principal) {
        var userId = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + principal.getName()))
                .getId();
        var updatedEntry = libraryService.updateLibraryEntry(userId, gameId, request);
        return ResponseEntity.ok().body(LibraryEntryResponse.fromEntity(updatedEntry));
    }
}
