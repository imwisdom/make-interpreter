package parser.ast;

import java.util.Objects;

public class IdNode implements ValueNode{
	
	String idString;
	
	public IdNode(String text)
	{
		idString = text;
	}
	@Override
	public boolean equals(Object o)
	{
		if(this==o) return true;
		if(!(o instanceof IdNode)) return false;
		IdNode idNode = (IdNode) o;
		return Objects.equals(idString, idNode.idString);
	}
	@Override
	public String toString()
	{
		return idString;
	}
	
}
