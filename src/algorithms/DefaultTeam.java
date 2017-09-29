package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.BitSet;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadLocalRandom;

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
		System.out.println("Running greedy");

		ArrayList<Point> rest = (ArrayList<Point>) points.clone();
		ArrayList<Point> sol = new ArrayList<>();
		
		ArrayList<Point> notSol = new ArrayList<>();
		ArrayList<Integer> bests = new ArrayList<>(256);
		
		while(!rest.isEmpty()) {
			int maxNeigh = 0;
			int maxI = 0;

			bests.clear();
		
			for (int i = 0; i < rest.size(); i++) {
				Point p = rest.get(i);
				int nbNeigh = 0;
				for (Point q : rest) {
					if (p != q && isNeighbour(p, q, edgeThreshold)) {
						nbNeigh++;
					}
				}
				// improving
				if (maxNeigh < nbNeigh) {
					maxNeigh = nbNeigh;
					bests.clear();
					bests.add(i);
				} else if (maxNeigh == nbNeigh) { // equalizing 
					bests.add(i);
				}
			}

			maxI = bests.get(ThreadLocalRandom.current().nextInt(bests.size()));

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

		return localSearch32(notSol, localSearch(notSol, sol, edgeThreshold), edgeThreshold);
	}

	private ArrayList<Point> localSearch32(ArrayList<Point> rest, ArrayList<Point> sol, int thr) {
		int thrS = 9*thr*thr;
		for(int i = 0; i < sol.size(); i++) {
			Point p = swapRemove(sol, i);
			for(int j = i; j < sol.size(); j++) {
				Point q = swapRemove(sol, j);
				for(int k = j; k < sol.size(); k++) {
					Point r = swapRemove(sol, k);

					if (p.distanceSq(q) < thrS 
						&& p.distanceSq(r) < thrS 
						&& q.distanceSq(r) < thrS) {
				
						ArrayList<Point> toCheck = new ArrayList<>();
						toCheck.add(p);
						toCheck.add(q);
						toCheck.add(r);
						for (Point pk : rest) {
							if (isNeighbour(pk, p, thr) || isNeighbour(pk, q, thr) || isNeighbour(pk, r, thr)) {
								toCheck.add(pk);
							}
						}

						for (int l = 0; l < rest.size(); l++) {
							Point n1 = swapRemove(rest, l);
							for (int m = l; m < rest.size(); m++) {
								Point n2 = swapRemove(rest, m);

								if (n1.distanceSq(p) < thrS && n1.distanceSq(q) < thrS && n1.distanceSq(r) < thrS && n2.distanceSq(p) < thrS	&& n2.distanceSq(q) < thrS && n2.distanceSq(r) < thrS){
									rest.add(p);
									rest.add(q);
									rest.add(r);

									sol.add(n1);
									sol.add(n2);
									
									if (isBDSM(toCheck, sol, thr)) {
										System.out.println("🌟 " + sol.size());
										return localSearch32(rest, sol, thr);
									} 
									
									sol.remove(sol.size()-1);
									sol.remove(sol.size()-1);

									rest.remove(rest.size() - 1);
									rest.remove(rest.size() - 1);
									rest.remove(rest.size() - 1);
								}
								swapBack(rest, m, n2);
							}
							swapBack(rest, l, n1);		
						}
					}
					swapBack(sol, k, r);
				}
				swapBack(sol, j, q);
			}
			swapBack(sol, i, p);
		}
		
		return sol;
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
								
				if (p.distanceSq(q) < thrS) {				
					for (int k = 0; k < rest.size(); k++) {
						Point n = swapRemove(rest, k);
						
						if (n.distanceSq(p) < thrS && n.distanceSq(q) < thrS){
							rest.add(p);
							rest.add(q);
							sol.add(n);
							
							if (isBDSM(toCheck, sol, thr)) {
								System.out.println("⭐ " + sol.size());
								return localSearch(rest, sol, thr);
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
		long begin = new Date().getTime();
		long MAX_TIME = 1000; // max time to execute optimizations, in ms
		ArrayList<Point> result;
		ArrayList<Point> best = new ArrayList<>();
		int bestSize = Integer.MAX_VALUE;

		while (new Date().getTime() < begin + MAX_TIME) {
			result = greedyWithLS(points, edgeThreshold);
			// System.out.println(result.size());
			// System.out.println(best.size());
			if (result.size() < bestSize) {
				best = result;
				bestSize = result.size();
			}
		}
		
		return best;
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
