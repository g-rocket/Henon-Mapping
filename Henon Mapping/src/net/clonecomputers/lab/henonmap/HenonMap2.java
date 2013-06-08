package net.clonecomputers.lab.henonmap;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.imageio.stream.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class HenonMap2 extends JPanel implements Runnable {

	public long frameNum = 0;

	private GifSequenceWriter gsw;
	private BufferedImage canvas;
	private Pair[][] positions;
	private double a,b;

	private double minX = -20, maxX = 20;
	private double minY = -20, maxY = 20;
	private double zoomX = 1F, zoomY = 1F; // GUI zoom
	private double fSc = 1F; // fabric scale (amount starting matrix is scaled up/down)

	private double eMiX = minX / fSc, eMaX = maxX / fSc;
	private double eMiY = minY / fSc, eMaY = maxY / fSc;
	private double eZoX = zoomX * fSc, eZoY = zoomY * fSc;

	public boolean displaying = true;

	private boolean shouldStopLooping = false;

	private boolean looping = true;

	private int stepsPerStep = 1;

	private int fps = 5;

	private float deltaAlpha = 1F;

	private long nextFrameTime;

	private boolean savingGIF;

	private Queue<KeyEvent> keysToDealWith = new LinkedList<KeyEvent>();

	public HenonMapParamaters getParamaters(){
		return new HenonMapParamaters(a, b,
				minX, maxX, minY, maxY, zoomX, zoomY, fSc,
				displaying, looping, stepsPerStep, fps, deltaAlpha);
	}

	public void setParamaters(HenonMapParamaters hmp){
		setSomeParamaters(hmp,hmp.paramatersToCopy);
		/*this.a = hmp.a; this.b = hmp.b;

		this.minX = hmp.minX; this.maxX = hmp.maxX;
		this.minY = hmp.minY; this.maxY = hmp.maxY;
		this.zoomX = hmp.zoomX; this.zoomY = hmp.zoomY;
		this.fSc = hmp.fSc;

		eMiX = minX / fSc; eMaX = maxX / fSc;
		eMiY = minY / fSc; eMaY = maxY / fSc;
		eZoX = zoomX * fSc; eZoY = zoomY * fSc;

		this.displaying = hmp.displaying;

		this.looping = hmp.looping;

		this.stepsPerStep = hmp.stepsPerStep;*/
	}

	public void setSomeParamaters(HenonMapParamaters hmp, int whichOnesAsBits){
		Boolean[] ba = intToBoolA(whichOnesAsBits);

		if(ba[0]) this.a = hmp.a;
		if(ba[1]) this.b = hmp.b;

		if(ba[2]) this.minX = hmp.minX; 
		if(ba[3]) this.maxX = hmp.maxX;
		if(ba[4]) this.minY = hmp.minY;
		if(ba[5]) this.maxY = hmp.maxY;
		if(ba[6]) this.zoomX = hmp.zoomX;
		if(ba[7]) this.zoomY = hmp.zoomY;
		if(ba[8]) this.fSc = hmp.fSc;

		eMiX = minX / fSc; eMaX = maxX / fSc;
		eMiY = minY / fSc; eMaY = maxY / fSc;
		eZoX = zoomX * fSc; eZoY = zoomY * fSc;

		if(ba[9]) this.displaying = hmp.displaying;

		if(ba[10]) this.looping = hmp.looping;

		if(ba[11]) this.stepsPerStep = hmp.stepsPerStep;

		if(ba[12]) this.fps = hmp.fps;
		if(ba[13]) this.deltaAlpha = hmp.deltaAlpha;
	}

	public static Boolean[] intToBoolA(int i){
		LinkedList<Boolean> l = new LinkedList<Boolean>();
		for(int j = 0; j < 32; j++){
			l.addFirst((i & 1) == 1);
			i = i >> 1;
		}
		return l.toArray(new Boolean[0]);
	}

	public static int boolsToInt(boolean... bools){
		int i = 0;
		for(boolean b: bools){
			if(b) i |= 1;
			i = i << 1;
		}
		return i;
	}

	private class HenonMapWindowKeyListener extends KeyAdapter{
		private HenonMap2 hm;
		private JFrame window;
		private int w, h;
		private BufferedReader configurations;

		public HenonMapWindowKeyListener(HenonMap2 hm, JFrame window, 
				int w, int h, BufferedReader configurations){
			this.hm = hm;
			this.window = window;
			this.w = w;
			this.h = h;
			this.configurations = configurations;
		}

		@Override
		public void keyReleased(KeyEvent e) {
			keysToDealWith.add(e);
			synchronized(hm){
				hm.notifyAll();
			}
		}
	}

	public void dealWithKey(int keyCode, int modifiers, String keyText){
		HenonMapWindowKeyListener kl = 
				(HenonMapWindowKeyListener) this.getTopLevelAncestor()
				.getComponentAt(2, 2).getKeyListeners()[0];
		HenonMap2 hm = kl.hm; // used to be in HenonMapWindowKeyListener
		JFrame window = kl.window;
		int w = kl.w, h = kl.h;
		BufferedReader configurations = kl.configurations;
		switch(keyCode) {
		case KeyEvent.VK_SPACE:
			System.out.println("toggling display");
			hm.displaying = !hm.displaying;
			if(hm.displaying) hm.updateCanvas();
			break;
		case KeyEvent.VK_F:
			System.out.println(hm.frameNum);
			break;
		case KeyEvent.VK_N:
			hm.destroy();

			next(window, w, h, configurations);
			break;
		case KeyEvent.VK_R:
			hm.destroy();
			next(hm.getParamaters(), window, w, h, configurations);
			break;
		case KeyEvent.VK_P:
			hm.looping = !hm.looping;
			synchronized(hm){
				hm.notifyAll();
			}
			break;
		case KeyEvent.VK_COMMA:
			if(!hm.looping){
				hm.step();
				hm.updateCanvas();
			}
			break;
		case KeyEvent.VK_PERIOD:
			if(!hm.looping){
				hm.step();
				hm.step();
				hm.updateCanvas();
			}
			break;
		case KeyEvent.VK_SLASH:
			if(!hm.looping){
				hm.step();
				hm.step();
				hm.step();
				hm.step();
				hm.updateCanvas();
			}
			break;
		case KeyEvent.VK_Q:
			hm.destroy();
			window.dispose();
			System.exit(0);
			break;
		case KeyEvent.VK_S:
			String newFps = JOptionPane.showInputDialog(window,
					"Please input new fps");
			if(newFps == null) break;
			fps = Integer.parseInt(newFps);
			break;
		case KeyEvent.VK_A:
			String newDA = JOptionPane.showInputDialog(window,
					"Please input new background fade between frames");
			if(newDA == null) break;
			deltaAlpha = Float.parseFloat(newDA);
			break;
		case KeyEvent.VK_O:
			save();
			break;
		case KeyEvent.VK_G:
			if(savingGIF){
				finalizeGif();
			}else{
				boolean wasLooping = hm.looping;
				if(KeyEvent.getKeyModifiersText(modifiers)
						.toLowerCase().contains("shift")) {
					System.out.println("shift");
					hm.destroy();
					HenonMapParamaters hmp = hm.getParamaters();
					hmp.looping = false;
					next(hmp, window, w, h, configurations);
					for(int i = 0; i < 100; i++) Thread.yield();
					while(hm == null) Thread.yield();
					beginGIF(true);
					hm.looping = wasLooping;
					synchronized(hm){
						hm.notifyAll();
					}
				}else{
					System.out.println("no shift");
					beginGIF(false);
				}
			}
			break;
			/*case KeyEvent.VK_1:
				hm.stepsPerStep = 1;
				break;
			case KeyEvent.VK_2:
				if(hm.stepsPerStep != 2) hm.stepsPerStep = 2;
				else stepsPerStep = 1;
				break;
			case KeyEvent.VK_4:
				if(hm.stepsPerStep != 2) hm.stepsPerStep = 4;
				else stepsPerStep = 1;
				break;*/
		case KeyEvent.VK_0:
		case KeyEvent.VK_1:
		case KeyEvent.VK_2:
		case KeyEvent.VK_3:
		case KeyEvent.VK_4:
		case KeyEvent.VK_5:
		case KeyEvent.VK_6:
		case KeyEvent.VK_7:
		case KeyEvent.VK_8:
		case KeyEvent.VK_9:
			hm.stepsPerStep = Integer.parseInt(keyText);
			break;
		default:
			System.err.println("what do you mean by " + keyText);
		}
	}

	public static void next(JFrame window, int w, int h,
			BufferedReader configurations){
		next(null, null, window, w, h, configurations);
	}

	public static void next(Pair ab, JFrame window, int w, int h){
		next(null, ab, window, w, h, null);
	}

	public static void next(JFrame window, int w, int h){
		next(null, null, window, w, h, null);
	}

	public static void next(HenonMapParamaters hmp, JFrame window, int w, int h){
		next(hmp, null, window, w, h, null);
	}

	public static void next(HenonMapParamaters hmp, JFrame window, int w, int h,
			BufferedReader configurations){
		next(hmp, null, window, w, h, configurations);
	}

	public static void next(HenonMapParamaters hmp, Pair ab, JFrame window,
			int w, int h, BufferedReader configurations){
		if(ab == null && hmp == null){
			if(configurations == null){
				ab = HenonMap2.askForNewAB(window);
				if(ab == null) return;
			}else{
				String thisConfig = "not a valid config";
				try {
					while(thisConfig != null && 
							(thisConfig.equals("not a valid config")
									|| thisConfig.startsWith("#"))){
						thisConfig = configurations.readLine();
					}
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
				if(thisConfig == null){
					next(window, w, h, null);
					return;
				}
				System.out.println(thisConfig);
				String[] abStr = thisConfig.split("\t");
				ab = new Pair(Double.parseDouble(abStr[0]),
						Double.parseDouble(abStr[1]));
			}
		}
		if(ab != null){
			hmp = new HenonMapParamaters(ab.x,ab.y);
		}
		try {
			window.dispose();
			HenonMap2.display(hmp, w, h, configurations);
		} catch (InterruptedException ie) {
			throw new RuntimeException(ie);
		}
	}

	public static void display(double a, double b,
			int w, int h, boolean shouldStartLooping) throws InterruptedException{
		display(new HenonMapParamaters(a, b, shouldStartLooping), w, h, null);
	}

	public static void display(int w, int h, boolean shouldStartLooping,
			BufferedReader configurations) throws InterruptedException{
		String thisConfig = null;
		try {
			while(thisConfig == null || thisConfig.startsWith("#")){
				thisConfig = configurations.readLine();
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		System.out.println(thisConfig);
		String[] abStr = thisConfig.split("\t");
		Pair ab = new Pair(Double.parseDouble(abStr[0]),
				Double.parseDouble(abStr[1]));
		display(new HenonMapParamaters(ab.x, ab.y, shouldStartLooping),
				w, h, configurations);
	}

	public static void display(double a, double b,
			int w, int h, boolean shouldStartLooping,
			BufferedReader configurations) throws InterruptedException{
		display(new HenonMapParamaters(a, b, shouldStartLooping),
				w, h, configurations);
	}

	public static void display(HenonMapParamaters hmp,
			int w, int h, BufferedReader configurations) throws InterruptedException{
		JFrame window = new JFrame("Henon Mappings");
		HenonMap2 hm = new HenonMap2(hmp.a, hmp.b, w, h);
		hm.addMouseListener(hm.new ClickCoordinatesEchoMouseListener());
		window.addKeyListener(hm.new HenonMapWindowKeyListener(hm, window, w, h,
				configurations));
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().add(hm);
		window.setResizable(false);
		window.pack();
		window.setVisible(true);
		hm.setParamaters(hmp);
		new Thread(hm).start();
	}

	private class ClickCoordinatesEchoMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			System.out.println(HenonMap2.this.toMath(new Pair(e.getX(),e.getY())));
		}
	}

	private void loop() throws InterruptedException {
		while(!shouldStopLooping){
			while(keysToDealWith.size() > 0){
				KeyEvent e = keysToDealWith.remove();
				dealWithKey(e.getKeyCode(), e.getModifiers(),
						KeyEvent.getKeyText(e.getKeyCode()));
			}
			if(displaying) {
				long sleepTime = nextFrameTime - System.currentTimeMillis();
				if(sleepTime > 0) Thread.sleep(sleepTime);
				else System.out.println("can't keep up");
				nextFrameTime = System.currentTimeMillis() + 1000/fps;
				if(savingGIF){
					saveOneFrameOfGIF();
				}
			}
			if(!looping) synchronized(this){
				this.wait();
			}
			if(looping) for(int i = 0; i < stepsPerStep; i++) step();
			if(displaying) updateCanvas();
		}
	}

	protected static Pair askForNewAB(JFrame window) {
		String input = JOptionPane.showInputDialog(window, 
				"Please input new paramaters in the form \"a, b\"");
		if(input == null) return null;
		String[] ab = input.split(",");
		for(int i = 0; i < ab.length; i++) ab[i] = ab[i].trim();
		return new Pair(Double.parseDouble(ab[0]), Double.parseDouble(ab[1]));
	}

	protected void destroy() {
		shouldStopLooping = true;
		synchronized(this){
			this.notifyAll();
		}
		// clean up
	}

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args)
			throws InterruptedException, FileNotFoundException {
		switch(args.length){
		case 0:
			HenonMap2.display(.05, 1.05, 750, 750, true);
			break;
		case 1:
			File f = new File(args[0]);
			BufferedReader in = new BufferedReader(new FileReader(f));
			HenonMap2.display(750, 750, true, in);
			break;
		case 2:
			HenonMap2.display(Double.parseDouble(args[0]),
					Double.parseDouble(args[1]), 750, 750, true);
			break;
		default:
			StringBuilder sb = new StringBuilder();
			for(String s: args){
				sb.append(s);
				sb.append(" ");
			}
			sb.deleteCharAt(sb.length()-1);
			HenonMap2.main(new String[]{sb.toString()});
			//System.out.println("I dont understand " + args.length + " arguments");
		}
	}

	public HenonMap2(double a, double b, int width, int height) {
		this.a = a;
		this.b = b;
		positions = new Pair[width][height];
		canvas = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		this.setPreferredSize(new Dimension(width, height));
	}

	private void step() {
		for(int lx = 0; lx < positions.length; lx++){
			for(int ly = 0; ly < positions[0].length; ly++){
				//if(tempColors[x][y].getAlpha() == 0) continue;
				//double oX = x,oY = y;
				double x = positions[lx][ly].x, y = positions[lx][ly].y;
				positions[lx][ly].x = (y+1-a*x*x);
				positions[lx][ly].y = (b*x);
				//System.out.printf("(%d, %d) -> (%d, %d)\n",oX,oY,x,y);
			}
		}
		frameNum++;
	}

	private void begin() {
		nextFrameTime = System.currentTimeMillis() + 1000/fps;
		for(int i = 0; i < positions.length; i++){
			for(int j = 0; j < positions[i].length; j++){
				positions[i][j] = toMath(new Pair(i,j));
			}
		}
		if(displaying) updateCanvas();
	}

	private Pair toMath(Pair p){
		return new Pair((p.x/getWidth())*(eMaX-eMiX)+eMiX,(p.y/getHeight())*(eMiY-eMaY)+eMaY);
	}

	private Pair toGUI(Pair p){
		return new Pair(((p.x/eZoX)-eMiX)/(eMaX-eMiX),(p.y/eZoY-eMaY)/(eMiY-eMaY));
	}

	private boolean inBounds(double x, double y) {
		return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
	}
	/**
	 * @param p from toGUI(Pair) (p.x and p.y are b/w 0 and 1)
	 * @return cool color scheme
	 */
	private Color getColor(Pair p){
		//double h = ((int)(p.x*1000)^(int)(p.y*1000)%512)/512F;
		//return Color.getHSBColor(h, 1, 1);
		double xD = Math.abs(1-(2*p.x));
		double yD = Math.abs(1-(2*p.y));
		double bVal = chop(1-(xD + yD),0,1);
		return new Color((float)p.x,(float)p.y,(float)bVal);
	}

	private double chop(double val, double min, double max){
		if(val < min) return min;
		if(val > max) return max;
		return val;
	}

	private synchronized void updateCanvas(){
		if(positions[0][0] == null) return;
		Graphics g = canvas.getGraphics();
		g.setColor(new Color(0F,0F,0F,deltaAlpha));
		g.fillRect(0, 0, getWidth(), getHeight());
		for(int x = 0; x < positions.length; x++){
			for(int y = 0; y < positions[0].length; y++){
				Pair p = toGUI(positions[x][y]);
				if(inBounds(p.x*getWidth(),p.y*getHeight())){
					canvas.setRGB(x, y, getColor(p).getRGB());
				}
			}
		}
		this.repaint();
	}

	@Override
	protected synchronized void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(canvas, null, null);
	}

	@Override
	public void run() {
		try {
			begin();
			loop();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void save(){
		boolean wasPaused = looping;
		looping = false;
		try{
			JFileChooser fc = new JFileChooser();
			fc.showSaveDialog(HenonMap2.this);
			BufferedOutputStream os = null;
			String extension;
			File f = fc.getSelectedFile();
			if(f == null){
				System.out.println("canceled");
				return;
			}
			String[] a = f.getName().split(".");
			if(a.length < 2) extension = "png";
			else extension = a[a.length-1];
			try {
				os = new BufferedOutputStream(
						new FileOutputStream(f));
			} catch (FileNotFoundException e1) {
				try {
					System.out.println("can't find "+
							f.getCanonicalPath());
				} catch (IOException ioe1) {
					throw new RuntimeException(ioe1);
				}
			}
			try{
				ImageIO.write(canvas, extension, os);
			}finally{
				os.close();
			}
		}catch(Exception e){
			System.err.println("Exception while saving:");
			e.printStackTrace();
		}
		looping = wasPaused;
		if(looping) synchronized(this){
			this.notifyAll();
		}
	}

	public void beginGIF(boolean fromReset) {
		if(fromReset){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		try{
			boolean wasPaused = false;
			if(!fromReset){
				wasPaused = looping;
				looping = false;
			}
			JFileChooser fc = new JFileChooser();
			fc.showSaveDialog(HenonMap2.this);
			ImageOutputStream os = null;
			try {
				os = ImageIO.createImageOutputStream(new BufferedOutputStream(
						new FileOutputStream(fc.getSelectedFile())));
			} catch (FileNotFoundException e1) {
				System.err.println("can't find "+
						fc.getSelectedFile().getCanonicalPath());
			}
			gsw = new GifSequenceWriter(os, canvas.getType(), 1/fps, true);
			savingGIF = true;
			if(!fromReset){
				looping = wasPaused;
				if(looping) synchronized(this){
					this.notifyAll();
				}
			}
		}catch(Throwable t){
			System.err.println("problem while starting gif");
			t.printStackTrace();
		}
	}

	public void finalizeGif() {
		boolean wasPaused = looping;
		looping = false;
		Thread.yield();
		try{
			savingGIF = false;
			gsw.close();
			//((OutputStream)gsw.gifWriter.getOutput()).close();
		}catch(IOException e){
			System.err.println("Problem while finalizing gif");
			e.printStackTrace();
		}
		looping = wasPaused;
		if(looping) synchronized(this){
			this.notifyAll();
		}
	}

	public void saveOneFrameOfGIF() {
		try {
			gsw.writeToSequence(canvas);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
