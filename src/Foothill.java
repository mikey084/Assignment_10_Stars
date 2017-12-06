import cs_1c.*;

import java.text.DecimalFormat;
import java.util.*;


public class Foothill {

	public static void main(String[] args) throws Exception {

		int i, k, arraySize;
		double maxX, minX, maxY, minY, maxZ, minZ;
		final int NUM_COLS = 70;
		final int NUM_ROWS = 35;
		double[] xVals, yVals, zVals;

		//read file
		StarNearEarthReader fileInput = new StarNearEarthReader(
				"nearest_stars.txt");

		if (fileInput.readError()) {
			System.out.println("Error on File name: " + fileInput.getFileName());
			return;
		}

		//initialize array of star object
		arraySize = fileInput.getNumStars();
		SNE_Analyzer[] starArray = new SNE_Analyzer[arraySize];
		System.out.println("# of stars: " + fileInput.getNumStars());
		System.out.println("Nearest Stars File: " + fileInput.getFileName());
		for (k = 0; k < arraySize; k++)
			starArray[k] = new SNE_Analyzer(fileInput.getStar(k));

		System.out.println("");
		//display XYZ table coordinates
		for (k = 0; k < arraySize; k++)
			System.out.printf("%45s: %15s \n", starArray[k].getNameCommon(),
					starArray[k].coordToString());

		//arraySize is 100
		xVals = new double[arraySize];
		yVals = new double[arraySize];
		zVals = new double[arraySize];
		int[] ranks = new int[arraySize];
		//initialize min max
		maxX = minX = maxY = minY = maxZ = minZ = 0;

		//populate coordinates into X Y Z double arrays 
		//populate rank array
		//find min max for X Y Z coordinates
		for (k = 0; k < arraySize; k++) {
			double x = starArray[k].getX();
			double y = starArray[k].getY();
			double z = starArray[k].getZ();
			xVals[k] = x;
			yVals[k] = y;
			zVals[k] = z;
			ranks[k] = starArray[k].getRank();
			//			System.out.println(starArray[k].getRank());
			if (x > maxX) {
				maxX = x;
			}
			else if (x < minX) {
				minX = x;
			}
			if (y > maxY) {
				maxY = y;
			}
			else if (y < minY) {
				minY = y;
			}
			if (z > maxZ) {
				maxZ = z;
			}
			else if (z < minZ) {
				minZ = z;
			}
		}

		//Instantiate and Populate starMap Matrix 
		System.out.println("Displaying starMap: ");
		int row, col;
		System.out.println("Display: " + NUM_ROWS + " X " + NUM_COLS);
		System.out.println();
		SparseMat<Character> starMap = new SparseMat<Character>(
				NUM_ROWS, NUM_COLS, ' ');
		// scale x y coords
		for (i = 0; i < arraySize; i++) {
			double[] coordXY = new double[] {xVals[i],yVals[i]};
			int[] adjCoord = conversion(
					coordXY, minX, maxX, minY, maxY, NUM_ROWS, NUM_COLS);
			row = adjCoord[0];
			col = adjCoord[1];
			//set top 10 , type char
			if (ranks[i] <= 9) {
				starMap.set(row, col, ("" + ranks[i]).charAt(0));
			} 
			else {
				starMap.set(row, col, '*');
			}
		}

		//Set sun as 'S' 
		double[] coordSun = new double[] {0,0};
		int[] adjCoord = conversion(coordSun, minX, maxX, minY,
				maxY, NUM_ROWS, NUM_COLS);
		row = adjCoord[0];
		col = adjCoord[1];
		starMap.set(row, col, 'S');

		//Print loops for starMap matrices
		String output = "";
		for (row = 0; row < NUM_ROWS; row++) {
			output = "";
			for (col = 0; col < NUM_COLS; col++) {
				output += starMap.get(row, col);
			}
			System.out.println(output);
		}
	}



	private static int[] conversion(double[] coord, double minX, double maxX,
			double minY, double maxY, int numRows, int numCols) {
		int[] newArray = new int[2];
		newArray[0] = (int) ((coord[0] - minX) * numRows / (maxX - minX));
		newArray[1] = (int) ((coord[1] - minY) * numCols / (maxY - minY));
		return newArray;
	}
}
// SPARSE MATRIX DS class
class SparseMat<E> {

	private int rowSize, colSize;
	private E defaultVal;
	private FHarrayList <FHlinkedList<MatNode>> rows;
	private int k;

	public int getRowSize() { 
		return rowSize; 
	}
	public int getColSize() { 
		return colSize; 
	}

