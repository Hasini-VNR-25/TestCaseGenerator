package batch_3225;

import java.util.Scanner;

public class PrimeRange {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	Scanner sc=new Scanner(System.in);
	int lower,upper,i;
	System.out.println("enter lower boundary:");
	lower=sc.nextInt();
	System.out.println("enter upper boundary:");
	upper=sc.nextInt();
	System.out.println("Prime numbers in given range are:");
	int num=lower;
	
	for(num=lower;num<=upper;num++) 
	{
		int flag=1;
		for(i=2;i<=num/2;i++) 
		{
			if(num%i==0) 
			{
				flag=0;
				break;
			}
		}
		if(flag==1) 
		{
			System.out.println(num);

		}
	}
	sc.close();
	
	}

}
