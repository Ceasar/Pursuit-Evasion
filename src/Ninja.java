import java.io.*;
import java.util.*;

/**
 * The pirate uses a hashmap to guide him around until he gets within possible range of the Ninja.
 * @author Ceasar
 *
 */
public class Ninja implements Bot {

	static int HEARING_RANGE = 3;

	Graph g;
	Graph.Node location;
	Graph.Node ninjaGoal;
	Graph.Node pirateStart;

	static Set<Graph.Node> possible_n;
	static Set<Graph.Node> possible_p;

	Set<Graph.Node> illegal;
	Set<Graph.Node> legal;

	static HashMap<Set<Graph.Node>, Integer> all_distances;
	static HashMap<Set<Graph.Node>, Integer> legal_distances;

	static Set<Graph.Node> interceptSet;
	static Set<Graph.Node> interceptSet2;
	static Set<Graph.Node> mids2;
	static Graph.Node destination;

	boolean heard;
	static Set<Graph.Node> usable2;

	public Ninja(Graph graph) throws IOException {
		g = graph;
		all_distances = new HashMap<Set<Graph.Node>, Integer>();
		legal_distances = new HashMap<Set<Graph.Node>, Integer>();
		usable2 = new HashSet<Graph.Node>();
		heard = false;
		interceptSet = null;
		interceptSet2 = new HashSet<Graph.Node>();
		mids2 = new HashSet<Graph.Node>();
		destination = null;
	}

	public boolean visibleFrom(Graph.Node source, Graph.Node target){
		Set<Graph.Node> visible = source.getVisibleNodes();
		return visible.contains(target) || source == target;
	}

	public Set<Graph.Node> getIllegal(){
		Set<Graph.Node> illegal = new HashSet<Graph.Node>();
		for (Graph.Node node : g.getNodes()){
			if (visibleFrom(node, ninjaGoal))
				illegal.add(node);
		}
		return illegal;
	}

	public void setup(String pirateStart, String ninjaStart, String ninjaGoal) {
		this.ninjaGoal = g.getNode(ninjaGoal);

		this.pirateStart = g.getNode(pirateStart);
		possible_p = new HashSet<Graph.Node>();
		possible_p.add(this.pirateStart);

		location = g.getNode(ninjaStart);
		possible_n = new HashSet<Graph.Node>();
		possible_n.add(location);

		illegal = getIllegal();
		legal = g.getNodes();
		legal.removeAll(illegal);
	}

	//---------------------------------------------------------------------------------------------------------------
	//TOOLS
	//---------------------------------------------------------------------------------------------------------------

