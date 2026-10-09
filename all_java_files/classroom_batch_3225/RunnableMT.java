package batch_3225;
class MyThread11 implements Runnable{
	public void run() {
		try {
			int  i;
			for(i=1;i<=5;i++) {
				System.out.println(Thread.currentThread().getName()+" "+ i);
				Thread.sleep(1000);
			}
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}
class MyThread12 implements Runnable{
	public void run() {
		try {
			int i;
			for(i=11;i<=15;i++) {
				System.out.println(Thread.currentThread().getName()+" "+ i);
				Thread.sleep(3000);
			}
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}
public class RunnableMT {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MyThread11 mt1=new MyThread11();
		MyThread12 mt2=new MyThread12();
		Thread t1=new Thread(mt1);
		Thread t2=new Thread(mt2);
		t1.setName("Chinnu");
		t2.setName("Vicky");
		t1.start();
		t2.start();

	}

}
