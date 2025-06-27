package com.nhlstenden.foodorder.command;

public interface Command {
    void execute();
    void undo();
}
