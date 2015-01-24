/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ben
 */
public class Midfielder extends Player {



    @Override
    public void postInfo() {

        if (canSeeNothing) { // if you can't see anything
            canSeeNothingAction();
        } else if (canSeeBall) { // player can see ball
            if (canSeeGoalOwn) { // if player can see own goal
                if (distanceGoalOwn < 20) { // if within 40m of own goal
                    // wait for the ball to leave our area
                } else {
                    attack();
                }
            } else { // if aren't withing 40m of our goal
                attack(); // attack
            }
        } else if (canSeeGoalOwn) {
            if (distanceGoalOwn < 20) { // if within 40m of own goal
                turn(180);
                //player.dash(50);
            } else {
                turn(90);
            }
        } else { // cannot see the ball
            turn(90); // turn 90 degrees
            //canSeeAnythingAction();
        }
    }

    public void attack() {
        if (ballPlayerNumber == playerNumber) { // if player is nearest to the ball
            if (distanceBall < 0.7) { // within distance to kick
                if (canSeeGoalOther) { // can see other goal
                    if (distanceGoalOther < 30) { // if within shooting range
                        strike();
                    } else if (closestOpponentDistance <= 15) { // not in shooting range and opponent near
                        double opponentDir = playerDirection[closestOpponentArrayPosition];
                        double turnAngle;
                        if (-10 < directionGoalOther && directionGoalOther < 10) { // if goal is within 20 degree angle ahead
                            if (opponentDir < 0) { // opponent is to the left
                                turnAngle = opponentDir + 20;
                            } else { // opponent is to the right
                                turnAngle = opponentDir - 20;
                            }
                        } else { // if not facing the goal
                            turnAngle = directionGoalOther; // set turn angle to the goal
                        }
                        fastDribble(turnAngle); // fast dribble past the opponent 
                    } else { // not in shooting range and opponent not near
                        fastDribble(directionGoalOther); // fast dribble towards the goal
                    }
                } else { // can't see other goal
                    // pass the ball to striker or midfield player
                    for (int i = 11; i > 5; i--) { // loop from player 11 to 6: strikers and midfield
                        if (i == playerNumber) { // if this player
                            // do nothing
                        } else if (playerDistance[i] != 0) { // if player distance is known
                            if (playerDistance[i] < 30) { // if player within passing distance
                                pass(playerBallDistance[i], playerDirection[i]); // pass to the player
                                player.turn(directionGoalOther); // turn towards the other goal
                                player.dash(20); // move towards the goal
                            }
                        }
                    }
                    getPlayer().kick(8, 90); // kick ball to the right
                    player.turn(90); // turn towards ball
                    //getPlayer().dash(80); // run towards ball
                }
            } else { // not close enough to kick
                turnTowardBall();
                getPlayer().dash(80);
            }
        } else { // if someone else is nearest the ball
            player.turn(directionBall); // turn towards ball              
            if (distanceBall > 10) { // if other striker is over 10m away
                player.dash(40);
            } else if (distanceBall > 20) { // if other striker is over 20m away
                player.dash(80);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "Midfielder";
    }
}
