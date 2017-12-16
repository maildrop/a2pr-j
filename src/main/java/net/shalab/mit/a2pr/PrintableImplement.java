// -*- coding: utf-8; -*-
package net.shalab.mit.a2pr;
import java.lang.*;
import java.util.*;
import java.util.logging.Logger;
import java.awt.Graphics2D;
import java.awt.print.Printable;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import static java.awt.print.Printable.NO_SUCH_PAGE;
import static java.awt.print.Printable.PAGE_EXISTS;

/**

 */
public final class PrintableImplement implements java.awt.print.Printable{
  private static final Logger logger = Logger.getLogger(PrintableImplement.class.getName());
  private final ArrayList<String> src;
  private ArrayList< ArrayList<LineEntity> > layout;
  private final A2prConfigure configure;
  private final A2prContext context;
  private java.awt.font.TextLayout headerText;
  private float header_height;

  private java.awt.font.TextLayout titleText;
  private float title_height;
  
  private java.awt.font.TextLayout footerText ;
  private float footer_height;
  
  public PrintableImplement(final ArrayList<String> src,
                            final A2prConfigure configure ,
                            final A2prContext context){
    this.src = src;
    this.configure = configure;
    this.context = context;
    this.headerText = null;
    this.header_height = 0.0f;
    this.titleText = null;
    this.title_height = 0.0f;
    this.footerText = null;
    this.footer_height = 0.0f;
    layout = null;
  }

  private static final ArrayDeque< ArrayList<LineEntity> >
    doLayout( final ArrayList<String> lines,
              final java.awt.font.FontRenderContext fontRenderContext  ) {
    ArrayDeque< ArrayList<LineEntity> > deque = new ArrayDeque<ArrayList<LineEntity>>();
    
    deque.offerLast( new ArrayList<LineEntity>() );
    return deque;
  }

  private synchronized int getColumnNum(){
    return 2;
  }

  private synchronized float getColumnMargin(){
    return 11.33858268f; // inch, (= 4mm )
  }
  
  private synchronized java.util.Map<java.text.AttributedCharacterIterator.Attribute,Object> getHeaderAttributes(){
    return configure.headerAttributes;
  }

  private synchronized java.util.Map<java.text.AttributedCharacterIterator.Attribute,Object> getTitleAttributes(){
    return configure.titleAttributes;
  }

  private synchronized java.util.Map<java.text.AttributedCharacterIterator.Attribute,Object> getFotterAttributes(){
    return configure.footerAttributes;
  }
  
  private synchronized boolean hasPage(final int pageIndex){
    return ( pageIndex < (int)(Math.ceil( (double)layout.size() / (double)getColumnNum() )) ) ?
      true : false;
  }

