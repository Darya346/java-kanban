package model;

import model.Status;
import model.Task;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", 1, Status.DONE);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
        assertEquals(task1.hashCode(), task2.hashCode(), "HashCode должен совпадать для одинаковых ID");
    }

    @Test
    void tasksWithDifferentIdShouldNotBeEqual() {
        Task task1 = new Task("Task", "Description", 1, Status.NEW);
        Task task2 = new Task("Task", "Description", 2, Status.NEW);

        assertNotEquals(task1, task2, "Задачи с разными ID не должны быть равны");
    }
}