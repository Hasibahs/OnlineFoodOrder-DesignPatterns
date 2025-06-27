package com.nhlstenden.foodorder.command;

import com.nhlstenden.foodorder.model.User;
import com.nhlstenden.foodorder.persistence.UserRepository;

public class ChangeUserRoleCommand implements UserCommand {
    private final UserRepository repo;
    private final User user;
    private final String newRole;
    private final String oldRole;

    public ChangeUserRoleCommand(UserRepository repo, User user, String newRole) {
        this.repo = repo;
        this.user = user;
        this.newRole = newRole;
        this.oldRole = user.getRole();
    }

    @Override
    public void execute() {
        user.setRole(newRole);
        repo.update(user);
    }

    @Override
    public void undo() {
        user.setRole(oldRole);
        repo.update(user);
    }
}
