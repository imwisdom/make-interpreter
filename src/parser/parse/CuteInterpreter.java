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
	static HashMap<String, Node> tempTable = new HashMap<String, Node>();	//람다를 위한 테이블이다. 여기에 변수와 변수의 값이 저장된다
	
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
			if(aTable.containsKey(rootExpr.toString()))//rootExpr가 define된 노드인 경우 그 노드의 값을 return한다.
			{
				return lookupTable(rootExpr.toString());
			}	
			else if(tempTable.containsKey(rootExpr.toString()))//rootExpr가 lambda로 정의된 노드인 경우 그 노드의 값을 return한다.
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
				if(aTable.containsKey(((ListNode) rootExpr).car().toString()))//rootExpr가 define된 노드인 경우 그 노드의 값을 return한다.
				{	
					Node value = lookupTable(((ListNode) rootExpr).car().toString());
					//value가 lambda식 인지를 판별한다.
					if(value instanceof ListNode && ((ListNode) value).car() instanceof FunctionNode
							&& ((FunctionNode)((ListNode) value).car()).funcType==FunctionType.LAMBDA)
					{	//lambda식인 경우 ( id x )꼴의 식에서 x를 cdr로 만들고, 람다 식을 car로 만들어서 runExpr를 실행한다.
						//이렇게 하면 ( id x )꼴의 식에서의 x값이 람다식의 변수로 대응 되도록 람다식을 만들 수 있다.
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
		{	//list의 car는 람다부분, cdr은 값 부분이다.
			Node isfuncNode = ((ListNode) list.car()).car();
			//isfuncNode가 functionNode이고 lambda일때 다음 코드를 실행한다.
			if(isfuncNode instanceof FunctionNode && ((FunctionNode) isfuncNode).funcType==FunctionType.LAMBDA
					&&list.cdr()!=ListNode.EMPTYLIST&&list.cdr()!=null)
			{	//tempTable에 변수가 저장되어 있으면 그 변수를 key값으로 해서 get()을 하여 key에 대한 value로 binding한다.
				if(tempTable.containsKey(list.cdr().car().toString()))
					binding((ListNode)list.car(), tempTable.get(list.cdr().car().toString()));
				else	//tempTable에 변수가 저장되어있지 않으면 변수를 lambda 식의 맨 뒤에 있는 값과 binding한다.
					binding((ListNode)list.car(), list.cdr().car());
				//lambda를 functionNode로, 나머지 list들을 cdr로 해서 runFunction 실행
				//runFunction에서 x에 값을 집어넣는다.
				return runFunction((FunctionNode)isfuncNode, ((ListNode) list.car()).cdr());
			}
		}
		return list;
	}
	private static Node runFunction(FunctionNode operator, ListNode operand)
	{
		Node op = runExpr(operand);	//operand가 중첩이 되거나 숨겨진 값이 있을 수 있으므로 runExpr를 해줌
		Node car, cdr;
		
		if(op instanceof ListNode)	//operand가 중첩이 되거나 숨겨진 값이 있을 수 있으므로 한 번 runExpr를 해주는 데, 그렇게 얻어낸 값이 ListNode일 경우에는
		{							//operand의 원래 타입이 ListNode이므로 operand의 값으로 넣어줄 수 있다.
			operand=(ListNode) op;
			//(lambda의 경우) 여기서 (x)가 car이 되고, x에 대한 식 ( + x 1 ) 이 cdr이 된다.
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
			car = op;	//runExpr해서 나온 최종 operand의 값이 ListNode가 아닐 경우에는 car에 op를 넣어주고 더이상 ListNode가 아니므로 cdr은 EMPTY로 한다
			cdr = ListNode.EMPTYLIST;
		}
		
		switch(operator.funcType)
		{
		case CAR:

			Node carNode;		//맨 처음 operand가 QuoteNode일 경우(car이 QuoteNode이고 cdr은 EMPTY상태)
			if((operand.cdr() == ListNode.EMPTYLIST || operand.cdr() == null) && operand.car() instanceof QuoteNode)
			{	//QuoteNode안에 있는 값에 대한 car을 가져온다.
				carNode = ((ListNode) ((QuoteNode) operand.car()).nodeInside()).car();
				//ListNode나 IdNode, FunctionNode일 경우에는 '처리를 해준다.
				if(carNode instanceof ListNode || carNode instanceof IdNode || carNode instanceof FunctionNode)
					return new QuoteNode(carNode);
				else	//그 외의 노드는 그냥 값만 출력!
					return carNode;
			}		//car의 값(runExpr하여 얻은 값)이 QuotenNode일 경우
			else if(car instanceof QuoteNode)
			{	//QuoteNode 안에 있는 값에 대한 car을 가져온다.
				carNode = runFunction(operator, (ListNode)((QuoteNode) car).nodeInside());
				//ListNode나 IdNode, FunctionNode일 경우에는 '처리를 해준다.
				if(carNode instanceof ListNode || carNode instanceof IdNode || carNode instanceof FunctionNode)
					return new QuoteNode(carNode);
			}
			else carNode = car;	//QuoteNode에 있지 않은 경우에는 현재 car 값 자체가 답이므로 바로 return 해준다.
			return carNode;

		case CDR:
			Node cdrNode;		//맨 처음 operand가 QuoteNode일 경우(car이 QuoteNode이고 cdr은 EMPTY상태)
			if((operand.cdr() == ListNode.EMPTYLIST || operand.cdr() == null) && operand.car() instanceof QuoteNode)
			{	//QuoteNode안에 있는 값에 대한 cdr을 가져와서 Quote화 시킨다.
				cdrNode = ((ListNode) ((QuoteNode) operand.car()).nodeInside()).cdr();
				return new QuoteNode(cdrNode);
			}//car의 값(runExpr하여 얻은 값)이 QuotenNode일 경우
			else if(car instanceof QuoteNode)
			{	//QuoteNode 안에 있는 값에 대한 cdr을 가져온다.
				cdrNode = runFunction(operator, (ListNode)((QuoteNode) car).nodeInside());
				//ListNode나 IdNode일 경우에는 '처리를 해준다.
				if(cdrNode instanceof ListNode)
					return new QuoteNode(cdrNode);
				else
					return new QuoteNode(ListNode.cons(cdrNode, ListNode.EMPTYLIST));
			}
			else cdrNode = cdr;	//QuoteNode에 있지 않은 경우에는 현재 cdr 값 자체가 답이므로 바로 return 해준다.
			return cdrNode;
			 
		case CONS:
			Node consNode;
			if(car instanceof ListNode)
			{
				ListNode li =(ListNode)car;
				
				if(li.car() instanceof QuoteNode)	//li.car가 QuoteNode일 경우에는 그 노드의 안에 있는 노드를 cdr에 넣는다.
					consNode = runQuote(li);
				else
					consNode = car;//QuoteNode가 아니라면 그 노드 자체를 cdr에 넣는다.
			}
			else if(car instanceof QuoteNode)//car가 QuoteNode일 경우에는 car 안에 있는 노드를 cdr에 넣는다.
				consNode = ((QuoteNode) car).nodeInside();
			else
				consNode = car;
			
			Node inList;	//위 과정을 통해 cdr에 넣을 car값인 consNode를 car로, cdr(QuoteNode type) 안에 있는 노드를 cdr로 지정하여 ListNode를 만든다.
			inList = ListNode.cons(consNode, (ListNode)((QuoteNode) cdr).nodeInside());
			//만든 ListNode를 QuoteNode로 만들어서 return한다.
			return new QuoteNode(inList);
		case NULL_Q:
			if(operand == ListNode.EMPTYLIST)	//operand가 empty일 경우 true
				return BooleanNode.TRUE_NODE;
			if(car instanceof ListNode)	//car가 listNode이면 null이 아니므로 false
				return BooleanNode.FALSE_NODE;
			if(car instanceof QuoteNode)	//car가 quoteNode일 경우에는 그 안에 있는 값을 판별해야 된다.
				return runFunction(operator, (ListNode)((QuoteNode) car).nodeInside());
			else
				return BooleanNode.FALSE_NODE;

				
		case ATOM_Q:
			if(operand == ListNode.EMPTYLIST)	//null도 atom에 포함되므로 true
				return BooleanNode.TRUE_NODE;
			if(car instanceof QuoteNode)	//car가 QuoteNode일 경우에는 car의 안의 노드를 본다.
			{
				Node inNode = ((QuoteNode) car).nodeInside();	//car의 안에 있는 노드
				if(inNode instanceof ListNode)
				{	//안에 있는 노드가 ListNode일 경우에는 비어있으면 true, 아니면 false
					if(inNode == ListNode.EMPTYLIST) return BooleanNode.TRUE_NODE;
					else return BooleanNode.FALSE_NODE;
				}
				else return BooleanNode.TRUE_NODE;
			}
			else if(car instanceof ListNode)	//car가 ListNode일 경우 비어있으면 true, 아니면 false
			{
				if(car == ListNode.EMPTYLIST) return BooleanNode.TRUE_NODE;
				else return BooleanNode.FALSE_NODE;
			}
			else return BooleanNode.TRUE_NODE;	//QuoteNode나 ListNode가 아닐 경우 무조건 true
		case EQ_Q:
			Node lefteqNode;
			Node righteqNode;
			
			lefteqNode = openList(car);	//겹겹이 있을 수도 있으므로 openList해준다.
			righteqNode = openList(cdr);
			
			if(lefteqNode instanceof ListNode && righteqNode instanceof ListNode)
			{	//left와 right 노드가 둘 다 ListNode일 경우에는 둘의 id값을 비교하여 return한다.
				if(lefteqNode == righteqNode) return BooleanNode.TRUE_NODE;
				else return BooleanNode.FALSE_NODE;
			}
			else
			{	//left와 right 노드의 value를 비교한다. 같으면 true, 아니면 false
				if(lefteqNode.toString().equals(righteqNode.toString()))
					return BooleanNode.TRUE_NODE;
				else return BooleanNode.FALSE_NODE;
			}	
		case COND:
			if(car instanceof BooleanNode)	//car가 booleanNode일 경우에는 car의 값에 따라 답이 달라진다.
			{
				if(car == BooleanNode.TRUE_NODE)	//car가 true일 경우에는 car 뒤에 있는 노드를 return하며, 아니면 false이다.
					return ((ListNode) cdr).car();
				else
					return BooleanNode.FALSE_NODE;
			}
			ListNode leftNode = (ListNode)car;	//car가 booleanNode가 아닌 경우 car는 booleanNode를 return 하는 ListNode이다.
			ListNode rightNode = (ListNode)cdr;

			if(runExpr(leftNode.car()) == BooleanNode.TRUE_NODE)	//car를 runExpr해서 나온 최종 값이 true일 경우에는 그 옆에 있는 값을 return한다.
				if(leftNode.cdr().car() instanceof ListNode)
					return leftNode.cdr();	//leftNode.cdr.car이 ListNode일 경우 leftNode.cdr도, listNode.cdr.car도 list이므로 listNode.cdr를 return한다.
				else
					return leftNode.cdr().car();	//leftNode.cdr.car이 ListNode가 아닐 경우에는 그 값을 return한다.
			else if(runExpr(rightNode.car()) == BooleanNode.TRUE_NODE)	//위의 if경우와 똑같다. car의 값이 false인 경우 그 다음 리스트인 cdr를 검사한다.
				if(rightNode.cdr().car() instanceof ListNode)
					return rightNode.cdr();
				else
					return rightNode.cdr().car();
			else if(((ListNode) cdr).cdr()!=ListNode.EMPTYLIST)	//cdr.cdr이 empty가 아닐 경우 cdr도 검사 대상이 될 수 있으므로 runFunction()으로 검사한다.
					return runFunction(operator, (ListNode)cdr);
			else
				return BooleanNode.FALSE_NODE;	//default값 return false

		case NOT :
			Node boolNode = car;
			if(boolNode instanceof BinaryOpNode)	//boolNode가 BinaryOpNode일 경우 runBinary로 검사한다.
				boolNode = runBinary(operand);
			if(boolNode == BooleanNode.FALSE_NODE)	//boolNode가 false일 경우 true, true일 경우 false
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;
		
		case DEFINE :
			insertTable(((IdNode)car).toString(), cdr);//insertTable에 idNode인 car와 car의 value가 될 cdr를 넣는다.
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
	private static Node openList(Node list)	//eq를 위한 함수. list가 많은 리스트와 쿼트노드로 겹쳐져있을 때가 있으므로
	{								//list의 맨 깊숙이 있는 값을 가져와야 되므로 이 함수가 필요하다.
		if(list instanceof ListNode)
		{
			if(((ListNode) list).cdr() == ListNode.EMPTYLIST)	//겹쳐져있는 list이므로 list의 car로 넘어간다.
				return openList(((ListNode) list).car());
			else
				return list;	//list이 단순히 겹쳐지기만 한 리스트가 아니라 list.car과 list.cdr이 명확히 있는 list이므로				//이때는 그냥 바로 return해준다.
		}
		else return list;
	}
	private static void binding(ListNode lambdaNode, Node value)
	{	//여기서 ListNode lambdaNode는 ListNode.car()가 'lambda'인 ListNode이다.
		//예를들어서 ( lambda (x) (+ x 1 ) ) 이런 형태
		
		//x에 value를 binding하기 위해 변수 x를 만듦
		IdNode x = (IdNode) ((ListNode) lambdaNode.cdr().car()).car();
		//임시 binding
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
		
		if(car instanceof IdNode)	//car가 IdNode일 경우에는 define 혹은 lambda가 선언된 상태일 수도 있으므로 runExpr로 검사해준다.
			car = runExpr(car);
		if(cdr instanceof IdNode)	//cdr가 IdNode일 경우에는 define 혹은 lambda가 선언된 상태일 수도 있으므로 runExpr로 검사해준다.
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
