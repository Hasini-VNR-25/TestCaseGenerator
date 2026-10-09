package batch_3225;

abstract class NoObjCls{
	int x,y;
	abstract void eat();
	abstract void work();
	void sleep() {
		System.out.println("Sleep by 11pm");
	};
}
class SubCls extends NoObjCls{
	void eat() {
		System.out.println("Biryani for dinner");
	}
	void work() {
		System.out.println("Complete CBP");
	}
}

public class AbstractClasses {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NoObjCls a=new SubCls();
		a.work();
		a.eat();
		a.sleep();
		

	}

}
