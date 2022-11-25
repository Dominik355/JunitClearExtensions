package actualTests;

import extensions.db.ClearTables;
import org.example.Main;
import org.example.entity.Device;
import org.example.entity.User;
import org.example.repo.DeviceRepository;
import org.example.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ClearTables(excluded = {"users"})
public class ShowcaseTests {

    private static final String TEST_DIRECTORY = "/tmp/testDir";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @AfterEach
    void afterEach() throws IOException {
        deleteDirectory(TEST_DIRECTORY);
    }

    @Test
    @Order(1)
    public void test1() {
        System.out.println("Test 1 starting");
        userRepository.save(new User("username1", "USER"));
        userRepository.save(new User("username2", "USER"));
        userRepository.save(new User("username3", "ADMIN"));

        deviceRepository.save(new Device("mobile", "android"));
        deviceRepository.save(new Device("mobile", "ios"));

        assertEquals(3, userRepository.findAll().size());
        assertEquals(2, deviceRepository.findAll().size());
    }

    /**
     * Devices should be truncated, but users should be kept - because of excluded param in @ClearTables annotation
     */
    @Test
    @Order(2)
    public void test2() {
        System.out.println("Test 2 starting");
        assertEquals(3, userRepository.findAll().size());
        assertEquals(0, deviceRepository.findAll().size());

        deviceRepository.save(new Device("mobile", "ios"));
        assertEquals(1, deviceRepository.findAll().size());
    }

    @Test
    @Order(3)
    public void test3() {
        System.out.println("Test 3 starting");
        assertEquals(3, userRepository.findAll().size());
        assertEquals(0, deviceRepository.findAll().size());
    }

    // now test clearence of files

    @Test
    @Order(4)
    public void test4() throws IOException {
        try {
            Files.createDirectory(Paths.get(TEST_DIRECTORY));
            Files.createFile(Paths.get(TEST_DIRECTORY + "/file1"));
        } catch (Exception ex) {
            System.out.println("Excception: " + ex);
        }

        File testFile = new File(TEST_DIRECTORY, "file1");
        assertTrue(testFile.exists());
    }

    @Test
    @Order(5)
    public void test5() throws IOException {
        File testFile = new File(TEST_DIRECTORY, "file1");
        assertTrue(!testFile.exists());
    }

    public static void deleteDirectory(String first, String... more) throws IOException {
        Path directory = Path.of(first, more);

        if (Files.exists(directory)) {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    System.out.println("Deleting file: " + path);
                    Files.delete(path);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException ioException) throws IOException {
                    System.out.println("Deleting dir: " + directory);
                    if (ioException == null) {
                        Files.delete(directory);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw ioException;
                    }
                }
            });
        }
    }

}
