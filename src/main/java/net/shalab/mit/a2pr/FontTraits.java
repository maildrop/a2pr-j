// -*- coding: utf-8; compile-command: "compile.bat"; -*-

package net.shalab.mit.a2pr;

import java.lang.*;
import java.util.*;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;

import static java.awt.font.TextAttribute.*;
import static java.text.CharacterIterator.DONE;

public class FontTraits{
  private static final java.util.logging.Logger logger =
    java.util.logging.Logger.getLogger("net.shalab.mit.a2pr");
  private static class SampleFont{
    final String fontFamily;
    final Map<? extends AttributedCharacterIterator.Attribute,?> attributes;
    final Font sampleFont;

    private SampleFont( final String fontFamily,
                final java.util.Map<? extends AttributedCharacterIterator.Attribute,?> attributes ){
      this.fontFamily = fontFamily;
      this.attributes = attributes;
      this.sampleFont = new Font( attributes );
    }
    
    public final synchronized SampleFont deriveFontSize( float fontSize ){
      final Map<AttributedCharacterIterator.Attribute, Object > derive =
        new HashMap<>( attributes );
      derive.put( SIZE , Float.valueOf( fontSize ) );
      return new SampleFont( this.fontFamily , derive );
    }
    public final synchronized SampleFont deriveFontFamily( String family ){
      final Map<AttributedCharacterIterator.Attribute, Object > derive =
        new HashMap<>( attributes );
      derive.put( FAMILY , family );
      return new SampleFont( family , derive );
    }

    public final synchronized SampleFont
      deriveFont( final Map<? extends AttributedCharacterIterator.Attribute , ?> attributes){
      final Map<AttributedCharacterIterator.Attribute , Object > derive =
        new HashMap<>( this.attributes );
      for( Map.Entry<? extends AttributedCharacterIterator.Attribute , ?> v : attributes.entrySet() ){
        derive.put( (AttributedCharacterIterator.Attribute)v.getKey() , (Object)v.getValue() );
      }
      return new SampleFont( this.fontFamily , derive );
    }

    public final java.awt.Font getFont(){
      return sampleFont;
    }

    static final SampleFont cocreate( final String fontFamily ,
                                      final Map<? extends AttributedCharacterIterator.Attribute,?> attributes ){
      return new SampleFont( fontFamily ,
                             new HashMap<AttributedCharacterIterator.Attribute, Object>( attributes ) );
    }

    /* デバッグ用のメソッド */
    public final synchronized String toString(){
      StringBuilder sb = new StringBuilder();
      sb.append( fontFamily );
      sb.append( "[" );
      sb.append( sampleFont.toString() );
      sb.append( "]" );
      sb.append( "{" );
      for( Map.Entry<? extends AttributedCharacterIterator.Attribute , ?> v : attributes.entrySet() ){
        sb.append( v.getKey() );
        sb.append( ":" );
        sb.append( v.getValue() );
        sb.append( "," );
      }
      sb.append( "}" );
      return sb.toString();
    }
  }

  private final Deque<SampleFont> sampleFonts;
  
  /* コンストラクタ */
  FontTraits(Map<? extends AttributedCharacterIterator.Attribute,?> baseFontAttributes)
  {
    sampleFonts = new ArrayDeque<>();
    sampleFonts.add( new SampleFont( String.class.cast( baseFontAttributes.get( TextAttribute.FAMILY ) ) ,
                                     baseFontAttributes ) );
  }

  public synchronized void addFirst( String family )
  {
    assert( !sampleFonts.isEmpty() );
    sampleFonts.addFirst( sampleFonts.peekLast().deriveFontFamily( family ) );
  }