	// input check create empty 
	public SparseMat( int numRows, int numCols, E defaultVal) {
		if ( numRows < 1 || numCols < 1 )
			throw new IllegalArgumentException();

		rowSize = numRows;
		colSize = numCols;
		allocateEmptyMatrix();
		this.defaultVal = defaultVal;
	}

	private void allocateEmptyMatrix(){
		rows = new FHarrayList<>(rowSize);
		for (k = 0; k < this.rowSize; k++){
			this.rows.add(new FHlinkedList<>());  
		}
	}

	public void clear() {
		for (k = 0; k < rowSize; k++)
			rows.get(k).clear();
	}
	private boolean valid(int rows, int cols) {
		if (rows >= 0 && rows < rowSize && cols >= 0 && cols < colSize){
			return true;
		}
		return false;
	}

	public boolean set(int r, int c, E x)
	{
		if (!valid(r, c)){
			return false;
		}

		ListIterator<MatNode> iter;

		// iterate along the row, looking for column c
		for (iter = (ListIterator<MatNode>)rows.get(r).listIterator();
				iter.hasNext();) {
			if (iter.next().col == c) {
				if (x.equals(defaultVal)) {
					iter.remove();
				}
				else{
					iter.previous().data = x;
				}
				return true;
			}
		}

		// not found
		if ( !x.equals(defaultVal) )
			rows.get(r).add( new MatNode(c, x) );
		return true;
	}
	public E get(int r, int c) {

		if (!valid(r, c))
			throw new IndexOutOfBoundsException();

		ListIterator<MatNode> iter;

		// iterate along the row, looking for column c
		for (iter = (ListIterator<MatNode>)rows.get(r).listIterator();
				iter.hasNext();) {
			if ( iter.next().col == c ) {
				return iter.previous().data;
			}
		}
		// not found
		return defaultVal;
	}

	public void showSubSquare(int start, int size) {
		int row, col;

		if (start < 0 || size < 0 || start + size > rowSize 
				|| start + size > colSize ){
			return;
		}

		for (row = start; row < start + size; row++) {
			for (col = start; col < start + size; col++) {
				System.out.print( String.format(
						"%4.1f", (Double)get(row, col)) + " " );
			}
			System.out.println();
		}
		System.out.println();
	}
	protected class MatNode implements Cloneable {
		public int col;
		public E data;

		// we need a default constructor for lists
		MatNode() {
			col = 0;
			data = null;
		}

		MatNode(int cl, E dt) {
			col = cl;
			data = dt;
		}

		public Object clone() throws CloneNotSupportedException {
			// shallow copy
			MatNode newObject = (MatNode)super.clone();
			return (Object) newObject;
		}
	};
}
class SNE_Analyzer extends StarNearEarth {
	private double x, y, z;

	// construct an SNE_Analyzer from a StartNearEarth object
	public SNE_Analyzer( StarNearEarth sne )
	{
		setRank(sne.getRank()); 
		setNameCns(sne.getNameCns());
		setNumComponents(sne.getNumComponents());
		setNameLhs(sne.getNameLhs());
		setRAsc(sne.getRAsc());
		setDec(sne.getDec());
		setPropMotionMag(sne.getPropMotionMag());
		setPropMotionDir(sne.getPropMotionDir());
		setParallaxMean(sne.getParallaxMean());
		setParallaxVariance(sne.getParallaxVariance());
		SetBWhiteDwarfFlag(sne.getWhiteDwarfFlag());
		setSpectralType(sne.getSpectralType());
		setMagApparent(sne.getMagApparent());
		setMagAbsolute(sne.getMagAbsolute());
		setMass(sne.getMass());
		setNotes(sne.getNotes());
		setNameCommon(sne.getNameCommon()); 
		calcCartCoords();
	}

	public void calcCartCoords() {
		double lightYears = 3.262 / (getParallaxMean());
		double rAsc = Math.toRadians(getRAsc());
		double dec = Math.toRadians(getDec());
		x = lightYears * Math.cos(dec) * Math.cos(rAsc);
		y = lightYears * Math.cos(dec) * Math.sin(rAsc);
		z = lightYears * Math.sin(dec);
	}
	double getX() {
		return x;
	}

	double getY() {
		return y;
	}

	double getZ() {
		return z;
	}
	public String coordToString() {
		DecimalFormat df = new DecimalFormat("##.###");
		return String.format(" %10s %10s %10s", df.format(x),
				df.format(y), df.format(z));
	}
}