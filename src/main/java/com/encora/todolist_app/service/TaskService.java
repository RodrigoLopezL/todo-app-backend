package com.encora.todolist_app.service;

import com.encora.todolist_app.models.Priority;
import com.encora.todolist_app.models.Task;
import com.encora.todolist_app.utils.comparators.DueDateTaskComparator;
import com.encora.todolist_app.utils.comparators.PriorityTaskComparator;
import com.encora.todolist_app.utils.comparators.UrgentTaskComparator;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final List<Task> taskList = new ArrayList<>();
    private Duration avgTotalTime = Duration.ZERO;
    private Duration avgTimeLowP = Duration.ZERO;
    private Duration avgTimeMediumP = Duration.ZERO;
    private Duration avgTimeHighP = Duration.ZERO;
    //quick solution id
    int id = 0;

    public TaskService() {
        addTask(new Task(0,"Hacer comida", LocalDateTime.of(2025, Month.FEBRUARY, 1, 0, 0, 0), LocalDateTime.of(2025, Month.FEBRUARY, 1, 0, 0, 0),Priority.MEDIUM,false));
        addTask(new Task(0,"Hacer tarea",LocalDateTime.of(2025, Month.FEBRUARY, 1, 0, 0, 0),LocalDateTime.of(2025, Month.FEBRUARY, 2, 0, 0, 0),Priority.HIGH,true));
        addTask(new Task(0,"Hacer gym",LocalDateTime.of(2025, Month.FEBRUARY, 1, 0, 0, 0),LocalDateTime.of(2025, Month.FEBRUARY, 3, 0, 0, 0),Priority.LOW,false));
        addTask(new Task(0,"Hacer nada",LocalDateTime.of(2025, Month.FEBRUARY, 1, 0, 0, 0),LocalDateTime.of(2025, Month.FEBRUARY, 1, 0, 0, 0),Priority.LOW,false));
        addTask(new Task(0,"gym",LocalDateTime.of(2025, Month.FEBRUARY, 1, 0, 0, 0),LocalDateTime.of(2025, Month.FEBRUARY, 9, 0, 0, 0),Priority.LOW,true));
    }

    public List<Task> getAllTasks(){
        return taskList;
    }

    public void sortTaskByDueDate(){
        taskList.sort(new DueDateTaskComparator());
    }

    public void sortTaskByPriority(){
        taskList.sort(new PriorityTaskComparator());
    }

    public void sortTaskByUrgency(){
        taskList.sort(new UrgentTaskComparator());
    }

    public List<Task> filterTaskByStatus(boolean status){
        return taskList.stream()
                .filter(task -> task.isDone() == status)
                .collect(Collectors.toList());
    }

    public List<Task> filterTaskByText(String text){
        return taskList.stream()
                .filter(task -> task.getText().contains(text))
                .collect(Collectors.toList());
    }

    public List<Task> filterTaskByPriority(Priority priority){
        return taskList.stream()
                .filter(task -> task.getPriority().equals(priority))
                .collect(Collectors.toList());
    }

    public Task addTask(Task task){
        task.setId(id++);
        task.setTimeFrame(Duration.between(task.getCreationDate(),task.getDueDate()));
        taskList.add(task);
        return task;
        // # check what return if something go wrong
    }

    public  Task updateTask(int id,Task task){
        return taskList.set(id,task);
    }

    public Task updateStatusDoneTask(int id){
        Task task = taskList.get(id);
        task.setDone(true);
        return task;
    }
    public Task updateStatusUndoneTask(int id){
        Task task = taskList.get(id);
        task.setDone(false);
        return task;
    }

    public void deleteTask(int id){
        taskList.remove(id);
    }

    public Map<String,Duration> avgTimesAllTask(){
        Map<String,Duration> mapAvgTimes = new HashMap<>();

        for (Task task:taskList){

            avgTotalTime = avgTotalTime.plus(task.getTimeFrame());
            if(task.getPriority() == Priority.LOW){
                avgTimeLowP = avgTimeLowP.plus(task.getTimeFrame());
            } else if (task.getPriority() == Priority.MEDIUM) {
                avgTimeMediumP = avgTimeMediumP.plus(task.getTimeFrame());
            }else{
               avgTimeHighP = avgTimeHighP.plus(task.getTimeFrame());
            }
        }
        mapAvgTimes.put("AvgTotalTime",avgTotalTime);
        mapAvgTimes.put("avgTimeLowPriority",avgTimeLowP);
        mapAvgTimes.put("avgTimeMediumPriority",avgTimeMediumP);
        mapAvgTimes.put("avgTimeHighPriority",avgTimeHighP);

        return mapAvgTimes;
    }




}
