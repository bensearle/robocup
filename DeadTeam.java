
//~--- non-JDK imports --------------------------------------------------------

import com.github.robocup_atan.atan.model.AbstractTeam;
import com.github.robocup_atan.atan.model.ControllerCoach;
import com.github.robocup_atan.atan.model.ControllerPlayer;

/**
 * A class to setup a Player Silly AbstractTeam.
 *
 * @author Atan
 */
public class DeadTeam extends AbstractTeam {

    /**
     * Constructs a new simple silly team.
     *
     * @param name The team name.
     * @param port The port to connect to SServer.
     * @param hostname The SServer hostname.
     * @param hasCoach True if connecting a coach.
     */
    public DeadTeam(String name, int port, String hostname, boolean hasCoach) {
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
        return new DeadPlayer();
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
