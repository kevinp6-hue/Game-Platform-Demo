package Kevin.Peyton.Game.Platform.Demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import Kevin.Peyton.Game.Platform.Demo.repository.PurchaseRepository;
import Kevin.Peyton.Game.Platform.Demo.repository.UserRepository;
import Kevin.Peyton.Game.Platform.Demo.repository.GameRepository;
import Kevin.Peyton.Game.Platform.Demo.entity.Purchase;
import Kevin.Peyton.Game.Platform.Demo.entity.User;
import Kevin.Peyton.Game.Platform.Demo.entity.Game;

import Kevin.Peyton.Game.Platform.Demo.exception.ConflictException;

@Service
public class PurchasesService {
    private final PurchaseRepository purchaseRepository;  
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public PurchasesService(
            PurchaseRepository purchaseRepository,
            UserRepository userRepository,
            GameRepository gameRepository) {
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    @Transactional(readOnly = true)
    public List<Purchase> getPurchasesByUserId(Integer userId) {
        requireUser(userId);
        return purchaseRepository.findByUser_Id(userId);
    }

    @Transactional
    public Purchase purchaseGame(Integer userId, Integer gameId) {
        var user = requireUser(userId);
        var game = requireGame(gameId);

        if (!purchaseRepository.findByUser_IdAndGame_IdAndIsRefunded(userId, gameId, false).isEmpty()) {
            throw new ConflictException("User has already purchased this game: " + gameId);
        }

        var purchase = new Purchase();
        purchase.setUser(user);
        purchase.setGame(game);
        purchase.setPricePaid(game.getCurrentPrice());
        return purchaseRepository.save(purchase);

    }

    @Transactional
    public Purchase refundPurchase(Integer purchaseId) {
        var purchase = requirePurchase(purchaseId);
        if (purchase.getIsRefunded()) {
            throw new ConflictException("Purchase has already been refunded: " + purchaseId);
        }
        purchase.setIsRefunded(true);
        return purchaseRepository.save(purchase);
    }

    private Purchase requirePurchase(Integer purchaseId) {
        return purchaseRepository.findById(purchaseId).orElseThrow(() -> new EntityNotFoundException("Purchase not found: " + purchaseId));
    }

    private User requireUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private Game requireGame(Integer gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));
    }
}