  private synchronized void printImplement( final Graphics2D graphics,
                                            final java.awt.print.PageFormat pageFormat ,
                                            final int pageIndex ){
    final Rectangle2D pageRect =
      new Rectangle2D.Double( pageFormat.getImageableX(),pageFormat.getImageableY(),
                              pageFormat.getImageableWidth(),pageFormat.getImageableHeight());
    final float body_heigth =
      ( (float)(pageRect.getHeight())) - (header_height + title_height + footer_height);
    final float body_width = 
      ( ((float)(pageRect.getWidth())) - ( (float)getColumnMargin() * (float)( getColumnNum() - 1 ) ) ) / ((float)getColumnNum()) ;

    
    final java.util.Stack<java.awt.geom.AffineTransform> transform_stack = new java.util.Stack<>();
    transform_stack.push( graphics.getTransform() );

    try{
      graphics.translate( pageRect.getX(), pageRect.getY() );
      // ヘッダの描画
      headerText.draw( graphics ,
                       (float)(pageRect.getWidth() - headerText.getAdvance() - 5.0) , headerText.getAscent() );
      // フッタの描画
      footerText.draw( graphics ,
                       5.0f, (float)pageRect.getHeight() -(  footerText.getDescent() + footerText.getLeading() ) );
      {
        final TextLayout sheet_page_layout =
          new TextLayout( String.format( "%1$d/%2$d" , pageIndex + 1 , (int)Math.ceil((double)layout.size() / 2.0 ) ),
                          getFotterAttributes() , graphics.getFontRenderContext() );
        sheet_page_layout.draw( graphics ,
                                (float)(pageRect.getWidth() - sheet_page_layout.getAdvance() - 5.0f ) ,
                                (float)pageRect.getHeight() -(  footerText.getDescent() + footerText.getLeading() ) );
      }

      for( int col = 0 ; col < getColumnNum() ; ++col ){
        if( pageIndex * getColumnNum() + col < layout.size() ){
          transform_stack.push( graphics.getTransform() );
          try{
            graphics.translate( ( ( body_width + getColumnMargin() ) * (float)col ),
                                header_height );
            {
              final java.awt.Color oldColor = graphics.getColor();
              try{
                graphics.setColor( new java.awt.Color( 0xf0,0xf0,0xf0 ) );
                graphics.fill( new Rectangle2D.Float( 0.f, 0.0f,
                                                      body_width - 1.0f , title_height ) );
              }finally{
                graphics.setColor( oldColor );
              }
            }
            
            graphics.draw( new Rectangle2D.Float( 0.f, 0.0f,
                                                  body_width - 1.0f , title_height ) );
            titleText.draw( graphics, (float)(body_width - titleText.getAdvance() )/2.0f , titleText.getAscent() );
            if( configure.draw_column_page ){ // ページ番号の描画

              final Map<java.text.AttributedCharacterIterator.Attribute,Object> attr = new HashMap<>( getTitleAttributes() );
              attr.put( java.awt.font.TextAttribute.SIZE , Float.valueOf( 9.0f ) );

              final TextLayout column_page_layout = 
                new TextLayout( String.format("Page %1$d/%2$d", pageIndex * getColumnNum() + col + 1, layout.size() ),
                                attr, graphics.getFontRenderContext() );
              column_page_layout.draw( graphics ,
                                       body_width - column_page_layout.getAdvance() - 5.0f,
                                       titleText.getAscent() );
              
              
            }
            graphics.draw( new Rectangle2D.Float( 0,title_height, body_width -1.0f, body_heigth ));


          }finally{
            graphics.setTransform( transform_stack.pop() );
          }

          transform_stack.push( graphics.getTransform() );
          try{
            graphics.translate( ( ( body_width + getColumnMargin() ) * (float)col ),
                                header_height + title_height);
            int lastDrawLineNumber = -1;
            float y = 0.0f;
            final java.awt.Font bodyFont = new java.awt.Font( java.awt.Font.MONOSPACED , java.awt.Font.PLAIN, 7 );
            
            for( LineEntity entity: layout.get( pageIndex*getColumnNum() + col )){
              
              y += entity.textLayout.getAscent() + entity.textLayout.getDescent() + entity.textLayout.getLeading();
              
              if( lastDrawLineNumber != entity.line_number ){
                lastDrawLineNumber = entity.line_number;
                TextLayout layout =
                  new TextLayout( Integer.toString( entity.line_number ),
                                  bodyFont,
                                  graphics.getFontRenderContext() );
                layout.draw( graphics,((float)( 18.0f ) - layout.getAdvance()) , y);
              }
              entity.textLayout.draw( graphics, 20.0f, y );
            }
          }finally{
            graphics.setTransform( transform_stack.pop() );
          }
          
        }
      }
    }finally{
      graphics.setTransform( transform_stack.pop() );
    }
    return;
  }
  
