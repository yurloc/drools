package org.drools.integrationtests;

import org.drools.Message;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.KieServices;
import org.kie.builder.IncrementalResults;
import org.kie.builder.InternalKieBuilder;
import org.kie.builder.KieBuilder;
import org.kie.builder.KieFileSystem;
import org.kie.runtime.KieContainer;
import org.kie.runtime.KieSession;

import static junit.framework.Assert.assertEquals;

public class IncrementalCompilationTest {

    @Test
    public void testIncrementalCompilation() throws Exception {
        String drl1 = "package org.drools\n" +
                "rule R1 when\n" +
                "   $m : Message()\n" +
                "then\n" +
                "end\n";

        String drl2_1 = "package org.drools\n" +
                "rule R2_1 when\n" +
                "   $m : Message( message == \"Hi Universe\" )\n" +
                "then\n" +
                "end\n";

        String drl2_2 = "package org.drools\n" +
                "rule R2_2 when\n" +
                "   $m : Message( message == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        KieServices ks = KieServices.Factory.get();

        KieFileSystem kfs = ks.newKieFileSystem()
                .write("src/main/resources/r1.drl", drl1)
                .write("src/main/resources/r2.drl", drl2_1);

        KieBuilder kieBuilder = ks.newKieBuilder( kfs ).buildAll();
        KieContainer kieContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());

        KieSession ksession = kieContainer.newKieSession();
        ksession.insert(new Message("Hello World"));
        assertEquals( 1, ksession.fireAllRules() );

        kfs.write("src/main/resources/r2.drl", drl2_2);
        ((InternalKieBuilder)kieBuilder).createFileSet("src/main/resources/r2.drl").build();

