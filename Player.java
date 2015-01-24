/**
 * The RCSS environment is:
 * * Partially-Observable
 * * Multi-agent
 * * Stochastic
 * * Sequential
 * * Semi-dynamic
 * * Continuous
 * 
 * Our team's AI is a reflex agent. Each class has a number of rules defining behaviour in certain situations.
 * Though it has a basic model of the world (positions of all flags), it doesn't store any states of things it can't see.
 * 
 * Changes
 * =======
 * Renamed class to Player - much more descriptive than Simple.
 * Created subclasses for Defender, Midfielder & Striker - they mostly do the same thing so sharing code makes sense.
 * 
 * Imported BetterGoalie and renamed to Goalie (also converted to subclass of Player).
 * 
 * BS: make everything that is private, protected, so it can be used in our subclasses
 * MW: Create Item.java: class to hold info about stuff we can see (used for flags and ball)
 * 
 */
//~--- non-JDK imports --------------------------------------------------------

import com.github.robocup_atan.atan.model.ActionsPlayer;
import com.github.robocup_atan.atan.model.ControllerPlayer;
import com.github.robocup_atan.atan.model.enums.Errors;
import com.github.robocup_atan.atan.model.enums.Flag;
import com.github.robocup_atan.atan.model.enums.Line;
import com.github.robocup_atan.atan.model.enums.Ok;
import com.github.robocup_atan.atan.model.enums.PlayMode;
import com.github.robocup_atan.atan.model.enums.RefereeMessage;
import com.github.robocup_atan.atan.model.enums.ServerParams;
import com.github.robocup_atan.atan.model.enums.ViewAngle;
import com.github.robocup_atan.atan.model.enums.ViewQuality;
import com.github.robocup_atan.atan.model.enums.Warning;

import org.apache.log4j.Logger;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.awt.Point;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * A simple controller. It implements the following simple behaviour. If the
 * client sees nothing (it might be out of the field) it turns 180 degree. If
 * the client sees the own goal and the distance is less than 40 and greater
 * than 10 it turns to his own goal and dashes. If it cannot see the own goal
 * but can see the ball it turns to the ball and dashes. If it sees anything but
 * not the ball or the own goals it dashes a little bit and turns a fixed amount
 * of degree to the right.
 *
 * @author Atan
 */
public class Player implements ControllerPlayer {

    /**
     *kick the ball and follow it
     */
    protected void dribble() {
        // kick the ball somewhere and follow it
        double angle = 30; // 
        getPlayer().kick(30, angle); // kick ball
        turn(angle); // turn towards the ball
        getPlayer().dash(100); // run towards the ball
    }
    
    /**
     *Stop the ball so we can start to dribble/kick in a different direction
     * NOT YET WORKING
     */
    protected void stopBall() {
        int angle = (int) (ball.dirChange -180);
        int power = 20;
        getPlayer().kick(power, angle);
    }
    
    private Double direction;
    private Point position;
    
    /**
     *Wrap player.turn() so we can keep track of our own direction
     * @param angle
     */
    protected void turn(Double angle) {
        getPlayer().turn(angle);
        if (direction != null) direction += angle;
    }
    protected void turn(int angle) {
        //I hate Java.
        turn((double) angle);
    }
    
