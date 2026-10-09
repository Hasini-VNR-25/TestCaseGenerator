package batch_3225;

import java.util.Scanner;

public class Quadratic {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int a,b,c,d;
		Scanner sc =new Scanner(System.in);
		System.out.println("enter a,b,c values: ");
		a=sc.nextInt();
		b=sc.nextInt();
		c=sc.nextInt();
		d=b*b-4*a*c;
		double root1=((-b+Math.sqrt(d))/2*a);
		double root2=((-b-Math.sqrt(d))/2*a);
		if(d==0) {
			System.out.println("roots are:"+ root1 +"and"+ root2);
		}
		else if(d>0) {
			System.out.println("roots are:"+ root1 +"and"+ root2);
		}
		else {
			System.out.println("imaginary roots");
		}
		sc.close();
	}

}
