package com.poproject.app;

import javafx.concurrent.Task;
import javafx.stage.Stage;

public abstract class GifferTask<T> extends Task<T> {
    public void updateGifferProgress(double a, double b) {
        updateProgress(a,b);
    }
    public void updateGifferMessage(String s) {updateMessage(s);}
}