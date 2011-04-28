import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Visual extends Canvas{

	Graph graph;
	PathFinder pathfinder;
	private HashMap<String, Graph.Node> locs;
	boolean waiting;

	//Swing tools
	BufferStrategy strategy;
	Toolkit toolkit;
	Dimension screensize;

	public Visual(Graph graph){
		this.graph = graph;
		this.pathfinder = new PathFinder(graph);
		locs = new HashMap<String, Graph.Node>();

		toolkit = java.awt.Toolkit.getDefaultToolkit();
		screensize = toolkit.getScreenSize();
		addKeyListener(new KeyInputHandler());
		waiting = true;
	}

	JFrame container;
	public void init(){
		container = new JFrame("Visual");

		JPanel panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(screensize);
		setBounds(0,0,screensize.width,screensize.height);
		panel.setLayout(null);
		panel.add(this);

		setIgnoreRepaint(true);

		container.pack();
		container.setResizable(true);
		container.setVisible(true);
		container.setExtendedState(JFrame.MAXIMIZED_BOTH);

		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		requestFocus();

		createBufferStrategy(2);
		strategy = getBufferStrategy();
	}

	public void movedCharacter(String pirateChar, String loc){
		locs.put(pirateChar, graph.getNode(loc));
	}

	final double WIDTH = 2400;
	final double HEIGHT = 3200;
	final int RADIUS = 7;
	public void draw() {
		Dimension dim = container.getSize();
		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, dim.width, dim.height);

		Set<Graph.Node> nodes = graph.getNodes();
		g.setColor(Color.black);
		//Collection<Double> potentials = Pirate.potentialmap.values();
		for (Graph.Node node : nodes){
			g.setColor(Color.black);
			g.drawString(Integer.toString(Simulator.totalScore), 10, 10);
			int x = (int) (node.getX() / WIDTH * dim.width);
			int y = (int) (node.getY() / HEIGHT * dim.height);
			g.drawOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
			for (Graph.Node neighbor : node.getNeighbors().keySet()){
				int nx = (int) (neighbor.getX() / WIDTH * dim.width);
				int ny = (int) (neighbor.getY() / HEIGHT * dim.height);
				g.drawLine(x, y, nx, ny);
			}
			//double potential = (int)(Pirate.potentialmap.get(node) * 1000) / 1000.0;
			if (Ninja.mids2.contains(node)){
				g.setColor(new Color(0, 255, 155));
				g.fillOval(x - RADIUS - 3, y - RADIUS - 3, RADIUS * 2 + 6, RADIUS * 2 + 6);
			}
			if (Ninja.possible_p.contains(node)){
				g.setColor(new Color(105, 0, 0));
				g.fillOval(x - RADIUS + 1, y - RADIUS + 1, RADIUS * 2 - 2, RADIUS * 2 - 2);
			}
//			if (Ninja.possible_n.contains(node)){
//				g.setColor(new Color(0, 0, 105));
//				g.fillOval(x - RADIUS + 1, y - RADIUS + 1, RADIUS * 2 - 2, RADIUS * 2 - 2);
//			}
			if (Ninja.usable2.contains(node)){
				g.setColor(new Color(0, 255, 0));
				g.drawOval(x - RADIUS - 3, y - RADIUS - 3, RADIUS * 2 + 6, RADIUS * 2 + 6);
			}
			if (Ninja.interceptSet.contains(node)){
				g.setColor(new Color(255, 255, 0));
				g.fillOval(x - RADIUS - 3, y - RADIUS - 3, RADIUS * 2 + 6, RADIUS * 2 + 6);
			}
			if (Ninja.interceptSet2.contains(node)){
				g.setColor(new Color(255, 125, 0));
				g.fillOval(x - RADIUS - 3, y - RADIUS - 3, RADIUS * 2 + 6, RADIUS * 2 + 6);
			}
			if (node.equals(Ninja.destination)){
				g.setColor(new Color(0, 255, 255));
				g.fillOval(x - RADIUS - 3, y - RADIUS - 3, RADIUS * 2 + 6, RADIUS * 2 + 6);
			}
		}

		for (String character : locs.keySet()){
			Graph.Node loc = locs.get(character);
			int x = (int) (loc.getX() / WIDTH * dim.width);
			int y = (int) (loc.getY() / HEIGHT * dim.height);
			if (character == Simulator.PIRATE_CHAR)
				g.setColor(Color.RED);
			else
				g.setColor(Color.BLUE);
			if (character == Simulator.TARGET_CHAR)
				g.fillRect(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
			else
				g.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
//			int hradius = 300;
//			int hradiusx = (int)(hradius / WIDTH * dim.width);
//			int hradiusy = (int)(hradius / HEIGHT * dim.height);
//			int[] xpoints = new int[4];
//			xpoints[0] = x; xpoints[1] = x + hradiusx; xpoints[2] = x; xpoints[3] = x - hradiusx;
//			int[] ypoints = new int[4];
//			ypoints[0] = y + hradiusy; ypoints[1] = y; ypoints[2] = y - hradiusy; ypoints[3] = y;
//			g.drawPolygon(xpoints, ypoints, 4);
		}

		if (locs.get(Simulator.PIRATE_CHAR) == locs.get(Simulator.NINJA_CHAR)){
			g.drawString("Pirate wins.", 20, 10);
		}
		else if (locs.get(Simulator.TARGET_CHAR) == locs.get(Simulator.NINJA_CHAR)){
			g.drawString("Ninja wins.", 20, 10);
		}
		if (pathfinder.dijkstra(locs.get(Simulator.PIRATE_CHAR), locs.get(Simulator.NINJA_CHAR)).size() <= 4){
			Graph.Node pirate = locs.get(Simulator.PIRATE_CHAR);
			Graph.Node ninja = locs.get(Simulator.NINJA_CHAR);
			int px = (int)(pirate.getX() / WIDTH * dim.width);
			int py = (int)(pirate.getY() / HEIGHT * dim.height);
			int nx = (int)(ninja.getX() / WIDTH * dim.width);
			int ny = (int)(ninja.getY() / HEIGHT * dim.height);
			g.drawLine(px, py, nx, ny);
		}

		g.dispose();
		strategy.show();
		waiting = true;
		while (waiting)
			try { Thread.sleep(0); } catch (Exception e) {}
	}

	private class KeyInputHandler extends KeyAdapter{

		public KeyInputHandler() {}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				waiting = false;
			}
		} 

		public void keyTyped(KeyEvent e) {
			if (e.getKeyChar() == 27) {
				System.exit(0);
			}
		}
	}

}
