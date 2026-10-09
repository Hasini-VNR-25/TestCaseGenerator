package batch_3225;
import java.util.Scanner;
interface IConverter{
	double convert(double input);
}
class DollarToINRConverter implements IConverter{
	public double convert(double input) {
		return 95*input;
	}
}
class YENToINRConverter implements IConverter{
	public double convert(double input) {
		return 0.62*input;
	}
}
class EUROToINRConverter implements IConverter{
	public double convert(double input) {
		return 110.75*input;
	}
}

public class CurConverter {

	public static void main(String[] args) 
	{	Scanner sc=new Scanner(System.in);
		System.out.println("Enter Amount:");
		double amt=sc.nextDouble();
		
		IConverter obj=null;
		System.out.println("1.Dollar 2.YEN 3.EURO");
		System.out.println("Enter option to convert:");
		int opt=sc.nextInt();

		switch(opt) {
		case 1-> obj=new DollarToINRConverter();
		case 2->obj=new YENToINRConverter();
		case 3->obj=new EUROToINRConverter();
		}
		
		
		if(obj!=null) {
			System.out.println("After Converion: "+ obj.convert(amt));
		}

	}

}
