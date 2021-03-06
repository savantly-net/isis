package demoapp.dom.types.javamath.bigintegers.jdo;

import java.math.BigInteger;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class JavaMathBigIntegerJdoSeedService extends SeedServiceAbstract {

    public JavaMathBigIntegerJdoSeedService() {
        super(JavaMathBigIntegerJdoEntityFixture::new);
    }

    static class JavaMathBigIntegerJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(JavaMathBigIntegerJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });
        }

        @Inject
        RepositoryService repositoryService;

        @Inject
        Samples<BigInteger> samples;

    }
}
