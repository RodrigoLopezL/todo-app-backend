package com.encora.todolist_app.models;

public class StateTaskDTO {
    private int id;
    private boolean status;

    public StateTaskDTO(int id, boolean status) {
        this.id = id;
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
