package model;

import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicIntegrationTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void epicCannotBeAddedAsItsOwnSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Test Epic", "Description"));

        // Пытаемся создать подзадачу с epicId равным ID самого эпика
        Subtask invalidSubtask = new Subtask("Invalid", "Description", Status.NEW, epic.getId());

        // Должен вернуть null, так как epic не может быть своим же эпиком
        assertNull(taskManager.createSubtask(invalidSubtask),
                "Epic не должен быть добавлен в качестве своей же подзадачи");
    }
}