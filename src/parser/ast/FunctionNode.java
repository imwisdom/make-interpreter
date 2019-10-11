package parser.ast;

import java.util.HashMap;
import java.util.Map;

import lexer.TokenType;

// ���⸦ �ۼ��ϰ� �ص� ���� �� 18.4.10
//BinaryOpNode�� �����Ͽ� �ۼ��Ͽ���.
public class FunctionNode implements ValueNode{
	
	public enum FunctionType {
		//FunctionNode type��
		ATOM_Q { TokenType tokenType() {return TokenType.ATOM_Q;}},
		CAR { TokenType tokenType() {return TokenType.CAR;}},
		CDR { TokenType tokenType() {return TokenType.CDR;}},
		COND { TokenType tokenType() {return TokenType.COND;}},
		CONS { TokenType tokenType() {return TokenType.CONS;}},
		DEFINE { TokenType tokenType() {return TokenType.DEFINE;}},
		EQ_Q { TokenType tokenType() {return TokenType.EQ_Q;}},
		LAMBDA { TokenType tokenType() {return TokenType.LAMBDA;}},
		NOT { TokenType tokenType() {return TokenType.NOT;}},
		NULL_Q { TokenType tokenType() {return TokenType.NULL_Q;}};
		
		private static Map<TokenType, FunctionType> fromFunctionType = new HashMap<TokenType, FunctionType>();
		
		static {
			for(FunctionType fType : FunctionType.values())
			{	//token type�� function type�� �����Ͽ� map�� �ִ´�
				fromFunctionType.put(fType.tokenType(), fType);
			}
		}
		static FunctionType getFunctionType(TokenType tType)
		{	//token type�� �̿��� function type�� return�Ѵ�
			return fromFunctionType.get(tType);
		}
		//functiontype�� ���� tokenType() �Լ��� ������ �޶����Ƿ� abstract ���ѳ��´�.
		abstract TokenType tokenType();
		
		
	}
	public FunctionType funcType;
	
	@Override	//value�� name�� return
	public String toString(){
		return funcType.name();
	}
	//value�� �ش��ϴ� functiontype�� set�Ѵ�
	public void setValue(TokenType tType) {
		
		FunctionType fType = FunctionType.getFunctionType(tType);
		funcType = fType;
	
	}
}
