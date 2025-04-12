package com.encora.todolist_app.utils.comparators;

import com.encora.todolist_app.models.Task;

import java.util.Comparator;

public class UrgentTaskComparator implements Comparator<Task> {
    @Override
    public int compare(Task t1, Task t2) {
        if(t1.getDueDate()!=null || t2.getDueDate() != null) {
            int dateSoon = t1.getDueDate().compareTo(t2.getDueDate());
            if (dateSoon != 0) {
                return dateSoon;
            }
        }
        return t2.getPriority().compareTo(t1.getPriority());
    }
}
