package http;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class JsonConverter {

    public static String taskToJson(Task task) {
        if (task == null) return "null";

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"id\":").append(task.getId()).append(",");
        jsonBuilder.append("\"name\":\"").append(escapeJson(task.getName())).append("\",");
        jsonBuilder.append("\"description\":\"").append(escapeJson(task.getDescription())).append("\",");
        jsonBuilder.append("\"status\":\"").append(task.getStatus()).append("\",");
        jsonBuilder.append("\"type\":\"TASK\",");

        if (task.getDuration() != null) {
            jsonBuilder.append("\"duration\":").append(task.getDuration().toMinutes()).append(",");
        } else {
            jsonBuilder.append("\"duration\":0,");
        }

        if (task.getStartTime() != null) {
            jsonBuilder.append("\"startTime\":\"").append(task.getStartTime()).append("\"");
        } else {
            jsonBuilder.append("\"startTime\":null");
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    public static String epicToJson(Epic epic) {
        if (epic == null) return "null";

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"id\":").append(epic.getId()).append(",");
        jsonBuilder.append("\"name\":\"").append(escapeJson(epic.getName())).append("\",");
        jsonBuilder.append("\"description\":\"").append(escapeJson(epic.getDescription())).append("\",");
        jsonBuilder.append("\"status\":\"").append(epic.getStatus()).append("\",");
        jsonBuilder.append("\"type\":\"EPIC\",");

        if (epic.getDuration() != null) {
            jsonBuilder.append("\"duration\":").append(epic.getDuration().toMinutes()).append(",");
        } else {
            jsonBuilder.append("\"duration\":0,");
        }

        if (epic.getStartTime() != null) {
            jsonBuilder.append("\"startTime\":\"").append(epic.getStartTime()).append("\",");
        } else {
            jsonBuilder.append("\"startTime\":null,");
        }

        if (epic.getEndTime() != null) {
            jsonBuilder.append("\"endTime\":\"").append(epic.getEndTime()).append("\",");
        } else {
            jsonBuilder.append("\"endTime\":null,");
        }

        jsonBuilder.append("\"subtaskIds\":").append(epic.getSubtaskIds().toString());
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    public static String subtaskToJson(Subtask subtask) {
        if (subtask == null) return "null";

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"id\":").append(subtask.getId()).append(",");
        jsonBuilder.append("\"name\":\"").append(escapeJson(subtask.getName())).append("\",");
        jsonBuilder.append("\"description\":\"").append(escapeJson(subtask.getDescription())).append("\",");
        jsonBuilder.append("\"status\":\"").append(subtask.getStatus()).append("\",");
        jsonBuilder.append("\"type\":\"SUBTASK\",");
        jsonBuilder.append("\"epicId\":").append(subtask.getEpicId()).append(",");

        if (subtask.getDuration() != null) {
            jsonBuilder.append("\"duration\":").append(subtask.getDuration().toMinutes()).append(",");
        } else {
            jsonBuilder.append("\"duration\":0,");
        }

        if (subtask.getStartTime() != null) {
            jsonBuilder.append("\"startTime\":\"").append(subtask.getStartTime()).append("\"");
        } else {
            jsonBuilder.append("\"startTime\":null");
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    public static String tasksToJson(List<Task> tasks) {
        if (tasks == null) return "[]";

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (int taskIndex = 0; taskIndex < tasks.size(); taskIndex++) {
            jsonBuilder.append(taskToJson(tasks.get(taskIndex)));
            if (taskIndex < tasks.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    public static String epicsToJson(List<Epic> epics) {
        if (epics == null) return "[]";

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (int epicIndex = 0; epicIndex < epics.size(); epicIndex++) {
            jsonBuilder.append(epicToJson(epics.get(epicIndex)));
            if (epicIndex < epics.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    public static String subtasksToJson(List<Subtask> subtasks) {
        if (subtasks == null) return "[]";

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (int subtaskIndex = 0; subtaskIndex < subtasks.size(); subtaskIndex++) {
            jsonBuilder.append(subtaskToJson(subtasks.get(subtaskIndex)));
            if (subtaskIndex < subtasks.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    public static Task taskFromJson(String json) {
        try {
            if (json == null || json.trim().isEmpty() || json.equals("null")) {
                return null;
            }

            // Простой парсинг JSON
            String cleanJson = json.trim().replaceAll("[{}\"]", "");
            String[] keyValuePairs = cleanJson.split(",");

            int taskId = 0;
            String taskName = "";
            String taskDescription = "";
            Status taskStatus = Status.NEW;
            Duration taskDuration = Duration.ZERO;
            LocalDateTime taskStartTime = null;

            for (String keyValuePair : keyValuePairs) {
                String[] keyValue = keyValuePair.split(":", 2);
                if (keyValue.length != 2) continue;

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                switch (key) {
                    case "id":
                        taskId = Integer.parseInt(value);
                        break;
                    case "name":
                        taskName = unescapeJson(value);
                        break;
                    case "description":
                        taskDescription = unescapeJson(value);
                        break;
                    case "status":
                        taskStatus = Status.valueOf(value);
                        break;
                    case "duration":
                        taskDuration = Duration.ofMinutes(Long.parseLong(value));
                        break;
                    case "startTime":
                        if (!value.equals("null")) {
                            taskStartTime = LocalDateTime.parse(value);
                        }
                        break;
                }
            }

            if (taskId == 0) {
                return new Task(taskName, taskDescription, taskStatus, taskDuration, taskStartTime);
            } else {
                return new Task(taskName, taskDescription, taskId, taskStatus, taskDuration, taskStartTime);
            }
        } catch (Exception exception) {
            return null;
        }
    }

    public static Epic epicFromJson(String json) {
        try {
            if (json == null || json.trim().isEmpty() || json.equals("null")) {
                return null;
            }

            String cleanJson = json.trim().replaceAll("[{}\"]", "");
            String[] keyValuePairs = cleanJson.split(",");

            int epicId = 0;
            String epicName = "";
            String epicDescription = "";
            Status epicStatus = Status.NEW;
            Duration epicDuration = Duration.ZERO;
            LocalDateTime epicStartTime = null;
            LocalDateTime epicEndTime = null;

            for (String keyValuePair : keyValuePairs) {
                String[] keyValue = keyValuePair.split(":", 2);
                if (keyValue.length != 2) continue;

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                switch (key) {
                    case "id":
                        epicId = Integer.parseInt(value);
                        break;
                    case "name":
                        epicName = unescapeJson(value);
                        break;
                    case "description":
                        epicDescription = unescapeJson(value);
                        break;
                    case "status":
                        epicStatus = Status.valueOf(value);
                        break;
                    case "duration":
                        epicDuration = Duration.ofMinutes(Long.parseLong(value));
                        break;
                    case "startTime":
                        if (!value.equals("null")) {
                            epicStartTime = LocalDateTime.parse(value);
                        }
                        break;
                    case "endTime":
                        if (!value.equals("null")) {
                            epicEndTime = LocalDateTime.parse(value);
                        }
                        break;
                }
            }

            Epic epic;
            if (epicId == 0) {
                epic = new Epic(epicName, epicDescription);
            } else {
                epic = new Epic(epicName, epicDescription, epicId, epicStatus, epicDuration, epicStartTime, epicEndTime);
            }

            return epic;
        } catch (Exception exception) {
            return null;
        }
    }

    public static Subtask subtaskFromJson(String json) {
        try {
            if (json == null || json.trim().isEmpty() || json.equals("null")) {
                return null;
            }

            String cleanJson = json.trim().replaceAll("[{}\"]", "");
            String[] keyValuePairs = cleanJson.split(",");

            int subtaskId = 0;
            String subtaskName = "";
            String subtaskDescription = "";
            Status subtaskStatus = Status.NEW;
            int subtaskEpicId = 0;
            Duration subtaskDuration = Duration.ZERO;
            LocalDateTime subtaskStartTime = null;

            for (String keyValuePair : keyValuePairs) {
                String[] keyValue = keyValuePair.split(":", 2);
                if (keyValue.length != 2) continue;

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                switch (key) {
                    case "id":
                        subtaskId = Integer.parseInt(value);
                        break;
                    case "name":
                        subtaskName = unescapeJson(value);
                        break;
                    case "description":
                        subtaskDescription = unescapeJson(value);
                        break;
                    case "status":
                        subtaskStatus = Status.valueOf(value);
                        break;
                    case "epicId":
                        subtaskEpicId = Integer.parseInt(value);
                        break;
                    case "duration":
                        subtaskDuration = Duration.ofMinutes(Long.parseLong(value));
                        break;
                    case "startTime":
                        if (!value.equals("null")) {
                            subtaskStartTime = LocalDateTime.parse(value);
                        }
                        break;
                }
            }

            if (subtaskId == 0) {
                return new Subtask(subtaskName, subtaskDescription, subtaskStatus, subtaskEpicId, subtaskDuration, subtaskStartTime);
            } else {
                return new Subtask(subtaskName, subtaskDescription, subtaskId, subtaskStatus, subtaskEpicId, subtaskDuration, subtaskStartTime);
            }
        } catch (Exception exception) {
            return null;
        }
    }

    private static String escapeJson(String inputString) {
        if (inputString == null) return "";
        return inputString.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String inputString) {
        if (inputString == null) return "";
        return inputString.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}