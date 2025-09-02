import manager.TaskManager;
import manager.InMemoryTaskManager;
import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new InMemoryTaskManager();

        System.out.println("=== Создание задач ===");

        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание задачи 1", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание задачи 2", Status.NEW));

        Epic epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic1.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic1.getId()));

        Epic epic2 = taskManager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        Subtask subtask3 = taskManager.createSubtask(new Subtask("Подзадача 3", "Описание подзадачи 3", Status.NEW, epic2.getId()));

        System.out.println(String.format("Задачи: %s", taskManager.getAllTasks()));
        System.out.println(String.format("Эпики: %s", taskManager.getAllEpics()));
        System.out.println(String.format("Подзадачи: %s", taskManager.getAllSubtasks()));

        System.out.println("=== Изменение статусов ===");

        task1.setStatus(Status.DONE);
        taskManager.updateTask(task1);

        task2.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task2);

        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);

        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        subtask3.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask3);

        System.out.println("После изменений:");
        System.out.println(String.format("Задачи: %s", taskManager.getAllTasks()));
        System.out.println(String.format("Эпики: %s", taskManager.getAllEpics()));
        System.out.println(String.format("Подзадачи: %s", taskManager.getAllSubtasks()));

        System.out.println("=== Удаление ===");
        taskManager.deleteTaskById(task1.getId());
        taskManager.deleteEpicById(epic1.getId());

        System.out.println("После удаления:");
        System.out.println(String.format("Задачи: %s", taskManager.getAllTasks()));
        System.out.println(String.format("Эпики: %s", taskManager.getAllEpics()));
        System.out.println(String.format("Подзадачи: %s", taskManager.getAllSubtasks()));
    }
}