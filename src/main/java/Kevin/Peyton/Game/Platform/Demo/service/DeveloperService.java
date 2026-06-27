package Kevin.Peyton.Game.Platform.Demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import Kevin.Peyton.Game.Platform.Demo.entity.Developer;
import Kevin.Peyton.Game.Platform.Demo.entity.Game;

import Kevin.Peyton.Game.Platform.Demo.repository.DeveloperRepository;
import Kevin.Peyton.Game.Platform.Demo.repository.GameRepository;

@Service
public class DeveloperService {
    private final DeveloperRepository developerRepository;
    private final GameRepository gameRepository;

    public DeveloperService(DeveloperRepository developerRepository, GameRepository gameRepository) {
        this.developerRepository = developerRepository;
        this.gameRepository = gameRepository;
    }


    @Transactional(readOnly = true)
    public List<Developer> getAllDevelopers() {
        return developerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Developer getDeveloperById(Integer id) {
        return requireDeveloper(id);
    }

    @Transactional(readOnly = true)
    public List<Game> getGamesByDeveloperId(Integer developerId) {
        return gameRepository.findByDeveloperId(developerId);
    }

    @Transactional(readOnly = true)
    public Developer getDeveloperByName(String name) {
        return developerRepository.findByDevName(name).orElseThrow(() -> new EntityNotFoundException("Developer not found: " + name));
    }

    private Developer requireDeveloper(Integer id) {
        return developerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Developer not found: " + id));
    }
}