    /**
     *Try to figure out where I am
     * based one some trigonometry
     * @return
     * 
     *   v player
     *   a......
     *   |\    .
     *   | \   .
     *  y|  \x .
     *   |   \ .
     *   |    \.c <closest
     *   |    /|
     *   |   / |
     *   |  /z |q
     *   | /   |
     *   |/____|
     *   b  p  d
     *   ^
     *   next
     */
    protected Point position() {
        if (position != null) return position;
        if (flags.size() > 1) {
            Item closest = null;
            Item next = null;
            
            //find the closest 2 visible flags
            for (Item f : flags) {
                if (closest == null || closest.distance > f.distance) {
                    next = closest;
                    closest = f;
                } else if (next == null || next.distance > f.distance) {
                    next = f;
                }
            }
            Point pos_c = closest.position();
            Point pos_b = next.position();
            
            double x = closest.distance;
            double y = next.distance;
            double z;
            
            double angle_bc; //reference angle for calculating the angle of Xca
            //(skip bcd triangle if the flags are parallel)
            if (pos_b.x == pos_c.x) {
                angle_bc = (pos_c.y > pos_b.y) ? 0 : 180;
                //z = abs(pos_c.y - pos_b.y);
            } else if (pos_b.y == pos_c.x) {
                angle_bc = (pos_c.x > pos_b.x) ? 90 : -90;
                //z = abs(pos_c.x - pos_b.x);
            } else {
                double p = abs(pos_c.x-pos_b.x);
                double q = abs(pos_c.y-pos_b.y);
                //Pythagoras' theorem
                z = abs(sqrt(pow(q,2)+pow(p,2)));
                //arccosine to calculate an angle based on the sides
                double angle_c = acos((pow(x,2)+pow(y,2)-pow(z,2))/2*x*y);

                angle_bc = atan2(p,q);
            }
            
            double angle_a = abs(toRadians(closest.direction - next.direction));
            
            //facingDirection = ;
            
            position = new Point((int)toDegrees(x*cos(angle_bc)),(int)toDegrees(x*sin(angle_bc)));
            
            return position;
        }
        return null;
    }

    
    protected Item ball;
    
    /**
     * Extended HashMap to return a default value
     * @param <K>
     * @param <V>
     */
    protected class DefMap<K,V> extends HashMap<K,V> {
        HashMap<K,V> map = new HashMap<>();
        V def;
        
        protected DefMap(V def) {
            this.def=def;
        }
        @Override
        public V get(Object K) {
            if (containsKey(K)) {
                return super.get(K);
            } else {
                return def;
            }
        }
    }

    /**
     *Store the currently visible flags.
     * Not sure whether it's best to generalise it to contain all visible things,
     * or create other lists for other things? What do you think?
     */
    protected ArrayList<Item> flags = new ArrayList<>();
    
    protected static int    count         = 0;
    protected static Logger log           = Logger.getLogger(Player.class);
    protected Random        random        = null;
    
    protected boolean       canSeeNothing = true;
    
    double closestOpponentDistance = 100;
    int closestOpponentArrayPosition;
    
    int playerNumber;
    protected byte[] sayArray = new byte[10];
    // sayArray[0] = player number
    // sayArray[1] = distance from the ball
    
    protected ActionsPlayer player;
    protected boolean canSeeGoalOwn = false;
    protected double distanceGoalOwn;
    protected double directionGoalOwn;    
    protected boolean canSeeGoalOther = false;
    protected double distanceGoalOther;
    protected double directionGoalOther;
    
    
    protected boolean canSeeSeeLine = false;
    protected double distanceSeeLine;
    protected double directionSeeLine;
    
    protected boolean canSeePlayerOther = false;
    protected double distancePlayerOther;
    protected double directionPlayerOther;
    
    protected boolean canSeePlayerOwn = false;
    protected double distancePlayerOwn;
    protected double directionPlayerOwn;
    
    protected boolean canSeeBall = false;
    protected double directionBall;
    protected double distanceBall;
    
    // variables used by goalie and defence
    protected double distBall = 1000;
    protected double dirBall = 0;
    protected double dirOwnGoal = 0;
    protected double distGoal = -1.0;
    protected boolean canSeeGoal = false;
    protected boolean canSeeGoalLeft = false;
    protected boolean canSeeGoalRight = false;
    protected boolean canSeeFieldEnd = false;
    protected boolean alreadySeeingGoal = false;
    protected boolean canSeePenalty = false;
    protected double dirMultiplier = 1.0;
    protected double goalTurn;
    protected boolean needsToRetreat = false;
    
    /**
     * Constructs a new simple client.
     */
    public Player() {
        random = new Random(System.currentTimeMillis() + count);
        count++;
    }

    /** {@inheritDoc} */
    @Override
    public ActionsPlayer getPlayer() {
        return player;
    }

    /** {@inheritDoc} */
    @Override
    public void setPlayer(ActionsPlayer p) {
        player = p;
    }

