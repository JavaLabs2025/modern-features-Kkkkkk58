package org.lab.repository;

import org.lab.domain.project.Project;

public class ProjectRepository extends InMemoryRepository<Project> {

    public ProjectRepository() {
        super(Project::id);
    }
}
