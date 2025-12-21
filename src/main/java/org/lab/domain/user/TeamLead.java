package org.lab.domain.user;

public record TeamLead() implements Role {

    @Override
    public String displayName() {
        return "TeamLead";
    }

    @Override
    public boolean canManageTickets() {
        return true;
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