    /** {@inheritDoc} */
    @Override
    public void preInfo() {
        playerNumber = getPlayer().getNumber();
        canSeeNothing = true;
        
        flags = new ArrayList<>();
        ball = null;
        
        position = null;
        
        canSeeGoalOther = false;
        canSeeGoalOwn = false;
        canSeeSeeLine = false;
        canSeePlayerOther = false;    
        canSeePlayerOwn = false;
        canSeeBall = false;
        
        ballPlayerDistance = 1000; // reset the value for the closest player to the ball
        playerBallDistance = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // 0 means cannot see the ball
        playerDirection = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // 0 means cannot see the ball
        playerDistance = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // 0 means cannot see the ball
        closestOpponentDistance = 100; // set closest opponent as far away
        
        // reset the goalie varibles
        distBall = 1000;
        distGoal = 1000;
        canSeeGoal = false;
        canSeeGoalLeft = false;
        canSeeGoalRight = false;
        canSeePenalty = false;
        canSeeFieldEnd = false;
        goalTurn = 0.0; // angle to turn away from the goal
    }

    /** {@inheritDoc} */
    @Override
    public void postInfo() {
        //String sayString = Base64.encode(sayArray);
        //player.say(sayString);
        //System.out.println("BEN 123 SAY "+Base64.encode(sayArray)+" :: "+sayArray[0]+" :: "+sayArray[1]);
        
        player.say("hellooooBEN");
        
        /*if (canSeeNothing) {
            canSeeNothingAction();
        } else if (canSeeOwnGoal) {
            if ((distanceOwnGoal < 40) && (distanceOwnGoal > 10)) {
                canSeeOwnGoalAction();
            } else if (canSeeBall) {
                canSeeBallAction();
            } else {
                canSeeAnythingAction();
            }
        } else if (canSeeBall) {
            canSeeBallAction();
        } else {
            canSeeAnythingAction();
        }*/
    } 
    
    /**
     *Attempt to pass the ball to another player
     * @param number
     * OBVIOUSLY NOT YET WORKING
     */
    public void passBall(int number) {
        
    }
    
    public int passPower (double distance){
        return (int) (100 - 100/distance * 20);
    }
    
    public void pass (double distance, double direction){
        //int passPower =(int)(100 - 100/distance * 20);
        int passPower =(int)(distance * 2);
        getPlayer().kick(passPower, direction);
    }
    
