package lexer;

import static lexer.TransitionOutput.GOTO_ACCEPT_ID;
import static lexer.TransitionOutput.GOTO_ACCEPT_INT;
import static lexer.TransitionOutput.GOTO_EOS;
import static lexer.TransitionOutput.GOTO_FAILED;
import static lexer.TransitionOutput.GOTO_MATCHED;
import static lexer.TransitionOutput.GOTO_SHARP;
import static lexer.TransitionOutput.GOTO_SIGN;
import static lexer.TransitionOutput.GOTO_START;
import static lexer.TokenType.FALSE;
import static lexer.TokenType.INT;
import static lexer.TokenType.MINUS;
import static lexer.TokenType.PLUS;
import static lexer.TokenType.TRUE;


enum State {
	START {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
					context.append(v);
					return GOTO_ACCEPT_ID;
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR: //special charactor가 들어온 경우 
					if (v=='-'||v=='+') { //'-'나 '+' 이면 부호이므로 
					context.append(v);		//append해서 넣어주고 SIGN으로 간다
					return GOTO_SIGN;
					}
					else if (v=='#') {  //sharp가 true or false 상태인데
					context.append(v);	//sharp에 대한 char가 '#'이므로
					return GOTO_SHARP;	//append해주고 SHARP로 간다
					}
					else { 
					context.append(v);	//v를 append를 해준다
					String lexime = context.getLexime();
					//GOTO_MATCHED 함수로 v의 타입을 문자와 매치를 시켜야 되는 데
					//GOTO_MATCHED가 받는 함수 인자가 String(lexime)이다. 
					//그러므로 context.getLexime()를 받아서
					//문자 한 글자가 아닌 lexime를 받아서 이를 GOTO_MATCHED의 함수 인자로 넣는다.
					//fromSpecialCharactor는 TokenType class의 함수로서, 함수 인자(문자)의 TokenType을 return하는 함수이다.
					return GOTO_MATCHED(TokenType.fromSpecialCharactor(v), lexime);
					
					}
				case WS:
					return GOTO_START;
				case END_OF_STREAM:
					return GOTO_EOS;
				default:
					throw new AssertionError();
			}
		}
	},
	ACCEPT_ID {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_ID;
				case SPECIAL_CHAR:
					return GOTO_FAILED;
				case WS:
				case END_OF_STREAM:
					return GOTO_MATCHED(Token.ofName(context.getLexime()));
				default:
					throw new AssertionError();
			}
		}
	},
	ACCEPT_INT {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			switch ( ch.type() ) {
				case LETTER:
					return GOTO_FAILED;
				case DIGIT:
					context.append(ch.value());
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:
					return GOTO_FAILED;
				case WS:
					return GOTO_MATCHED(INT, context.getLexime());
				case END_OF_STREAM:
					return GOTO_MATCHED(INT, context.getLexime());
				default:
					throw new AssertionError();
			}
		}
	},
	SHARP {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
					switch ( v ) {
						case 'T':
							context.append(v);
							return GOTO_MATCHED(TRUE, context.getLexime());
						case 'F':
							context.append(v);
							return GOTO_MATCHED(FALSE, context.getLexime());
						default:
							return GOTO_FAILED;
					}
				default:
					return GOTO_FAILED;
			}
		}
	},
	SIGN {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
					return GOTO_FAILED;
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:
					return GOTO_FAILED;
				case WS:
					String lexme = context.getLexime();
					switch ( lexme ) {
						case "+":
							return GOTO_MATCHED(PLUS, lexme);
						case "-":
							return GOTO_MATCHED(MINUS, lexme);
						default:
							throw new AssertionError();
					}
				case END_OF_STREAM:
					return GOTO_FAILED;
				default:
					throw new AssertionError();
			}
		}
	},
	MATCHED {
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	FAILED{
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	EOS {
		@Override
		public TransitionOutput transit(ScanContext context) {
			return GOTO_EOS;
		}
	};
	
	abstract TransitionOutput transit(ScanContext context);
}
