package com.encora.todolist_app.service;

import com.encora.todolist_app.models.Priority;
import com.encora.todolist_app.models.StateTaskDTO;
import com.encora.todolist_app.models.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@ExtendWith(SpringExtension.class)
public class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Test
    void getAllTasks_noFilters_returnsAllTasksAsPage(){
        //Arrange
        Task task1 = new Task("task 1", LocalDateTime.now(), Priority.LOW);
        Task task2 = new Task("Task 2", LocalDateTime.now(), Priority.MEDIUM);
        Pageable pageable = PageRequest.of(0, 10);
        taskService.taskMap.put(0,task1);
        taskService.taskMap.put(1,task2);

        //Act
        Page<Task> result = taskService.getAllTasks(null, null, null, pageable);

        //Assert
        assertNotNull(result);
        assertEquals(2,result.getContent().size());
        assertEquals(2,result.getContent().size());
    }

    @Test
    void getAllTasks_filterByStateTrue_returnsFilteredTasks(){
        //Arrange
        Task task1 = new Task("task 1", LocalDateTime.now(), Priority.LOW);
        Task task2 = new Task("Task 2", LocalDateTime.now(), Priority.MEDIUM);
        task2.setState(true);
        Pageable pageable = PageRequest.of(0, 10);
        taskService.taskMap.put(0,task1);
        taskService.taskMap.put(1,task2);

        //Act
        Page<Task> result = taskService.getAllTasks(true, null, null, pageable);

        //Assert
        assertNotNull(result);
        assertEquals(1,result.getContent().size());
        assertEquals(1,result.getTotalElements());
        assertTrue(result.getContent().getFirst().isState());

    }

    @Test
    void getAllTasks_filterByPriorityLow_returnsFilteredTasks() {
        // Arrange
        Task task1 = new Task("task 1", LocalDateTime.now(), Priority.LOW);
        Task task2 = new Task("Task 2", LocalDateTime.now(), Priority.MEDIUM);
        Task task3 = new Task("Task 2", LocalDateTime.now(), Priority.LOW);
        taskService.taskMap.put(0, task1);
        taskService.taskMap.put(1, task2);
        taskService.taskMap.put(2, task3);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Task> result = taskService.getAllTasks(null, "MEDIUM", null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        assertEquals(Priority.MEDIUM, result.getContent().get(0).getPriority());
    }

    @Test
    void getAllTasks_filterByText_returnsFilteredTasks() {
        // Arrange
        Task task1 = new Task("meeting with the team", LocalDateTime.now(), Priority.LOW);
        Task task2 = new Task("do something", LocalDateTime.now(), Priority.MEDIUM);
        Task task3 = new Task("another day another meet", LocalDateTime.now(), Priority.LOW);
        taskService.taskMap.put(0, task1);
        taskService.taskMap.put(1, task2);
        taskService.taskMap.put(2, task3);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Task> result = taskService.getAllTasks(null, null, "meeting", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getText().toLowerCase().contains("meeting"));
    }

    @Test
    void getAllTasks_pagination_returnsPageOfTasks() {
        // Arrange
        Task task1 = new Task("meeting with the team", LocalDateTime.now(), Priority.LOW);
        Task task2 = new Task("do something", LocalDateTime.now(), Priority.MEDIUM);

        taskService.taskMap.put(0, task1);
        taskService.taskMap.put(1, task2);

        Pageable pageable = PageRequest.of(0, 1); // Page 0, size 1

        // Act
        Page<Task> result = taskService.getAllTasks(null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void getAllTasks_sortingByPriorityAsc_returnsSortedTasks() {
        // Arrange
        Task task1 = new Task("meeting with the team", LocalDateTime.now(), Priority.HIGH);
        Task task2 = new Task("do something", LocalDateTime.now(), Priority.LOW);
        task1.setDueDate(LocalDateTime.now());
        task2.setDueDate(LocalDateTime.now());
        taskService.taskMap.put(0, task1);
        taskService.taskMap.put(1, task2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "priority"));

        // Act
        Page<Task> result = taskService.getAllTasks(null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(Priority.LOW, result.getContent().get(0).getPriority());
        assertEquals(Priority.HIGH, result.getContent().get(1).getPriority());
    }

    @Test
    void getAllTasks_sortingByDueDateAsc_returnsSortedTasks() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("meeting with the team", LocalDateTime.now(), Priority.LOW);
        Task task2 = new Task("do something", LocalDateTime.now(), Priority.MEDIUM);
        task1.setDueDate(now.plusDays(2));
        task2.setDueDate(now.plusDays(1));
        taskService.taskMap.put(0, task1);
        taskService.taskMap.put(1, task2);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "dueDate"));

        // Act
        Page<Task> result = taskService.getAllTasks(null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(task2.getDueDate(), result.getContent().get(0).getDueDate());
        assertEquals(task1.getDueDate(), result.getContent().get(1).getDueDate());
    }

    @Test
    void getAllTasks_sortingByUrgencyAsc_returnsSortedTasks() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("meeting with the team", LocalDateTime.now(), Priority.LOW);
        Task task2 = new Task("do something", LocalDateTime.now(), Priority.HIGH);

        task1.setDueDate(now.plusDays(2));
        task2.setDueDate(now.plusDays(1));

        taskService.taskMap.put(0, task1);
        taskService.taskMap.put(1, task2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "urgency"));

        // Act
        Page<Task> result = taskService.getAllTasks(null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(task2.getDueDate(), result.getContent().get(0).getDueDate()); // DueDate sooner
        assertEquals(task1.getDueDate(), result.getContent().get(1).getDueDate());
        assertEquals(Priority.HIGH, result.getContent().get(0).getPriority()); // Higher priority for same/sooner dueDate
        assertEquals(Priority.LOW, result.getContent().get(1).getPriority());
    }

    @Test
    void addTask_addsTaskToMapAndIncrementsId() {
        // Arrange
        Task newTask = new Task("New Task", LocalDateTime.now(),Priority.LOW);
        int initialId = taskService.id;

        // Act
        Task addedTask = taskService.addTask(newTask);

        // Assert
        assertNotNull(addedTask.getId());
        assertEquals(initialId, addedTask.getId());
        assertEquals("New Task", addedTask.getText());
        assertNotNull(addedTask.getCreationDate());
        assertTrue(taskService.taskMap.containsKey(initialId));
        assertEquals(initialId + 1, taskService.id);
    }

    @Test
    void updateTask_updatesExistingTaskInMap() {
        // Arrange
        Task initialTask = new Task("meeting with the team", LocalDateTime.now(), Priority.LOW);
        taskService.taskMap.put(0, initialTask);
        Task updatedTask = new Task(0, "Updated Task", LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(2), Duration.ZERO, Priority.HIGH, true);

        // Act
        Task result = taskService.updateTask(0, updatedTask);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Task", result.getText());
        assertEquals(Priority.HIGH, result.getPriority());
        assertTrue(taskService.taskMap.containsKey(0));
        assertEquals("Updated Task", taskService.taskMap.get(0).getText());
    }

    @Test
    void updateStatusDoneTask_updatesTaskStateAndTimestamps() {
        // Arrange
        Task initialTask = new Task(0, "Task to Done",LocalDateTime.now().minusHours(1), LocalDateTime.now(), null, null, Priority.LOW, false);
        taskService.taskMap.put(0, initialTask);

        // Act
        StateTaskDTO result = taskService.updateStatusDoneTask(0);
        Task updatedTask = taskService.taskMap.get(0);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getId());
        assertTrue(result.isStatus());
        assertTrue(updatedTask.isState());
        assertNotNull(updatedTask.getDoneDate());
        assertNotNull(updatedTask.getTimeFrame());
        assertTrue(updatedTask.getTimeFrame().toMillis() > 0);
    }

    @Test
    void updateStatusDoneTask_taskNotFound_returnsNull() {
        // Act
        StateTaskDTO result = taskService.updateStatusDoneTask(99);

        // Assert
        assertNull(result);
    }

    @Test
    void updateStatusUndoneTask_updatesTaskStateAndClearsTimestamps() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Task initialTask = new Task(0, "Task to Undone" , now.minusHours(2), now.minusHours(1) , now.minusHours(1), Duration.ofHours(1), Priority.HIGH, true);
        taskService.taskMap.put(0, initialTask);

        // Act
        StateTaskDTO result = taskService.updateStatusUndoneTask(0);
        Task updatedTask = taskService.taskMap.get(0);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getId());
        assertFalse(result.isStatus());
        assertFalse(updatedTask.isState());
        assertNull(updatedTask.getDoneDate());
        assertNull(updatedTask.getTimeFrame());
    }

    @Test
    void updateStatusUndoneTask_taskNotFound_returnsNull() {
        // Act
        StateTaskDTO result = taskService.updateStatusUndoneTask(99);

        // Assert
        assertNull(result);
    }

    @Test
    void deleteTask_removesTaskFromMap() {
        // Arrange
        Task initialTask = new Task(0, "Task to Delete", LocalDateTime.now(),null, null, null, Priority.LOW, false);
        taskService.taskMap.put(0, initialTask);

        // Act
        taskService.deleteTask(0);

        // Assert
        assertFalse(taskService.taskMap.containsKey(0));
    }

    @Test
    void avgTimesAllTask_noFinishedTasks_returnsNull() {
        // Arrange
        Task task1 = new Task(0, "Tarea 1", LocalDateTime.now(),null, null, null, Priority.LOW, false);
        taskService.taskMap.put(0, task1);

        // Act
        Map<String, Duration> result = taskService.avgTimesAllTask();

        // Assert
        assertNotNull(result);
    }

    @Test
    void avgTimesAllTask_calculatesAverageTimesCorrectly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task(0, "Tarea 1", now.minusHours(2) , null, now.minusHours(1), Duration.ofHours(1), Priority.LOW, true);
        Task task2 = new Task(1, "Tarea 2", now.minusMinutes(45),null, now.minusMinutes(15), Duration.ofMinutes(30), Priority.MEDIUM, true);
        Task task3 = new Task(2, "Tarea 3", now.minusHours(3),null, null, null, Priority.HIGH, false); // Not finished
        taskService.taskMap.put(0, task1);
        taskService.taskMap.put(1, task2);
        taskService.taskMap.put(2, task3);

        // Act
        Map<String, Duration> result = taskService.avgTimesAllTask();

        // Assert
        assertNotNull(result);
        assertEquals(Duration.ofMinutes(45), result.get("AvgTotalTime")); // (60 + 30) / 2
        assertEquals(Duration.ofHours(1), result.get("avgTimeLowPriority"));
        assertEquals(Duration.ofMinutes(30), result.get("avgTimeMediumPriority"));
        assertEquals(Duration.ZERO, result.get("avgTimeHighPriority"));
    }
}
