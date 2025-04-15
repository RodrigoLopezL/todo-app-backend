package com.encora.todolist_app.repository;

import com.encora.todolist_app.models.Task;
import com.encora.todolist_app.utils.comparators.UrgentTaskComparator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryTaskRepository implements TaskRepository {

    Map<Integer, Task> taskMap = new HashMap<>();
    private Integer nextId = 0;

    @Override
    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(nextId++);
        }else{
            if (taskMap.containsKey(task.getId())){
                return null;
            }
        }
        task.setCreationDate(LocalDateTime.now());
        taskMap.put(task.getId(), task);
        return task;
    }


    @Override
    public Optional<Task> findById(Integer id) {
        return Optional.ofNullable(taskMap.get(id));
    }


    @Override
    public Page<Task> findAll(Pageable pageable) {
        List<Task> allTasks = new ArrayList<>(taskMap.values());
        if (pageable.getSort().isSorted()) {
            Comparator<Task> comparator = getTaskComparator(pageable);
            if (comparator != null) {
                allTasks.sort(comparator);
            }
        }
        return getPagedResult(allTasks, pageable);
    }

    @Override
    public List<Task> getAllTask(){
        Collection<Task> collectionTask = taskMap.values();
        return new ArrayList<>(collectionTask);
    }

    @Override
    public void deleteById(Integer id) {
        taskMap.remove(id);
    }

    @Override
    public Task replaceTask(Integer id, Task task) {
        Task taskAux = taskMap.get(id);
        if (taskAux != null){
            task.setCreationDate(taskAux.getCreationDate());
            taskMap.replace(id,task);
            return taskMap.get(id);
        }
        return null;

    }

    @Override
    public Page<Task> findAllByStateAndPriorityAndText(Boolean state, String priority, String text, Pageable pageable) {
        List<Task> filteredTasks = taskMap.values().stream()
                .filter(task -> state == null || task.isState() == state)
                .filter(task -> priority == null || task.getPriority().toString().equalsIgnoreCase(priority))
                .filter(task -> text == null || (task.getText() != null && task.getText().toLowerCase().contains(text.toLowerCase())))
                .collect(Collectors.toList());
        if (pageable.getSort().isSorted()) {
            Comparator<Task> comparator = getTaskComparator(pageable);
            if (comparator != null) {
                filteredTasks.sort(comparator);
            }
        }
        return getPagedResult(filteredTasks, pageable);
    }

    private Page<Task> getPagedResult(List<Task> tasks, Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, tasks.size());
        List<Task> pagedTasks = tasks.subList(start, end);
        return new PageImpl<>(pagedTasks, pageable, tasks.size());
    }
    private static Comparator<Task> getTaskComparator(Pageable pageable) {
        Comparator<Task> comparator = null;
        for (Sort.Order order : pageable.getSort()) {
            Comparator<Task> currentComparator = switch (order.getProperty()) {
                case "priority" -> Comparator.comparing(Task::getPriority);
                case "dueDate" -> Comparator.comparing(Task::getDueDate, Comparator.nullsFirst(Comparator.naturalOrder()));
                case "urgency" -> new UrgentTaskComparator();
                default -> null;
            };
            if (currentComparator != null) {
                if (order.getDirection() == Sort.Direction.DESC) {
                    currentComparator = currentComparator.reversed();
                }
                if (comparator == null) {
                    comparator = currentComparator;
                } else {
                    comparator = comparator.thenComparing(currentComparator);
                }
            }
        }
        return comparator;
    }
}