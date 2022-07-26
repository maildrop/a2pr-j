import java.lang.*;
import java.io.*;
import java.util.*;
import java.awt.Font;
import java.awt.font.*;
import java.awt.print.*;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaPrintableArea;

import org.apache.commons.cli.*;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;

import net.shalab.mit.a2pr.*;

import static javax.print.attribute.standard.OrientationRequested.LANDSCAPE;

public final class a2pr{
    private static final java.util.logging.Logger logger =
        java.util.logging.Logger.getLogger("a2pr");

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

    /**
       このクラスはインスタンス化しない
    */
    private a2pr(){};

    public static final boolean checkFileStatus( final File file ){
        assert file != null;
        if( ! file.exists() ){
            System.err.println( file.toString() + " is not found." );
            return false;
        }

        if( ! file.canRead() ){
            System.err.println( file.toString() + " is not readable.");
            return false;
        }

        return true;
    }

    public static final void jobQueueFile( final File file ,
                                           final A2prConfigure configure ,
                                           final java.nio.charset.Charset defaultCharset )
        throws FileNotFoundException, IOException {

        assert file != null;
        assert configure != null;

        if(! checkFileStatus( file ) ){
            return;
        }
        
        final A2prContext context = new A2prContext()
            .setName( file.getName() )
            .setCreationDate( new Date( file.lastModified() ) )
            .setJobDate( new Date() ) ;
        final java.util.ArrayList<String> input = new java.util.ArrayList<>();
        
        try( final BOMInputStream bomInputStream = new BOMInputStream( new FileInputStream( file ) , true )){
            
            if( bomInputStream.hasBOM() ){
                bomInputStream.skip( bomInputStream.getBOM().length() );
            }
            
            try( final java.io.Reader reader_ = new java.io.InputStreamReader( bomInputStream ,
                                                                               ( bomInputStream.hasBOM() ?
                                                                                 java.nio.charset.StandardCharsets.UTF_8 :
                                                                                 defaultCharset  ));
                 final java.io.BufferedReader reader = new java.io.BufferedReader( reader_ ); ){
                
                String line;
                while( (line = reader.readLine() ) != null ){
                    input.add( line );
                }
            }
        }
        
        final java.awt.print.PrinterJob pj = java.awt.print.PrinterJob.getPrinterJob();
        
        pj.setPrintable( new PrintableImplement( input , new A2prConfigure(), context)) ;
        { 
            final javax.print.attribute.PrintRequestAttributeSet attributes =
                new javax.print.attribute.HashPrintRequestAttributeSet();
            attributes.add( javax.print.attribute.standard.OrientationRequested.LANDSCAPE);
            //attributes.add(new javax.print.attribute.standard.JobName(doc.getJobName(), null));
            final float leftMargin   = 8; 
            final float rightMargin  = 8;
            final float topMargin    = 8;
            final float bottomMargin = 8;
            
            final javax.print.attribute.standard.MediaSize mediaSize = javax.print.attribute.standard.MediaSize.ISO.A4;
            attributes.add(new javax.print.attribute.standard.MediaPrintableArea(leftMargin, topMargin,
                                                                                 mediaSize.getX(javax.print.attribute.standard.MediaPrintableArea.MM) - ( leftMargin + rightMargin ),
                                                                                 mediaSize.getY(javax.print.attribute.standard.MediaPrintableArea.MM) - ( topMargin + bottomMargin ),
                                                                                 javax.print.attribute.standard.MediaPrintableArea.MM));
            
            if( pj.printDialog(attributes) ){
                try{
                    pj.print(attributes);
                }catch( java.awt.print.PrinterException pe ){
                    System.err.println( pe.toString());
                }
            }
        }
        return;
    }

    public static final void jobQueueFileList( final java.util.List<String> input_filename_list ,
                                               final A2prConfigure configure ,
                                               final java.nio.charset.Charset defaultCharset ){
        for( final String input_filename : input_filename_list ){
            final File input_file = new File( input_filename );
            if( checkFileStatus( input_file ) ){
                try{
                    jobQueueFile( input_file , configure , defaultCharset );
                }catch( final FileNotFoundException fnfe ){
                    logger.warning( fnfe.toString() );
                }catch( final IOException ioe ){
                    logger.severe( ioe.toString() );
                }
            }
        }
        return;
    }

    
    public static final void main(final String[] args){
        final CommandLineParser commandLineParser = new DefaultParser();
        try{
            final A2prConfigure configure = new A2prConfigure();
            final CommandLine commandLine = commandLineParser.parse( options , args );
            if( commandLine.hasOption( "h" ) ){
                return;
            }

            final java.nio.charset.Charset defaultCharset;

            if( commandLine.hasOption( "e" ) ){
                defaultCharset = java.nio.charset.Charset.forName( commandLine.getOptionValue( "e" ) );
            }else if( commandLine.hasOption("utf-8") ){
                defaultCharset = java.nio.charset.StandardCharsets.UTF_8;
            }else{
                defaultCharset = java.nio.charset.Charset.defaultCharset();
            }

            final var argList = commandLine.getArgList();
            if( commandLine.hasOption( "f" ) ){
                final var theFile = new ArrayList<String>();
                theFile.add( commandLine.getOptionValue( "f" ) );
                jobQueueFileList( theFile , configure , defaultCharset ); 
            }else if( ! argList.isEmpty() ){
                jobQueueFileList( argList , configure , defaultCharset );
            }
            /* 正常終了 */
            return;
        }catch( final org.apache.commons.cli.ParseException pe ){
            logger.severe( pe.toString() );
        }
        return;
    }
}
