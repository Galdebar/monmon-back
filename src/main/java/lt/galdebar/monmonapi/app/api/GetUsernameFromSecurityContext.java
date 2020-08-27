package lt.galdebar.monmonapi.app.api;

import org.springframework.security.core.context.SecurityContextHolder;

public interface GetUsernameFromSecurityContext {
    default String getUserName(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
