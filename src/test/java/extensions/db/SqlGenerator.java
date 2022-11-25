package extensions.db;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * In case of too many/too complicated methods, just make enum constants a separate classes implementing SqlGenerator interface.
 * And turn SqlGenerator into something like Visitor from Visitor pattern
 */
public enum SqlGenerator {
    MS_SQL {
        @Override
        public boolean isCompatible(DatabaseMetaData metadata) throws SQLException {
            return metadata.getDatabaseProductName().equals("Microsoft SQL Server");
        }

        @Override
        public String getAllTables() {
            return "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES;";
        }

        @Override
        public String disableIntegrity() {
            return "ALTER TABLE ? NOCHECK CONSTRAINT ALL";
        }

        @Override
        public String enableIntegrity() {
            return "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT ALL";
        }
    },
    H2 {
        @Override
        public boolean isCompatible(DatabaseMetaData metadata) throws SQLException {
            return metadata.getDatabaseProductName().equals("H2");
        }

        @Override
        public String getAllTables() {
            return "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA != 'INFORMATION_SCHEMA';";
        }

        @Override
        public String disableIntegrity() {
            return "SET REFERENTIAL_INTEGRITY FALSE";
        }

        @Override
        public String enableIntegrity() {
            return "SET REFERENTIAL_INTEGRITY TRUE";
        }
    };

    /**
     * This method is used to select right SQL Generator
     * @param metadata The database metadata.
     */
    public abstract boolean isCompatible(DatabaseMetaData metadata) throws SQLException;
    public abstract String getAllTables();
    public abstract String disableIntegrity();
    public abstract String enableIntegrity();

    public static final EnumSet<SqlGenerator> GENERATORS = EnumSet.allOf(SqlGenerator.class);

    public static Optional<SqlGenerator> getGenerator(DatabaseMetaData metaData) {
        return GENERATORS.stream()
                         .filter(CheckedPredicate.wrapped(generator -> generator.isCompatible(metaData)))
                         .findFirst();
    }

    @FunctionalInterface
    interface CheckedPredicate<T> {

        boolean test(T t) throws Exception;

        static <T> Predicate<T> wrapped(CheckedPredicate<T> func) {
            return t -> {
                try {
                    return func.test(t);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

}