  private synchronized int print( final Graphics2D graphics,
                     final java.awt.print.PageFormat pageFormat,
                     final int pageIndex )
    throws java.awt.print.PrinterException{

    final java.awt.font.FontRenderContext frc =  graphics.getFontRenderContext();
    if( headerText == null ){
      do{
        if( configure.isPrintUserName() ){
          final String userName = System.getProperty( "user.name" );
          if( userName != null ){
            headerText = new java.awt.font.TextLayout( String.format("Printed by %1$s", userName),
                                                       getHeaderAttributes(), frc );
            header_height = (float)( headerText.getAscent() + headerText.getDescent() + headerText.getLeading() );
            break;
          }
        }
        // dummy 
        headerText = new java.awt.font.TextLayout( " ", 
                                                   getHeaderAttributes(), frc );
        header_height = 0.0f;
      }while( false );
    }

    if( titleText == null ){
      titleText = new java.awt.font.TextLayout( context.name == null ? " " : context.name  ,
                                                getTitleAttributes() , frc );
      title_height = (float)( titleText.getAscent() + titleText.getDescent() + titleText.getLeading() );
    }

    if( footerText == null ){
      synchronized( context ){
        java.text.DateFormat format = java.text.DateFormat.getDateTimeInstance( java.text.DateFormat.FULL ,
                                                                                java.text.DateFormat.FULL );
        synchronized( format ){
          footerText =  new java.awt.font.TextLayout( format.format(context.jobDate ) ,
                                                      getFotterAttributes() , frc );
          
        }
      }
      footer_height = (float)( footerText.getAscent() + footerText.getDescent() + footerText.getLeading() );
    }
    
    if( this.layout == null ){ 
      final Rectangle2D pageRect =
        new Rectangle2D.Double( pageFormat.getImageableX(),pageFormat.getImageableY(),
                                pageFormat.getImageableWidth(),pageFormat.getImageableHeight());
      final float body_heigth =
        ( (float)(pageRect.getHeight())) - (header_height + title_height + footer_height);
      final float body_width = 
        ( ((float)(pageRect.getWidth())) - ( (float)getColumnMargin() * (float)( getColumnNum() - 1 ) ) ) / ((float)getColumnNum()) -20.0f;
      this.layout = 
        new ArrayList<ArrayList<LineEntity>> ( PlainTextLayout.cocreate( src ,  body_width ,body_heigth , 7.0f , frc ) );
    }
    if( this.layout != null ){
      if( hasPage( pageIndex ) ){
        printImplement( graphics , pageFormat, pageIndex );
        return PAGE_EXISTS;
      }
    }
    return NO_SUCH_PAGE;
  }

  public synchronized int print( final java.awt.Graphics graphics ,
                    final java.awt.print.PageFormat pageFormat,
                    final int pageIndex )
    throws java.awt.print.PrinterException{
    try{
      if( Graphics2D.class.isInstance( graphics ) ){
        return this.print( Graphics2D.class.cast( graphics ) , pageFormat, pageIndex );
      }
    }catch( java.awt.print.PrinterException pe ){
      logger.warning( pe.toString());
      throw pe;
    }
    return NO_SUCH_PAGE;
  }


  
  public static void main(String[] args ){

    final java.awt.print.PrinterJob pj = java.awt.print.PrinterJob.getPrinterJob();
    String inputFilePath = "PlainTextLayout.java";
    final ArrayList<String> src = new ArrayList<String>();
    try{
      java.io.BufferedReader br =
        new java.io.BufferedReader( new java.io.InputStreamReader( new java.io.FileInputStream( inputFilePath ), "utf-8" ) );
      try{
        try{
          for( String line = br.readLine(); line != null ;  line = br.readLine()){
            src.add( line );
          }
          
        }catch( java.io.IOException ioe ){
          System.err.println( ioe.toString());
        }
      }finally{
        br.close();
      }
    }catch( java.io.FileNotFoundException fne ){
      System.err.println( fne.toString());
    }catch( java.io.IOException ioe ){
      System.err.println( ioe.toString());
    }

    A2prContext context = new A2prContext();
    context.name = inputFilePath;
    context.creationDate = new Date(); // temporary 
    context.jobDate = new Date();
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
  }
}