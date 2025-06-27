package com.nhlstenden.foodorder.command;

import com.nhlstenden.foodorder.model.User;
import com.nhlstenden.foodorder.persistence.UserRepository;

public class DeleteUserCommand implements UserCommand {
    private final UserRepository repo;
    private final User user;

    public DeleteUserCommand(UserRepository repo, User user) {
        this.repo = repo;
        this.user = user;
    }

    @Override
    public void execute() {
        repo.delete(user.getUsername());
    }

    @Override
    public void undo() {
        repo.save(user);
    }
}