	public List<Graph.Node> reconstruct_path(HashMap<Graph.Node, Graph.Node> came_from, Graph.Node current_node){
		//Given a hashmap with parents, builds up the path to a node.
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
		//A breadth first search from a source to a goal.
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

	public Set<Graph.Node> getVisible(Graph.Node source){
		return source.getVisibleNodes();
	}

	public Set<Graph.Node> getHearable(Graph.Node source){
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

	public Set<Graph.Node> creep(Set<Graph.Node> current){
		Set<Graph.Node> creep = new HashSet<Graph.Node>(); 
		for (Graph.Node node : current){
			Set<Graph.Node> neighbors = node.getNeighbors().keySet();
			creep.addAll(neighbors);
		}
		return creep;
	}

	public Set<Graph.Node> vcreep(Set<Graph.Node> current){
		Set<Graph.Node> creep = new HashSet<Graph.Node>(); 
		for (Graph.Node node : current){
			Set<Graph.Node> neighbors = node.getNeighbors().keySet();
			for (Graph.Node neighbor : neighbors){
				if (visibleFrom(neighbor, node))
					creep.add(neighbor);
			}

		}
		return creep;
	}

	//--------------------------------------------------------------------------------------------------------------
	//ANALYSIS
	//--------------------------------------------------------------------------------------------------------------

	public void updatePossible(boolean canHear){
		possible_p = creep(possible_p);
		possible_p.removeAll(illegal);
		possible_n = creep(possible_n);
		if (canHear){
			possible_p.retainAll(getHearable(location));
			possible_p.removeAll(getVisible(location));
			for (Graph.Node node: possible_p){
				possible_n.retainAll(getHearable(node));
			}
		} else
			possible_p.removeAll(getHearable(location));
	}

	public int getDistance(Graph.Node source, Graph.Node target, boolean use_legal){
		int dist = 0;
		HashSet<Graph.Node> nodes = new HashSet<Graph.Node>();
		nodes.add(source);
		nodes.add(target);
		if (use_legal) {
			if (legal_distances.containsKey(nodes))
				dist = legal_distances.get(nodes);
			else{
				List<Graph.Node> path = breadth(source, target, legal);
				if (path != null)
					dist = path.size();
				else
					dist = Integer.MAX_VALUE;
				legal_distances.put(nodes, dist);
			}
			return dist;
		} else {
			if (all_distances.containsKey(nodes))
				dist = all_distances.get(nodes);
			else{
				List<Graph.Node> path = breadth(source, target, g.getNodes());
				if (path != null)
					dist = path.size();
				else
					dist = Integer.MAX_VALUE;
				all_distances.put(nodes, dist);
			}
			return dist;
		}
	}

	public HashMap<Graph.Node, Integer> getDifferenceTo(){
		//Gets the time difference to a node for the ninja and pirate.
		HashMap<Graph.Node, Integer> diff_map = new HashMap<Graph.Node, Integer>();
		for (Graph.Node node: legal){
			Graph.Node closest = getClosestPossible(possible_p, node, true);
			int pDist = getDistance(closest, node, true);
			int nDist = getDistance(location, node, false);
			if (getDistance(closest, location, true) % 2 == 0){
				if (Math.abs(pDist - nDist) == 1)
					diff_map.put(node, 777);
			}
			else{
				int diff = pDist - nDist;
				diff_map.put(node, diff);
			}
			//			int diff = pDist - nDist;
			//			diff_map.put(node, diff);
		}
		return diff_map;
	}

	public Set<Graph.Node> getMidPoints(){
		//Finds all the points that the pirate and ninja can reach in the same time.
		Set<Graph.Node> mids = new HashSet<Graph.Node>();
		HashMap<Graph.Node, Integer> distances = getDifferenceTo();
		for (Graph.Node node : distances.keySet()){
			int distance = Math.abs(distances.get(node));
			if (distance == 0 || distance == 777)
				mids.add(node);
		}
		mids2 = mids;
		return mids;
	}

	public Set<Graph.Node> getIntercepts(){
		//Finds the intercept point.
		Set<Graph.Node> intercepts = new HashSet<Graph.Node>();
		Set<Graph.Node> mids = getMidPoints();
		int min = Integer.MAX_VALUE;
		for (Graph.Node mid : mids){
			int distance = getDistance(mid, ninjaGoal, false);
			if (distance < min){
				intercepts.clear();
				min = distance;
				intercepts.add(mid);
			} else if (distance == min){
				intercepts.add(mid);
			}
		}
		return intercepts;
	}

	public Set<Graph.Node> getUsable(Set<Graph.Node> interceptSet, boolean canHear){
		Set<Graph.Node> usable = g.getNodes();

		//Remove the intercept
		usable.removeAll(interceptSet);

		//Remove all nodes visible from intercept
		if (canHear){
			Set<Graph.Node> visible = new HashSet<Graph.Node>();
			for (Graph.Node node : usable){
				for (Graph.Node intercept : interceptSet){
					if (visibleFrom(node, intercept)){
						visible.add(node);
						break;
					}
				}
			}
			usable.removeAll(visible);
			usable.add(location);
		}

		//Remove all disjoint nodes.
		Set<Graph.Node> disjoint = new HashSet<Graph.Node>();
		for (Graph.Node node : usable){
			if (breadth(node, location, usable) == null)
				disjoint.add(node);
		}
		usable.removeAll(disjoint);
		return usable;

	}

	public Graph.Node getClosestPossible(Set<Graph.Node> usable, Graph.Node target, boolean use_legal){
		//Get the point closest to the goal.
		Graph.Node closest = null;
		int min = Integer.MAX_VALUE;
		for (Graph.Node node : usable){
			int distance = getDistance(node, target, use_legal);
			if (distance < min){
				min = distance;
				closest = node;
			}
		}
		return closest;
	}

	public String nextMove(boolean canHear) {
		updatePossible(canHear);
		mids2 = getMidPoints();
		interceptSet = getIntercepts();
		if (canHear){
			heard = true;
			mids2.retainAll(creep(possible_p));
			usable2 = getUsable(mids2, canHear);
			interceptSet2 = mids2;
			destination = getClosestPossible(usable2, ninjaGoal, false);
		} else{
			if (location == destination){
				heard = false;
			}
		}

		if (heard){ //Move to the point closest to the goal and hope we don't hear anything.
			usable2 = getUsable(interceptSet2, canHear);
			List<Graph.Node> path = breadth(location, destination, usable2);
			if (path != null)
				if (path.size() > 1)
					location = path.get(1);
				else
					location = path.get(0);
		}
		else{ //If we never heard our opponent, we're just going to head straight to goal.
			interceptSet2.clear();
			List<Graph.Node> direct = breadth(location, ninjaGoal, g.getNodes());
			if (direct.size() > 1)
				location = direct.get(1);
			else
				location = direct.get(0);
		}

		return location.getId();
	}

	public String getLocation() {
		return location.getId();
	}
}
