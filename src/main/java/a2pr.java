// -*- compile-command: "javac -g -encoding utf-8 a2pr.java"; -*-

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

import net.shalab.mit.a2pr.*;
import static javax.print.attribute.standard.OrientationRequested.LANDSCAPE;

public final class a2pr{
  private a2pr(){};

  public static void jobQueueFileList(final String[] srcs,final A2prConfigure configure){
    assert( srcs != null );
    assert( configure != null );
    
    for( String path : srcs ){
      try{
        File file = new File( path ) ;
        if( ! file.exists() ){
          System.err.println( file.toString() + " is not found." );
          continue;
        }
        if( ! file.canRead() ){
          System.err.println( file.toString() + " is not readable.");
          continue;
        }

        final A2prContext context = new A2prContext();
        context.name = file.getName();
        context.creationDate = new Date(file.lastModified() );
        context.jobDate = new Date();
        
        final ArrayList<String> src = new ArrayList<>();
        try(
            final FileInputStream fileInputStream = new FileInputStream( file );
            final InputStreamReader inputStreamReader = new InputStreamReader( fileInputStream , "utf-8" );
            final BufferedReader bufferedReader = new BufferedReader( inputStreamReader ) ){
          String line;
          while( (line = bufferedReader.readLine() ) != null ){
            src.add( line );
          }
        }
        final java.awt.print.PrinterJob pj = java.awt.print.PrinterJob.getPrinterJob();

        pj.setPrintable( new PrintableImplement( src , new A2prConfigure(), context)) ;
        { 
          javax.print.attribute.PrintRequestAttributeSet attributes =
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
      }catch( java.io.FileNotFoundException fne ){
        System.err.println( fne.toString());
      }catch( java.io.IOException ioe ){
        System.err.println( ioe.toString());
      }
    }
  }
  
  public static void main(String[] args){
    jobQueueFileList( args , new A2prConfigure() );
    return;
  }
};

