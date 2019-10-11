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
		// BinaryOpNode�� ���Ͽ� �ۼ�
		// +, -, /, *�� �ش�
		case DIV:
		case EQ:
		case MINUS:
		case GT:
		case PLUS:
		case TIMES:
		case LT:	//BinaryOpNode Ÿ���� ��ü�� �ϳ� �����Ѵ�
			BinaryOpNode biNode = new BinaryOpNode();
			biNode.setValue(tType);	//DIV~LT������ case�� ������ tType�� BinaryOpType�̹Ƿ�(BinaryOpType�� ���ϹǷ�)
			return biNode;	//���� ��ü�� set�� ���� �� return�Ѵ�.
		// FunctionNode�� ���Ͽ� �ۼ�
		// Ű���尡 FunctionNode�� �ش�
		case ATOM_Q:
		case CAR:
		case CDR:
		case COND:
		case CONS:
		case DEFINE:
		case EQ_Q:
		case LAMBDA:
		case NOT:
		case NULL_Q:	//FunctionNode Ÿ���� ��ü�� �ϳ� �����Ѵ�
			FunctionNode funcNode = new FunctionNode();
			funcNode.setValue(tType); //ATOM_Q~NULL_Q������ case�� ������ tType�� Functiontype�̹Ƿ�
			return funcNode;	//���� ��ü�� set�� ���� �� return �Ѵ�

		// BooleanNode�� ���Ͽ� �ۼ�
		case FALSE:	//tType�� booleantype �̹Ƿ� booleanNode ��ü�� �ϳ� �����Ͽ� value�� �־��ش�
			return BooleanNode.FALSE_NODE;
		case TRUE:	//tType�� booleantype �̹Ƿ� booleanNode ��ü�� �ϳ� �����Ͽ� value�� �־��ش�
			return BooleanNode.TRUE_NODE;
		// case L_PAREN�� ���� case R_PAREN�� ��쿡 ���ؼ� �ۼ�
		// L_PAREN�� ��� parseExprList()�� ȣ���Ͽ� ó��
		case L_PAREN:	//L_PAREN�� list�� �����̹Ƿ� ListNode ��ü�� ���� list ���� value���� �����Ѵ� 
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
			// head�� next�� ����� head�� ��ȯ�ϵ��� �ۼ�
			System.out.println("Parsing Error!");
			return null;
		}

	}

	// List�� value�� �����ϴ� �޼ҵ�
	private ListNode parseExprList() {
		
		Node head = parseExpr();
		// head�� next ��带 set�Ͻÿ�.
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
