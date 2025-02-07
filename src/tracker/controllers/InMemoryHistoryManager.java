package tracker.controllers;

import tracker.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private Node head;
    private Node tail;

    private final Map<Integer, Node> idToNode;

    private static class Node {
        private Task task;
        private Node prev;
        private Node next;

        public Node(Node prev, Task task, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }

    public InMemoryHistoryManager() {
        idToNode = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        final int taskId = task.getId();
        if (idToNode.containsKey(taskId)) {
            Node node = idToNode.get(taskId);
            removeNode(node);
        }
        linkLast(task);
    }

    public void linkLast(Task task) {
        final Node oldTail = tail;
        final Node newNode = new Node(null, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            newNode.prev = oldTail;
            oldTail.next = newNode;
        }
        idToNode.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {
        final Node prevNode = node.prev;
        final Node nextNode = node.next;
        if (prevNode == null && nextNode == null) {
            head = tail = null;
        } else if (prevNode == null && nextNode != null) {
            nextNode.prev = null;
            head = nextNode;
        } else if (prevNode != null && nextNode == null) {
            prevNode.next = null;
            tail = prevNode;
        } else {
            prevNode.next = nextNode;
            nextNode.prev = prevNode;
        }
        Task task = node.task;
        idToNode.remove(task.getId());
    }

    public List<Task> getTasks() {
        final List<Task> history = new ArrayList<>();
        Node node = tail;
        while (node != null) {
            Task task = node.task;
            history.add(task);
            node = node.prev;
        }
        return history;
    }

    @Override
    public List<Task> getHistory() {
        if (idToNode.isEmpty()) {
            return Collections.emptyList();
        }
        return getTasks();
    }

    @Override
    public void remove(int id) {
        if (!idToNode.containsKey(id)) {
            return;
        }
        Node node = idToNode.get(id);
        removeNode(node);
    }
}