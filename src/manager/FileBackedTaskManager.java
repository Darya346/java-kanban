package manager;

import model.*;
import model.TaskType;

import java.io.*;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    protected void save() {
        try (PrintWriter writer = new PrintWriter(file)) {
            // Записываем заголовок
            writer.println("id,type,name,status,description,epic");

            // Сохраняем все задачи
            for (Task task : getAllTasks()) {
                writer.println(toString(task));
            }
            for (Epic epic : getAllEpics()) {
                writer.println(toString(epic));
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.println(toString(subtask));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    private String toString(Task task) {
        if (task instanceof Epic) {
            return String.format("%d,EPIC,%s,%s,%s,",
                    task.getId(),
                    task.getName(),
                    task.getStatus(),
                    task.getDescription());
        } else if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            return String.format("%d,SUBTASK,%s,%s,%s,%d",
                    subtask.getId(),
                    subtask.getName(),
                    subtask.getStatus(),
                    subtask.getDescription(),
                    subtask.getEpicId());
        } else {
            return String.format("%d,TASK,%s,%s,%s,",
                    task.getId(),
                    task.getName(),
                    task.getStatus(),
                    task.getDescription());
        }
    }

    private Task fromString(String value) {
        String[] fields = value.split(",", -1); // -1 чтобы сохранить пустые поля
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case TASK:
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.updateStatus(status);
                return epic;
            case SUBTASK:
                int epicId = fields[5].isEmpty() ? 0 : Integer.parseInt(fields[5]);
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            if (!file.exists()) {
                return manager;
            }

            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            // Пропускаем заголовок и пустые строки
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) {
                    continue;
                }

                Task task = manager.fromString(line);
                if (task instanceof Epic) {
                    manager.epics.put(task.getId(), (Epic) task);
                } else if (task instanceof Subtask) {
                    manager.subtasks.put(task.getId(), (Subtask) task);
                } else {
                    manager.tasks.put(task.getId(), task);
                }

                // Обновляем счетчик id
                if (task.getId() >= manager.nextId) {
                    manager.nextId = task.getId() + 1;
                }
            }

            // Восстанавливаем связи для подзадач в эпиках
            for (Subtask subtask : manager.subtasks.values()) {
                Epic epic = manager.epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(subtask.getId());
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }

        return manager;
    }

    // Дополнительное задание: пользовательский сценарий
    public static void main(String[] args) {
        // Создаем временный файл для тестирования
        try {
            File tempFile = File.createTempFile("tasks", ".csv");

            // Создаем первый менеджер и добавляем задачи
            FileBackedTaskManager manager1 = new FileBackedTaskManager(tempFile);

            Task task1 = manager1.createTask(new Task("Task 1", "Description 1", Status.NEW));
            manager1.createTask(new Task("Task 2", "Description 2", Status.IN_PROGRESS));

            Epic epic1 = manager1.createEpic(new Epic("Epic 1", "Epic description"));
            Subtask subtask1 = manager1.createSubtask(new Subtask("Subtask 1", "Sub description", Status.DONE, epic1.getId()));
            manager1.createSubtask(new Subtask("Subtask 2", "Sub description 2", Status.NEW, epic1.getId()));

            // Просматриваем некоторые задачи для истории
            manager1.getTaskById(task1.getId());
            manager1.getEpicById(epic1.getId());
            manager1.getSubtaskById(subtask1.getId());

            // Создаем второй менеджер из того же файла
            FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(tempFile);

            // Проверяем, что все задачи восстановились
            System.out.println("Проверка восстановления данных:");
            System.out.println("Задачи в manager2: " + manager2.getAllTasks().size());
            System.out.println("Эпики в manager2: " + manager2.getAllEpics().size());
            System.out.println("Подзадачи в manager2: " + manager2.getAllSubtasks().size());
            System.out.println("История в manager2: " + manager2.getHistory().size());

            // Очищаем временный файл
            tempFile.deleteOnExit();

        } catch (IOException e) {
            System.err.println("Ошибка при работе с файлом: " + e.getMessage());
        }
    }
}