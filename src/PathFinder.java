import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * <p>The class you need to write for milestone one. Note that this class has a
 * no-argument constructor. The design and test components of this assignment
 * are up to you; we provide neither tests for the code nor a specification for
 * your graph representation. You need to design and write a tested graph
 * representation, and then write tests and implementation for the methods in
 * this interface.
 * <p>Important Notes for All the Methods / Algorithms:
 * <ol>
 * <li>Paths are represented by lists of nodes along the path. If you need to
 *     return a path with just one node, the list should contain just the node.
 *     For example, a zero edge path SKIR_1_114_1 to SKIR_1_114_1 should be
 *     returned something like <code>Arrays.asList("SKIR_1_114_1")</code>.</li>
 * <li>When the algorithms have to choose among nodes, choose the node that's
 *     alphabetically first to break any ties. For example, in Dijkstra's
 *     algorithm if the best known distance is 10 and both SKIR_1_114_1 and
 *     SKIR_1_190_6 have this distance, process SKIR_1_114_1 next since it's
 *     alphabetically first.</li>
 * <li>Since you know the file formats, you can easily construct your own files
 *     for testing...but be sure to test with the original files as well to
 *     make sure you didn't make mistakes in your test files.</li>
 * <li>Review the logger documentation to see how you can use it to help debug
 *     your code. Here is a simple snippet that builds a logger that prints
 *     debug information on a visual display, in a table, and in the console:
 * <pre>
 *     int delay = 100; // msecs to pause after update to slow visualization
 *     Logger console = Logger.Impl.newConsoleLogger();
 *     Logger image = Logger.Impl.newSwingImageLogger(delay);
 *     Logger table = Logger.Impl.newSwingTableLogger();
 *     // When combining loggers, put swing image logger last so delay is last.
 *     Logger logger = Logger.Impl.newCombinedLogger(
 *         table, Logger.Impl.newCombinedLogger(console, image));
 * </pre>
 * </li>
 * <li>Each algorithm specifies the log tag to use for the tested log calls.
 *     You can make other log calls with a different tag; only those made with
 *     the given tag will be tested. Do not use the tags "BREADTH", "DEPTH",
 *     "DIJKSTRA" for your personal log messages.
 * </ol>
 */
public class PathFinder {

	public static final String BREADTH_TAG = "BREADTH";   // don't change this
	public static final String DEPTH_TAG = "DEPTH";       // don't change this
	public static final String DIJKSTRA_TAG = "DIJKSTRA"; // don't change this
	public static final String PATH_CHARACTER = "geek";   // don't change this
	public static final String ASTAR_TAG = "ASTAR";       // don't change this

	/* IMPORTANT! Don't change the class or method declarations. You should
	 * only add code within the methods, though you're allowed to add helper
	 * methods as needed.
	 */
	private final Graph g;
	/**
	 * @param nodeFileName the node file to load into a graph-like structure
	 * @param edgeFileName the edge file to load into a graph-like structure
	 * @throws IOException if the node and edge file cannot be loaded
	 */
	public PathFinder(String nodeFileName, String edgeFileName, String visiFile) throws IOException {
		g = new Graph(nodeFileName,edgeFileName, visiFile);
	}

	public PathFinder(Graph g){
		this.g = g;
	}

	/**
	 * Execute breadth first search on the given graph to find the path from
	 * {@code sourceNode} to {@code destinationNode} which contains the fewest
	 * number of edges. The algorithm finds discovery edges in a testable
	 * order. Call {@link Logger#traversedEdge(String, String, String)} once
	 * for each discovery edge in the order that they are found. Use the tag
	 * {@link #BREADTH_TAG}, the parent (earlier discovered) node as the second
	 * argument, and the child (later discovered) node as the third argument.
	 * For testing purposes continue the BFS until you have traversed the
	 * entire connected component of the source node.
	 * @return the sequence of Graph Nodes that form the path with the fewest
	 * number of edges, or null if there is no such path; the path should be
	 * made of discovery edges
	 */
	public List<Graph.Node> breadth(Graph.Node sourceNode, Graph.Node destinationNode) {
		List<Graph.Node> marked = new ArrayList<Graph.Node>();
		TreeNode root = new TreeNode(sourceNode);
		Queue<TreeNode> queue = new LinkedList<TreeNode>();
		queue.add(root);
		while (!queue.isEmpty()){
			TreeNode current = queue.poll();
			if (current.node != destinationNode){
				marked.add(current.node);
				for (Graph.Node child : current.node.getNeighbors().keySet()){
					if (!marked.contains(child))
						queue.add(new TreeNode(child, current));
				}
			}
			else{
				return getAncestors(current);
			}
		}
		//If here, target is not in tree.
		return null;
	}

	public List<Graph.Node> getAncestors(TreeNode child){
		List<Graph.Node> ancestors = new ArrayList<Graph.Node>();
		TreeNode currentgen = child;
		while (currentgen.parent != null){
			ancestors.add(currentgen.node);
			currentgen = currentgen.parent;
		}
		ancestors.add(currentgen.node);
		Collections.reverse(ancestors);
		return ancestors;
	}

