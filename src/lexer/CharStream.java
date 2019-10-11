package lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

class CharStream {
	private final StringBuffer reader;
	private Character cache;
	private int i=0;
	
	static CharStream from(StringBuffer buf) {
		return new CharStream(buf);
	}
	
	CharStream(StringBuffer reader) {
		this.reader = reader;
		this.cache = null;
	}
	
	Char nextChar() {
		if ( cache != null ) {
			char ch = cache;
			cache = null;
			
			return Char.of(ch);
		}
		else {
			
			if ( i == reader.length() ) {
				return Char.end();
			}
			else {
				int ch = reader.charAt(i++);
				return Char.of((char)ch);
			}
			
		}
	}
	
	void pushBack(char ch) {
		cache = ch;
	}
}
