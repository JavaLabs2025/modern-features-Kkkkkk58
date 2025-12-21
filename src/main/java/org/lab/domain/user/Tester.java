package org.lab.domain.user;

public record Tester() implements Role {

    @Override
    public String displayName() {
        return "Tester";
    }

    @Override
    public boolean canManageTickets() {
        return false;
    }

    @Override
    public boolean canExecuteTickets() {
        return false;
    }

    @Override
    public boolean canCreateBugReports() {
        return true;
    }

    @Override
    public boolean canFixBugs() {
        return false;
    }

    @Override
    public boolean canTestBugs() {
        return true;
    }
}