	/**
	 * Execute depth first search on the given graph to find any path from
	 * {@code sourceNode} to {@code destinationNode}. The algorithm finds
	 * discovery edges in a testable order. Call
	 * {@link Logger#traversedEdge(String, String, String)} once for each
	 * discovery edge in the order that they are found. Use the tag
	 * {@link #DEPTH_TAG}, the parent (earlier discovered) node as the second
	 * argument, and the child (later discovered) node as the third argument.
	 * For testing purposes continue the DFS until you have traversed the
	 * entire connected component of the source node.
	 * @return the sequence of Graph Nodes of the first path found, or null if
	 * there is no such path; the path should be made of discovery edges
	 */
	public List<Graph.Node> depth(Graph.Node sourceNode, Graph.Node destinationNode) {
		List<Graph.Node> path = dfs(new ArrayList<Graph.Node>(), sourceNode, destinationNode);
		path.add(sourceNode);
		Collections.reverse(path);
		return path;
	}

	public List<Graph.Node> dfs(List<Graph.Node> marked, Graph.Node node, Graph.Node target){
		marked.add(node);
		Set<Graph.Node> neighbors = node.getNeighbors().keySet();
		for (Graph.Node neighbor : neighbors){
			if (!marked.contains(neighbor)){
				if (neighbor.equals(target)){
					List<Graph.Node> list = new ArrayList<Graph.Node>();
					list.add(target);
					return list;
				}
				else{
					List<Graph.Node> list = dfs(marked, neighbor, target);
					if (list.contains(target)){
						list.add(neighbor);
						return list;
					}
				}
			}
		}
		return new ArrayList<Graph.Node>();
	}

	/**
	 * Execute Dijkstra's algorithm on the given graph to find the shortest
	 * path from {@code sourceNode} to {@code destinationNode}. The length of
	 * the path is defined as the sum of the edge weights, which are provided
	 * in the edge file. Every time the algorithm pulls a node into the cloud,
	 * call {@link Logger#movedCharacter(String, String)} with the name
	 * {@link #PATH_CHARACTER} and the location equal to the new cloud node.
	 * Every time the algorithm uses an edge to relax the known distances to a
	 * node <b>which is not yet in the cloud</b>, call
	 * {@link Logger#traversedEdge(String, String, String)} with the tag
	 * {@link #DIJKSTRA_TAG}, the node just pulled into the cloud as the second
	 * argument, and the node whose known distance may be relaxed as the third
	 * argument. Do this even if the known distance to the node doesn't
	 * decrease. For testing purposes run the algorithm until all nodes are in
	 * the cloud and be sure to relax the edges using alphabetical ordering of
	 * the nodes.
	 * @return the sequence of node GUIDs of the shortest path found, or null
	 * if there is no such path
	 */
	public List<Graph.Node> dijkstra(Graph.Node sourceNode, Graph.Node destinationNode) {
		HashMap<Graph.Node, Double> dist = new HashMap<Graph.Node, Double>();
		HashMap<Graph.Node, Graph.Node> previous = new HashMap<Graph.Node, Graph.Node>();

		for (Graph.Node node : g.getNodes()){
			dist.put(node, Double.POSITIVE_INFINITY);
			previous.put(node, null);
		}
		dist.put(sourceNode, 0.0);
		Set<Graph.Node> nodes = g.getNodes();
		while (!nodes.isEmpty()){
			Graph.Node current = closest(nodes, dist);
			if (dist.get(current) == Double.POSITIVE_INFINITY)
				break;
			nodes.remove(current);
			if (current == destinationNode){
				List<Graph.Node> sequence = new ArrayList<Graph.Node>();
				while (previous.get(current) != null){
					sequence.add(current);
					current = previous.get(current);
				}
				sequence.add(sourceNode);
				Collections.reverse(sequence);
				return sequence;
			}
			for (Graph.Node neighbor : current.getNeighbors().keySet()){
				double alt = dist.get(current) + current.getNeighbors().get(neighbor);
				if (alt < dist.get(neighbor)){
					dist.put(neighbor, alt);
					previous.put(neighbor, current);
				}
			}
		}
		return new ArrayList<Graph.Node>();
	}

	public HashMap<Graph.Node, Double> dijkstra2(Graph.Node sourceNode, Graph.Node destinationNode) {
		HashMap<Graph.Node, Double> dist = new HashMap<Graph.Node, Double>();
		HashMap<Graph.Node, Graph.Node> previous = new HashMap<Graph.Node, Graph.Node>();

		for (Graph.Node node : g.getNodes()){
			dist.put(node, Double.POSITIVE_INFINITY);
			previous.put(node, null);
		}
		dist.put(sourceNode, 0.0);
		Set<Graph.Node> nodes = g.getNodes();
		while (!nodes.isEmpty()){
			Graph.Node current = closest(nodes, dist);
			if (dist.get(current) == Double.POSITIVE_INFINITY)
				break;
			nodes.remove(current);
			for (Graph.Node neighbor : current.getNeighbors().keySet()){
				double alt = dist.get(current) + current.getNeighbors().get(neighbor);
				if (alt < dist.get(current)){
					dist.put(neighbor, alt);
					previous.put(neighbor, current);
				}
			}
		}
		return dist;
	}

