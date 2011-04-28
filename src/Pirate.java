import java.io.*;
import java.util.*;

/**
 * The pirate uses a hashmap to guide him around until he gets within possible range of the Ninja.
 * @author Ceasar
 *
 */
public class Pirate implements Bot {
	
	static final int HEARING_RANGE = 3;

	Graph g;
	Graph.Node location;
	Graph.Node ninjaGoal;
	
	Set<Graph.Node> possible;
	Set<Graph.Node> illegal;
	Set<Graph.Node> legal; 
	
	HashMap<Set<Graph.Node>, Integer> ninja_distance;
	HashMap<Set<Graph.Node>, Integer> pirate_distance;

	public Pirate(Graph graph) throws IOException {
		g = graph;
		
		ninja_distance = new HashMap<Set<Graph.Node>, Integer>();
		pirate_distance = new HashMap<Set<Graph.Node>, Integer>();
	}
	
	public boolean visibleFrom(Graph.Node source, Graph.Node target){
		Set<Graph.Node> visible = source.getVisibleNodes();
		return visible.contains(target) || source == target;
	}
	
	public Set<Graph.Node> getIllegal(){
		//Gets illegal nodes. (For pirate.)
		Set<Graph.Node> illegal = new HashSet<Graph.Node>();
		for (Graph.Node node : g.getNodes()){
			if (visibleFrom(node, ninjaGoal))
				illegal.add(node);
		}
		return illegal;
	}
	
	public void setup(String pirateStart, String ninjaStart, String ninjaGoal) {
		location = g.getNode(pirateStart);
		this.ninjaGoal = g.getNode(ninjaGoal);
		
		possible = new HashSet<Graph.Node>();
		possible.add(g.getNode(ninjaStart));
		illegal = getIllegal();
		legal = g.getNodes();
		legal.removeAll(illegal);
	}

	public int getDistance(Graph.Node source, Graph.Node target, boolean use_legal){
		//Gets the distance to a point using caching.
		int dist = 0;
		HashSet<Graph.Node> key = new HashSet<Graph.Node>();
		key.add(source);
		key.add(target);
		if (use_legal) {
			if (pirate_distance.containsKey(key))
				dist = pirate_distance.get(key);
			else{
				List<Graph.Node> path = breadth(source, target, legal);
				if (path != null) dist = path.size(); else dist = Integer.MAX_VALUE;
				pirate_distance.put(key, dist);
			}
			return dist;
		} else {
			if (ninja_distance.containsKey(key)) dist = ninja_distance.get(key);
			else{
				List<Graph.Node> path = breadth(source, target, g.getNodes());
				if (path != null) dist = path.size(); else dist = Integer.MAX_VALUE;
				ninja_distance.put(key, dist);
			}
			return dist;
		}
	}

	public HashMap<Graph.Node, Integer> getDifferenceTo(Graph.Node point){
		//Gets the difference in time for the ninja and pirate to reach a node.
		HashMap<Graph.Node, Integer> map = new HashMap<Graph.Node, Integer>();
		for (Graph.Node node: legal){
			int pDist = getDistance(location, node, true);
			int nDist = getDistance(getClosestPossible(possible, node), node, false);
			int diff = pDist - nDist;
			map.put(node, diff);
		}
		return map;
	}

	public Set<Graph.Node> getMidPoints(Graph.Node closest){
		//Finds the points where the ninja and pirate are equidistant.
		Set<Graph.Node> mids = new HashSet<Graph.Node>();
		HashMap<Graph.Node, Integer> distances = getDifferenceTo(closest);
		int min;
		if (distances.values().contains(0)) min = 0; else min = -1; //If pirate and ninja are right next to each other in a tunnel.
		for (Graph.Node node : distances.keySet())
			if (distances.get(node) == min) mids.add(node);
		return mids;
	}

