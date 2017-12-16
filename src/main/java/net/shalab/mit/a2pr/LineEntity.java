// -*- encode: utf-8; -*-
package net.shalab.mit.a2pr;
import java.lang.*;

public final class LineEntity{
  final java.awt.font.TextLayout textLayout;
  final int line_number;

  public LineEntity( final java.awt.font.TextLayout textLayout, final int line_number ){
    assert( textLayout!= null );
    this.textLayout = textLayout;
    this.line_number = line_number;
  }

  public final java.awt.geom.Rectangle2D getBounds(){
    assert( textLayout != null );
    return textLayout.getBounds();
  }
  public final double getHeight(){
    return this.getBounds().getHeight();
  }
}