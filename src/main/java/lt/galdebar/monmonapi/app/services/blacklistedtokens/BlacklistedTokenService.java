package lt.galdebar.monmonapi.app.services.blacklistedtokens;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.app.persistence.domain.jwtokens.BlacklistedTokenDTO;
import lt.galdebar.monmonapi.app.persistence.domain.jwtokens.BlacklistedTokenEntity;
import lt.galdebar.monmonapi.app.persistence.repositories.BlacklistedTokenRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BlacklistedTokenService {
    private final BlacklistedTokenRepo tokenRepo;

    @Transactional
    public BlacklistedTokenDTO addToken(String token){
        BlacklistedTokenEntity tokenEntity = new BlacklistedTokenEntity();
        tokenEntity.setToken(token);
        tokenEntity.setDateAdded(LocalDateTime.now());
        return tokenRepo.save(tokenEntity).getDTO();
    }

    public boolean tokenExists(String token){
        return tokenRepo.existsBlacklistedTokenByToken(token);
    }

}
