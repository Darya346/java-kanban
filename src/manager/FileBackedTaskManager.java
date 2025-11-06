package manager;

import model.*;
import model.TaskType;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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
            writer.println("id,type,name,status,description,epic,duration,startTime");

            for (Task task : getAllTasks()) {
                writer.println(taskToString(task));
            }
            for (Epic epic : getAllEpics()) {
                writer.println(taskToString(epic));
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.println(taskToString(subtask));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    private String taskToString(Task task) {
        String durationStr = task.getDuration() != null
                ? String.valueOf(task.getDuration().toMinutes()) : "";
        String startTimeStr = task.getStartTime() != null
                ? task.getStartTime().format(DATE_TIME_FORMATTER) : "";

        if (task instanceof Epic) {
            Epic epic = (Epic) task;
            String endTimeStr = epic.getEndTime() != null
                    ? epic.getEndTime().format(DATE_TIME_FORMATTER) : "";
            return String.format("%d,EPIC,%s,%s,%s,,%s,%s,%s",
                    task.getId(),
                    task.getName(),
                    task.getStatus(),
                    task.getDescription(),
                    durationStr,
                    startTimeStr,
                    endTimeStr);
        } else if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            return String.format("%d,SUBTASK,%s,%s,%s,%d,%s,%s",
                    subtask.getId(),
                    subtask.getName(),
                    subtask.getStatus(),
                    subtask.getDescription(),
                    subtask.getEpicId(),
                    durationStr,
                    startTimeStr);
        } else {
            return String.format("%d,TASK,%s,%s,%s,,%s,%s",
                    task.getId(),
                    task.getName(),
                    task.getStatus(),
                    task.getDescription(),
                    durationStr,
                    startTimeStr);
        }
    }

    private Task taskFromString(String value) {
        String[] fields = value.split(",", -1);
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        Duration duration = fields[6].isEmpty() ? Duration.ZERO
                : Duration.ofMinutes(Long.parseLong(fields[6]));
        LocalDateTime startTime = fields[7].isEmpty() ? null
                : LocalDateTime.parse(fields[7], DATE_TIME_FORMATTER);

        switch (type) {
            case TASK:
                Task task = new Task(name, description, id, status, duration, startTime);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description, id, status, duration, startTime, null);
                if (fields.length > 8 && !fields[8].isEmpty()) {
                    LocalDateTime endTime = LocalDateTime.parse(fields[8], DATE_TIME_FORMATTER);
                    epic.setEndTime(endTime);
                }
                return epic;
            case SUBTASK:
                int epicId = fields[5].isEmpty() ? 0 : Integer.parseInt(fields[5]);
                Subtask subtask = new Subtask(name, description, id, status, epicId, duration, startTime);
                return subtask;
            default:
                throw new IllegalArgumentException(String.format("Неизвестный тип задачи: %s", type));
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            if (!file.exists()) {
                return manager;
            }

            String content = Files.readString(file.toPath());
            String[] fileLines = content.split("\n");

            for (int lineNumber = 1; lineNumber < fileLines.length; lineNumber++) {
                String currentLine = fileLines[lineNumber].trim();
                if (currentLine.isEmpty()) {
                    continue;
                }

                Task task = manager.taskFromString(currentLine);
                if (task instanceof Epic) {
                    manager.epics.put(task.getId(), (Epic) task);
                } else if (task instanceof Subtask) {
                    manager.subtasks.put(task.getId(), (Subtask) task);
                } else {
                    manager.tasks.put(task.getId(), task);
                }

                if (task.getId() >= manager.nextId) {
                    manager.nextId = task.getId() + 1;
                }

                if (task.getStartTime() != null) {
                    manager.prioritizedTasks.add(task);
                }
            }

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

    public static void main(String[] args) {
        try {
            File tempFile = File.createTempFile("tasks", ".csv");

            FileBackedTaskManager manager1 = new FileBackedTaskManager(tempFile);

            Task task1 = manager1.createTask(new Task("Task 1", "Description 1", Status.NEW,
                    Duration.ofMinutes(30), LocalDateTime.now()));
            manager1.createTask(new Task("Task 2", "Description 2", Status.IN_PROGRESS,
                    Duration.ofHours(1), LocalDateTime.now().plusHours(2)));

            Epic epic1 = manager1.createEpic(new Epic("Epic 1", "Epic description"));
            Subtask subtask1 = manager1.createSubtask(new Subtask("Subtask 1", "Sub description",
                    Status.DONE, epic1.getId(), Duration.ofMinutes(45),
                    LocalDateTime.now().plusHours(1)));
            manager1.createSubtask(new Subtask("Subtask 2", "Sub description 2",
                    Status.NEW, epic1.getId(), Duration.ofMinutes(15),
                    LocalDateTime.now().plusHours(3)));

            manager1.getTaskById(task1.getId());
            manager1.getEpicById(epic1.getId());
            manager1.getSubtaskById(subtask1.getId());

            FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(tempFile);

            System.out.println("Проверка восстановления данных:");
            System.out.println("Задачи в manager2: " + manager2.getAllTasks().size());
            System.out.println("Эпики в manager2: " + manager2.getAllEpics().size());
            System.out.println("Подзадачи в manager2: " + manager2.getAllSubtasks().size());
            System.out.println("История в manager2: " + manager2.getHistory().size());
            System.out.println("Приоритетные задачи в manager2: " + manager2.getPrioritizedTasks().size());

            tempFile.deleteOnExit();

        } catch (IOException e) {
            System.err.println("Ошибка при работе с файлом: " + e.getMessage());
        }
    }
}