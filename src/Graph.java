import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author CIS 121 TA Staff
 * 
 */
public class Graph {
	private Map<String,Node> nodes;
	
	public Graph(){
		nodes = new HashMap<String,Node>();
	}
	/**
	 * 
	 * @param nodeFile the filename for the node file
	 * @param edgeFile the filename for the edge file
	 * @param visibleFile the filename for the visibility edge file
	 * @throws IOException
	 */
	public Graph(String nodeFile, String edgeFile, String visibleFile) throws IOException{
		nodes = new HashMap<String,Node>();
		buildNodes(nodeFile);
		buildEdges(edgeFile);
		if(visibleFile != null) //We don't need visible if we just test PathFinder
			buildVisibleEdges(visibleFile);
	}
	/**
	 * 
	 * @return the set of nodes in the graph
	 */
	public Set<Node> getNodes(){
		Set<Node> s = new HashSet<Node>();
		for(Node n : nodes.values()){
			s.add(n);
		}
		return s;
	}
	/**
	 * 
	 * @param s the unique id of the node
	 * @return the node with id s
	 */
	public Node getNode(String s){
		return nodes.get(s);
	}
	/**
	 * 
	 * @param n a node to add to the graph
	 */
	public void addNode(Node n){
		nodes.put(n.getId(), n);
	}
	
	/**
	 * 
	 * @param nodeFile the filename for the node file
	 * @throws IOException
	 */
	private void buildNodes(String nodeFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(nodeFile));
		String s = br.readLine(); //Remove header info
		s = br.readLine();
		while(s != null){
			String[] data = s.split(",");
			String id = data[0].trim(), building = data[1].trim(), room = data[2].trim();
			int x = Integer.parseInt(data[3].trim()), y = Integer.parseInt(data[4].trim());
			nodes.put(id,new Node(x,y,id,room,building));
			s = br.readLine();
		}
	}
	
	/**
	 * 
	 * @param edgeFile the filename for the edge file
	 * @throws IOException
	 */
	private void buildEdges(String edgeFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(edgeFile));
		String s = br.readLine(); //Remove header info
		s = br.readLine();
		while(s != null){
			String[] data = s.split(",");
			Node a = getNode(data[0].trim()), b = getNode(data[1].trim());
			double weight = Double.parseDouble(data[2].trim());
			a.addNeighbor(b, weight);
			b.addNeighbor(a, weight);
			s = br.readLine();
		}
	}
	
	/**
	 * 
	 * @param visibleFile the filename for the visibility edge file
	 * @throws IOException
	 */
	private void buildVisibleEdges(String visibleFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(visibleFile));
		String s = br.readLine(); //Remove header info
		s = br.readLine();
		while(s != null){
			String[] data = s.split(",");
			Node a = getNode(data[0].trim()), b = getNode(data[1].trim());
			a.addVisibleNode(b);
			b.addVisibleNode(a);
			s = br.readLine();
		}
	}
	
	//----------------------------------------------------------------
	//                       Node Inner Class
	//----------------------------------------------------------------
	public class Node implements Comparable<Node>{
		private final String room, building, id;
		private final int x, y;
		private double distance; //For Dijkstra's and A-star
		private Map<Node,Double> neighbors;
		private Set<Node> visibleNodes;
		
		/**
		 * 
		 * @param x the x location of the node
		 * @param y the y location of the node
		 * @param id the Globally Unique ID (guid)
		 * @param room the room this node represents
		 * @param building the building in which this node exists
		 */
		public Node(int x, int y, String id, String room, String building){
			neighbors = new HashMap<Node,Double>();
			visibleNodes = new HashSet<Node>();
			this.x = x; this.y = y;
			this.id = id; this.room = room;
			this.building = building;
			distance = 0;
		}
		
		/**
		 * 
		 * @param n the node to copy
		 */
		public Node(Node n){
			this.x = n.getX(); this.y = n.getY();
			this.id = n.getId(); this.room = n.getRoom();
			this.building = n.getBuilding();
		}
		
		//Method to get and set instance variables

		public Map<Node,Double> getNeighbors(){ return neighbors; }
		public Set<Node> getVisibleNodes(){ return visibleNodes; }
		
		public int getX(){ return x; }
		public int getY(){ return y; }
		public String getId(){ return id; }
		public String getRoom(){ return room; }
		public String getBuilding(){ return building; }
		
		public double getDistance(){ return distance; }
		public void setDistance(double distance){ this.distance = distance; }
		
		/**
		 * 
		 * @param n the node to attach to this node
		 * @param edgeWeight the weight of the edge attaching the node
		 */
		public void addNeighbor(Node n, double edgeWeight){
			neighbors.put(n, edgeWeight);
		}
		/**
		 * 
		 * @param n the neighbor whose edge must be removed
		 */
		public void removeNeighbor(Node n){
			if(neighbors.containsKey(n)){ neighbors.remove(n); }
		}
		/**
		 * 
		 * @param n the node between which to add a visibility edge
		 */
		public void addVisibleNode(Node n){
			visibleNodes.add(n);
		}
		/**
		 * 
		 * @param n the node whose edge must be removed
		 */
		public void removeVisibleEdge(Node n){
			if(visibleNodes.contains(n)){visibleNodes.remove(n); }
		}
		//Misc Methods
		@Override 
		public int hashCode() { return id.hashCode(); }

		@Override
		public int compareTo(Node n) {
			int check = ((Double) distance).compareTo(n.getDistance());
			if(check == 0){
				check = this.getId().compareTo(n.getId());
			}
			return check;
		}
		
		public String toString(){
			return room + " " + building + " " + id;
		}
	}
}
