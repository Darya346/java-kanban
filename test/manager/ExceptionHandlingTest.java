package manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHandlingTest {

    @TempDir
    Path tempDir;

    @Test
    void managerSaveException_shouldBeThrownOnSaveError() {
        File invalidFile = new File("/invalid/path/tasks.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(invalidFile);

        assertThrows(ManagerSaveException.class, () -> {
            manager.save();
        });
    }

    @Test
    void noException_withValidFile() {
        File validFile = tempDir.resolve("valid.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(validFile);

        assertDoesNotThrow(() -> {
            manager.save();
        });
    }
}