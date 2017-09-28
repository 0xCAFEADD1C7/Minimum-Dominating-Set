package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DefaultTeam {	
	public boolean isNeighbour(Point p, Point q, int thr) {
		return p.distanceSq(q) < thr*thr;
	}
	
	private static <T> T swapRemove(List<T> lst, int i) {
		int end = lst.size() - 1;
		if (i == end) {
			return lst.remove(i);
		} 
		T swapped = lst.remove(end);
		T removed = lst.get(i);
		lst.set(i, swapped);
		return removed;
	}
	
	private static <T> void swapBack(List<T> lst, int i, T val) {
		if (i >= lst.size()) {
			lst.add(val);
		} else {
			T swapped = lst.get(i);
			lst.set(i, val);
			lst.add(swapped);
		}
	}
	
	public ArrayList<Point> greedyWithLS(ArrayList<Point> points, int edgeThreshold) {
		ArrayList<Point> rest = (ArrayList<Point>) points.clone();
		ArrayList<Point> sol = new ArrayList<>();
		
		ArrayList<Point> notSol = new ArrayList<>();
		
		while(!rest.isEmpty()) {
			int maxNeigh = 0;
			int maxI = 0;
		
			for (int i = 0; i < rest.size(); i++) {
				Point p = rest.get(i);
				int nbNeigh = 0;
				for (Point q : rest) {
					if (p != q && isNeighbour(p, q, edgeThreshold)) {
						nbNeigh++;
					}
				}
				if (maxNeigh < nbNeigh) {
					maxNeigh = nbNeigh;
					maxI = i;
				}
			}

			Point p = swapRemove(rest, maxI);
			sol.add(p);
			
			for (int i = 0; i < rest.size(); i++) {
				Point q = rest.get(i);
				if (p != q && isNeighbour(p, q, edgeThreshold)) {
					notSol.add(q);
					
					swapRemove(rest, i);
					i--;
				}
			}
		}

		return localSearch(notSol, sol, edgeThreshold);
	}
	
	private static void check(boolean cond, String mes) {
		if (!cond) throw new Error(mes);
	}
	
	public ArrayList<Point> localSearch(ArrayList<Point> rest, ArrayList<Point> sol, int thr) {
		int thrS = 9*thr*thr;
		for(int i = 0; i < sol.size(); i++) {
			Point p = swapRemove(sol, i);
			for(int j = i; j < sol.size(); j++) {
				Point q = swapRemove(sol, j);
				
				ArrayList<Point> toCheck = new ArrayList<>();
				toCheck.add(p);
				toCheck.add(q);
				for (int k = 0; k < rest.size(); k++) {
					Point pk = rest.get(k);
					if (isNeighbour(pk, p, thr) || isNeighbour(pk, q, thr)) {
						toCheck.add(pk);
					}
				}
				
				check(toCheck.stream().filter(e -> sol.contains(e)).toArray().length == 0, "check failed");
				
				if (p.distanceSq(q) < thrS) {				
					for (int k = 0; k < rest.size(); k++) {
						Point n = swapRemove(rest, k);
						
//						check(rest.stream().filter(e -> sol.contains(e)).toArray().length == 0, "941");
//						check(toCheck.stream().filter(e -> sol.contains(e)).toArray().length == 0, "942");
						
						if (n.distanceSq(p) < thrS && n.distanceSq(q) < thrS){
							rest.add(p);
							rest.add(q);
							sol.add(n);
							
							// TODO 
							boolean conflict = toCheck.remove(n);
							boolean conflict2 = toCheck.remove(n);
//							check(!toCheck.contains(n), "LAST C HD");
							if (isBDSM(toCheck, sol, thr)) {
								System.out.println("🦄 " + sol.size());
								return localSearch(rest, sol, thr);
							} 
							
							if (conflict) {
								toCheck.add(n);
							}
							
							sol.remove(sol.size()-1);
							rest.remove(rest.size() - 1);
							rest.remove(rest.size() - 1);
						}
						swapBack(rest, k, n);					
					}
				}
				swapBack(sol, j, q);
			}
			swapBack(sol, i, p);
		}
		
		return sol;
	}

	private boolean isBDSM(ArrayList<Point> rest, ArrayList<Point> sol, int thr) {
		BitSet visited = new BitSet(rest.size());
		visited.clear();
		int cpt = 0;
		
		for (int i = 0; i < sol.size(); i++) {
			Point p = sol.get(i);
			
			for (int j = 0; j < rest.size(); j++) {	
				Point q = rest.get(j);
				
				if (isNeighbour(p, q, thr)) {
					if (!visited.get(j)) {
						cpt++;
					}
					visited.set(j);
				}
			}
		}
		
		return cpt == rest.size();
	}

	public ArrayList<Point> calculDominatingSet(ArrayList<Point> points, int edgeThreshold) {
		
		return greedyWithLS(points, edgeThreshold);
	}


	//FILE PRINTER
	private void saveToFile(String filename,ArrayList<Point> result){
		int index=0;
		try {
			while(true){
				BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(filename+Integer.toString(index)+".points")));
				try {
					input.close();
				} catch (IOException e) {
					System.err.println("I/O exception: unable to close "+filename+Integer.toString(index)+".points");
				}
				index++;
			}
		} catch (FileNotFoundException e) {
			printToFile(filename+Integer.toString(index)+".points",result);
		}
	}
	private void printToFile(String filename,ArrayList<Point> points){
		try {
			PrintStream output = new PrintStream(new FileOutputStream(filename));
			int x,y;
			for (Point p:points) output.println(Integer.toString((int)p.getX())+" "+Integer.toString((int)p.getY()));
			output.close();
		} catch (FileNotFoundException e) {
			System.err.println("I/O exception: unable to create "+filename);
		}
	}

	//FILE LOADER
	private ArrayList<Point> readFromFile(String filename) {
		String line;
		String[] coordinates;
		ArrayList<Point> points=new ArrayList<Point>();
		try {
			BufferedReader input = new BufferedReader(
					new InputStreamReader(new FileInputStream(filename))
					);
			try {
				while ((line=input.readLine())!=null) {
					coordinates=line.split("\\s+");
					points.add(new Point(Integer.parseInt(coordinates[0]),
							Integer.parseInt(coordinates[1])));
				}
			} catch (IOException e) {
				System.err.println("Exception: interrupted I/O.");
			} finally {
				try {
					input.close();
				} catch (IOException e) {
					System.err.println("I/O exception: unable to close "+filename);
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Input file not found.");
		}
		return points;
	}
}
