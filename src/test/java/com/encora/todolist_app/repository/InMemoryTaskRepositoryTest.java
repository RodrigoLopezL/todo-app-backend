package com.encora.todolist_app.repository;

import com.encora.todolist_app.models.Priority;
import com.encora.todolist_app.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskRepositoryTest {
    private InMemoryTaskRepository taskRepository;
    private Task task1;
    private Task task2;
    private Task task3;
    LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        taskRepository = new InMemoryTaskRepository();
        task1 = new Task(null, "Task 1",null, null, null, null, Priority.MEDIUM, false);
        task2 = new Task(null, "Task 2", null,now.plusDays(2),null, null, Priority.HIGH, false);
        task3 = new Task(null, "Find this urgent",null,now.plusDays(1), null, null, Priority.LOW, false);
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
    }

    @Test
    void save_shouldAssignIdAndCreationDate() {
        Task newTask = new Task(null, "New Task", null,null, null, null, Priority.LOW, false);
        Task savedTask = taskRepository.save(newTask);
        assertNotNull(savedTask.getId());
        assertNotNull(savedTask.getCreationDate());
        assertEquals("New Task", taskRepository.findById(savedTask.getId()).get().getText());
    }

    @Test
    void save_withExistingId_shouldReturnNullIfIdExists() {
        Task existingTask = new Task(task1.getId(), "existing task",null, null, null, null, Priority.HIGH, true);
        Task result = taskRepository.save(existingTask);
        assertNull(result);
        assertEquals("Task 1", taskRepository.findById(task1.getId()).get().getText());
    }

    @Test
    void findById_shouldReturnTaskIfExists() {
        Optional<Task> foundTask = taskRepository.findById(task2.getId());
        assertTrue(foundTask.isPresent());
        assertEquals("Task 2", foundTask.get().getText());
    }

    @Test
    void findById_shouldReturnEmptyOptionalIfNotFound() {
        Optional<Task> foundTask = taskRepository.findById(999);
        assertTrue(foundTask.isEmpty());
    }

    @Test
    void findAll_withoutSorting_shouldReturnAllTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> allTasksPage = taskRepository.findAll(pageable);
        assertEquals(3, allTasksPage.getContent().size());
    }

    @Test
    void findAll_withPrioritySorting_shouldReturnSortedTasks() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("priority"));
        Page<Task> sortedTasksPage = taskRepository.findAllByStateAndPriorityAndText(null,null,null,pageable);
        assertEquals(Priority.LOW, sortedTasksPage.getContent().get(0).getPriority());
        assertEquals(Priority.MEDIUM, sortedTasksPage.getContent().get(1).getPriority());
        assertEquals(Priority.HIGH, sortedTasksPage.getContent().get(2).getPriority());
    }

    @Test
    void findAll_withDueDateSorting_shouldReturnSortedTasks() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("dueDate"));
        Page<Task> sortedTasksPage = taskRepository.findAllByStateAndPriorityAndText(null,null,null,pageable);
        assertNull(sortedTasksPage.getContent().get(0).getDueDate());
        assertEquals(now.plusDays(1), sortedTasksPage.getContent().get(1).getDueDate());
        assertEquals(now.plusDays(2), sortedTasksPage.getContent().get(2).getDueDate());

        Pageable pageableDesc = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dueDate"));
        Page<Task> sortedTasksPageDesc = taskRepository.findAll(pageableDesc);
        assertEquals(now.plusDays(2), sortedTasksPageDesc.getContent().get(0).getDueDate());
        assertEquals(now.plusDays(1), sortedTasksPageDesc.getContent().get(1).getDueDate());
        assertNull(sortedTasksPageDesc.getContent().get(2).getDueDate());
    }


    @Test
    void findAll_withMultipleSorting_shouldReturnSortedTasks() {
        //first sort by priority asc, after by dueDate adc
        Pageable pageable = PageRequest.of(0, 10, Sort.by("priority").ascending().and(Sort.by("dueDate").ascending()));
        Page<Task> sortedTasksPage = taskRepository.findAllByStateAndPriorityAndText(null,null,null,pageable);
        assertEquals(Priority.LOW, sortedTasksPage.getContent().get(0).getPriority());
        assertEquals(Priority.MEDIUM, sortedTasksPage.getContent().get(1).getPriority());
        assertEquals(Priority.HIGH, sortedTasksPage.getContent().get(2).getPriority());
        assertEquals(now.plusDays(1), sortedTasksPage.getContent().get(0).getDueDate()); // Priority LOW
        assertNull(sortedTasksPage.getContent().get(1).getDueDate()); // Priority MEDIUM
        assertEquals(now.plusDays(2),sortedTasksPage.getContent().get(2).getDueDate()); // Priority HIGH

    }

    @Test
    void getAllTask_shouldReturnAllTasksAsList() {
        List<Task> allTasks = taskRepository.getAllTask();
        assertEquals(3, allTasks.size());
    }

    @Test
    void deleteById_shouldRemoveTask() {
        taskRepository.deleteById(task1.getId());
        assertFalse(taskRepository.findById(task1.getId()).isPresent());
        assertEquals(2, taskRepository.getAllTask().size());
    }

    @Test
    void replaceTask_shouldReplaceExistingTask() {
        Task replacementTask = new Task(task1.getId(), "Task 1 update",null,now.plusDays(3), null, null, Priority.HIGH, true);
        Task replacedTask = taskRepository.replaceTask(task1.getId(), replacementTask);
        assertEquals(replacementTask, replacedTask);
        assertEquals("Task 1 update", taskRepository.findById(task1.getId()).get().getText());
    }

    @Test
    void replaceTask_shouldReturnNullIfIdNotFound() {
        Task replacementTask = new Task(999, "Tarea Inexistente", LocalDateTime.now().plusDays(3),null,null, null, Priority.HIGH, true);
        Task replacedTask = taskRepository.replaceTask(999, replacementTask);
        assertNull(replacedTask);
        assertFalse(taskRepository.findById(999).isPresent());
    }

    @Test
    void findAllByStateAndPriorityAndText_withFilters_shouldReturnMatchingTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> results1 = taskRepository.findAllByStateAndPriorityAndText(false, "LOW", "urgent", pageable);
        assertEquals(1, results1.getContent().size());
        assertEquals("Find this urgent", results1.getContent().get(0).getText());

        Page<Task> results2 = taskRepository.findAllByStateAndPriorityAndText(null, "HIGH", null, pageable);
        assertEquals(1, results2.getContent().size());
        assertEquals("Task 2", results2.getContent().get(0).getText());

        Page<Task> results3 = taskRepository.findAllByStateAndPriorityAndText(null, null, "task", pageable);
        assertEquals(2, results3.getContent().size());
        assertTrue(results3.getContent().stream().anyMatch(task -> task.getText().equals("Task 1")));
        assertTrue(results3.getContent().stream().anyMatch(task -> task.getText().equals("Task 2")));
    }

    @Test
    void findAllByStateAndPriorityAndText_withNullFilters_shouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> all = taskRepository.findAllByStateAndPriorityAndText(null, null, null, pageable);
        assertEquals(3, all.getContent().size());
    }


}
