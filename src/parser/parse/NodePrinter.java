package parser.parse;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import parser.ast.*;

public class NodePrinter {
	private StringBuffer sb = new StringBuffer();
	private Node root;

	public NodePrinter(Node root){
		this.root = root;
	}

	private void printList(ListNode listNode) {
		if(listNode == ListNode.EMPTYLIST) {
			return;
		}
		//추가
		if(sb.length()==0 && !(listNode.car() instanceof QuoteNode))
		{
			sb.append("(");
			printNode(listNode.car());	
			printNode(listNode.cdr());	
			sb.append(")");
		}
		else if(listNode.car() instanceof ListNode && !(((ListNode)listNode.car()).car() instanceof QuoteNode)) 
		{
			sb.append("(");
			printNode(listNode.car());	
			sb.append(")");
			printNode(listNode.cdr());	
		}
		else
		{
			printNode(listNode.car());	
			printNode(listNode.cdr());	
		}

	}
	private void printNode(QuoteNode quoteNode) {
		if (quoteNode.nodeInside() == null) 
			return;
		
		if (quoteNode.nodeInside() == ListNode.EMPTYLIST)
		{
			sb.append("'( )");
			return;
		}
		sb.append("'");	
		
		if(quoteNode.nodeInside() instanceof ListNode)
		{
			sb.append("(");
			printNode(quoteNode.nodeInside());
			sb.append(")");
		}
		else
			printNode(quoteNode.nodeInside());
	}
	private void printNode(Node node)
	{
		if(node == null)
			return;
		//추가
		if(node instanceof ListNode) {	//node가 ListNode일 경우 ListNode에 대한 프린트를 한다
			
			if(node == ListNode.EMPTYLIST)
				return;
			
			ListNode ln = (ListNode) node;
			
			printList(ln);

		}
		else if(node instanceof QuoteNode){	//node가 QuoteNode일 경우 QuoteNode에 대한 프린트를 한다
			QuoteNode ln = (QuoteNode) node;
			printNode((QuoteNode)ln);
		}
		else {
			sb.append(" " + node + " ");	//node가 일반 valuenode 일 경우 그냥 프린트해줌
		}

	}
   
	public void prettyPrint(){
		printNode(root);
		System.out.println("... "+sb);
	}
}
