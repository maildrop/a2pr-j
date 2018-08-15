// -*- coding: utf-8; compile-command: "compile.bat"; -*-
// !"#$%&'()*+,-./\abcdefghijklmnopqrstuvwxyz
// 日本語どうですか？¥バックスラッシュと\¥が同時表示できるかどうか？
// utf-8 だと別の文字コードが当てられているはず → できた

package net.shalab.mit.a2pr;

import java.lang.*;
import java.io.*;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ArrayDeque;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

/**
   a2pr の実装のための画面フォーマットルーチン

*/
public class PlainTextLayout{
/*
  TODO 
  ・本文部分のフォントの指定
  ・本文の文字をコードポイント単位で分割しているけど、サロゲートペアの処理
  ・フォントの指定の仕組みをもうチョイ考える必要 font.properties ファイル？
 */
  private PlainTextLayout(){};

  static final ArrayDeque< ArrayList<LineEntity> >
    cocreate( final ArrayList<String> lines,
              final float page_width ,
              final float page_height ,
              final FontRenderContext fontRenderContext ){
    return cocreate( lines, page_width , page_height , 12.0f , fontRenderContext );
  }
  static final ArrayDeque< ArrayList<LineEntity> >
    cocreate( final ArrayList<String> lines ,
              final float page_width ,
              final float page_height,
              final float font_size,
              final FontRenderContext fontRenderContext ){
    assert lines != null;
    assert page_width > 0 ;
    assert page_height > 0 ;
    assert font_size > 0;
    assert fontRenderContext != null;
    final ArrayList< LineEntity >  entities = new ArrayList< LineEntity >();
    // まずは行ごとに分解
    final HashMap<AttributedCharacterIterator.Attribute,Object> attributes = 
      new HashMap<AttributedCharacterIterator.Attribute,Object>();
    attributes.put( java.awt.font.TextAttribute.FAMILY , "Source Code Pro");
    if( font_size > 0 ){
      attributes.put( java.awt.font.TextAttribute.SIZE , Float.valueOf(font_size) );
    }

    final HashMap<AttributedCharacterIterator.Attribute,Object> nonAscii =
      new HashMap<AttributedCharacterIterator.Attribute,Object>(attributes);
    nonAscii.put( java.awt.font.TextAttribute.FAMILY , Font.MONOSPACED );
    
    final HashMap<AttributedCharacterIterator.Attribute,Object> keyword_attributes =
      new HashMap<AttributedCharacterIterator.Attribute,Object>(attributes);
    keyword_attributes.put( java.awt.font.TextAttribute.WEIGHT , java.awt.font.TextAttribute.WEIGHT_ULTRABOLD );
    
    final AttributedString emptyAttributedString = new AttributedString( " " , attributes );

    final String[] keywordlist = {"abstract", "assert" , "boolean" , "break" , "byte" ,
                                  "case" , "catch" , "char" , "class" , /* "const" , */
                                  "continue" , "default" , "do" , "double" , "else" ,
                                  "enum", "extends" ,"final" , "finally", "float" ,
                                  "for", /* "goto" , */ "if", "implements" , "import" ,
                                  "instanceof" ,"int", "insterface" , "long", "native" ,
                                  "new", "package", "private" , "protected" , "public" ,
                                  "return" , "short" , "static" , "strictfp" , "super" ,
                                  "switch", "synchronized", "this", "throw" ,"throws" ,
                                  "transient" , "try" , "void" ,"volatile" , "while" ,
                                  "true", "false" , "null" };
    final java.util.regex.Pattern keyword_pattern =
      java.util.regex.Pattern.compile( new Object(){
          String call(){
            StringBuilder patternString = new StringBuilder();
            for( int i = 0 ; i < keywordlist.length ; ++i ){
              if( i != 0 ){
                patternString.append( "|" );
              }
              patternString.append("(?:\\b");
              patternString.append(keywordlist[i]);
              patternString.append("\\b)" );
            }
            return patternString.toString();
          }
        }.call() );


    final FontTraits sampling_font = new FontTraits( nonAscii  );
    sampling_font.addFirst( "Source Code Pro" );
    
    
    Font sample_font = new Font( attributes );
    int line_number = 0;
    for( String l : lines ){
      ++line_number;

      final AttributedString l_with_attr;

      if( l.length() == 0 ){ // 空文字列の場合は、代案として空白一つを使う
        l_with_attr = emptyAttributedString;
      }else{
        if( false ){
          l_with_attr = new AttributedString( l , attributes );
          for( int i = 0 ; i < l.length(); ++i ){
            if(! sample_font.canDisplay(l.codePointAt( i )) ){
              final int start = i;
              // 表示できない文字が続くのを読み進める
              for(++i; i < l.length() && (!sample_font.canDisplay( l.codePointAt(i) )); ++i ){
                ; /* nothing */
              }
            l_with_attr.addAttributes( nonAscii,start,i );
            }
          }
        }else{
          AttributedString tmp;
          try{
            tmp = sampling_font.createAttributedString( l );
          }catch( java.nio.charset.CharacterCodingException cce ){
            tmp = new AttributedString( l , attributes );
          }
          l_with_attr = tmp;
        }

        
        final java.util.regex.Matcher m = keyword_pattern.matcher( l );
        while( m.find() ){
          l_with_attr.addAttributes( keyword_attributes , m.start(), m.end()  );
        }

      }

      final int str_length = ( l_with_attr == emptyAttributedString ) ? 1 : l.length();
      final LineBreakMeasurer measurer =
        new LineBreakMeasurer( l_with_attr.getIterator(),fontRenderContext );
      while( measurer.getPosition() < str_length ){
        entities.add( new LineEntity( measurer.nextLayout( page_width ), line_number ) );
      }
    }

    final ArrayDeque< ArrayList<LineEntity> > result = new ArrayDeque< ArrayList<LineEntity> >();
    {
      float y = Float.POSITIVE_INFINITY;
      for( LineEntity entity : entities ){
        final float line_height = new Object(){
            float call( LineEntity entity ){
              final TextLayout layout = entity.textLayout;
              return layout.getAscent() + layout.getDescent() + layout.getLeading();
            }
          }.call( entity );
        y+= line_height;
        if(! ( y  < page_height ) ){ // おさまりません
          y = line_height;
          result.addLast( new ArrayList<LineEntity>() );
        }
        result.getLast().add( entity );
      }
    }
    
    return result;
  }

