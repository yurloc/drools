package org.drools.compiler.kproject.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryFileSystemTest {

    private static final Logger LOG = LoggerFactory.getLogger( MemoryFileSystemTest.class );

    @Test
    public void testWriteThreadSafety() throws InterruptedException, ExecutionException, TimeoutException {
        final MemoryFileSystem mfs = new MemoryFileSystem();
        final ExecutorService es = Executors.newCachedThreadPool();
        final List<String> paths = new ArrayList<String>();
        for ( int i = 0; i < 10; i++ ) {
            paths.add( "/dir/test" + i );
        }
        final String resName = "a/b/c/x";
        final List<Runnable> tasks = new ArrayList<Runnable>();
        final List<Future<?>> results = new ArrayList<Future<?>>();
        int threads = 17;
        for ( int i = 0; i < threads; i++ ) {
            final int id = i;
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Random rnd = new Random( id );
                    byte[] bytes = new byte[64];
                    rnd.nextBytes( bytes );
                    for ( int j = 0; j < 100000; j++ ) {
//                        mfs.write( paths.get( rnd.nextInt( paths.size() ) ), bytes, true );
                        mfs.write( rnd.nextInt( 100 ) + "/test", bytes, true );
                    }
                    LOG.info( "Thread {} finished writing.", id );
                }
            };
            tasks.add( task );
        }

        for ( int i = 0; i < threads; i++ ) {
            results.add( es.submit( tasks.get( i ) ) );
        }

        for ( int i = 0; i < threads; i++ ) {
            results.get( i ).get( 10000, TimeUnit.MILLISECONDS );
        }
    }
}
