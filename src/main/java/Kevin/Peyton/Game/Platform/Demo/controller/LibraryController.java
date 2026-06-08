package Kevin.Peyton.Game.Platform.Demo.controller;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

import Kevin.Peyton.Game.Platform.Demo.service.LibraryService;
import Kevin.Peyton.Game.Platform.Demo.dto.library.LibraryEntryResponse;
import Kevin.Peyton.Game.Platform.Demo.repository.UserRepository;
import Kevin.Peyton.Game.Platform.Demo.dto.library.LibraryUpdateRequest;


@Validated
@RestController
public class LibraryController {
    private final LibraryService libraryService;
    private final UserRepository userRepository;
    
    public LibraryController(LibraryService libraryService, UserRepository userRepository) {
        this.libraryService = libraryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/API/me/library")
    public List<LibraryEntryResponse> getMyLibrary(Principal principal) {
        var userId = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + principal.getName()))
                .getId();
        var libraryEntries = libraryService.getLibraryByUserId(userId);
        return libraryEntries.stream().map(LibraryEntryResponse::fromEntity).toList();
    }

    @PatchMapping("/API/me/library/{gameId}")
    public ResponseEntity<LibraryEntryResponse> updateLibraryEntry(
            @PathVariable @Positive Integer gameId,
            @RequestBody LibraryUpdateRequest request,
            Principal principal) {
        var userId = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + principal.getName()))
                .getId();
        var updatedEntry = libraryService.updateLibraryEntry(userId, gameId, request);
        return ResponseEntity.ok().body(LibraryEntryResponse.fromEntity(updatedEntry));
    }
}
