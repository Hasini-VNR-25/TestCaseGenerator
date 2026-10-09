package batch_3225;
class Animal{
	void sound (){
		System.out.println("Oooooo");
	}
	static void play() {
		System.out.println("Hockey");
	}
}
class Dog extends Animal{
	void sound() {
		System.out.println("Bow Bow Bow");
	}
	static void play() {
		System.out.println("Kabaddi");
	}
}
class Cat extends Animal{
	void sound() {
		System.out.println("Meow Meow Meow");
	}
	static void play() {
		System.out.println("Tennis");
	}
}

public class Driver3 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Animal obj=new Animal();
		obj.sound();//Oooooo
		Animal obj2;
		obj2=new Dog();
		obj2.sound();//bow bow bow
		obj2=new Cat();
		obj2.sound();//Meow Meow Meow
		//all above are called based on object type
		
		Animal obj3;
		obj3=new Animal();
		obj3.play();//hockey
		obj3=new Dog();
		obj3.play();//hockey
		obj3=new Cat();
		obj3.play();//hockey
		//all above are called based on Reference Type i.e Animal
	}
	/*
	 * In java, we can over ride only NON STATIC METHODS
	 * we can hide STATIC METHODS
	 * */

}