    /**
     * method to dribble with the ball
     * @param direction is the direction the player is to dribble
     */
    public void fastDribble(double direction){
        getPlayer().kick(20, direction); // kick ball in direction
        player.turn(direction); // turn towards ball
        getPlayer().dash(100); // run towards ball
    }
    
    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagRight(Flag flag, double distance, double direction, double distChange, double dirChange,
                                 double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        
        flags.add(new Item(flag,"right",distance,direction,distChange,dirChange,bodyFacingDirection,headFacingDirection));
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagLeft(Flag flag, double distance, double direction, double distChange, double dirChange,
                                double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        
        flags.add(new Item(flag,"left",distance,direction,distChange,dirChange,bodyFacingDirection,headFacingDirection));
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagOwn(Flag flag, double distance, double direction, double distChange, double dirChange,
                               double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        
        flags.add(new Item(flag,"own",distance,direction,distChange,dirChange,bodyFacingDirection,headFacingDirection));
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagOther(Flag flag, double distance, double direction, double distChange, double dirChange,
                                 double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        
        flags.add(new Item(flag,"other",distance,direction,distChange,dirChange,bodyFacingDirection,headFacingDirection));
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagCenter(Flag flag, double distance, double direction, double distChange, double dirChange,
                                  double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        
        flags.add(new Item(flag,"center",distance,direction,distChange,dirChange,bodyFacingDirection,headFacingDirection));
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagCornerOwn(Flag flag, double distance, double direction, double distChange, double dirChange,
                                     double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        
        flags.add(new Item(flag,"cornerOwn",distance,direction,distChange,dirChange,bodyFacingDirection,headFacingDirection));
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagCornerOther(Flag flag, double distance, double direction, double distChange,
                                       double dirChange, double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        
        flags.add(new Item(flag,"cornerOwn",distance,direction,distChange,dirChange,bodyFacingDirection,headFacingDirection));
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagPenaltyOwn(Flag flag, double distance, double direction, double distChange,
                                      double dirChange, double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        canSeePenalty = true;
        flags.add(new Item(flag,"penaltyOwn",distance,direction,distChange,dirChange,bodyFacingDirection,headFacingDirection));
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagPenaltyOther(Flag flag, double distance, double direction, double distChange,
            double dirChange, double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        
        flags.add(new Item(flag,"penaltyOther",distance,direction,distChange,dirChange,bodyFacingDirection,headFacingDirection));
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagGoalOwn(Flag flag, double distance, double direction, double distChange, double dirChange,
                                   double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        canSeeGoalOwn = true;
        distanceGoalOwn = distance;
        flags.add(new Item(flag,"goalOwn",distance,direction,distChange,dirChange,bodyFacingDirection,headFacingDirection));
                
        if (!alreadySeeingGoal) { // if can't see goal
            dirMultiplier *= -1.0; // flip the direction multiplier 
        }

        if (flag.compareTo(Flag.CENTER) == 0) { // if can see the centre of the goal
            directionGoalOwn = direction;
            distGoal = distance; // set the distance
            dirOwnGoal = direction; // set the direction
            canSeeGoal = true; // can see goal
            goalTurn = 180; // set the goal turn to 180 degrees
        }
        if (flag.compareTo(Flag.LEFT) == 0) { // if can see left of goal
            canSeeGoalLeft = true; // can see left post
            goalTurn = 90; // set the goal turn to 90 degrees
        }
        if (flag.compareTo(Flag.RIGHT) == 0) { // if can see right of goal
            canSeeGoalRight = true; // can see right post
            goalTurn = -90; // set the goal turn to -90 degrees
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagGoalOther(Flag flag, double distance, double direction, double distChange, double dirChange,
                                     double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        
        flags.add(new Item(flag,"goalOther",distance,direction,distChange,dirChange,bodyFacingDirection,headFacingDirection));
        if (flag == Flag.CENTER) {
            canSeeGoalOther = true;
            distanceGoalOther = distance;
            directionGoalOther = direction;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeLine(Line line, double distance, double direction, double distChange, double dirChange,
                            double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        
        canSeeSeeLine = true;
        distanceSeeLine = distance;
        directionSeeLine = direction;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeePlayerOther(int number, boolean goalie, double distance, double direction, double distChange,
                                   double dirChange, double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        if (distance < closestOpponentDistance){
            closestOpponentDistance = distance; // set the distance of the closest opponent
            closestOpponentArrayPosition = number+11; // set the array positon of the closest opponent 
        }
                
        canSeePlayerOther = true;
        distancePlayerOther = distance;
        directionPlayerOther = direction;
        
        // add the seen player to the array
        playerDistance[number+11] = distance;
        playerDirection[number+11] = direction;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeePlayerOwn(int number, boolean goalie, double distance, double direction, double distChange,
                                 double dirChange, double bodyFacingDirection, double headFacingDirection) {
        
        System.out.println("BEN *** "+number +" + "+ goalie+" + "+ distance+" + "+ direction+" + "+ distChange+" + "+ dirChange+" + "+bodyFacingDirection+" + "+ headFacingDirection);
        if (number >= 0){ // if number is not negative, -1 is showing 
            canSeeNothing = false;

            canSeePlayerOwn = true;
            distancePlayerOwn = distance;
            directionPlayerOwn = direction;

            // add the seen player to the array
            playerDistance[number] = distance;
            playerDirection[number] = direction;
        }
        
        if (distance < ballPlayerDistance){
            ballPlayerDistance = distance;
            ballPlayerNumber = number;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeBall(double distance, double direction, double distChange, double dirChange,
                            double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing      = false;
       
        distBall = distance;
        dirBall = direction;
        
        //this.ball = new Item(null,"ball",distance,direction,distChange,dirChange,
        //        bodyFacingDirection,headFacingDirection);
        
        this.canSeeBall    = true;
        this.distanceBall  = distance;
        this.directionBall = direction;
        
        sayArray[0] = (byte) playerNumber; // add the player number to the say array
        sayArray[1] = (byte) distance; // add the ball distance to the say array
        
        playerBallDistance[playerNumber] = distance;
        
        if (distance <= ballPlayerDistance){
            ballPlayerDistance = distance;
            ballPlayerNumber = playerNumber;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoHearReferee(RefereeMessage refereeMessage) {}
    //we are football players so we ignore the referee.

    /** {@inheritDoc} */
    @Override
    public void infoHearPlayMode(PlayMode playMode) {
        //added GOAL_OTHER and GOAL_OWN as times to reset position using move()
        if     (playMode == PlayMode.BEFORE_KICK_OFF || 
                playMode == PlayMode.GOAL_OTHER ||
                playMode == PlayMode.GOAL_OWN) {
            this.pause(1000);
            switch (this.getPlayer().getNumber()) {
                case 1 :
                    this.getPlayer().move(-50, 0);
                    break;
                case 2 :
                    this.getPlayer().move(-40, 10);
                    break;
                case 3 :
                    this.getPlayer().move(-40, -10);
                    break;
                case 4 :
                    this.getPlayer().move(-40, -20);
                    break;
                case 5 :
                    this.getPlayer().move(-40, 20);
                    break;
                case 6 :
                    this.getPlayer().move(-30, 0);
                    break;
                case 7 :
                    this.getPlayer().move(-20, 20);
                    break;
                case 8 :
                    this.getPlayer().move(-20, 0);
                    break;
                case 9 :
                    this.getPlayer().move(-20, -20);
                    break;
                case 10 :
                    this.getPlayer().move(-10, 10);
                    break;
                case 11 :
                    this.getPlayer().move(-10, -10);
                    break;
                default :
                    throw new Error("number must be initialized before move");
            }
            direction = (double) (0);
        }
    }

    double ballPlayerDistance = 10000; // distance between ball and our nearest player
    int ballPlayerNumber; // number of our player nearest the ball
    double[] playerBallDistance = new double[12]; // playerBallDistance[0] is player 0 distance from the ball
    double[] playerDirection = new double[24]; // direction of all the players
    double[] playerDistance = new double[24]; // distance of all of the players
    
    /** {@inheritDoc} */
    @Override
    public void infoHearPlayer(double direction, String message) {
        System.out.println("BEN 123 "+message);
        /*byte[] heardBytes = Base64.decode(message);
        int heardPlayerNumber = (int) heardBytes[0]; // get the player number
        int ballDistance = (int) heardBytes[1]; // get distance from the ball

        System.out.println("BEN 123 HEAR "+message+" :: "+heardBytes[0]+" :: "+heardBytes[1]);
        playerBallDistance[heardPlayerNumber] = ballDistance; // add distance to the player distance from ball array
        
        if(playerDirection[heardPlayerNumber] == 0){ // if the player has not been seen by this player
            playerDirection[heardPlayerNumber] = direction; // set the direction of the player
            playerDistance[heardPlayerNumber] = ballDistance; // set the distance of the player for passing
        }
        
        if (ballDistance < ballPlayerDistance){ // if the player is closer to the ball that the current player
            ballPlayerDistance = ballDistance; // set the distance
            ballPlayerNumber = heardPlayerNumber; // set the number
        }*/
    }

    /** {@inheritDoc} */
    @Override
    public void infoSenseBody(ViewQuality viewQuality, ViewAngle viewAngle, double stamina, double unknown,
                              double effort, double speedAmount, double speedDirection, double headAngle,
                              int kickCount, int dashCount, int turnCount, int sayCount, int turnNeckCount,
                              int catchCount, int moveCount, int changeViewCount) {}

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return "Simple";
    }

    /** {@inheritDoc} */
    @Override
    public void setType(String newType) {}

    /** {@inheritDoc} */
    @Override
    public void infoHearError(Errors error) {}

    /** {@inheritDoc} */
    @Override
    public void infoHearOk(Ok ok) {}

    /** {@inheritDoc} */
    @Override
    public void infoHearWarning(Warning warning) {}

    /** {@inheritDoc} */
    @Override
    public void infoPlayerParam(double allowMultDefaultType, double dashPowerRateDeltaMax,
                                double dashPowerRateDeltaMin, double effortMaxDeltaFactor, double effortMinDeltaFactor,
                                double extraStaminaDeltaMax, double extraStaminaDeltaMin,
                                double inertiaMomentDeltaFactor, double kickRandDeltaFactor,
                                double kickableMarginDeltaMax, double kickableMarginDeltaMin,
                                double newDashPowerRateDeltaMax, double newDashPowerRateDeltaMin,
                                double newStaminaIncMaxDeltaFactor, double playerDecayDeltaMax,
                                double playerDecayDeltaMin, double playerTypes, double ptMax, double randomSeed,
                                double staminaIncMaxDeltaFactor, double subsMax) {}

    /** {@inheritDoc} */
    @Override
    public void infoPlayerType(int id, double playerSpeedMax, double staminaIncMax, double playerDecay,
                               double inertiaMoment, double dashPowerRate, double playerSize, double kickableMargin,
                               double kickRand, double extraStamina, double effortMax, double effortMin) {}

    /** {@inheritDoc} */
    @Override
    public void infoCPTOther(int unum) {}

    /** {@inheritDoc} */
    @Override
    public void infoCPTOwn(int unum, int type) {}

    /** {@inheritDoc} */
    @Override
    public void infoServerParam(HashMap<ServerParams, Object> info) {}

    /**
     * This is the action performed when the player can see the ball.
     * It involves running at it and kicking it...
     */
    protected void canSeeBallAction() {
        
        getPlayer().dash(30);
        turnTowardBall();
        if (distanceBall < 0.7) {
            getPlayer().kick(50, randomKickDirectionValue());
        }
        if (log.isDebugEnabled()) {
            //log.debug("b(" + directionBall + "," + distanceBall + ")");
        }
    }

    /**
     * If the player can see anything that is not a ball or a goal, it turns.
     */
    protected void canSeeAnythingAction() {
        getPlayer().dash(this.randomDashValueSlow());
        turn(20); //all calls to getPlayer().turn() replaced with Player::turn wrapper
        if (log.isDebugEnabled()) {
            //log.debug("a");
        }
    }

    /**
     * If the player can see nothing, it turns 180 degrees.
     */
    protected void canSeeNothingAction() {
        turn(180);
        if (log.isDebugEnabled()) {
            //log.debug("n");
        }
    }

    /**
     * If the player can see its own goal, it goes and stands by it...
     */
    protected void canSeeOwnGoalAction() {
        getPlayer().dash(this.randomDashValueFast());
        turnTowardOwnGoal();
        if (log.isDebugEnabled()) {
            //log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
        }
    }

     /**
      * Randomly choose a very fast dash value
     * {@inheritDoc}
     */
    protected int randomDashValueVeryFast() {
        return 100 + random.nextInt(30);
    }
    
    /**
     * Randomly choose a fast dash value.
     * @return
     */
    protected int randomDashValueFast() {
        return 30 + random.nextInt(100);
    }

    /**
     * Randomly choose a slow dash value.
     * @return
     */
    protected int randomDashValueSlow() {
        return -10 + random.nextInt(50);
    }

    /**
     * Turn towards the ball.
     */
    protected void turnTowardBall() {
        turn(directionBall);
    }

    /**
     * Turn towards our goal.
     */
    protected void turnTowardOwnGoal() {
        turn(directionGoalOwn);
    }
    
    /**
     * BS: add method to turn the player towards the other goal
     * Turn towards other goal.
     */
    protected void turnTowardOtherGoal() {
        turn(directionGoalOther);
    }

    /**
     * Randomly choose a kick direction.
     * @return
     */
    protected int randomKickDirectionValue() {
        return -45 + random.nextInt(90);
    }
    
    /**
     * This method implements all the clever stuff we do when 
     * trying to score a goal, like kicking the ball towards the goal.
     */
    protected void strike() {
        getPlayer().kick(100, directionGoalOther); // shoot
    }

    /**
     * Pause the thread.
     * @param ms How long to pause the thread for (in ms).
     */
    protected synchronized void pause(int ms) {
        try {
            this.wait(ms);
        } catch (InterruptedException ex) {
            //log.warn("Interrupted Exception ", ex);
        }
    }
}
