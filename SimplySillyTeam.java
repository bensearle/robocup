
//~--- non-JDK imports --------------------------------------------------------

import com.github.robocup_atan.atan.model.AbstractTeam;
import com.github.robocup_atan.atan.model.ControllerCoach;
import com.github.robocup_atan.atan.model.ControllerPlayer;

/**
 * A class to setup a Player Silly AbstractTeam.
 *
 * @author Atan
 */
public class SimplySillyTeam extends AbstractTeam {

    /**
     * Constructs a new simple silly team.
     *
     * @param name The team name.
     * @param port The port to connect to SServer.
     * @param hostname The SServer hostname.
     * @param hasCoach True if connecting a coach.
     */
    public SimplySillyTeam(String name, int port, String hostname, boolean hasCoach) {
        super(name, port, hostname, hasCoach);
    }

    /**
     * {@inheritDoc}
     *
     * The first controller of the team is silly the others are simple.
     */
    @Override
    public ControllerPlayer getNewControllerPlayer(int number) {
        //return new Player();
        if (number == 0) {
            return new Goalie();
        } else if (number < 5){
            return new Defender();
        } else if (number < 9){
            return new Midfielder();
        } else {
            return new Striker();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Generates a new coach.
     */
    @Override
    public ControllerCoach getNewControllerCoach() {
        return new Coach();
    }
}
