package lt.galdebar.shoppingitems;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import(ShoppingItemsModuleConfiguration.class)
@Configuration
public @interface EnableShoppingItemsModule {
}