        kieContainer.updateToVersion(ks.getRepository().getDefaultReleaseId());
        ksession = kieContainer.newKieSession();
        ksession.insert(new Message("Hello World"));
        assertEquals( 2, ksession.fireAllRules() );
    }

    @Test
    public void testDeletedFile() throws Exception {
        String drl1 = "package org.drools\n" +
                "rule R1 when\n" +
                "   $m : Message()\n" +
                "then\n" +
                "end\n";

        String drl2 = "package org.drools\n" +
                "rule R2_2 when\n" +
                "   $m : Message( message == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        KieServices ks = KieServices.Factory.get();

        KieFileSystem kfs = ks.newKieFileSystem()
                .write("src/main/resources/r1.drl", drl1)
                .write("src/main/resources/r2.drl", drl2);

        KieBuilder kieBuilder = ks.newKieBuilder( kfs ).buildAll();
        KieContainer kieContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());

        KieSession ksession = kieContainer.newKieSession();
        ksession.insert(new Message("Hello World"));
        assertEquals( 2, ksession.fireAllRules() );

        kfs.delete("src/main/resources/r1.drl");
        ((InternalKieBuilder)kieBuilder).createFileSet("src/main/resources/r1.drl").build();

        kieContainer.updateToVersion(ks.getRepository().getDefaultReleaseId());
        ksession = kieContainer.newKieSession();
        ksession.insert(new Message("Hello World"));
        assertEquals( 1, ksession.fireAllRules() );
    }

    @Test
    public void testIncrementalCompilationWithAddedError() throws Exception {
        String drl1 = "package org.drools\n" +
                "rule R1 when\n" +
                "   $m : Message()\n" +
                "then\n" +
                "end\n";

        String drl2_1 = "package org.drools\n" +
                "rule R2_1 when\n" +
                "   $m : Message( message == \"Hi Universe\" )\n" +
                "then\n" +
                "end\n";

        String drl2_2 = "package org.drools\n" +
                "rule R2_2 when\n" +
                "   $m : Message( mesage == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        KieServices ks = KieServices.Factory.get();

        KieFileSystem kfs = ks.newKieFileSystem()
                .write("src/main/resources/r1.drl", drl1)
                .write("src/main/resources/r2.drl", drl2_1);

        KieBuilder kieBuilder = ks.newKieBuilder( kfs ).buildAll();
        KieContainer kieContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());

        KieSession ksession = kieContainer.newKieSession();
        ksession.insert(new Message("Hello World"));
        assertEquals( 1, ksession.fireAllRules() );

        kfs.write("src/main/resources/r2.drl", drl2_2);
        IncrementalResults results = ((InternalKieBuilder) kieBuilder).createFileSet("src/main/resources/r2.drl").build();

        assertEquals(1, results.getAddedMessages().size());
        assertEquals(0, results.getRemovedMessages().size());

        kieContainer.updateToVersion(ks.getRepository().getDefaultReleaseId());
        ksession = kieContainer.newKieSession();
        ksession.insert(new Message("Hello World"));
        assertEquals( 1, ksession.fireAllRules() );
    }

    @Test
    public void testIncrementalCompilationWithRemovedError() throws Exception {
        String drl1 = "package org.drools\n" +
                "rule R1 when\n" +
                "   $m : Message()\n" +
                "then\n" +
                "end\n";

        String drl2_1 = "package org.drools\n" +
                "rule R2_1 when\n" +
                "   $m : Message( mesage == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        String drl2_2 = "package org.drools\n" +
                "rule R2_2 when\n" +
                "   $m : Message( message == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        KieServices ks = KieServices.Factory.get();

        KieFileSystem kfs = ks.newKieFileSystem()
                .write("src/main/resources/r1.drl", drl1)
                .write("src/main/resources/r2.drl", drl2_1);

        KieBuilder kieBuilder = ks.newKieBuilder( kfs ).buildAll();
        assertEquals( 1, kieBuilder.getResults().getMessages(org.kie.builder.Message.Level.ERROR).size() );

        kfs.write("src/main/resources/r2.drl", drl2_2);
        IncrementalResults results = ((InternalKieBuilder) kieBuilder).createFileSet("src/main/resources/r2.drl").build();

        assertEquals( 0, results.getAddedMessages().size() );
        assertEquals( 1, results.getRemovedMessages().size() );

        KieContainer kieContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
        KieSession ksession = kieContainer.newKieSession();
        ksession.insert(new Message("Hello World"));
        assertEquals( 2, ksession.fireAllRules() );
    }

    @Test
    @Ignore("Successive uses of incremental builds does not return expect messages")
    public void testIncrementalCompilationAddErrorThenRemoveError() throws Exception {
        //Valid
        String drl1 = "package org.drools\n" +
                "rule R1 when\n" +
                "   $m : Message()\n" +
                "then\n" +
                "end\n";

        //Field is unknown ("mesage" not "message")
        String drl2_1 = "package org.drools\n" +
                "rule R2_1 when\n" +
                "   $m : Message( mesage == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        //Valid
        String drl2_2 = "package org.drools\n" +
                "rule R2_2 when\n" +
                "   $m : Message( message == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        KieServices ks = KieServices.Factory.get();

        KieFileSystem kfs = ks.newKieFileSystem()
                .write( "src/main/resources/r1.drl", drl1 );

        KieBuilder kieBuilder = ks.newKieBuilder( kfs ).buildAll();
        assertEquals( 0, kieBuilder.getResults().getMessages( org.kie.builder.Message.Level.ERROR ).size() );

        //Add file with error - expect 1 "added" error message
        kfs.write( "src/main/resources/r2.drl", drl2_1 );
        IncrementalResults addResults = ( (InternalKieBuilder) kieBuilder ).createFileSet( "src/main/resources/r2.drl" ).build();

        assertEquals( 1, addResults.getAddedMessages().size() );
        assertEquals( 0, addResults.getRemovedMessages().size() );

        //Update flawed file with correct version - expect 0 "added" error messages and removal of 1 previous error
        kfs.write( "src/main/resources/r2.drl", drl2_2 );
        IncrementalResults removeResults = ( (InternalKieBuilder) kieBuilder ).createFileSet( "src/main/resources/r2.drl" ).build();

        assertEquals( 0, removeResults.getAddedMessages().size() );
        assertEquals( 1, removeResults.getRemovedMessages().size() );
    }

    @Test
    @Ignore("Successive uses of incremental builds does not return expect messages")
    public void testIncrementalCompilationAddTwoErrorsThenRemove1Error() throws Exception {
        //Fact Type is unknown ("Mesage" not "Message")
        String drl1 = "package org.drools\n" +
                "rule R1 when\n" +
                "   $m : Mesage()\n" +
                "then\n" +
                "end\n";

        //Field is unknown ("mesage" not "message")
        String drl2_1 = "package org.drools\n" +
                "rule R2_1 when\n" +
                "   $m : Message( mesage == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        //Valid
        String drl2_2 = "package org.drools\n" +
                "rule R2_2 when\n" +
                "   $m : Message( message == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        KieServices ks = KieServices.Factory.get();

        KieFileSystem kfs = ks.newKieFileSystem()
                .write( "src/main/resources/r1.drl", drl1 );

        //Initial file contains errors
        KieBuilder kieBuilder = ks.newKieBuilder( kfs ).buildAll();
        assertEquals( 1, kieBuilder.getResults().getMessages( org.kie.builder.Message.Level.ERROR ).size() );

        //Add file with error - expect 1 "added" error message
        kfs.write( "src/main/resources/r2.drl", drl2_1  );
        IncrementalResults addResults = ( (InternalKieBuilder) kieBuilder ).createFileSet( "src/main/resources/r2.drl" ).build();

        assertEquals( 1, addResults.getAddedMessages().size() );
        assertEquals( 0, addResults.getRemovedMessages().size() );

        //Update flawed file with correct version - expect 0 "added" error messages and removal of 1 previous error relating to updated file
        kfs.write( "src/main/resources/r2.drl", drl2_2 );
        IncrementalResults removeResults = ( (InternalKieBuilder) kieBuilder ).createFileSet( "src/main/resources/r2.drl" ).build();

        assertEquals( 0, removeResults.getAddedMessages().size() );
        assertEquals( 1, removeResults.getRemovedMessages().size() );
    }

}
