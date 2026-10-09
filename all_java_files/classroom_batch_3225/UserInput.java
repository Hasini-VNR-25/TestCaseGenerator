package batch_3225;

import java.util.Scanner;

public class UserInput {

	public static void main(String[] args) {
//		reading data from user and adding
//		create the object of Scanner class using new operator.
//		constructor used to construct an object
		
		
		int a,b,c;
		Scanner sc =new Scanner(System.in);//object of Scanner class
		System.out.println("enter a value: ");
		//new String();//object of String class
		a=sc.nextInt();
		System.out.println("enter b value: ");
		b=sc.nextInt();
		c=a+b;
		System.out.println("a+b="+c);		
		sc.close();//to free sc

	}

}
