package dcdholder.chess;

//TODO: fix Coord methods which "fake" the static modifier
public class Coord {
	private byte x,y;
	
	public byte getX() {return this.x;}
	public byte getY() {return this.y;}
	
	//get it to stop bugging me for casts
	Coord(int x, int y) {this((byte)x,(byte)y);}
	Coord(byte x, int y) {this(x,(byte)y);}
	Coord(int x, byte y) {this((byte)x,y);}
	
	boolean bothDimsBetween1And8() {if((this.x<=8&&this.x>=1)&&(this.y<=8&&this.y>=1)) {return true;} else {return false;}}
	
	static public Coord addCoords(Coord coordA, Coord coordB) {
		int newCoordX = coordA.x + coordB.x;
		int newCoordY = coordA.y + coordB.y;
		
		return new Coord(newCoordX,newCoordY);
	}
	
	static public Coord subCoords(Coord coordA, Coord coordB) {
		int newCoordX = coordB.x - coordA.x;
		int newCoordY = coordB.y - coordA.y;
		
		return new Coord(newCoordX,newCoordY);
	}
	
	public Coord absCoord() {return new Coord(Math.abs(this.x),Math.abs(this.y));}
	
	public String toString() {return "X value: " + this.x + ", Y value: " + this.y;}
	
	public int hashCode() {
		return this.x+this.y;
	}
	public boolean equals(Object o) {
		if(o==this) {return true;}
		if(o instanceof Coord) {
			Coord testCoord = (Coord)o;
			if(this.x==testCoord.x && this.y==testCoord.y) {return true;} else {return false;}
		}
		return false;
	}
	
	Coord() {
		this.x = 0;
		this.y = 0;
	}
	Coord(byte x, byte y) {
		this.x = x;
		this.y = y;
	}
	Coord(Coord copyCoord) {
		this(copyCoord.x,copyCoord.y);
	}
}