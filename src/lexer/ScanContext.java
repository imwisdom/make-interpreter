package lexer;

import java.io.File;
import java.io.FileNotFoundException;


class ScanContext {
	private final CharStream input;
	private StringBuilder builder;
	
	ScanContext(StringBuffer buf) {
		this.input = CharStream.from(buf);
		this.builder = new StringBuilder();
	}
	
	CharStream getCharStream() {
		return input;
	}
	
	String getLexime() {
		String str = builder.toString();
		builder.setLength(0);
		return str;
	}
	
	void append(char ch) {
		builder.append(ch);
	}
}
