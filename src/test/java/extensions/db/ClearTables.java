package extensions.db;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ExtendWith(ClearDatabaseExtension.class)
@Target(ElementType.TYPE)
public @interface ClearTables {

    String[] excluded() default {};

}
