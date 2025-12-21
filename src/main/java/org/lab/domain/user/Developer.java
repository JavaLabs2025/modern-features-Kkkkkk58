package org.lab.domain.user;

public record Developer() implements Role {

    @Override
    public String displayName() {
        return "Developer";
    }

    @Override
    public boolean canManageTickets() {
        return false;
    }

    @Override
    public boolean canExecuteTickets() {
        return true;
    }

    @Override
    public boolean canCreateBugReports() {
        return true;
    }

    @Override
    public boolean canFixBugs() {
        return true;
    }

    @Override
    public boolean canTestBugs() {
        return false;
    }
}
