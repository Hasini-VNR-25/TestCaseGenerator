package batch_3225;

class Box{
	double height,width,depth;
	double calcVol() {
		System.out.println(this);
		return height*width*depth;
	}
	void setValues(double h, double w, double d) {
		height=h;
		width=w;
		depth=d;
	}
}
class BoxDemo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Box b1=new Box();
//		System.out.println(b1);//gives reference
//		b1.height=10;
//		b1.width =20;
//		b1.depth=30;
//		System.out.println(b1.calcVol());
		b1.setValues(10, 20, 30);
		double vol1 = b1.calcVol();
		System.out.println(vol1);
	}

}