	public Graph.Node getIntercept(Graph.Node closest){
		//Get the intercept node.
		Graph.Node intercept = null;
		Set<Graph.Node> mids = getMidPoints(closest);
		int min = Integer.MAX_VALUE;
		for (Graph.Node mid : mids){
			int distance = getDistance(mid, ninjaGoal, false);
			if (distance < min){
				min = distance;
				intercept = mid;
			}
		}
		return intercept;
	}

	public Graph.Node getClosestPossible(Set<Graph.Node> usable, Graph.Node target){
		//Get the point closest to the goal.
		Graph.Node closest = null;
		int min = Integer.MAX_VALUE;
		for (Graph.Node node : usable){
			int distance = getDistance(node, target, false);
			if (distance < min){
				min = distance;
				closest = node;
			}
		}
		return closest;
	}

	public Set<Graph.Node> getHearable(){
		//Get the nodes within hearing range.
		Set<Graph.Node> hearable = new HashSet<Graph.Node>();
		hearable.add(location);
		for (int i=0; i < HEARING_RANGE; i++){
			Set<Graph.Node> creep = new HashSet<Graph.Node>(); 
			for (Graph.Node node : hearable){
				Set<Graph.Node> neighbors = node.getNeighbors().keySet();
				creep.addAll(neighbors);
			}
			hearable.addAll(creep);
		}
		return hearable;
	}

	public Set<Graph.Node> getVisible(Graph.Node source){
		//Gets visible nodes.
		return source.getVisibleNodes();
	}

	public void creep(Set<Graph.Node> current){
		Set<Graph.Node> creep = new HashSet<Graph.Node>(); 
		for (Graph.Node node : current){
			Set<Graph.Node> neighbors = node.getNeighbors().keySet();
			creep.addAll(neighbors);
		}
		current.addAll(creep);
	}

	public void updatePossible(boolean canHear){
		creep(possible);
		if (canHear){
			possible.retainAll(getHearable());
			possible.removeAll(getVisible(location));
		} else
			possible.removeAll(getHearable());
	}

	public String nextMove(boolean canHear) {
		updatePossible(canHear);
		Graph.Node closest = getClosestPossible(possible, ninjaGoal);
		Graph.Node intercept = getIntercept(closest);
		List<Graph.Node> path = breadth(location, intercept, legal);
		if (path != null){
			if (path.size() > 1)
				location = path.get(1);
			else
				location = path.get(0);
		}
		return location.getId();
	}
	
	public List<Graph.Node> reconstruct_path(HashMap<Graph.Node, Graph.Node> came_from, Graph.Node current_node){
		//Given a hashmap of parents, reconstructs the path taken.
		if (came_from.get(current_node) != null){
			List<Graph.Node> p = reconstruct_path(came_from, came_from.get(current_node));
			p.add(current_node);
			return p;
		}
		else{
			List<Graph.Node> p = new ArrayList<Graph.Node>();
			p.add(current_node);
			return p;
		}
	}

	public List<Graph.Node> breadth(Graph.Node sourceNode, Graph.Node destinationNode, Set<Graph.Node> legal ) {
		//A breadth first search from source to destination.
		HashMap<Graph.Node, Graph.Node> came_from = new HashMap<Graph.Node, Graph.Node>();
		came_from.put(sourceNode, null);
		Queue<Graph.Node> queue = new LinkedList<Graph.Node>();
		queue.add(sourceNode);
		while (!queue.isEmpty()){
			Graph.Node current = queue.poll();			
			if (current != destinationNode){
				if (current == null)
					continue;
				Map<Graph.Node, Double> test = current.getNeighbors(); 
				Set<Graph.Node> neighbors = test.keySet();
				for (Graph.Node neighbor : neighbors){
					if (!came_from.containsKey(neighbor) && legal.contains(neighbor)){
						came_from.put(neighbor, current);
						queue.add(neighbor);
					}
				}
			}
			else
				return reconstruct_path(came_from, current);
		}
		//If here, target is not in tree.
		return null;
	}

	public String getLocation() {
		return location.getId();
	}
}
