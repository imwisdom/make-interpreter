package parser.parse;

import parser.ast.BinaryOpNode;
import parser.ast.BinaryOpNode.BinType;
import parser.ast.BooleanNode;
import parser.ast.FunctionNode;
import parser.ast.FunctionNode.FunctionType;
import parser.ast.IdNode;
import parser.ast.IntNode;
import parser.ast.ListNode;
import parser.ast.Node;
import parser.ast.QuoteNode;
import parser.ast.ValueNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import lexer.TokenType;

public class CuteInterpreter {

	static HashMap<String, Node> aTable = new HashMap<String, Node>();
	static HashMap<String, Node> tempTable = new HashMap<String, Node>();	//���ٸ� ���� ���̺��̴�. ���⿡ ������ ������ ���� ����ȴ�
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Scanner scan = new Scanner(System.in);
		while(true)
		{
			System.out.print("> ");
			String input = scan.nextLine();
			
			if(input.equals("-")) { System.out.println("exit interpreter"); break; }
			StringBuffer buf = new StringBuffer(input);

			CuteParser cuteParser = new CuteParser(buf);
			 
			Node parseTree = cuteParser.parseExpr();
			Node resultNode = runExpr(parseTree);
			NodePrinter nodePrinter = new NodePrinter(resultNode);
			nodePrinter.prettyPrint();
		}
	}
	private static void errorLog(String err)
	{
		System.out.println(err);
	}
	public static Node runExpr(Node rootExpr)
	{
		if(rootExpr == null)
			return null;
		if(rootExpr instanceof IdNode)
		{
			if(aTable.containsKey(rootExpr.toString()))//rootExpr�� define�� ����� ��� �� ����� ���� return�Ѵ�.
			{
				return lookupTable(rootExpr.toString());
			}	
			else if(tempTable.containsKey(rootExpr.toString()))//rootExpr�� lambda�� ���ǵ� ����� ��� �� ����� ���� return�Ѵ�.
			{
				Node ret = tempTable.get(rootExpr.toString());
				return ret;
			}
			else
				return rootExpr;
		}
		else if (rootExpr instanceof IntNode)
			return rootExpr;
		else if (rootExpr instanceof BooleanNode)
			return rootExpr;
		else if (rootExpr instanceof ListNode)
		{
			if(rootExpr != ListNode.EMPTYLIST)
			{
				if(aTable.containsKey(((ListNode) rootExpr).car().toString()))//rootExpr�� define�� ����� ��� �� ����� ���� return�Ѵ�.
				{	
					Node value = lookupTable(((ListNode) rootExpr).car().toString());
					//value�� lambda�� ������ �Ǻ��Ѵ�.
					if(value instanceof ListNode && ((ListNode) value).car() instanceof FunctionNode
							&& ((FunctionNode)((ListNode) value).car()).funcType==FunctionType.LAMBDA)
					{	//lambda���� ��� ( id x )���� �Ŀ��� x�� cdr�� �����, ���� ���� car�� ���� runExpr�� �����Ѵ�.
						//�̷��� �ϸ� ( id x )���� �Ŀ����� x���� ���ٽ��� ������ ���� �ǵ��� ���ٽ��� ���� �� �ִ�.
						ListNode cdrNode = ListNode.cons(((ListNode) rootExpr).cdr().car(), ListNode.EMPTYLIST);
						return runExpr(ListNode.cons(value, cdrNode));
					}
				}
				else if(tempTable.containsKey(((ListNode) rootExpr).car().toString()))
				{
					Node value = tempTable.get(((ListNode) rootExpr).car().toString());
					
					if(value instanceof ListNode && ((ListNode) value).car() instanceof FunctionNode
							&& ((FunctionNode)((ListNode) value).car()).funcType==FunctionType.LAMBDA)
					{
						return runExpr(ListNode.cons(value, ((ListNode) rootExpr).cdr()));
					}
				}
			}
		
			return runList((ListNode) rootExpr);
		}
		else
			errorLog("run Expr error");
		
		return null;
	}
	private static Node runList(ListNode list)
	{
		if(list.equals(ListNode.EMPTYLIST))
			return list;
		if(list.car() instanceof FunctionNode && ((FunctionNode)list.car()).funcType!=FunctionType.LAMBDA)
			return runFunction((FunctionNode)list.car(), (ListNode)stripList(list.cdr()));
		if(list.car() instanceof BinaryOpNode)
			return runBinary(list);
		if(list.car() instanceof ListNode)
		{	//list�� car�� ���ٺκ�, cdr�� �� �κ��̴�.
			Node isfuncNode = ((ListNode) list.car()).car();
			//isfuncNode�� functionNode�̰� lambda�϶� ���� �ڵ带 �����Ѵ�.
			if(isfuncNode instanceof FunctionNode && ((FunctionNode) isfuncNode).funcType==FunctionType.LAMBDA
					&&list.cdr()!=ListNode.EMPTYLIST&&list.cdr()!=null)
			{	//tempTable�� ������ ����Ǿ� ������ �� ������ key������ �ؼ� get()�� �Ͽ� key�� ���� value�� binding�Ѵ�.
				if(tempTable.containsKey(list.cdr().car().toString()))
					binding((ListNode)list.car(), tempTable.get(list.cdr().car().toString()));
				else	//tempTable�� ������ ����Ǿ����� ������ ������ lambda ���� �� �ڿ� �ִ� ���� binding�Ѵ�.
					binding((ListNode)list.car(), list.cdr().car());
				//lambda�� functionNode��, ������ list���� cdr�� �ؼ� runFunction ����
				//runFunction���� x�� ���� ����ִ´�.
				return runFunction((FunctionNode)isfuncNode, ((ListNode) list.car()).cdr());
			}
		}
		return list;
	}
	private static Node runFunction(FunctionNode operator, ListNode operand)
	{
		Node op = runExpr(operand);	//operand�� ��ø�� �ǰų� ������ ���� ���� �� �����Ƿ� runExpr�� ����
		Node car, cdr;
		
		if(op instanceof ListNode)	//operand�� ��ø�� �ǰų� ������ ���� ���� �� �����Ƿ� �� �� runExpr�� ���ִ� ��, �׷��� �� ���� ListNode�� ��쿡��
		{							//operand�� ���� Ÿ���� ListNode�̹Ƿ� operand�� ������ �־��� �� �ִ�.
			operand=(ListNode) op;
			//(lambda�� ���) ���⼭ (x)�� car�� �ǰ�, x�� ���� �� ( + x 1 ) �� cdr�� �ȴ�.
			car = operand.car();	
			cdr = operand.cdr();

			car = openList(car);
			cdr = openList(cdr);
			if(!(car instanceof QuoteNode || operator.funcType==FunctionType.DEFINE))
				car = runExpr(car);
			if(!(cdr instanceof QuoteNode))
				cdr = runExpr(cdr);
		}
		else 
		{
			car = op;	//runExpr�ؼ� ���� ���� operand�� ���� ListNode�� �ƴ� ��쿡�� car�� op�� �־��ְ� ���̻� ListNode�� �ƴϹǷ� cdr�� EMPTY�� �Ѵ�
			cdr = ListNode.EMPTYLIST;
		}
		
		switch(operator.funcType)
		{
		case CAR:

			Node carNode;		//�� ó�� operand�� QuoteNode�� ���(car�� QuoteNode�̰� cdr�� EMPTY����)
			if((operand.cdr() == ListNode.EMPTYLIST || operand.cdr() == null) && operand.car() instanceof QuoteNode)
			{	//QuoteNode�ȿ� �ִ� ���� ���� car�� �����´�.
				carNode = ((ListNode) ((QuoteNode) operand.car()).nodeInside()).car();
				//ListNode�� IdNode, FunctionNode�� ��쿡�� 'ó���� ���ش�.
				if(carNode instanceof ListNode || carNode instanceof IdNode || carNode instanceof FunctionNode)
					return new QuoteNode(carNode);
				else	//�� ���� ���� �׳� ���� ���!
					return carNode;
			}		//car�� ��(runExpr�Ͽ� ���� ��)�� QuotenNode�� ���
			else if(car instanceof QuoteNode)
			{	//QuoteNode �ȿ� �ִ� ���� ���� car�� �����´�.
				carNode = runFunction(operator, (ListNode)((QuoteNode) car).nodeInside());
				//ListNode�� IdNode, FunctionNode�� ��쿡�� 'ó���� ���ش�.
				if(carNode instanceof ListNode || carNode instanceof IdNode || carNode instanceof FunctionNode)
					return new QuoteNode(carNode);
			}
			else carNode = car;	//QuoteNode�� ���� ���� ��쿡�� ���� car �� ��ü�� ���̹Ƿ� �ٷ� return ���ش�.
			return carNode;

		case CDR:
			Node cdrNode;		//�� ó�� operand�� QuoteNode�� ���(car�� QuoteNode�̰� cdr�� EMPTY����)
			if((operand.cdr() == ListNode.EMPTYLIST || operand.cdr() == null) && operand.car() instanceof QuoteNode)
			{	//QuoteNode�ȿ� �ִ� ���� ���� cdr�� �����ͼ� Quoteȭ ��Ų��.
				cdrNode = ((ListNode) ((QuoteNode) operand.car()).nodeInside()).cdr();
				return new QuoteNode(cdrNode);
			}//car�� ��(runExpr�Ͽ� ���� ��)�� QuotenNode�� ���
			else if(car instanceof QuoteNode)
			{	//QuoteNode �ȿ� �ִ� ���� ���� cdr�� �����´�.
				cdrNode = runFunction(operator, (ListNode)((QuoteNode) car).nodeInside());
				//ListNode�� IdNode�� ��쿡�� 'ó���� ���ش�.
				if(cdrNode instanceof ListNode)
					return new QuoteNode(cdrNode);
				else
					return new QuoteNode(ListNode.cons(cdrNode, ListNode.EMPTYLIST));
			}
			else cdrNode = cdr;	//QuoteNode�� ���� ���� ��쿡�� ���� cdr �� ��ü�� ���̹Ƿ� �ٷ� return ���ش�.
			return cdrNode;
			 
		case CONS:
			Node consNode;
			if(car instanceof ListNode)
			{
				ListNode li =(ListNode)car;
				
				if(li.car() instanceof QuoteNode)	//li.car�� QuoteNode�� ��쿡�� �� ����� �ȿ� �ִ� ��带 cdr�� �ִ´�.
					consNode = runQuote(li);
				else
					consNode = car;//QuoteNode�� �ƴ϶�� �� ��� ��ü�� cdr�� �ִ´�.
			}
			else if(car instanceof QuoteNode)//car�� QuoteNode�� ��쿡�� car �ȿ� �ִ� ��带 cdr�� �ִ´�.
				consNode = ((QuoteNode) car).nodeInside();
			else
				consNode = car;
			
			Node inList;	//�� ������ ���� cdr�� ���� car���� consNode�� car��, cdr(QuoteNode type) �ȿ� �ִ� ��带 cdr�� �����Ͽ� ListNode�� �����.
			inList = ListNode.cons(consNode, (ListNode)((QuoteNode) cdr).nodeInside());
			//���� ListNode�� QuoteNode�� ���� return�Ѵ�.
			return new QuoteNode(inList);
		case NULL_Q:
			if(operand == ListNode.EMPTYLIST)	//operand�� empty�� ��� true
				return BooleanNode.TRUE_NODE;
			if(car instanceof ListNode)	//car�� listNode�̸� null�� �ƴϹǷ� false
				return BooleanNode.FALSE_NODE;
			if(car instanceof QuoteNode)	//car�� quoteNode�� ��쿡�� �� �ȿ� �ִ� ���� �Ǻ��ؾ� �ȴ�.
				return runFunction(operator, (ListNode)((QuoteNode) car).nodeInside());
			else
				return BooleanNode.FALSE_NODE;

				
		case ATOM_Q:
			if(operand == ListNode.EMPTYLIST)	//null�� atom�� ���ԵǹǷ� true
				return BooleanNode.TRUE_NODE;
			if(car instanceof QuoteNode)	//car�� QuoteNode�� ��쿡�� car�� ���� ��带 ����.
			{
				Node inNode = ((QuoteNode) car).nodeInside();	//car�� �ȿ� �ִ� ���
				if(inNode instanceof ListNode)
				{	//�ȿ� �ִ� ��尡 ListNode�� ��쿡�� ��������� true, �ƴϸ� false
					if(inNode == ListNode.EMPTYLIST) return BooleanNode.TRUE_NODE;
					else return BooleanNode.FALSE_NODE;
				}
				else return BooleanNode.TRUE_NODE;
			}
			else if(car instanceof ListNode)	//car�� ListNode�� ��� ��������� true, �ƴϸ� false
			{
				if(car == ListNode.EMPTYLIST) return BooleanNode.TRUE_NODE;
				else return BooleanNode.FALSE_NODE;
			}
			else return BooleanNode.TRUE_NODE;	//QuoteNode�� ListNode�� �ƴ� ��� ������ true
		case EQ_Q:
			Node lefteqNode;
			Node righteqNode;
			
			lefteqNode = openList(car);	//����� ���� ���� �����Ƿ� openList���ش�.
			righteqNode = openList(cdr);
			
			if(lefteqNode instanceof ListNode && righteqNode instanceof ListNode)
			{	//left�� right ��尡 �� �� ListNode�� ��쿡�� ���� id���� ���Ͽ� return�Ѵ�.
				if(lefteqNode == righteqNode) return BooleanNode.TRUE_NODE;
				else return BooleanNode.FALSE_NODE;
			}
			else
			{	//left�� right ����� value�� ���Ѵ�. ������ true, �ƴϸ� false
				if(lefteqNode.toString().equals(righteqNode.toString()))
					return BooleanNode.TRUE_NODE;
				else return BooleanNode.FALSE_NODE;
			}	
		case COND:
			if(car instanceof BooleanNode)	//car�� booleanNode�� ��쿡�� car�� ���� ���� ���� �޶�����.
			{
				if(car == BooleanNode.TRUE_NODE)	//car�� true�� ��쿡�� car �ڿ� �ִ� ��带 return�ϸ�, �ƴϸ� false�̴�.
					return ((ListNode) cdr).car();
				else
					return BooleanNode.FALSE_NODE;
			}
			ListNode leftNode = (ListNode)car;	//car�� booleanNode�� �ƴ� ��� car�� booleanNode�� return �ϴ� ListNode�̴�.
			ListNode rightNode = (ListNode)cdr;

			if(runExpr(leftNode.car()) == BooleanNode.TRUE_NODE)	//car�� runExpr�ؼ� ���� ���� ���� true�� ��쿡�� �� ���� �ִ� ���� return�Ѵ�.
				if(leftNode.cdr().car() instanceof ListNode)
					return leftNode.cdr();	//leftNode.cdr.car�� ListNode�� ��� leftNode.cdr��, listNode.cdr.car�� list�̹Ƿ� listNode.cdr�� return�Ѵ�.
				else
					return leftNode.cdr().car();	//leftNode.cdr.car�� ListNode�� �ƴ� ��쿡�� �� ���� return�Ѵ�.
			else if(runExpr(rightNode.car()) == BooleanNode.TRUE_NODE)	//���� if���� �Ȱ���. car�� ���� false�� ��� �� ���� ����Ʈ�� cdr�� �˻��Ѵ�.
				if(rightNode.cdr().car() instanceof ListNode)
					return rightNode.cdr();
				else
					return rightNode.cdr().car();
			else if(((ListNode) cdr).cdr()!=ListNode.EMPTYLIST)	//cdr.cdr�� empty�� �ƴ� ��� cdr�� �˻� ����� �� �� �����Ƿ� runFunction()���� �˻��Ѵ�.
					return runFunction(operator, (ListNode)cdr);
			else
				return BooleanNode.FALSE_NODE;	//default�� return false

		case NOT :
			Node boolNode = car;
			if(boolNode instanceof BinaryOpNode)	//boolNode�� BinaryOpNode�� ��� runBinary�� �˻��Ѵ�.
				boolNode = runBinary(operand);
			if(boolNode == BooleanNode.FALSE_NODE)	//boolNode�� false�� ��� true, true�� ��� false
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;
		
		case DEFINE :
			insertTable(((IdNode)car).toString(), cdr);//insertTable�� idNode�� car�� car�� value�� �� cdr�� �ִ´�.
			break;
		case LAMBDA :
			if(cdr instanceof ListNode && ((ListNode) cdr).cdr() == ListNode.EMPTYLIST)
				return ((ListNode) cdr).car();
			else
				return cdr;
		default:
			break;
		}
		return null;
	}
	private static void insertTable(String id, Node value)
	{
		aTable.put(id, value);
	}
	private static Node lookupTable(String id)
	{
		return aTable.get(id);
	}
	private static Node openList(Node list)	//eq�� ���� �Լ�. list�� ���� ����Ʈ�� ��Ʈ���� ���������� ���� �����Ƿ�
	{								//list�� �� ����� �ִ� ���� �����;� �ǹǷ� �� �Լ��� �ʿ��ϴ�.
		if(list instanceof ListNode)
		{
			if(((ListNode) list).cdr() == ListNode.EMPTYLIST)	//�������ִ� list�̹Ƿ� list�� car�� �Ѿ��.
				return openList(((ListNode) list).car());
			else
				return list;	//list�� �ܼ��� �������⸸ �� ����Ʈ�� �ƴ϶� list.car�� list.cdr�� ��Ȯ�� �ִ� list�̹Ƿ�				//�̶��� �׳� �ٷ� return���ش�.
		}
		else return list;
	}
	private static void binding(ListNode lambdaNode, Node value)
	{	//���⼭ ListNode lambdaNode�� ListNode.car()�� 'lambda'�� ListNode�̴�.
		//������ ( lambda (x) (+ x 1 ) ) �̷� ����
		
		//x�� value�� binding�ϱ� ���� ���� x�� ����
		IdNode x = (IdNode) ((ListNode) lambdaNode.cdr().car()).car();
		//�ӽ� binding
		if(value instanceof ListNode && ((ListNode) value).car() instanceof QuoteNode)
			tempTable.put(x.toString(), ((ListNode) value).car());
		else
			tempTable.put(x.toString(), value);
	}
	private static Node stripList(ListNode node)
	{
		if(node.car() instanceof ListNode && node.cdr() == ListNode.EMPTYLIST)
		{
			Node listNode = node.car();
			return listNode;
		}
		else
			return node;
	}
	private static Node runBinary(ListNode list)
	{
		BinaryOpNode operator = (BinaryOpNode) list.car();
		
		Node cdr;
		if(list.cdr().cdr() == null || list.cdr().cdr() == ListNode.EMPTYLIST)
			cdr = new IntNode("0");
		else cdr = list.cdr().cdr();
			
		Node car = list.cdr().car();
		
		if(car instanceof ListNode)
			car = runExpr(car);
		if(cdr instanceof ListNode)
			cdr = runExpr(((ListNode)cdr).car());
		
		if(car instanceof IdNode)	//car�� IdNode�� ��쿡�� define Ȥ�� lambda�� ����� ������ ���� �����Ƿ� runExpr�� �˻����ش�.
			car = runExpr(car);
		if(cdr instanceof IdNode)	//cdr�� IdNode�� ��쿡�� define Ȥ�� lambda�� ����� ������ ���� �����Ƿ� runExpr�� �˻����ش�.
			cdr = runExpr(cdr);
		
		if(car instanceof IntNode)
			car = new IntNode(car.toString());
		if(cdr instanceof IntNode)
			cdr = new IntNode(cdr.toString());
		Integer answer = 0;
		
		switch(operator.binType)
		{
		case PLUS :
			answer = ((IntNode)car).getValue()+((IntNode)cdr).getValue();
			return new IntNode(Integer.toString(answer));
		case MINUS :
			answer = ((IntNode)car).getValue()-((IntNode)cdr).getValue();
			return new IntNode(Integer.toString(answer));
		case TIMES :
			answer = ((IntNode)car).getValue()*((IntNode)cdr).getValue();
			return new IntNode(Integer.toString(answer));
		case DIV :
			answer = ((IntNode)car).getValue()/((IntNode)cdr).getValue();
			return new IntNode(Integer.toString(answer));
		case LT :
			if(((IntNode)car).getValue()<((IntNode)cdr).getValue())
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;
		case GT :
			if(((IntNode)car).getValue()>((IntNode)cdr).getValue())
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;
		case EQ :
			if(((IntNode)car).getValue()-((IntNode)cdr).getValue()==0)
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;
		default:
			break;
		}
		return null;
	}
	private static Node runQuote(ListNode node)
	{
		return ((QuoteNode)node.car()).nodeInside();
	}
	
	
	
	
	
	
	
	
	
	

}