	public Graph.Node closest(Set<Graph.Node> nodes, HashMap<Graph.Node, Double> dist){
		Graph.Node closest = null;
		double shortest = Double.POSITIVE_INFINITY;
		for (Graph.Node node : nodes){
			if (dist.get(node) < shortest){
				shortest = dist.get(node);
				closest = node;
			}
		}
		return closest;
	}

	/**
	 * <p><a href="http://en.wikipedia.org/wiki/A*_search_algorithm">A star
	 * </a> is a generalization of Dijkstra's algorithm that under certain
	 * circumstances  shortens the time it takes to find a shortest path. This
	 * is useful when the search is time-constrained, for example if you are
	 * writing a search in a real-time simulation or if you are searching over
	 * a very very very large graph. Even though these motivations are missing
	 * in this project, If you're adventurous you will have fun implementing
	 * the A star search algorithm as described in the link (use the straight
	 * line distance heuristic, but take the floor of all distances to keep
	 * things discrete).
	 * <p>Much like Dijkstra's algorithm, A star performs a relaxation step in
	 * which it iterates over edges to update the guesses for distances to
	 * neighbors of the node that's currently being processed. Call
	 * {@link Logger#traversedEdge(String, String, String)} with the tag
	 * {@link #ASTAR_TAG}, the node that's currently being processed as the
	 * second argument, and the other node in the edge as the third argument.
	 * As in the method for Dijkstra's algorithm, relax the edges using 
	 * alphabetical ordering of the nodes.
	 * @return the sequence of node GUID)s of the shortest path found, or null
	 * if there is no such path
	 */
//	public List<Graph.Node> aStar(Graph.Node sourceNode, Graph.Node destinationNode, HeuristicI heuristicFunction) {
//		Set<Graph.Node> closedset = new HashSet<Graph.Node>();
//		Set<Graph.Node> openset = new HashSet<Graph.Node>();
//		openset.add(sourceNode);
//		HashMap<Graph.Node, Graph.Node> came_from = new HashMap<Graph.Node, Graph.Node>();
//		
//		HashMap<Graph.Node, Double> g_score = new HashMap<Graph.Node, Double>();
//		HashMap<Graph.Node, Double> h_score = new HashMap<Graph.Node, Double>();
//		HashMap<Graph.Node, Double> f_score = new HashMap<Graph.Node, Double>();
//		
//		g_score.put(sourceNode, 0.0);
//		h_score.put(sourceNode, sourceNode.getDistance());
//		f_score.put(sourceNode, h_score.get(sourceNode));
//		
//		while (!openset.isEmpty()){
//			Graph.Node current = closest(openset, f_score);
//			if (current == destinationNode)
//				return reconstruct_path(came_from, current);
//			openset.remove(current);
//			closedset.add(current);
//			for (Graph.Node neighbor : current.getNeighbors().keySet()){
//				if (closedset.contains(neighbor))
//					continue;
//				double tenative_g_score = g_score.get(current) + current.getNeighbors().get(neighbor);
//				
//				boolean tenative_is_better;
//				if (!openset.contains(neighbor)){
//					openset.add(neighbor);
//					tenative_is_better = true;
//				}
//				else if (tenative_g_score < g_score.get(neighbor)){
//					tenative_is_better = true;
//				}
//				else
//					tenative_is_better = false;
//				
//				if (tenative_is_better){
//					came_from.put(neighbor, current);
//					g_score.put(neighbor, tenative_g_score);
//					h_score.put(neighbor, neighbor.getDistance());
//					f_score.put(neighbor, g_score.get(neighbor) + h_score.get(neighbor));
//				}
//			}
//		}
//		return null;
//	}
	
	public List<Graph.Node> reconstruct_path(HashMap<Graph.Node, Graph.Node> came_from, Graph.Node current_node){
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

	public Graph getGraph(){ return g; }

	private List<Graph.Node> getPath(Map<Graph.Node, Graph.Node> parentOf, Graph.Node sourceNode, Graph.Node destinationNode) {
		if (!parentOf.containsKey(destinationNode)) {
			return null;
		} else {
			List<Graph.Node> path = new LinkedList<Graph.Node>();
			while (destinationNode != null) {
				path.add(0, destinationNode);
				destinationNode = parentOf.get(destinationNode);
			}
			if(!path.get(0).equals(sourceNode))
				return null;
			return path;
		}
	}

	private class TreeNode{
		/*
		 * Helper class for BFS.
		 */
		 private Graph.Node node;
		 private TreeNode parent;

		 public TreeNode(Graph.Node node){
			 this.node = node;
		 }

		 public TreeNode(Graph.Node node, TreeNode parent){
			 this.node = node;
			 this.parent = parent;
		 }
	}
}
