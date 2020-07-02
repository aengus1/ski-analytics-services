package ski.crunch.build;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.Iterator;

/**
 * Gradle plugin that allows deploy dependencies to be defined in build files.
 *  When the deploy task is called on a module, the default behaviour that this plugin defines is to deploy all the
 *  other modules specified in deployDependencies.dependencies = ['my module'] prior to deploying this module.
 *
 *  TODO
 *  Gradle  will only deploy modules that have been changed since previous deploy.  To override this behaviour pass one
 *  of  the following system properties:
 *
 *  If the system property 'full' is passed in, then all modules will be redeployed by default.
 *  If the system property 'vcsIncremental' is passed in, then all the modules with changes in their source directory
 *  or serverless file, since the previous commit will be deployed.  (not yet implemented)
 *
 */
public class Deploy implements Plugin<Project> {


    @Override
    public void apply(Project target) {
        Task targetDeployTask = target.getTasksByName("deploy", false).iterator().next();
        DependenciesExtension extension = target.getExtensions().create("deploy", DependenciesExtension.class, target.getObjects());

        target.afterEvaluate(cl -> {
            if (target.getProperties().containsKey("full")) {
                System.out.println("full passed");
            } else if (target.getProperties().containsKey("vcs-incremental")) {
                System.out.println("vcs passed");
            } else {
                Iterator<Project> projectsIterator = target.getParent().getAllprojects().iterator();
                projectsIterator.forEachRemaining(proj -> {
                        if (!proj.getName().equals(target.getName()) && !target.getName().equals("cli")) {
                            if (extension.getDependencies().get().contains(proj.getName())) {
                                Task deployTask = proj.getTasksByName("deploy", false).iterator().next();
                                targetDeployTask.dependsOn(deployTask);
                            }
                        }
                });
            }
        });

    }

}

