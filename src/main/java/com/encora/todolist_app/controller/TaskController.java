package com.encora.todolist_app.controller;

import com.encora.todolist_app.models.Priority;
import com.encora.todolist_app.models.Task;
import com.encora.todolist_app.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
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

            case SortPriority -> {
                taskService.sortTaskByPriority();
                yield taskService.getAllTasks();
            }
            case SortDueDate -> {
                taskService.sortTaskByDueDate();
                yield taskService.getAllTasks();
            }
            case SortPriorityDueDate -> {
                taskService.sortTaskByUrgency();
                yield taskService.getAllTasks();
            }

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

    @PostMapping("/todos/{id}/done")
    Task updateStatusDoneTask(@PathVariable int id){
        return taskService.updateStatusDoneTask(id);
    }

    @PutMapping("todos/{id}/undone")
    Task updateStatusTask(@PathVariable int id){
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


