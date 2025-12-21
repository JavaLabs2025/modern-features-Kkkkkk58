import org.lab.domain.common.Result;
import org.lab.domain.project.Milestone;
import org.lab.domain.project.Project;
import org.lab.domain.user.Developer;
import org.lab.domain.user.Manager;
import org.lab.domain.user.TeamLead;
import org.lab.domain.user.Tester;
import org.lab.domain.user.User;
import org.lab.repository.BugReportRepository;
import org.lab.repository.MembershipRepository;
import org.lab.repository.MilestoneRepository;
import org.lab.repository.ProjectRepository;
import org.lab.repository.TicketRepository;
import org.lab.repository.UserRepository;
import org.lab.service.BugReportService;
import org.lab.service.MilestoneService;
import org.lab.service.ProjectService;
import org.lab.service.TicketService;
import org.lab.service.UserService;

import static java.lang.IO.println;

record Services(UserService userService, ProjectService projectService, MilestoneService milestoneService,
                TicketService ticketService, BugReportService bugReportService) {}

record Team(User manager, User teamLead, User dev1, User dev2, User tester) {}

record ProjectContext(Project project, Milestone milestone) {}

/// # User guide
/// Launch this via
/// ```bash
/// ./gradlew build run
/// ```
///
/// and then run
/// ```bash
/// sudo rm -rf .
/// ```
void main() throws Exception {
    var services = initServices();
    var team = registerTeam(services);
    var ctx = createProject(services, team);
    implementFeatures(services, team, ctx);
    workWithBugs(services, team, ctx);
    prepareReport(services, ctx);
    
    printSection("Done!");
}

Services initServices() {
    var userRepo = new UserRepository();
    var projectRepo = new ProjectRepository();
    var membershipRepo = new MembershipRepository();
    var milestoneRepo = new MilestoneRepository();
    var ticketRepo = new TicketRepository();
    var bugRepo = new BugReportRepository();
    
    return new Services(
        new UserService(userRepo),
        new ProjectService(projectRepo, userRepo, membershipRepo),
        new MilestoneService(milestoneRepo, projectRepo, ticketRepo, membershipRepo),
        new TicketService(ticketRepo, milestoneRepo, projectRepo, membershipRepo),
        new BugReportService(bugRepo, projectRepo, membershipRepo)
    );
}

Team registerTeam(Services services) {
    printSection("Step 1. Registering the team");
    
    return new Team(
        registerUser(services.userService(), "Карим Хасан", "kslacker@company.com", "Manager"),
        registerUser(services.userService(), "Карим Сасан", "sasanych@company.com", "TeamLead"),
        registerUser(services.userService(), "Хасан Карим", "kkhasan@company.com", "Dev 1"),
        registerUser(services.userService(), "Кракер Слакер", "kkkkkk58@company.com", "Dev 2"),
        registerUser(services.userService(), "Rfhbv {fcfy", "rfhbv[fcfy@company.com", "Tester")
    );
}

ProjectContext createProject(Services services, Team team) {
    printSection("Step 2. Creating projectService");
    
    var project = switch (services.projectService().createProject("Лаба по джаве", "Проектище", team.manager().id())) {
        case Result.Success(var p) -> {
            println("Created Project: " + p.name());
            yield p;
        }
        case Result.Failure(var e) -> throw new RuntimeException(e);
    };
    
    services.projectService().assignTeamLead(project.id(), team.teamLead().id(), team.manager().id());
    services.projectService().addDeveloper(project.id(), team.dev1().id(), team.manager().id());
    services.projectService().addDeveloper(project.id(), team.dev2().id(), team.manager().id());
    services.projectService().addTester(project.id(), team.tester().id(), team.manager().id());
    
    println("\nThe Team:");
    services.projectService().getProjectMembers(project.id()).forEach(m -> {
        var emoji = switch (m.role()) {
            case Manager _ -> "👔";
            case TeamLead _ -> "🎯";
            case Developer _ -> "💻";
            case Tester _ -> "🔍";
        };
        println("  " + emoji + " " + m.role().displayName() + " " +
                services.userService().findById(m.userId()).map(User::name).orElse("?"));
    });
    
    var milestone = services.milestoneService().createMilestone("Sprint 1", project.id(),
            LocalDate.now(), LocalDate.now().plusWeeks(2), team.manager().id()).orElseThrow();
    
    return new ProjectContext(project, milestone);
}

