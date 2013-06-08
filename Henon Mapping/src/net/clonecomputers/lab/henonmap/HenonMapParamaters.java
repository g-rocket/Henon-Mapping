package net.clonecomputers.lab.henonmap;

public class HenonMapParamaters {

	public double a,b;

	public double minX, maxX;
	public double minY, maxY;
	public double zoomX, zoomY; // GUI zoom
	public double fSc; // fabric scale (amount starting matrix is scaled up/down)

	public boolean displaying;

	public boolean looping;

	public int stepsPerStep;
	
	public int fps;

	public float deltaAlpha;
	
	public int paramatersToCopy = 0xFFFFFFFF;

	public HenonMapParamaters(double a, double b, double minX, double maxX, 
			double minY, double maxY, double zoomX, double zoomY, double fSc,
			boolean displaying, boolean looping, int stepsPerStep,
			int fps, float deltaAlpha){
		this.a = a; this.b = b;
		
		this.minX = minX; this.maxX = maxX;
		this.minY = minY; this.maxY = maxY;
		this.zoomX = zoomX; this.zoomY = zoomY;
		this.fSc = fSc;
		
		this.displaying = displaying;
		
		this.looping = looping;
		
		this.stepsPerStep = stepsPerStep;
		
		this.fps = fps;
		
		this.deltaAlpha = deltaAlpha;
		
		paramatersToCopy = 0xFFFFFFFF;
	}
	
	public HenonMapParamaters(double a, double b){
		this(a,b,0,0,0,0,0,0,0,false,false,0,0,0);
		paramatersToCopy = HenonMap2.boolsToInt(true,true);
	}
	
	public HenonMapParamaters(double a, double b, boolean shouldStartLooping){
		this(a,b,0,0,0,0,0,0,0,false,shouldStartLooping,0,0,0);
		paramatersToCopy = HenonMap2.boolsToInt(true,true,
				false,false,false,false,false,false,false,false,true,false,false);
	}
}
