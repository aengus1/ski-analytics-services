package ski.crunch.build;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;

import javax.inject.Inject;

public class DependenciesExtension {
    

        ListProperty<String> dependencies;

        @Inject()
        public DependenciesExtension(ObjectFactory objects) {
            dependencies = objects.listProperty(String.class);
        }

        public ListProperty<String> getDependencies() {
            return dependencies;
        }


    }
