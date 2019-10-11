package parser.ast;

import java.util.HashMap;
import java.util.Map;

import lexer.TokenType;

// 여기를 작성하게 해도 좋을 듯 18.4.10
//BinaryOpNode를 참고하여 작성하였다.
public class FunctionNode implements ValueNode{
	
	public enum FunctionType {
		//FunctionNode type들
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
			{	//token type과 function type을 매핑하여 map에 넣는다
				fromFunctionType.put(fType.tokenType(), fType);
			}
		}
		static FunctionType getFunctionType(TokenType tType)
		{	//token type을 이용해 function type을 return한다
			return fromFunctionType.get(tType);
		}
		//functiontype에 따라서 tokenType() 함수의 내용이 달라지므로 abstract 시켜놓는다.
		abstract TokenType tokenType();
		
		
	}
	public FunctionType funcType;
	
	@Override	//value의 name을 return
	public String toString(){
		return funcType.name();
	}
	//value에 해당하는 functiontype을 set한다
	public void setValue(TokenType tType) {
		
		FunctionType fType = FunctionType.getFunctionType(tType);
		funcType = fType;
	
	}
}
