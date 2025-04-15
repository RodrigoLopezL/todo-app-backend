package com.encora.todolist_app.service;

import com.encora.todolist_app.models.Priority;
import com.encora.todolist_app.models.StateTaskDTO;
import com.encora.todolist_app.models.Task;
import com.encora.todolist_app.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task1;
    private Task task2;
    private Pageable pageable;
    LocalDateTime now = LocalDateTime.now();
    @BeforeEach
    void setUp() {
        task1 = new Task(1, "Task 1",null ,now, null, null, Priority.MEDIUM, false);
        task2 = new Task(2, "Task 2",now.minusHours(1), now.plusDays(1), now.plusHours(2), Duration.ofHours(3), Priority.HIGH, true);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllTasks_shouldCallFindAllByStateAndPriorityAndTextRepository() {
        Page<Task> taskPage = new PageImpl<>(Collections.singletonList(task1), pageable, 1);
        when(taskRepository.findAllByStateAndPriorityAndText(false, "MEDIUM", "Task", pageable)).thenReturn(taskPage);

        Page<Task> result = taskService.getAllTasks(false, "MEDIUM", "Task", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(taskRepository, times(1)).findAllByStateAndPriorityAndText(false, "MEDIUM", "Task", pageable);
        verify(taskRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getAllTasks_withNullFilters_shouldCallFindAllRepository() {
        Page<Task> taskPage = new PageImpl<>(Collections.singletonList(task1), pageable, 1);
        when(taskRepository.findAllByStateAndPriorityAndText(null, null, null,pageable)).thenReturn(taskPage);

        Page<Task> result = taskService.getAllTasks(null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(taskRepository, times(1)).findAllByStateAndPriorityAndText(null, null, null,pageable);
    }

    @Test
    void addTask_shouldCallSaveRepositoryAndReturnSavedTask() {
        Task newTask = new Task(null, "New Task", null,null, null, null, Priority.LOW, false);
        Task savedTask = new Task(3, "New Task", now, null, null, null, Priority.LOW, false);
        when(taskRepository.save(newTask)).thenReturn(savedTask);

        Task result = taskService.addTask(newTask);

        assertNotNull(result);
        assertEquals(3, result.getId());
        assertEquals("New Task", result.getText());
        verify(taskRepository, times(1)).save(newTask);
    }

    @Test
    void updateTask_shouldCallFindByIdAndReplaceTaskRepositoryAndReturnUpdatedTaskIfExists() {
        Task updatedTask = new Task(1, "Updated Task 1", now,now.plusHours(2), null, null, Priority.HIGH, true);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task1));
        when(taskRepository.replaceTask(1, updatedTask)).thenReturn(updatedTask);

        Task result = taskService.updateTask(1, updatedTask);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Updated Task 1", result.getText());
        assertTrue(result.isState());
        verify(taskRepository, times(1)).findById(1);
        verify(taskRepository, times(1)).replaceTask(1, updatedTask);
    }

    @Test
    void updateTask_shouldReturnNullIfTaskNotFound() {
        Task updatedTask = new Task(1, "Updated Task 1", now,now.plusHours(2), null, null, Priority.HIGH, true);
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        Task result = taskService.updateTask(1, updatedTask);

        assertNull(result);
        verify(taskRepository, times(1)).findById(1);
        verify(taskRepository, never()).replaceTask(anyInt(), any(Task.class));
    }

    @Test
    void deleteTask_shouldCallDeleteByIdRepository() {
        taskService.deleteTask(1);
        verify(taskRepository, times(1)).deleteById(1);
    }

    @Test
    void avgTimesAllTask_shouldCallGetAllTaskRepositoryAndCalculateAverageTimes() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> allTasks = Arrays.asList(
                new Task(1, "Task A", now,null, now.plusHours(1), Duration.ofHours(1), Priority.LOW, true),
                new Task(2, "Task B", now.minusHours(2),null, now.plusHours(1), Duration.ofHours(3), Priority.MEDIUM, true),
                new Task(3, "Task C", now.minusMinutes(30),null, now.plusHours(2), Duration.ofMinutes(150), Priority.HIGH, true),
                new Task(4, "Task D", now.minusHours(3),null, null, null, Priority.LOW, false) // Not done
        );
        when(taskRepository.getAllTask()).thenReturn(allTasks);

        Map<String, Duration> avgTimes = taskService.avgTimesAllTask();

        assertNotNull(avgTimes);
        assertEquals(Duration.ofMinutes(130), avgTimes.get("AvgTotalTime")); // (60 + 180 + 150) / 3 = 130. Incorrect calculation in previous response.
        assertEquals(Duration.ofMinutes(60), avgTimes.get("avgTimeLowPriority")); // 60 / 1 = 60. Incorrect calculation.
        assertEquals(Duration.ofMinutes(180), avgTimes.get("avgTimeMediumPriority")); // 180 / 1 = 180. Incorrect calculation.
        assertEquals(Duration.ofMinutes(150), avgTimes.get("avgTimeHighPriority")); // 150 / 1 = 150. Incorrect calculation.

        verify(taskRepository, times(1)).getAllTask();
    }

    @Test
    void avgTimesAllTask_shouldReturnNullIfNoDoneTasks() {
        when(taskRepository.getAllTask()).thenReturn(Collections.singletonList(task1)); // Task 1 is not done

        Map<String, Duration> avgTimes = taskService.avgTimesAllTask();

        assertNull(avgTimes);
        verify(taskRepository, times(1)).getAllTask();
    }

    @Test
    void updateStatusDoneTask_shouldCallFindByIdAndReplaceTaskRepositoryAndUpdateStateAndTimes() {
        LocalDateTime now = LocalDateTime.now();
        Task taskToUpdate = new Task(1, "Task To Done", now.minusMinutes(30),null, null, null, Priority.LOW, false);
        when(taskRepository.findById(1)).thenReturn(Optional.of(taskToUpdate));
        when(taskRepository.replaceTask(eq(1), any(Task.class))).thenAnswer(invocation -> invocation.getArgument(1));

        StateTaskDTO result = taskService.updateStatusDoneTask(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertTrue(result.isStatus());
        assertNotNull(taskToUpdate.getDoneDate());
        Duration duration = Duration.ofSeconds(125, 500_000_000);
        assertEquals(Duration.ofMinutes(30), truncateToMinutes(taskToUpdate.getTimeFrame()));
        verify(taskRepository, times(1)).findById(1);
        verify(taskRepository, times(1)).replaceTask(eq(1), any(Task.class));
    }
    public static Duration truncateToMinutes(Duration duration) {
        long totalMinutes = duration.toMinutes();
        return Duration.ofMinutes(totalMinutes);
    }


    @Test
    void updateStatusDoneTask_shouldReturnNullIfTaskNotFound() {
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        StateTaskDTO result = taskService.updateStatusDoneTask(1);

        assertNull(result);
        verify(taskRepository, times(1)).findById(1);
        verify(taskRepository, never()).replaceTask(anyInt(), any(Task.class));
    }

    @Test
    void updateStatusUndoneTask_shouldCallFindByIdAndReplaceTaskRepositoryAndUpdateStateAndClearTimes() {
        when(taskRepository.findById(2)).thenReturn(Optional.of(task2));
        when(taskRepository.replaceTask(eq(2), any(Task.class))).thenAnswer(invocation -> invocation.getArgument(1));

        StateTaskDTO result = taskService.updateStatusUndoneTask(2);

        assertNotNull(result);
        assertEquals(2, result.getId());
        assertFalse(result.isStatus());
        assertNull(task2.getDoneDate());
        assertNull(task2.getTimeFrame());
        verify(taskRepository, times(1)).findById(2);
        verify(taskRepository, times(1)).replaceTask(eq(2), any(Task.class));
    }

    @Test
    void updateStatusUndoneTask_shouldReturnNullIfTaskNotFound() {
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        StateTaskDTO result = taskService.updateStatusUndoneTask(1);

        assertNull(result);
        verify(taskRepository, times(1)).findById(1);
        verify(taskRepository, never()).replaceTask(anyInt(), any(Task.class));
    }
}