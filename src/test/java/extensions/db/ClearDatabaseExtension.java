package extensions.db;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * Simple Junit extension for earing tables after test execution, instead of unsing @Transactional,
 * which can make testing much more pain in certain situations, where you actually want to test multiple transactions
 * in service layer or test rollback or... something.
 *
 * You can use whatever callbacks you want to of course
 */
public class ClearDatabaseExtension implements AfterTestExecutionCallback, AfterAllCallback {

    private static final Logger log = LoggerFactory.getLogger(ClearDatabaseExtension.class);

    private static final Set<String> EXCLUDED = Set.of("whatever_shouldn't_be_truncated");

    @Override
    public void afterAll(ExtensionContext context) {
        truncateTables(context);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Optional<ClearTables> annotation = getAnnotation(context, ClearTables.class);
        truncateTables(context, annotation.map(ClearTables::excluded).orElse(null));
    }

    private void truncateTables(ExtensionContext context, String... excluded) {
        Set<String> excludedTables = new HashSet<>(EXCLUDED);
        if (excluded != null ) {
            excludedTables.addAll(Arrays.asList(excluded));
        }

        DataSource dataSource = SpringExtension.getApplicationContext(context).getBean(DataSource.class);

        try (Connection connection = dataSource.getConnection()) {
            // Obtain right SqlGenerator
            SqlGenerator sqlGenerator = getGenerator(connection);

            // Disable integrity constraint
            connection.prepareStatement(sqlGenerator.disableIntegrity()).execute();

            // Truncate all tables
            ResultSet rs = connection.prepareStatement(sqlGenerator.getAllTables()).executeQuery();
            while (rs.next()) {
                String tableToTruncate = rs.getString(1);
                if (!containsIgnoreCase(excludedTables, tableToTruncate)) {
                    log.info("truncating table: " + tableToTruncate);
                    connection.prepareStatement("TRUNCATE TABLE " + tableToTruncate).execute();
                }
            }

            // Now maybe clear sequences also, but we don't use any specific sequences

            // Reenable the constraints
            connection.prepareStatement(sqlGenerator.enableIntegrity()).execute();

            connection.commit();
        } catch (SQLException ex) {
            log.info("Exception occured while clearing tables: \n", ex);
        }
    }

    private static <T extends Annotation> Optional<T> getAnnotation(ExtensionContext context, Class<T> clasz) {
        return ofNullable(context.getTestClass().get().getAnnotation(clasz));
    }

    private  SqlGenerator getGenerator(Connection connection) {
        try {
            log.debug("Obtaining SqlGenerator for " + connection.getMetaData().getDatabaseProductName());
            return SqlGenerator.getGenerator(connection.getMetaData())
                               .orElseThrow(() -> new IllegalStateException("No compatible SQL Generator found."));
        } catch (SQLException ex) {
            throw new DataAccessResourceFailureException("Failed to retrieve database metadata", ex);
        }
    }

    private boolean containsIgnoreCase(Collection<String> set, String str) {
        return set.stream().anyMatch(str::equalsIgnoreCase);
    }

}
