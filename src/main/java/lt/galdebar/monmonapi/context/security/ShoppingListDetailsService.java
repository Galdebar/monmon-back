package lt.galdebar.monmonapi.context.security;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListEntity;
import lt.galdebar.monmonapi.services.ShoppingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ShoppingListDetailsService implements UserDetailsService {

    @Autowired
    private ShoppingListService listService;

    @Override
    public UserDetails loadUserByUsername(String listName) throws UsernameNotFoundException {
        ShoppingListEntity listEntity = null;


        listEntity = listService.findByListName(listName);

        List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("user"));
        return new User(listEntity.getName(),listEntity.getPassword(),authorities);
    }
}
