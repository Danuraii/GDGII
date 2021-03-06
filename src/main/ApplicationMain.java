package main;

import ddf.minim.*;
import ddf.minim.analysis.*;
import model.Wall;
import processing.core.PApplet;

public class ApplicationMain extends PApplet{

	//fields
	Minim minim;
	AudioPlayer song;
	FFT fft;
	private final int BACKGROUND_COLOR = 0;
	//walls
	int numWalls = 500;
	Wall[] walls;
	
	//TODO
	// Variables qui définissent les "zones" du spectre
	// Par exemple, pour les basses, on prend seulement les premières 4% du spectre total
	float specLow = (float) 0.03; // 3%
	float specMid = (float) 0.125;  // 12.5%
	float specHi = (float) 0.20;   // 20%

	// Il reste donc 64% du spectre possible qui ne sera pas utilisé. 
	// Ces valeurs sont généralement trop hautes pour l'oreille humaine de toute facon.

	// Valeurs de score pour chaque zone
	float scoreLow = 0;
	float scoreMid = 0;
	float scoreHi = 0;

	// Valeur précédentes, pour adoucir la reduction
	float oldScoreLow = scoreLow;
	float oldScoreMid = scoreMid;
	float oldScoreHi = scoreHi;

	// Valeur d'adoucissement
	float scoreDecreaseRate = 25;

	
	
	public void settings(){
		 fullScreen(P3D);
		
	}
    
	
	public void setup(){

		 //load minim
		 minim = new Minim(this);
		 //load song
		 song = minim.loadFile("aphex.mp3");
		 
		 //load fft
		 fft = new FFT(song.bufferSize(), song.sampleRate());
		 
		  //visible walls
		  walls = new Wall[numWalls];

		  
		  //create wall objects
		  //Walls left
		  for (int i = 0; i < numWalls; i+=4) {
		   walls[i] = new Wall(0, height/2, 10, height, this); 
		  }
		  
		  //walls right
		  for (int i = 1; i < numWalls; i+=4) {
		   walls[i] = new Wall(width, height/2, 10, height, this); 
		  }
		  
		  //walls bottom
		  for (int i = 2; i < numWalls; i+=4) {
			  walls[i] = new Wall(width/2, height, width, 10, this); 
		  }
		  
		  //walls top
		  for (int i = 3; i < numWalls; i+=4) {
			  walls[i] = new Wall(width/2, 0, width, 10, this); 
		  }
		  
		  //background color
		  background(BACKGROUND_COLOR);
		  
		  //start the song
		  song.play(0);

		
	}
	
	public void draw(){
		 //make the song advance on every sample as it plays
		  fft.forward(song.mix);
		  
		  //calculate the power of 3 aspects
		  oldScoreLow = scoreLow;
		  oldScoreMid = scoreMid;
		  oldScoreHi = scoreHi;
		  
		  //re-initialize values
		  scoreLow = 0;
		  scoreMid = 0;
		  scoreHi = 0;
		 
		  //calculate new score
		  for(int i = 0; i < fft.specSize()*specLow; i++)
		  {
		    scoreLow += fft.getBand(i);
		  }
		  
		  for(int i = (int)(fft.specSize()*specLow); i < fft.specSize()*specMid; i++)
		  {
		    scoreMid += fft.getBand(i);
		  }
		  
		  for(int i = (int)(fft.specSize()*specMid); i < fft.specSize()*specHi; i++)
		  {
		    scoreHi += fft.getBand(i);
		  }
		  
		  //slow down descent
		  if (oldScoreLow > scoreLow) {
		    scoreLow = oldScoreLow - scoreDecreaseRate;
		  }
		  
		  if (oldScoreMid > scoreMid) {
		    scoreMid = oldScoreMid - scoreDecreaseRate;
		  }
		  
		  if (oldScoreHi > scoreHi) {
		    scoreHi = oldScoreHi - scoreDecreaseRate;
		  }
		  
		  //Volumoe of all the scores combinded to make it go quicker when needed
		  float scoreGlobal = (float) (0.66*scoreLow + 0.8*scoreMid + 1*scoreHi);
		  
		  //subtle color for the background
		  background(scoreLow/100, scoreMid/100, scoreHi/100);
		   
		  //Cube for every freqence band
		  for(int i = 0; i < numWalls; i++)
		  {
		    //Valeur de la bande de fréquence
		    float bandValue = fft.getBand(i);

		  }
		  
		  //Wall lines, save the previous value to be able to connect to the next one
		  float previousBandValue = fft.getBand(0);
		  
		  //distance between every point (negativ because it's on the z-axis)
		  float dist = -25;
		  
		  //Height multiplier
		  float heightMult = 2;
		  
		  //For every band
		  for(int i = 1; i < fft.specSize(); i++)
		  {
		    //value of the band
		    float bandValue = fft.getBand(i)*(1 + (i/50));
		    
		    //coler selection based on scores
		    stroke(100+scoreLow, 100+scoreMid, 100+scoreHi, 255-i);
		    strokeWeight(1 + (scoreGlobal/100));
		    
		    
		    //left-down
		    line(0, height-(previousBandValue*heightMult),(float)-12333.0, 0, height-(bandValue*heightMult), 200);
		    line((previousBandValue*heightMult), height, (float)-12447.0, (bandValue*heightMult), height, 200);
		    line(0, height-(previousBandValue*heightMult),(float) -10440.0, (bandValue*heightMult), height, dist*i);
		    
		    //left-up
		    line(0, (previousBandValue*heightMult), dist*(i-1), 0, (bandValue*heightMult), dist*i);
		    line((previousBandValue*heightMult), 0, dist*(i-1), (bandValue*heightMult), 0, dist*i);
		    line(0, (previousBandValue*heightMult), dist*(i-1), (bandValue*heightMult), 0, dist*i);
		    
		    //right-down
		    line(width, height-(previousBandValue*heightMult), dist*(i-1), width, height-(bandValue*heightMult), dist*i);
		    line(width-(previousBandValue*heightMult), height, dist*(i-1), width-(bandValue*heightMult), height, dist*i);
		    line(width, height-(previousBandValue*heightMult), dist*(i-1), width-(bandValue*heightMult), height, dist*i);
		    
		    //right-up
		    line(width, (previousBandValue*heightMult), dist*(i-1), width, (bandValue*heightMult), dist*i);
		    line(width-(previousBandValue*heightMult), 0, dist*(i-1), width-(bandValue*heightMult), 0, dist*i);
		    line(width, (previousBandValue*heightMult), dist*(i-1), width-(bandValue*heightMult), 0, dist*i);
		    
		    //Save the value for the next loop
		    previousBandValue = bandValue;
		  }
		  
		  //Wall rectangles
		  for(int i = 0; i < numWalls; i++)
		  {
		    //a band is assigned to each wall
		    float intensity = fft.getBand(i%((int)(fft.specSize()*specHi)));
		    walls[i].display(scoreLow, scoreMid, scoreHi, 1, 1);
		  }
		}

		
    public static void main(String[] args) {
        PApplet.main(new String[]{ApplicationMain.class.getName()});
    }
}
