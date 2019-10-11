package parser.ast;

public interface ListNode extends Node{

	static ListNode EMPTYLIST = new ListNode() {
		@Override
		public Node car()
		{
			return null;
		}
		@Override
		public ListNode cdr()
		{
			return null;
		}
	};
	
	static ListNode cons(Node head, ListNode tail)
	{
		return new ListNode() {
			@Override
			public Node car()
			{
				return head;
			}
			@Override
			public ListNode cdr()
			{
				return tail;
			}
		};
	}
	Node car();
	ListNode cdr();
}
