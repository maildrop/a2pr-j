// -*- coding: utf-8; -*-
package net.shalab.mit.a2pr;
import java.lang.*;
import java.io.*;
import java.awt.Font;
import java.awt.font.*;
import java.awt.print.*;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaPrintableArea;

import static javax.print.attribute.standard.OrientationRequested.LANDSCAPE;

public final class A2prConfigure{
  
  javax.print.attribute.standard.MediaSize mediaSize;
  float     column_margin;
  int       line_numbers; 
  String    bodyFontName;
  String    alternateBodyFontName;
  int       bodyFontStyle;
  float     bodyFontSize;
  
  boolean   draw_column_page;
  boolean   draw_user_name;

  java.util.Map<java.text.AttributedCharacterIterator.Attribute,Object> headerAttributes;
  java.util.Map<java.text.AttributedCharacterIterator.Attribute,Object> titleAttributes;
  java.util.Map<java.text.AttributedCharacterIterator.Attribute,Object> footerAttributes;
  java.util.Map<java.text.AttributedCharacterIterator.Attribute,Object> lineNumberAttribues;
  
  public A2prConfigure(){
    mediaSize     = MediaSize.ISO.A4;
    column_margin = 11.33858268f; // inch, (= 4mm )
    line_numbers  = 1;
    
    bodyFontName          = Font.MONOSPACED;
    alternateBodyFontName = Font.MONOSPACED;
    bodyFontStyle         = Font.PLAIN;
    bodyFontSize          = 8.0f;
    
    headerAttributes = new java.util.HashMap<>();
    {
      headerAttributes.put( java.awt.font.TextAttribute.FAMILY , Font.SERIF );
      headerAttributes.put( java.awt.font.TextAttribute.SIZE , Float.valueOf( 10.0f ) );
      headerAttributes.put( java.awt.font.TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR );
    }
    titleAttributes = new java.util.HashMap<>();
    {
      titleAttributes.put( java.awt.font.TextAttribute.FAMILY , Font.SANS_SERIF );
      titleAttributes.put( java.awt.font.TextAttribute.SIZE , Float.valueOf( 12.0f ) );
      titleAttributes.put( java.awt.font.TextAttribute.POSTURE , TextAttribute.POSTURE_REGULAR );
    }
    
    footerAttributes = new java.util.HashMap<>();
    {
      footerAttributes.put( java.awt.font.TextAttribute.FAMILY , Font.SERIF );
      footerAttributes.put( java.awt.font.TextAttribute.SIZE , Float.valueOf( 10.0f ) );
      footerAttributes.put( java.awt.font.TextAttribute.POSTURE , TextAttribute.POSTURE_REGULAR );
    }
    
    lineNumberAttribues = new java.util.HashMap<>();
    {
      lineNumberAttribues.put( java.awt.font.TextAttribute.FAMILY , Font.SERIF );
      lineNumberAttribues.put( java.awt.font.TextAttribute.POSTURE , TextAttribute.POSTURE_REGULAR );
    }

    this.draw_column_page = true;
    this.draw_user_name = true;
  }
  
  private final MediaPrintableArea getDefaultMediaPrintableArea(){
    final float leftMargin   = 8; 
    final float rightMargin  = 8;
    final float topMargin    = 8;
    final float bottomMargin = 8;
    final MediaSize mediaSize = this.mediaSize;
    return new MediaPrintableArea(leftMargin,
                                  topMargin,
                                  mediaSize.getX(MediaPrintableArea.MM) - ( leftMargin + rightMargin ),
                                  mediaSize.getY(MediaPrintableArea.MM) - ( topMargin + bottomMargin ),
                                  MediaPrintableArea.MM);
  }
  
  public final java.util.Map< java.text.AttributedCharacterIterator.Attribute,Object >
    createLineNumberAttributes(){
    final java.util.Map< java.text.AttributedCharacterIterator.Attribute,Object > result =
      new java.util.HashMap<>( lineNumberAttribues );
    lineNumberAttribues.put( java.awt.font.TextAttribute.SIZE , Float.valueOf( bodyFontSize ) );
    return result;
  }
  
  public final boolean isPrintLineNumber(){
    return ( line_numbers < 1 )? false : true;
  }
  
  public final PrintRequestAttributeSet createxPrintRequestAttributeSet(){
    PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
    attributes.add(LANDSCAPE);
    attributes.add( getDefaultMediaPrintableArea() );
    return attributes;
  }

  public final boolean isPrintUserName(){
    return draw_user_name;
  }
}
