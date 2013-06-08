package net.clonecomputers.lab.henonmap;
public class Pair{
	public double x;
	public double y;
	public Pair(double x, double y){
		this.x = x;
		this.y = y;
	}
	public Pair(){
		x = 0;
		y = 0;
	}
	public String toString(){
		return "(" + x + ", " + y + ")";
	}
	
	/**
	 * ONLY FOR TESTING MISC CODE
	 * has nothing to do with the pair class
	 * @param args
	 */
	public static void main(String[] args){
		System.out.println(java.util.Arrays.toString(
				javax.imageio.ImageIO.getWriterFormatNames()));
	}
	
}