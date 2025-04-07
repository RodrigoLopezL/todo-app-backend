package com.encora.todolist_app.utils.comparators;

import com.encora.todolist_app.models.Task;

import java.util.Comparator;

public class UrgentTaskComparator implements Comparator<Task> {
    @Override
    public int compare(Task t1, Task t2) {
        int datesoon = t1.getDueDate().compareTo(t2.getDueDate());
        if(datesoon !=0 ){
            return datesoon;
        }
        return t2.getPriority().compareTo(t1.getPriority());
    }
}
