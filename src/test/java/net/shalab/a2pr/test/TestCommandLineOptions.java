package net.shalab.a2pr.test;

import java.lang.*;
import java.nio.charset.Charset;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;


public final class TestCommandLineOptions {

    public static final org.apache.commons.cli.Options options =
        new Object(){
            final org.apache.commons.cli.Options cocreate(){
                final var options = new org.apache.commons.cli.Options();
                options.addOption( "f" , "file" , true , "target file" );
                options.addOption( "e" , "encoding" , true , "specify a file encoding" );
                options.addOption( null, "utf-8" , false , "specify a file encoding as utf-8" );
                options.addOption( "h" , "help" , false , "display this help message" );
                return options;
            }
        }.cocreate();

    @Test
    public void helloWorld(){
        assertEquals( "hello world" , "hello world" );
    }
        
    @Test
    public void testCommandLineArgs1(){
        final String args[] = new String[]{"-h" , "--file", "inputfile.txt", "help help help" , "-e" , "utf-8" , "--utf-8" };
        java.nio.charset.Charset charset = java.nio.charset.Charset.defaultCharset();
        
        final CommandLineParser commandLineParser = new DefaultParser();
        try{
            final CommandLine commandLine = commandLineParser.parse( options , args );
            assertTrue( commandLine.hasOption( "h" ) );
            assertTrue( commandLine.hasOption( "f" ) );
            assertTrue( commandLine.hasOption( "utf-8" ));

            do{ // encoding 
                if( commandLine.hasOption( "e" ) ){
                    final String e_option_value = StringUtils.trimToEmpty( commandLine.getOptionValue( "e" ) ) ;
                    assertTrue( java.nio.charset.Charset.isSupported( e_option_value ));
                    if( java.nio.charset.Charset.isSupported( e_option_value )){
                        charset = java.nio.charset.Charset.forName( e_option_value );
                        break;
                    }
                }
                
                if( commandLine.hasOption( "utf-8" ) ){
                    charset = java.nio.charset.StandardCharsets.UTF_8;
                    break;
                }
            }while( false );

            // input 
            if( commandLine.hasOption( "f" ) ){
                final String f_value = commandLine.getOptionValue( "f" ) ;
                assertNotNull( f_value );
                if( f_value != null){
                    assertEquals( f_value , "inputfile.txt" );
                }
            }else{
                for( final String arg : commandLine.getArgList() ){
                    System.out.println( arg );
                }
            }
        }catch( final org.apache.commons.cli.ParseException pe ){
            fail( pe );
        }
    }
}
