package batch_3225;

class MyThread1 extends Thread{
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
class MyThread2 extends Thread{
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

public class MultiThreading {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MyThread1 mt1=new MyThread1();
		MyThread2 mt2=new MyThread2();
		mt1.setName("Hasini");
		mt2.setName("Diya");
		mt1.start();
		mt2.start();
		
		
	}

}