  public synchronized AttributedString createAttributedString( final String src )
    throws java.nio.charset.CharacterCodingException{

    final AttributedString result = new AttributedString( src );
    final java.text.AttributedCharacterIterator iterator = result.getIterator();

    final SampleFont[] fontClasses = new SampleFont[ iterator.getEndIndex() - iterator.getBeginIndex() ];

    for( char c = iterator.first() ; c != DONE ; c = iterator.next() ){
      // 第一段階として、コードポイントを得る
      final int codePoint;

      final int char_begin = iterator.getIndex(); // サロゲートペアを考慮したときの文字の開始位置
      if( java.lang.Character.isHighSurrogate( c ) ){
        logger.fine( "highSurrogate found" );
        final char lowSurrogate = iterator.next();
        if ( lowSurrogate == DONE ){
          throw new java.nio.charset.MalformedInputException(iterator.current() );
        }
        if( !java.lang.Character.isSurrogatePair( c , lowSurrogate ) ){
          throw new java.nio.charset.MalformedInputException(iterator.current() );
        }
        codePoint = java.lang.Character.toCodePoint( c , lowSurrogate );
      }else{
        codePoint = (int)( c ); // サロゲートペアではない(BMP の文字なので）ので、int に拡張
      }

      // そのコードポイントが表示可能なフォントを探す
      for(final SampleFont sf : sampleFonts ){
        if( sf.getFont().canDisplay( codePoint ) ){

          assert ( Character.isHighSurrogate( c ) ?
                   // サロゲートペアならば、 index は一つずれている はず。
                   ( (char_begin - iterator.getBeginIndex() + 1) ==
                     (iterator.getIndex() - iterator.getBeginIndex()) ) :
                   // 非サロゲートペアならば、これは一致するはず。
                   ( char_begin - iterator.getBeginIndex() == iterator.getIndex() - iterator.getBeginIndex() ) );
          
          fontClasses[ char_begin - iterator.getBeginIndex() ] = sf;
          fontClasses[ iterator.getIndex() - iterator.getBeginIndex() ] = sf;
          break;
        }
      }
      assert (fontClasses[char_begin - iterator.getBeginIndex() ] != null) : "表示できるフォントが存在しない";
      
    }

    // 連続する体裁をまとめて設定する
    {
      int runStart = 0;
      SampleFont currentFont = fontClasses[0];
      for( int index = 0 ; index < fontClasses.length ; ++index ){
        if( fontClasses[index] != currentFont ){
          assert( result != null );
          assert( currentFont != null );
          assert( currentFont.attributes != null );
          result.addAttributes( currentFont.attributes , runStart , index );
          runStart = index;
          currentFont = fontClasses[index];
        }
      }
      result.addAttributes( currentFont.attributes , runStart , fontClasses.length );
    }
    return result;
  }
  
  
  public static void main( String[] args ){
    final Map<AttributedCharacterIterator.Attribute, Object> attr = new HashMap<>();
    attr.put( FAMILY , java.awt.Font.MONOSPACED );
    attr.put( SIZE , java.lang.Float.valueOf( 7.0f ) );
    System.out.println( SampleFont.cocreate( java.awt.Font.MONOSPACED , attr ).toString() ); 
    {
      final FontTraits ft = new FontTraits( attr );
      ft.addFirst( "Source Code Pro" );
      {
        StringBuilder sb = new StringBuilder();
        sb.append( "Font[" );
        sb.append( System.lineSeparator() );
        for( SampleFont sf : ft.sampleFonts ){
          sb.append( " " );
          sb.append( sf.toString() );
          sb.append( System.lineSeparator() );
        }
        sb.append( "];" );
        System.out.println( sb.toString() );
      }
      
      try{
        final AttributedCharacterIterator iterator =
          ft.createAttributedString("abcdefg日本語 入力𠮷サロゲートペア").getIterator() ;
        for( char c = iterator.first() ; c != DONE ; c = iterator.next() ){
          StringBuilder buffer = new StringBuilder();
          buffer.append( c ) ;
          buffer.append( "[");
          buffer.append( iterator.getAttribute( FAMILY ).toString());
          buffer.append( ":runStart = " + iterator.getRunStart() );
          buffer.append( ",runLimit = " + iterator.getRunLimit() );
          buffer.append( "]" );
          System.out.println( buffer.toString());
        }
      }catch( java.nio.charset.CharacterCodingException cce ){
        java.util.logging.Logger.getLogger("").warning( cce.toString());
      }
    }
  }
};
