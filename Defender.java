/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ben
 */
public class Defender extends Player {

    @Override

    public void postInfo() {
        if (distBall < 40){ // if the ball is within 15m
            if (distBall < 0.7){ // if the ball is within catching distance
                this.getPlayer().catchBall(dirBall); // catch the ball
                if (canSeeGoal) { // if can see own goal
                    this.getPlayer().kick(80, 180); // kick the ball behind
                } else {
                    this.getPlayer().kick(80, 0); // kick the ball ahead
                }
            } else if (distBall < 20) { // if the ball is within 5m
                needsToRetreat = true;
                getPlayer().turn(dirBall); // turn towards ball
                getPlayer().dash(50); // run towards ball
            } else { // ball is between 20m and 40m
                needsToRetreat = true;
                getPlayer().turn(dirBall); // turn towards ball
                getPlayer().dash(100); // run towards ball
            }
        } else {
            if (!canSeeGoal && !needsToRetreat) { // if can't see goal and don't need to retrear
                if (!canSeePenalty) { // if cant see penalty spot
                    getPlayer().turn(90); // turn 90 degrees
                    getPlayer().dash(randomDashValueFast()); // run forward
                } else if ((canSeeGoalLeft || canSeeGoalRight) && !canSeeFieldEnd) { // can see one post, but not the other
                    getPlayer().turn(-1.0 * goalTurn); // turn a flipped goalTurn
                    getPlayer().dash(randomDashValueSlow()); // run foward
                } else {
                    getPlayer().turn(25 * dirMultiplier); // turn 25 degrees either + or - dependent on whether can see goal
                }
            } else {
                if (!canSeeGoal) { // can't see own goal
                    getPlayer().turn(90); // turn 90 degrees
                    getPlayer().dash(randomDashValueSlow()); // 
                } else if (distGoal > 3.5) { // if goal is within 3.5m
                    if (!alreadySeeingGoal) { // if can't see goal

                        getPlayer().turn(dirOwnGoal); // turn towards goal
                        alreadySeeingGoal = true; // can now see goal
                    }
                    getPlayer().dash(randomDashValueVeryFast()); // run forwards

                } else {
                    needsToRetreat = false; // no longer need to retreat
                    if (alreadySeeingGoal) { // if can see goal
                        getPlayer().turn(goalTurn); // turn away from goal
                        alreadySeeingGoal = false; // can no longer see goal
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
        return "Defender";
    }

}
