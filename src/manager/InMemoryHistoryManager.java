package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }

    private final Map<Integer, Node> historyMap = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) return;

        remove(task.getId());


        Task taskCopy = copyTask(task);
        Node newNode = linkLast(taskCopy);
        historyMap.put(taskCopy.getId(), newNode);
    }

    private Task copyTask(Task original) {
        if (original instanceof Epic) {
            Epic epic = (Epic) original;
            Epic copy = new Epic(epic.getName(), epic.getDescription());
            copy.setId(epic.getId());
            copy.updateStatus(epic.getStatus());
            // Копируем список подзадач
            for (Integer subtaskId : epic.getSubtaskIds()) {
                copy.addSubtaskId(subtaskId);
            }
            return copy;
        } else if (original instanceof Subtask) {
            Subtask subtask = (Subtask) original;
            return new Subtask(subtask.getName(), subtask.getDescription(),
                    subtask.getId(), subtask.getStatus(), subtask.getEpicId());
        } else {
            return new Task(original.getName(), original.getDescription(),
                    original.getId(), original.getStatus());
        }
    }


    @Override
    public void remove(int id) {
        Node node = historyMap.get(id);
        if (node != null) {
            removeNode(node);
            historyMap.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private Node linkLast(Task task) {
        Node newNode = new Node(task);

        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }

        return newNode;
    }

    private void removeNode(Node node) {
        if (node == null) return;

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }

        node.prev = null;
        node.next = null;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;

        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }

        return tasks;
    }
}