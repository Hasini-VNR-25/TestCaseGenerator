package batch_3225;


class SuperClass
{
	void add() {
		System.out.println("Super Class Version");
	}
	void mul() {
		System.out.println("Mul Method Output");
	}
	static 
	void display() {
		System.out.println("Pulihora");
	}
}
class SubClass extends SuperClass
{	/* 
	RULES FOR OVER RIDING
	same method signature
	same return type
	checked exception thrown by the over riding method cannot be broader than over ridden method
	over riding method must not have weaker access privilege (private is weaker than default->throws error)
	*/
	void add()  {
		System.out.println("Sub Class Version");
	}
	void div() {
		System.out.println("Div Method Output");
	}
	static void display() {
		System.out.println("Dhadhojanam");
	}
}

public class MethodOverRiding1 {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SuperClass Mom= new SuperClass(); 
		SubClass daughter= new SubClass();
		SuperClass obj3=new SubClass();
		//can access parents own methods + over ridden method
		
		obj3.mul();
		obj3.add();
		//obj3.div();
		obj3.display();
		
		/*
		Mom.add();
		daughter.add();
		*/
		
	}
}