  public static void main( String[] args ){
    String inputFilePath = "PlainTextLayout.java";
    final ArrayList<String> src = new ArrayList<String>();
    try{
      BufferedReader br =
        new BufferedReader( new InputStreamReader( new FileInputStream( inputFilePath ), "utf-8" ) );
      try{
        try{
          for( String line = br.readLine(); line != null ;  line = br.readLine()){
            src.add( line );
          }
          
        }catch( IOException ioe ){
          System.err.println( ioe.toString());
        }
      }finally{
        br.close();
      }
    }catch( FileNotFoundException fne ){
      System.err.println( fne.toString());
    }catch( IOException ioe ){
      System.err.println( ioe.toString());
    }
    final Font bodyFont = new java.awt.Font( java.awt.Font.MONOSPACED , java.awt.Font.PLAIN, 7 );
    java.awt.print.PrinterJob pj = java.awt.print.PrinterJob.getPrinterJob();

    pj.setPrintable( new java.awt.print.Printable(){
        ArrayDeque< ArrayList<LineEntity> > layouts = null;  
        int lastPrintedIndex = -1;
        public int print(final java.awt.Graphics g ,final java.awt.print.PageFormat pf , final int pageIndex )
          throws java.awt.print.PrinterException{
          if( layouts == null ){
            layouts =
              PlainTextLayout.cocreate( src ,
                                        (float)(pf.getImageableWidth()/ 2.0d - 20.0f ),
                                        (float)(pf.getImageableHeight() - 10.0d),
                                        7.0f,
                                        new FontRenderContext(new java.awt.geom.AffineTransform(), false , false ) );
          }
          if( 0 <= pageIndex && pageIndex < (layouts.size() / 2) ){
            java.util.Iterator< ArrayList<LineEntity> > ite = layouts.iterator();
            for(int i =0; i < pageIndex && ite.hasNext(); ++i ){
              ite.next();
              if( ite.hasNext() ){
                ite.next();
              }
            }
            if( ite.hasNext() ){
              final java.awt.Graphics2D g2d = (java.awt.Graphics2D)g;
              if( g2d == null ){
                throw new java.awt.print.PrinterException("g2d == null");
              }
              int lastDrawLineNumber = -1;
              g2d.translate( pf.getImageableX() , pf.getImageableY() );
              {
                g2d.draw( new java.awt.geom.Rectangle2D.Double(1.0d,
                                                               1.0d,
                                                               pf.getImageableWidth()/2.0d ,
                                                               pf.getImageableHeight()-1.0d)  );
                Font oldFont = g2d.getFont();
                try{ // left_column
                  g2d.setFont( bodyFont );
                  float y = 0;
                  for( LineEntity entity : ite.next() ){
                    y +=
                      entity.textLayout.getAscent() + entity.textLayout.getDescent() + entity.textLayout.getLeading();
                    
                    if( lastDrawLineNumber != entity.line_number ){
                      lastDrawLineNumber = entity.line_number;
                      TextLayout layout =
                        new TextLayout( Integer.toString( entity.line_number ),
                                        bodyFont,
                                        g2d.getFontRenderContext() );
                      layout.draw( g2d,((float)( 18.0f ) - layout.getAdvance()) , y);
                    }
                    entity.textLayout.draw( g2d, 20.0f, y );
                  }
                }finally{
                  g2d.setFont( oldFont );
                }
              }
              if( ite.hasNext() ){
                // right_column
                {
                  g2d.draw( new java.awt.geom.Rectangle2D.Double(pf.getImageableWidth()/2.0d+1,
                                                                 1.0d,
                                                                 pf.getImageableWidth()/2.0d-2.0d,
                                                                 pf.getImageableHeight() -1.0d));
                  Font oldFont = g2d.getFont();
                  try{
                    g2d.setFont( bodyFont );
                    float y = 0;
                    for( LineEntity entity : ite.next() ){
                      y +=
                        entity.textLayout.getAscent() + entity.textLayout.getDescent() + entity.textLayout.getLeading();
                      if( true ){
                        if( lastDrawLineNumber != entity.line_number ){
                          lastDrawLineNumber = entity.line_number;
                          TextLayout layout =
                            new TextLayout( Integer.toString( entity.line_number ),
                                            bodyFont,
                                            g2d.getFontRenderContext() );
                          layout.draw( g2d,((float)(pf.getImageableWidth() / 2.0f + 18.0f) - layout.getAdvance()),y );
                        }
                      }
                      // g2d.drawString( String.format("%1$d", entity.line_number ), (float)(pf.getWidth()/2.0f ), y );
                      entity.textLayout.draw( g2d, (float)(pf.getImageableWidth() /2.0f + 18.0f) , y );
                    }
                  }finally{
                    g2d.setFont( oldFont );
                  }
                }
              }
              if( pageIndex != lastPrintedIndex ){
                lastPrintedIndex = pageIndex;
                System.out.print( "[" + pageIndex + "]" );
              }
              return  PAGE_EXISTS;
            }
          }
          return NO_SUCH_PAGE;
        };
      } );

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
    return;
  }
}

