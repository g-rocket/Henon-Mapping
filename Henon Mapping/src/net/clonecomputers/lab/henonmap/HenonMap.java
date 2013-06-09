package net.clonecomputers.lab.henonmap;

import java.awt.*;

import javax.swing.*;

@SuppressWarnings("serial")
public class HenonMap extends JPanel {
	
	private Color[][] colors;
	private float a,b;

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		JFrame window = new JFrame("Henon Mappings");
		HenonMap hm = new HenonMap(1.3F ,0.3F);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().add(hm);
		window.setResizable(false);
		window.pack();
		window.setVisible(true);
		hm.begin();
		while(true){
			Thread.sleep(100);
			hm.step();
		}
	}

	private synchronized void begin() {
		float w = colors.length, h = colors[0].length;
		for(int x = 0; x < w; x++){
			for(int y = 0; y < h; y++){
				colors[x][y] = new Color(x/w,y/h,((x^y)%128)/256F);
			}
		}
		this.repaint();
	}
	
	private synchronized void step() {
		Color[][] tempColors = new Color[colors.length][];
		for(int i = 0; i < tempColors.length; i++){
			tempColors[i] = colors[i].clone();
		}
		for(int x = 0; x < colors.length; x++){
			for(int y = 0; y < colors[0].length; y++){
				colors[x][y] = noAlpha(colors[x][y]);
			}
		}
		for(float lx = 0; lx < tempColors.length; lx++){
			for(float ly = 0; ly < colors[0].length; ly++){
				float x = lx, y = ly;
				//if(tempColors[x][y].getAlpha() == 0) continue;
				//float oX = x,oY = y;
				x -= colors.length/2; y -= colors[0].length/2;
				x /= 10; y /= 10;
				x = (ly+1-a*x*x);
				y = (b*lx);
				x *= 10; y *= 10;
				x += colors.length/2; y += colors[0].length/2;
				//System.out.printf("(%d, %d) -> (%d, %d)\n",oX,oY,x,y);
				if(inBounds(x,y)){
					colors[(int)x][(int)y] = tempColors[(int)lx][(int)ly];
				}
			}
		}
		this.repaint();
	}

	private Color noAlpha(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		return new Color(r,g,b,0);
	}

	private boolean inBounds(float x, float y) {
		return x >= 0 && x < colors.length && y >= 0 && y < colors[0].length;
	}

	public HenonMap(float a, float b) {
		this.a = a;
		this.b = b;
		colors = new Color[512][512];
		this.setPreferredSize(new Dimension(512, 512));
	}
	
	@Override
	protected synchronized void paintComponent(Graphics g){
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		for(int x = 0; x < colors.length; x++){
			for(int y = 0; y < colors[0].length; y++){
				g.setColor(colors[x][y]);
				g.drawRect(x, y, 0, 0);
			}
		}
	}
}
