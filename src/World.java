import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class World {

	Graph g;
	PathFinder pf;
	/**
	 * Creates a new world.
	 */
	public World(String nodeFile, String edgeFile, String visiFile) throws IOException{
		g = new Graph(nodeFile, edgeFile, visiFile);
		pf = new PathFinder(g);
	}

	/**
	 * Returns a random location in the world.
	 */
	public String getRandomLoc() {      
		Set<Graph.Node> nodes = g.getNodes();
		int item = new Random().nextInt(nodes.size());
		Iterator<Graph.Node> iter = nodes.iterator();
		for (int i = 0; i < item; i++){
			iter.next();
		}
		return iter.next().getId();
	}

	/**
	 * Returns whether the two nodes are within hearing distance of one
	 * another. Two nodes can hear each other exactly when there is a path of
	 * length at most three between them. (Use the normal edges, not the
	 * visibility edges.)
	 */
	public boolean canHearEachOther(String echo, String ohce){
		return pf.dijkstra(g.getNode(echo), g.getNode(ohce)).size() <= 4;
	}

	/**
	 * Returns true if and only if the pirate can see the ninja. Node A can see
	 * node B if A equals B or if the edge (A, B) is a visibility edge.
	 * @param pirate the pirate's node's GUID
	 * @param ninja the ninja's node's GUID
	 */
	public boolean pirateCatchesNinja(String pirate, String ninja) {
		Graph.Node pirateLoc = g.getNode(pirate);
		Set<Graph.Node> visible = pirateLoc.getVisibleNodes();
		Graph.Node ninjaLoc = g.getNode(ninja);
		return visible.contains(ninjaLoc) || pirate == ninja;
	}

	/**
	 * Returns whether the move is valid, meaning that a bot can traverse the
	 * edge (from, to).
	 * @param from the bot's starting node's GUID
	 * @param to the bot's ending node's GUID
	 */
	public boolean isValidMove(String from, String to) {
		Graph.Node fromLoc = g.getNode(from);
		Set<Graph.Node> adjacent = fromLoc.getNeighbors().keySet();
		Graph.Node toLoc = g.getNode(to);
		return (adjacent.contains(toLoc) || from.equals(to));                  
	}

	/**
	 * Returns true iff the pirate can see the goal. Node A can see node B if A
	 * equals B or if the edge (A, B) is a visibility edge.
	 * @param pirate the pirate's node's GUID
	 * @param goal the goal's node's GUID
	 */
	public boolean isPirateTooClose(String pirate, String goal){
		Graph.Node pirateLoc = g.getNode(pirate);
		Set<Graph.Node> visible = pirateLoc.getVisibleNodes();
		Graph.Node goalLoc = g.getNode(goal);
		return visible.contains(goalLoc);
	}

	public Graph getGraph(){ return g; }
}
