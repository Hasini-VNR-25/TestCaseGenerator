package batch_3225;

class A{
	int x=2,y=6;
	private int z=4;
}
class B extends A{
	int i=3,j=5,k=7;
	
}

public class Driver {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		A obj1=new A();
		B obj2=new B();
		System.out.println(obj1.x+obj1.y);
		//System.out.println(a.z);
		System.out.println(obj2.i);
		System.out.println(obj2.j);
		System.out.println(obj2.k);
		System.out.println(obj2.x);
		System.out.println(obj2.y);
	}

}
