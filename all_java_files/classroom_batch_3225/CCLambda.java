package batch_3225;
import java.util.Scanner;

@FunctionalInterface
interface IConverter1{
	double convert(double input);
}
public class CCLambda {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter Amount:");
		double amt=sc.nextDouble();
		
		IConverter1 obj=null;
		System.out.println("1.Dollar 2.YEN 3.EURO");
		System.out.println("Enter option to convert:");
		int opt=sc.nextInt();

		switch(opt) {
		case 1: obj=(input)->95*amt;
		break;
		case 2:obj=(input)->0.62*amt;
		break;
		case 3:obj=(input)->110.75*amt;
		break;
		}
		
		if(obj!=null) {
			System.out.println("After Converion: "+ obj.convert(amt));
		}
		sc.close();
	}
}


