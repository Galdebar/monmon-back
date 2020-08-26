package lt.galdebar.monmonapi.app.services.blacklistedtokens;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.app.persistence.domain.jwtokens.BlacklistedTokenDTO;
import lt.galdebar.monmonapi.app.persistence.domain.jwtokens.BlacklistedTokenEntity;
import lt.galdebar.monmonapi.app.persistence.repositories.BlacklistedTokenRepo;
import org.apache.tomcat.jni.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlacklistedTokenService {
    private final BlacklistedTokenRepo tokenRepo;
    private final int EXPIRY_GRACE_PERIOD_HOURS = 1;

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

    @Transactional
    public void deleteExpiredTokens(){
        List<BlacklistedTokenEntity> foundTokens = tokenRepo.findByDateAddedBefore(
                LocalDateTime.now().minusHours(EXPIRY_GRACE_PERIOD_HOURS)
        );
        tokenRepo.deleteAll(foundTokens);
    }

}
