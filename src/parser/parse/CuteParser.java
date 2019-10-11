package parser.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

import parser.ast.*;
import parser.ast.BinaryOpNode.BinType;
import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;

public class CuteParser {
	private Iterator<Token> tokens;
	private static Node END_OF_LIST = new Node() {};

	public CuteParser(StringBuffer buf) {
		
		tokens = Scanner.scan(buf);
	
	}

	private Token getNextToken() {
		if (!tokens.hasNext())
			return null;
		return tokens.next();
	}

	public Node parseExpr() {
		Token t = getNextToken();
		if (t == null) {
			System.out.println("No more token");
			return null;
		}
		TokenType tType = t.type();
		String tLexeme = t.lexme();
		
		switch (tType) {
		case ID:
			return new IdNode(tLexeme);
		case INT:
			if(tLexeme == null)
				System.out.println("???");
			return new IntNode(tLexeme);
		// BinaryOpNode에 대하여 작성
		// +, -, /, *가 해당
		case DIV:
		case EQ:
		case MINUS:
		case GT:
		case PLUS:
		case TIMES:
		case LT:	//BinaryOpNode 타입의 객체를 하나 선언한다
			BinaryOpNode biNode = new BinaryOpNode();
			biNode.setValue(tType);	//DIV~LT까지의 case로 왔으면 tType이 BinaryOpType이므로(BinaryOpType에 속하므로)
			return biNode;	//만든 객체에 set을 해준 뒤 return한다.
		// FunctionNode에 대하여 작성
		// 키워드가 FunctionNode에 해당
		case ATOM_Q:
		case CAR:
		case CDR:
		case COND:
		case CONS:
		case DEFINE:
		case EQ_Q:
		case LAMBDA:
		case NOT:
		case NULL_Q:	//FunctionNode 타입의 객체를 하나 선언한다
			FunctionNode funcNode = new FunctionNode();
			funcNode.setValue(tType); //ATOM_Q~NULL_Q까지의 case로 왔으면 tType이 Functiontype이므로
			return funcNode;	//만든 객체에 set을 해준 뒤 return 한다

		// BooleanNode에 대하여 작성
		case FALSE:	//tType이 booleantype 이므로 booleanNode 객체를 하나 선언하여 value로 넣어준다
			return BooleanNode.FALSE_NODE;
		case TRUE:	//tType이 booleantype 이므로 booleanNode 객체를 하나 선언하여 value로 넣어준다
			return BooleanNode.TRUE_NODE;
		// case L_PAREN일 경우와 case R_PAREN일 경우에 대해서 작성
		// L_PAREN일 경우 parseExprList()를 호출하여 처리
		case L_PAREN:	//L_PAREN은 list의 시작이므로 ListNode 객체를 만들어서 list 안의 value들을 저장한다 
			return parseExprList();

		case R_PAREN:
			return END_OF_LIST;
			
		case APOSTROPHE:
			QuoteNode quoteNode = new QuoteNode(parseExpr());
			ListNode listNode = ListNode.cons(quoteNode, ListNode.EMPTYLIST);
			return listNode;
		
		case QUOTE:
			return new QuoteNode(parseExpr());

		default:
			// head의 next를 만들고 head를 반환하도록 작성
			System.out.println("Parsing Error!");
			return null;
		}

	}

	// List의 value를 생성하는 메소드
	private ListNode parseExprList() {
		
		Node head = parseExpr();
		// head의 next 노드를 set하시오.
		if (head == null) // if next token is RPAREN
			return null;
		if(head == END_OF_LIST)
			return ListNode.EMPTYLIST;
		ListNode tail = parseExprList();
		if(tail==null)
			return null;
		return ListNode.cons(head, tail);
		
	}
}
