package batch_3225;

import java.util.Scanner;
public class OneDimArray {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int x[]=new int[5];
		System.out.println(x[4]);
		System.out.println(x[2]);
//		System.out.println(x[99]);
		
		Scanner sc=new Scanner(System.in);
		for(int i=0;i<5;i++) {
			System.out.println("enter element "+ i);
			x[i]=sc.nextInt();
		}
		System.out.println("array elements are:");
		/*
		 * for(int i=0;i<=5;i++) { System.out.println(x[i]); }
		 */
		
		//for each loop
		for(int y:x) {
			System.out.println(y);
		}
		sc.close();
	}

}
