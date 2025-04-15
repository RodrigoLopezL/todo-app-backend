package com.encora.todolist_app.models;

import jakarta.persistence.Entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Task {
    private Integer id;
    private String text;
    private LocalDateTime creationDate;
    private LocalDateTime dueDate;
    private LocalDateTime doneDate;
    private Duration timeFrame;
    private Priority priority;
    private boolean state;

    public Task() {
    }

    public Task(String text, LocalDateTime dueDate, Priority priority) {
    this.text = text;
    this.dueDate = dueDate;
    this.priority = priority;
    this.setState(false);
    }

    public Task(Integer id, String text, LocalDateTime creationDate, LocalDateTime dueDate, LocalDateTime doneDate, Duration timeFrame, Priority priority, boolean state) {
        this.id = id;
        this.text = text;
        this.creationDate = creationDate;
        this.dueDate = dueDate;
        this.doneDate = doneDate;
        this.timeFrame = timeFrame;
        this.priority = priority;
        this.state = state;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getDoneDate() { return doneDate; }

    public void setDoneDate(LocalDateTime doneDate) { this.doneDate = doneDate; }

    public Duration getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(Duration timeFrame) {
        this.timeFrame = timeFrame;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(getId(), task.getId()) && isState() == task.isState() && Objects.equals(getText(), task.getText()) && Objects.equals(getCreationDate(), task.getCreationDate()) && Objects.equals(getDueDate(), task.getDueDate()) && Objects.equals(getTimeFrame(), task.getTimeFrame()) && getPriority() == task.getPriority();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getText(), getCreationDate(), getDueDate(), getTimeFrame(), getPriority(), isState());
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", creationDate=" + creationDate +
                ", dueDate=" + dueDate +
                ", timeFrame=" + timeFrame +
                ", priority=" + priority +
                ", done=" + state +
                '}';
    }
}
