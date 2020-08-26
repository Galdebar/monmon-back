package lt.galdebar.monmonapi.app.scheduledtasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonapi.app.services.blacklistedtokens.BlacklistedTokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class ScheduleDeleteBlacklistTokens {
    private final BlacklistedTokenService tokenService;

    @Scheduled(cron = "${task.schedule.period}")
    public void cleanupBlacklistTokens(){
        log.info("Scheduled task- cleaning up blacklist tokens");
        tokenService.deleteExpiredTokens();
    }
}
