package batch_3225;

class Parent{
	void eat() {
		System.out.println("thinnava?");
	};
	void sleep() {
		System.out.println("slept ahh");
	}
	Parent random(){
		System.out.println("Idhi chuduuuu thikka ga undi");
		return null;
	}
}

class Child extends Parent{
	void eat() {
		System.out.println("Thinnnuuuuuu");
	};
	Child random() {
		System.out.println("entra idhi");
		return null;
	}
}
class DMD {

	public static void main(String ar[]) {
		// TODO Auto-generated method stub
		Parent x=new Child();
		x.eat();
		x.random();
		x.sleep();

	}

}
