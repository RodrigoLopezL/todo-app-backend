package com.encora.todolist_app.service;

import com.encora.todolist_app.models.Priority;
import com.encora.todolist_app.models.StateTaskDTO;
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

    private final Map<Integer,Task> taskMap = new HashMap<>();

    //quick solution id
    int id = 0;

    public TaskService() {
//        addTask(new Task(0,"do math homework", LocalDateTime.of(2025, Month.FEBRUARY, 1, 0, 0, 0),Priority.MEDIUM,false));
//        addTask(new Task(0,"do weekly essay",LocalDateTime.of(2025, Month.FEBRUARY, 2, 0, 0, 0),Priority.HIGH,true));
//        addTask(new Task(0,"spark meet",LocalDateTime.of(2025, Month.FEBRUARY, 3, 0, 0, 0),Priority.LOW,false));
//        addTask(new Task(0,"clean something",LocalDateTime.of(2025, Month.FEBRUARY, 1, 0, 0, 0),Priority.LOW,false));
//        addTask(new Task(0,"gym",LocalDateTime.of(2025, Month.FEBRUARY, 9, 0, 0, 0),Priority.LOW,true));
    }

    public List<Task> getAllTasks(){
        return new ArrayList<>(taskMap.values());
    }

    public List<Task> sortTaskByDueDate(){
        List<Task> taskList =  new ArrayList<>(taskMap.values());
        taskList.sort(new DueDateTaskComparator());
        return taskList;
    }

    public List<Task> sortTaskByPriority(){
        List<Task> taskList =  new ArrayList<>(taskMap.values());
        taskList.sort(new PriorityTaskComparator());
        return taskList;

    }

    public List<Task> sortTaskByUrgency(){
        List<Task> taskList =  new ArrayList<>(taskMap.values());
        taskList.sort(new UrgentTaskComparator());
        return taskList;

    }

    public List<Task> filterTaskByStatus(boolean status){
        List<Task> taskList =  new ArrayList<>(taskMap.values());
        return taskList.stream()
                .filter(task -> task.isState() == status)
                .collect(Collectors.toList());
    }

    public List<Task> filterTaskByText(String text){
        List<Task> taskList =  new ArrayList<>(taskMap.values());
        return taskList.stream()
                .filter(task -> task.getText().contains(text))
                .collect(Collectors.toList());
    }

    public List<Task> filterTaskByPriority(Priority priority){
        List<Task> taskList =  new ArrayList<>(taskMap.values());
        return taskList.stream()
                .filter(task -> task.getPriority().equals(priority))
                .collect(Collectors.toList());
    }

    public Task addTask(Task task){
        task.setId(id);
        task.setCreationDate(LocalDateTime.now());
        taskMap.put(id,task);
        id++;
        return task;
    }

    public  Task updateTask(int id,Task task){
        taskMap.replace(id,task);
        return task;
    }

    public StateTaskDTO updateStatusDoneTask(int id){
        Task task = taskMap.get(id);
        if(task != null){
            task.setState(true);
            task.setDoneDate(LocalDateTime.now());
            task.setTimeFrame(Duration.between(task.getCreationDate(),task.getDoneDate()));
            taskMap.replace(id,task);
            return new StateTaskDTO(task.getId(),task.isState());
        }
        return null;
    }
    public StateTaskDTO updateStatusUndoneTask(int id){
        Task task = taskMap.get(id);
        if(task != null){
            task.setState(false);
            task.setDoneDate(null);
            task.setTimeFrame(null);
            taskMap.replace(id,task);
            return new StateTaskDTO(task.getId(),task.isState());
        }
        return null;
    }

    public void deleteTask(int id){
        taskMap.remove(id);
    }

    public Map<String,Duration> avgTimesAllTask(){
        Map<String,Duration> mapAvgTimes = new HashMap<>();
        Duration avgTotalTime = Duration.ZERO;
        Duration avgTimeLowP = Duration.ZERO;
        Duration avgTimeMediumP = Duration.ZERO;
        Duration avgTimeHighP = Duration.ZERO;

        int totalTask= taskMap.size();
        int amountLowTask = 0;
        int amountMediumTask = 0;
        int amountHighTask=0;

        for (Map.Entry<Integer, Task> entry : taskMap.entrySet()) {
            Task task = entry.getValue();
            if (task.isState()){
                avgTotalTime = avgTotalTime.plus(task.getTimeFrame());
                if(task.getPriority() == Priority.LOW){
                    avgTimeLowP = avgTimeLowP.plus(task.getTimeFrame());
                    amountLowTask++;
                } else if (task.getPriority() == Priority.MEDIUM) {
                    avgTimeMediumP = avgTimeMediumP.plus(task.getTimeFrame());
                    amountMediumTask++;
                }else{
                   avgTimeHighP = avgTimeHighP.plus(task.getTimeFrame());
                   amountHighTask++;
                }
            }
        }

        mapAvgTimes.put("AvgTotalTime",totalTask != 0 ? avgTotalTime.dividedBy(totalTask) : Duration.ZERO);
        mapAvgTimes.put("avgTimeLowPriority",amountLowTask != 0 ? avgTimeLowP.dividedBy(amountLowTask): Duration.ZERO);
        mapAvgTimes.put("avgTimeMediumPriority",amountMediumTask != 0? avgTimeMediumP.dividedBy(amountMediumTask): Duration.ZERO);
        mapAvgTimes.put("avgTimeHighPriority",amountHighTask != 0? avgTimeHighP.dividedBy(amountHighTask): Duration.ZERO);

        return mapAvgTimes;
    }




}
