package com.encora.todolist_app.controller;

import com.encora.todolist_app.models.Priority;
import com.encora.todolist_app.models.StateTaskDTO;
import com.encora.todolist_app.models.Task;
import com.encora.todolist_app.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private final TaskService taskService = new TaskService();

    @GetMapping("/todos")
    List<Task> allTasks(@RequestParam(required = false) ActionsGet action,
                        @RequestParam(required = false) String text,
                        @RequestParam(required = false) Priority priority,
                        @RequestParam(required = false) boolean status) {
        if (action == null){
            action = ActionsGet.all;
        }
        return switch (action) {

            case SortPriority -> taskService.sortTaskByPriority();

            case SortDueDate -> taskService.sortTaskByDueDate();

            case SortPriorityDueDate -> taskService.sortTaskByUrgency();

            case FilterStatus -> taskService.filterTaskByStatus(status);

            case FilterText -> taskService.filterTaskByText(text);

            case FilterPriority -> taskService.filterTaskByPriority(priority);

            default -> taskService.getAllTasks();
        };
    }
    enum ActionsGet{
        SortPriority,SortDueDate, SortPriorityDueDate,FilterStatus,FilterText,FilterPriority,all
    }

    @GetMapping("/todos/time")
    Map<String, Duration> timeTaks(){
        return taskService.avgTimesAllTask();
    }

    @PostMapping("/todos")
    Task insertTask(@RequestBody Task task){
        return taskService.addTask(task);
    }

    @PatchMapping("/todos/{id}/done")
    @CrossOrigin(origins = "http://localhost:5173")
    StateTaskDTO updateStatusDoneTask(@PathVariable int id){
        return taskService.updateStatusDoneTask(id);
    }

    @PatchMapping("todos/{id}/undone")
    @CrossOrigin(origins = "http://localhost:5173")
    StateTaskDTO updateStatusTask(@PathVariable int id){
        return taskService.updateStatusUndoneTask(id);
    }

    @PutMapping("/todos/{id}")
    Task updateTask(@PathVariable int id,@RequestBody Task task){
        return taskService.updateTask(id,task);
    }

    @DeleteMapping("/todos/{id}")
    ResponseEntity<Void> deleteTask(@PathVariable int id){
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

}


