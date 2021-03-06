package demoapp.dom.types.javalang.booleans.jdo;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class WrapperBooleanJdoSeedService extends SeedServiceAbstract {

    public WrapperBooleanJdoSeedService() {
        super(WrapperBooleanJdoEntityFixture::new);
    }

    static class WrapperBooleanJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(WrapperBooleanJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });
        }

        @Inject
        RepositoryService repositoryService;

        @Inject
        Samples<Boolean> samples;
    }
}
