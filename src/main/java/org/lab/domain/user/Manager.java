package org.lab.domain.user;

public record Manager() implements Role {

    @Override
    public String displayName() {
        return "Manager";
    }

    @Override
    public boolean canManageTickets() {
        return true;
    }

    @Override
    public boolean canExecuteTickets() {
        return false;
    }

    @Override
    public boolean canCreateBugReports() {
        return false;
    }

    @Override
    public boolean canFixBugs() {
        return false;
    }

    @Override
    public boolean canTestBugs() {
        return false;
    }
}
