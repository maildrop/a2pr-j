package net.shalab.a2pr.test;

import org.apache.commons.cli.*;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;


public final class TestInput {
    private static final java.util.logging.Logger logger =
        java.util.logging.Logger.getLogger("net.shalab.a2pr.test.TestInput");
    // @BeforeAll
    public static void charsetName(){
        logger.info( java.nio.charset.Charset.defaultCharset().name() );
        for( final String available : java.nio.charset.Charset.availableCharsets().keySet() ){
            logger.info( available );
        }
    }
    
    @BeforeAll
    public static void beforeAll(){
        final ClassLoader classLoader = TestInput.class.getClassLoader();
        assertNotNull( classLoader.getResource( "utf-8-unix" ) );
        assertNotNull( classLoader.getResource( "utf-8-unix-with-signature"));
    }
    
    @Test
    public void encodingCheck(){
        final ClassLoader classLoader = TestInput.class.getClassLoader();
        for( final String resourceName : new String[]{"utf-8-unix", "utf-8-unix-with-signature"} ){
            try( final BOMInputStream bomInputStream = new BOMInputStream( classLoader.getResourceAsStream(resourceName) , true ) ){
                if( bomInputStream.hasBOM() ){
                    bomInputStream.skip( bomInputStream.getBOM().length() );
                }

                try( final java.io.Reader reader_ =
                     new java.io.InputStreamReader( bomInputStream ,
                                                    ( bomInputStream.hasBOM() ? java.nio.charset.StandardCharsets.UTF_8 : java.nio.charset.Charset.defaultCharset()));
                     final java.io.BufferedReader reader = new java.io.BufferedReader( reader_)){
                    
                }
                
            }catch( java.io.IOException ioe ){
                fail( ioe );
            }
        }
    }
    
}
