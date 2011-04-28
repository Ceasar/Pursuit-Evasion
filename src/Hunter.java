import java.io.*;
import java.util.*;

public class Hunter implements Bot {

	Graph g;
	PathFinder pf;
	Graph.Node location;
	Graph.Node ninjaStart;
	Graph.Node ninjaGoal;
	Set<Graph.Node> possible;
	
	private static final int HEARING_RANGE = 3;
	
	static HashMap<Graph.Node, Double> potentialmap;
	int turnsElapsed;
	
	public Hunter(Graph graph) throws IOException {
		g = graph;
		pf = new PathFinder(g);
		possible = new HashSet<Graph.Node>();
		potentialmap = new HashMap<Graph.Node, Double>();
		for (Graph.Node node : g.getNodes()){
			potentialmap.put(node, 0.0);
		}
	}
	public void setup(String pirateStart, String ninjaStart, String ninjaGoal) {;
		location = g.getNode(pirateStart);
		this.ninjaStart = g.getNode(ninjaStart);
		this.ninjaGoal = g.getNode(ninjaGoal);
		possible.add(this.ninjaStart);
		turnsElapsed = 1;
		potentialmap.put(this.ninjaStart, 1.0);
	}
	
	public boolean visibleFrom(Graph.Node source, Graph.Node target){
		Set<Graph.Node> visible = source.getNeighbors().keySet();
		return visible.contains(target) || source == target;
	}
	
	public Set<Graph.Node> hearableFrom(Graph.Node source){
		Set<Graph.Node> hearable = new HashSet<Graph.Node>();
		hearable.add(source);
		for (int i=1; i < HEARING_RANGE; i++){
			Set<Graph.Node> edge = new HashSet<Graph.Node>();
			for (Graph.Node node : hearable){
				edge.addAll(node.getNeighbors().keySet());
			}
			hearable.addAll(edge);
		}
		return hearable;
	}
	
	public String nextMove(boolean canHear) {
		Set<Graph.Node> hearable = hearableFrom(location);
		if (canHear)
			possible.retainAll(hearable);
		else
			possible.removeAll(hearable);
		HashMap<Graph.Node, Double> newpotentialmap = new HashMap<Graph.Node, Double>();
		for (Graph.Node node : g.getNodes()){
			newpotentialmap.put(node, 0.0);
		}
		Set<Graph.Node> creep = new HashSet<Graph.Node>(); 
		for (Graph.Node node : possible){
			Set<Graph.Node> neighbors = node.getNeighbors().keySet();
			creep.addAll(neighbors);
			double probability = 1;
			newpotentialmap.put(node, potentialmap.get(node) + newpotentialmap.get(node) + probability);
		}
		possible.addAll(creep);
		potentialmap = newpotentialmap;
		Set<Graph.Node> potentialmoves = location.getNeighbors().keySet();
		double best_score = 0;
		for (Graph.Node move : potentialmoves){
			hearable = hearableFrom(move);
			double score = 0;
			int dfg = pf.dijkstra(move, ninjaGoal).size();
			if (dfg > 2)
				score += 10 - pf.dijkstra(move, ninjaGoal).size();
			else
				continue;
			for (Graph.Node node : hearable){
				score += potentialmap.get(node) * potentialmap.get(node);
				Set<Graph.Node> neighbors = node.getNeighbors().keySet();
				for (Graph.Node neighbor : neighbors){
					score += potentialmap.get(neighbor) * potentialmap.get(neighbor);
				}
			}
			if (score > best_score){
				best_score = score;
				location = move;
			}
		}
		return location.getId();
	}
	
	public String getLocation() {
		return location.getId();
	}
	
	private class Heard{
		Graph.Node spot;
		int turn;
		
		public Heard(Graph.Node spot, int turn){
			this.spot = spot;
			this.turn = turn;
		}
	}
	
	private class Explore{
		Graph.Node spot;
		int turn;
		
		public Explore(Graph.Node spot, int turn){
			this.spot = spot;
			this.turn = turn;
		}
	}
}