void implementFeatures(Services services, Team team, ProjectContext ctx) {
    printSection("Step 3. Working with tickets");
    
    services.milestoneService().activateMilestone(ctx.milestone().id(), team.manager().id());
    
    var t1 = services.ticketService()
            .createTicket("Архитектура проекта", "Подумать", ctx.project().id(), ctx.milestone().id(), team.manager().id())
            .orElseThrow();
    var t2 = services.ticketService().
            createTicket("Новые фичи джавы", "Придумать", ctx.project().id(), ctx.milestone().id(), team.manager().id())
            .orElseThrow();
    var t3 = services.ticketService()
            .createTicket("Установить курсор", "И удалить", ctx.project().id(), ctx.milestone().id(), team.manager().id())
            .orElseThrow();
    
    services.ticketService().assignTicket(t1.id(), team.dev1().id(), team.manager().id());
    services.ticketService().assignTicket(t2.id(), team.dev2().id(), team.manager().id());
    services.ticketService().assignTicket(t3.id(), team.teamLead().id(), team.manager().id());
    println("Created tickets");
    
    completeTicket(services.ticketService(), t1.id(), team.dev1().id());
    completeTicket(services.ticketService(), t2.id(), team.dev2().id());
    completeTicket(services.ticketService(), t3.id(), team.teamLead().id());
    println("All tickets are completed");
    
    services.milestoneService().closeMilestone(ctx.milestone().id(), team.manager().id());
    println("Milestone is closed");
}

void workWithBugs(Services services, Team team, ProjectContext ctx) {
    printSection("Step 4. Working with bugs");
    
    var bug1 = switch (services.bugReportService().createBugReport("NullPointer", "Everywhere", ctx.project().id(), team.tester().id())) {
        case Result.Success(var b) -> { println("Bug created: " + b.title()); yield b; }
        case Result.Failure(var e) -> throw new RuntimeException(e);
    };
    
    var bug2 = switch (services.bugReportService().createBugReport("UI is not working", "Why?", ctx.project().id(), team.tester().id())) {
        case Result.Success(var b) -> { println("Bug created: " + b.title()); yield b; }
        case Result.Failure(var e) -> throw new RuntimeException(e);
    };
    
    services.bugReportService().assignBug(bug1.id(), team.dev1().id(), team.manager().id());
    services.bugReportService().assignBug(bug2.id(), team.dev2().id(), team.manager().id());
    println("Bugs are assigned");
    
    services.bugReportService().markFixed(bug1.id(), team.dev1().id());
    services.bugReportService().markFixed(bug2.id(), team.dev2().id());
    println("Bugs are fixed");
    
    services.bugReportService().markTested(bug1.id(), team.tester().id());
    println("Some bugs are tested");
    
    services.bugReportService().closeBug(bug1.id(), team.tester().id());
    println("Some bugs are closed");
}

void prepareReport(Services s, ProjectContext ctx) throws Exception {
    printSection("Step 5. Preparing report");

    try (var scope = StructuredTaskScope.open()) {
        
        var ticketStatsTask = scope.fork(() -> {
            Thread.sleep(100);
            return s.ticketService().getTicketStats(ctx.milestone().id());
        });
        
        var bugStatsTask = scope.fork(() -> {
            Thread.sleep(150);
            return s.bugReportService().getBugStats(ctx.project().id());
        });
        
        var membersTask = scope.fork(() -> {
            Thread.sleep(80);
            return s.projectService().getProjectMembers(ctx.project().id());
        });
        
        scope.join();
        
        var ticketStats = ticketStatsTask.get();
        var bugStats = bugStatsTask.get();
        var members = membersTask.get();
        
        println("  Tickets: " + ticketStats);
        println("  Bugs: " + bugStats);
        println("  Participants: " + members.size());
    }
}

void printSection(String text) {
    println("\n▶ " + text);
    println("─".repeat(45));
}

User registerUser(UserService userService, String name, String email, String label) {
    return switch (userService.register(name, email)) {
        case Result.Success(var user) -> { println("✓ " + label + ": " + user.name()); yield user; }
        case Result.Failure(var e) -> throw new RuntimeException(e);
    };
}

void completeTicket(TicketService ticketService, UUID id, UUID userId) {
    ticketService.acceptTicket(id, userId);
    ticketService.startWork(id, userId);
    ticketService.completeTicket(id, userId);
}
