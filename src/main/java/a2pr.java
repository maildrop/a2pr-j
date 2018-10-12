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
        {
          context.name = file.getName();
          context.creationDate = new Date(file.lastModified() );
          context.jobDate = new Date();
        }
        
        final ArrayList<String> src = new ArrayList<>();

        try( final FileInputStream fileInputStream = new FileInputStream( file ) ){
          // Byte Order Mark 対応
          try( final BufferedInputStream inputStream = new BufferedInputStream( fileInputStream ) ){
            final String streamCharacterEncode ; // 当該のファイルの使っているエンコード
            final boolean hasByteOrderMark; // UTF-8のバイトオーダーマークが存在する場合は true 無い場合は false

            if( 3 <= file.length() ){ 
              byte bom_mark[] = new byte[3];
              inputStream.mark(bom_mark.length);
              {
                final int read_size = inputStream.read( bom_mark , 0 , bom_mark.length );

                if( false ){
                  System.out.println( String.format( "read_size = %1$d : [0] = %2$s , [1] = %3$s , [2] = %4$s  , %5$s",
                                                     read_size,
                                                     Integer.toHexString( Byte.toUnsignedInt( bom_mark[0] ) ),
                                                     Integer.toHexString( Byte.toUnsignedInt( bom_mark[1] ) ),
                                                     Integer.toHexString( Byte.toUnsignedInt( bom_mark[2] ) ),
                                                     ( Byte.toUnsignedInt(bom_mark[0]) == 0xEF &&
                                                       Byte.toUnsignedInt(bom_mark[1]) == 0xBB &&
                                                       Byte.toUnsignedInt(bom_mark[2]) == 0xBF ) ? "true" : "false"
                                                     ));
                }
                
                if( read_size == bom_mark.length ){
                  hasByteOrderMark = ( Byte.toUnsignedInt(bom_mark[0]) == 0xEF &&
                                       Byte.toUnsignedInt(bom_mark[1]) == 0xBB &&
                                       Byte.toUnsignedInt(bom_mark[2]) == 0xBF ) ? true : false;
                }else{
                  hasByteOrderMark = false;
                }
                  
                if( hasByteOrderMark ){ 
                  streamCharacterEncode = "utf-8";
                }else{
                  // using file.encoding property.
                  streamCharacterEncode = java.lang.System.getProperty( "file.encoding" );
                  inputStream.reset();
                }
              }
                
            }else{ // 3byte 未満はわからないので file.encoding をそのまま使うことにする
              streamCharacterEncode = java.lang.System.getProperty( "file.encoding" );
              hasByteOrderMark = false;
            }

            if( false ){
              System.out.println( String.format( "encoding = %1$s %2$s" , streamCharacterEncode , hasByteOrderMark ? ", hasByteOrder" : "" ));
            }
            
            try(final InputStreamReader inputStreamReader = new InputStreamReader( inputStream , streamCharacterEncode );
                final BufferedReader bufferedReader = new BufferedReader( inputStreamReader ) ){
              String line;
              while( (line = bufferedReader.readLine() ) != null ){
                src.add( line );
              }
            }
            
          } // end of try inputStream
        } // end of try fileInputStream
          
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

