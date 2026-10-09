package batch_3225;

	
 class Box1{
		double length, breadth, height;
		void boxValues(double length,double breadth,double height){
			 this.length=length;
			 this.breadth=breadth;
			 this.height=height;
			
		}
		double Compute(){
			return length*breadth*height;
		}
	}
	
 public class Version2{
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//THIS KEYWORD
		Box1 b=new Box1();
		b.boxValues(10,20,30);
		double vol=b.Compute();
		System.out.println(vol);
	}
 }