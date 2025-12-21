package org.lab.domain.user;

public sealed interface Role permits Manager, TeamLead, Developer, Tester {

    String displayName();

    boolean canManageTickets();

    boolean canExecuteTickets();

    boolean canCreateBugReports();

    boolean canFixBugs();

    boolean canTestBugs();
}
