package Kevin.Peyton.Game.Platform.Demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;


import Kevin.Peyton.Game.Platform.Demo.dto.library.LibraryUpdateRequest;
import Kevin.Peyton.Game.Platform.Demo.repository.LibraryRepository;
import Kevin.Peyton.Game.Platform.Demo.repository.UserRepository;
import Kevin.Peyton.Game.Platform.Demo.entity.Library;
import Kevin.Peyton.Game.Platform.Demo.entity.User;
import Kevin.Peyton.Game.Platform.Demo.entity.LibraryId;

@Service
public class LibraryService {
    private final LibraryRepository libraryRepository;
    private final UserRepository userRepository;

    public LibraryService(LibraryRepository libraryRepository, UserRepository userRepository) {
        this.libraryRepository = libraryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Library> getLibraryByUserId(Integer userId) {
        requireUser(userId);
        return libraryRepository.findByUserId(userId);
    }

    @Transactional
    public Library updateLibraryEntry(Integer userId, Integer gameId, LibraryUpdateRequest request) {
        var libraryId = new LibraryId();
        libraryId.setUserId(userId);
        libraryId.setGameId(gameId);
        var library = requireLibrary(libraryId);

        if (request.totalPlaytimeMinutes() != null) {
            library.setTotalPlaytimeMinutes(request.totalPlaytimeMinutes());
        }
        if (request.isInstalled() != null) {
            library.setIsInstalled(request.isInstalled());
        }

        return libraryRepository.save(library);
    }

    private Library requireLibrary(LibraryId id) {
        return libraryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Library entry not found: " + id));
    }

    private User requireUser(Integer id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

}
