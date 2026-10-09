package batch_3225;

public class Switch {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/*
		 * int x=2; int y=switch(x) { case 1 -> 100; case 2 ->
		 * {System.out.println("Task(printing) before returing value"); yield 200;} case
		 * 3 -> 300; default -> 400; }; System.out.println("The value of y is " + y);
		 */
		 		
		int x=4;
		int y=switch(x) {
		case 1,2,3 -> 100;
		case 4,5,6 -> {System.out.println("Hasiiiii'");
						yield 200;
			}
		default -> 400;
		};
		System.out.println("The value of y is " + y);
	}

}
