package parser.ast;

import java.util.Objects;

public class IntNode implements ValueNode {

	private Integer value;
	
	public IntNode(String text)
	{
		this.value = new Integer(text);
	}
	public Integer getValue() 
	{
		return value;
	}
	@Override
	public boolean equals(Object o)
	{
		if(this==o) return true;
		if(!(o instanceof IntNode)) return false;
		IntNode intNode = (IntNode) o;
		return Objects.equals(value, intNode.value);
	}
	@Override
	public String toString(){
		return ""+value;
	}
	
}
