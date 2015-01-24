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

//~--- JDK imports ------------------------------------------------------------
import java.util.HashMap;
import java.util.Random;

/**
 * <p>
 * Silly class.</p>
 *
 * @author Atan
 */
public class Goalie extends Player {

    private double distBall = 1000;
    private double dirBall = 0;
    private double dirOwnGoal = 0;
    private double distGoal = -1.0;
    private boolean canSeeGoal = false;
    private boolean canSeeGoalLeft = false;
    private boolean canSeeGoalRight = false;
    private boolean canSeeFieldEnd = false;
    private boolean alreadySeeingGoal = false;
    private boolean canSeePenalty = false;
    private ActionsPlayer player;
    private Random random = null;
    private static int count = 0;
    private double dirMultiplier = 1.0;
    private double goalTurn;
    private boolean needsToRetreat = false;

    public Goalie() {
        random = new Random(System.currentTimeMillis() + count);
        count++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preInfo() {
        distBall = 1000;
        distGoal = 1000;
        canSeeGoal = false;
        canSeeGoalLeft = false;
        canSeeGoalRight = false;
        canSeePenalty = false;
        canSeeFieldEnd = false;
        goalTurn = 0.0; // angle to turn away from the goal
    }

    /**
     * {@inheritDoc}
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public void postInfo() {

        if (distBall < 15) // if the ball is within 15m
        {
            if (distBall < 0.7) // if the ball is within catching distance
            {
                if (canSeeGoal || canSeePenalty) // if can see goal or penalty spot
                {
                    this.getPlayer().catchBall(dirBall); // catch the ball
                }
                if (canSeeGoal) {
                    this.getPlayer().kick(60, 135); // kick the ball behind
                } else {
                    this.getPlayer().kick(60, 0); // kick the ball ahead
                }
            } else if (canSeeGoal || canSeePenalty) {
                if (distBall < 2) // if the ball is within 2m
                {
                    needsToRetreat = true;

                    getPlayer().turn(dirBall); // turn towards ball
                    getPlayer().dash(randomDashValueFast()); // run towards ball
                } else // ball is between 2m and 15m
                {
                    needsToRetreat = true;
                    getPlayer().turn(dirBall); // turn towards ball
                    getPlayer().dash(randomDashValueVeryFast()); // run towards ball
                }
            }
        } else {

            if (!canSeeGoal && !needsToRetreat) // if can't see goal and don't need to retreat
            {
                if (!canSeePenalty) // if cant see penalty spot
                {
                    getPlayer().turn(90); // turn 90 degrees
                    getPlayer().dash(randomDashValueFast()); // run forward
                } else if ((canSeeGoalLeft || canSeeGoalRight) && !canSeeFieldEnd) // can see one post, but not the other
                {
                    getPlayer().turn(-1.0 * goalTurn); // turn a flipped goalTurn
                    getPlayer().dash(randomDashValueSlow()); // run forward
                } else {
                    getPlayer().turn(25 * dirMultiplier); // turn 25 degrees either + or - dependent on whether can see goal
                }
            } else {

                if (!canSeeGoal) // can't see own goal
                {
                    getPlayer().turn(90); // turn 90 degrees
                    getPlayer().dash(randomDashValueSlow()); // run forwards
                } else if (distGoal > 3.5) // if goal is within 3.5m
                {
                    if (!alreadySeeingGoal) // if can't see goal
                    {
                        getPlayer().turn(dirOwnGoal); // turn towards goal
                        alreadySeeingGoal = true; // can now see goal
                    }

                    getPlayer().dash(randomDashValueVeryFast()); // run forwards
                } else {

                    needsToRetreat = false; // no longer need to retreat

                    if (alreadySeeingGoal) // if can see goal
                    {
                        getPlayer().turn(goalTurn); // turn away from goal
                        alreadySeeingGoal = false;  // can no longer see goal
                    } else {
                        alreadySeeingGoal = true; // can see goal
                    }
                }
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "Goalie";
    }
}
