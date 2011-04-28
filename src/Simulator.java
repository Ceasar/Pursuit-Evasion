/**
 * @author tas121.  Any concerns/complaints: discussion board please.  Feel free to modify 
 * as you'd like, but this is what we'll be using. 
 */

import java.io.IOException;
public class Simulator {
    private final static int NUM_SIMULATIONS = 200;
    private final static int VISUAL_PAUSE_MS = 1;
    final static int NINJA_WON = 1, PIRATE_WON = -1, TIE = 0;
    
    private static Bot makeNinja(World w) throws IOException {
        return new Ninja(w.getGraph());
    }
        
    private static Bot makePirate(World w) throws IOException {
        return new Pirate(w.getGraph());
        //return new Pirate(w.getGraph());
    }
    
    static int totalScore;
    public static void main(String[] args){
        if(args.length != 3){
            System.out.println("Wrong number of arguments.  Expected: java Simulator nodeFile edgeFile visibEdgeFile");
            System.exit(0);
        }
        totalScore = 0;
        setUpSimulation(args[0], args[1], args[2]);
        for(int x = 0; x < NUM_SIMULATIONS; x++){
            totalScore += run();
        }
        System.out.println("Game over, total score = "+  totalScore);
        System.out.println("Note: positive number = better ninja, negative number = better pirate");
    }
        
    /**
     * @return 0 if ties, 1 if Ninja won, -1 if Pirate won
     */
    final static String PIRATE_CHAR = "pirate", NINJA_CHAR = "ninja", TARGET_CHAR = "target";
    static Visual visual;
    static World world;
    private static void setUpSimulation(String nodes, String edges, String visible) {
        world = null;
        //sample starts;
        try{
            world = new World(nodes, edges, visible); 
        } catch (IOException e) {
            System.out.println("World crashes!");
            e.printStackTrace();
        }
        visual = new Visual(world.g);
        visual.init();
        
    }
    
    public static int run(){
    	Bot ninja = null, pirate = null;
    	try {
            ninja = makeNinja(world);
        } catch (IOException e) {
            System.out.println("Ninja crashes!");
            e.printStackTrace();
            return PIRATE_WON;
        }
        try{
            pirate = makePirate(world);
        } catch (IOException e) {
            System.out.println("Pirate crashes!");
            e.printStackTrace();
            return NINJA_WON;
        }
                
        String pSt = world.getRandomLoc(), nSt = world.getRandomLoc(), nGl = world.getRandomLoc();
        visual.movedCharacter(PIRATE_CHAR, pSt);
        visual.movedCharacter(NINJA_CHAR, nSt);
        visual.movedCharacter(TARGET_CHAR, nGl);
        Simulator sim = new Simulator(world, pirate, ninja, 100, nSt, nGl, pSt, visual);
        MoveResult mr = MoveResult.Nothing;
        do {
            mr = sim.nextMove();
            visual.draw();
        }while(mr == MoveResult.Nothing);
        System.out.println("Game over, result = " + mr);
        return mr.gameResult();
    }
        
    private World w;
    private Bot p, n;
    private int turnsLeft;
    private long ninjaTime, pirateTime;
    private Graph graph;
    private String ninjaGoal;
    private String pirateLast, ninjaLast;
    public Simulator(World world, Bot pirate, Bot ninja, int maxTurns, String ninjaStart, String ninjaGoal, String pirateStart, Visual visual){
        this.visual = visual;
        w = world;
        p = pirate; n = ninja; 
        turnsLeft = maxTurns;
        p.setup(pirateStart, ninjaStart, ninjaGoal);
        n.setup(pirateStart, ninjaStart, ninjaGoal);
        pirateLast = pirateStart;
        ninjaLast = ninjaStart;
        this.ninjaGoal = ninjaGoal; 
    }
        
    public MoveResult nextMove(){
        String newPLoc, newNLoc;
        //can you hear one another?
        boolean canHear = w.canHearEachOther(pirateLast, ninjaLast);
        try {
        	long startTime = System.nanoTime();
            newPLoc = p.nextMove(canHear);
            long endTime = System.nanoTime();
            pirateTime += endTime - startTime;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return MoveResult.PirateCrashed;
        }
        visual.movedCharacter(PIRATE_CHAR, newPLoc);
        try { 
        	long startTime = System.nanoTime();
            newNLoc = n.nextMove(canHear);
            long endTime = System.nanoTime();
            startTime += endTime - startTime;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return MoveResult.NinjaCrashed;
        }
        visual.movedCharacter(NINJA_CHAR, newNLoc);
        //ok, so nobody crashed.
        turnsLeft--;
        if(!w.isValidMove(pirateLast, newPLoc)) return MoveResult.PirateCheated;
        else if (w.isPirateTooClose(newPLoc, ninjaGoal)) return MoveResult.PirateCheated;
        else if(!w.isValidMove(ninjaLast, newNLoc)) return MoveResult.NinjaCheated;
        else if(ninjaGoal.equals(newNLoc)) return MoveResult.NinjaArrives;
        else if(w.pirateCatchesNinja(newPLoc, newNLoc)) return MoveResult.PirateCatchesNinja;
        else if (turnsLeft == 0) return MoveResult.TimeOver;
        else {
            //nothing so far
            pirateLast = newPLoc;
            ninjaLast = newNLoc;
            return MoveResult.Nothing;
        }
    }
        
    public enum MoveResult {
        //0 if ties, 1 if Ninja won, -1 if Pirate won
        //Note: timeout means the pirate won (since he prevented the ninja, right?
        //PS - if you're wondering how the heck this works, check out java enums.
        Nothing(TIE), TimeOver(PIRATE_WON), NinjaArrives(NINJA_WON), PirateCatchesNinja(PIRATE_WON),
            PirateCheated(NINJA_WON), NinjaCheated(PIRATE_WON), PirateCrashed(NINJA_WON), NinjaCrashed(PIRATE_WON);

        private MoveResult(int score){
            this.score = score;
        }
        private int score;
        public int gameResult(){return score;}
    };
}