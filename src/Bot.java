/**
 * Bot is the superclass of the Ninja and Pirate classes. Our test code assumes
 * that you have a constructor that accepts (nodeFile, edgeFile,
 * visibleEdgeFile) so you should start the subclass implementations with these
 * skeletons:
 * 
 * <pre>
 * public class Ninja implements Bot {
 *   public Ninja(String nodeFile, String edgeFile, String visibleEdgeFile) {
 *     // you must have this constructor
 *   }
 * }
 * 
 * public class Pirate implements Bot {
 *   public Pirate(String nodeFile, String edgeFile, String visibleEdgeFile) {
 *     // you must have this constructor
 *   }
 * }
 * </pre>
 */
public interface Bot {

    /**
     * Lets the bot make a single move.
     * @return the <code>GUID</code> of the node that the bot has decided to
     * move to next and update internal state (if any) to reflect that move
     */
    public String nextMove(boolean canHearOther);
        
    /**
     * Resets the bot to get ready for a game.
     * @param pirateStart where the pirate starts
     * @param ninjaStart where da ninja begin mon
     * @param ninjaGoal where you wanna go ninja?
     */
    public void setup(String pirateStart, String ninjaStart, String ninjaGoal);
}
