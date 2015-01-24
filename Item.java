
import com.github.robocup_atan.atan.model.enums.Flag;
import java.awt.Point;

/**
 *This internal class is designed to contain records
 * of the visible things on the pitch
 */
class Item {
    Flag flag = null;  //store the Flag enum, if this is a flag
    public final String type; //type (of flag). Together with flag this allows us to derive the coordinates of this item
    public final double distance; // \
    public final double direction;//  \_ these are taken directly from the canSee* interface.
    public final double distChange;// /
    public final double dirChange;// /

    Item(Flag flag, String type, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
        this.type = type;
        this.flag = flag;
        this.distance = distance;
        this.direction = direction;
        this.distChange = distChange;
        this.dirChange = dirChange;
    }

    public Point position() {
        int x = 0; //initialise to 0, a few of the values will remain 0 so we can skip reassigning
        int y = 0;
        if (flag != null) {
            if ("penaltyOther".equals(type) || "penaltyOwn".equals(type)) {
                x = ("penaltyOther".equals(type)) ? 36 : -36;
                //the penalty box is roughly 36 from the centre line,
                //and roughtly 40 wide
                switch (flag) {
                    case RIGHT:
                        y = 20;
                        break;
                    case LEFT:
                        y = -20;
                        break;
                    default:
                        throw new AssertionError(flag.name());
                        //other flags shouldn't be here
                }
            } else if ("cornerOther".equals(type) || "cornerOwn".equals(type)) {
                x = ("cornerOther".equals(type)) ? 52 : -52;
                //the pitch is roughly 105 long and 68 wide
                switch (flag) {
                    case RIGHT:
                        y = 34;
                        break;
                    case LEFT:
                        y = -34;
                        break;
                    default:
                        throw new AssertionError(flag.name());
                        //other flags shouldn't be here
                }
            } else if ("goalOther".equals(type)) {
                x = 52;
            } else if ("goalOwn".equals(type)) {
                x = -52;
            }
            //simply deal with the values we can easily pull from the Flag class
            //these should be pretty self-explanatory
            switch (flag) {
                case RIGHT_10:
                    y = 10;
                    break;
                case RIGHT_20:
                    y = 20;
                    break;
                case RIGHT_30:
                    y = 30;
                    break;
                case LEFT_10:
                    y = -10;
                    break;
                case LEFT_20:
                    y = -20;
                    break;
                case LEFT_30:
                    y = -30;
                    break;
                case OTHER_10:
                    x = 10;
                    break;
                case OTHER_20:
                    x = 20;
                    break;
                case OTHER_30:
                    x = 30;
                    break;
                case OTHER_40:
                    x = 40;
                    break;
                case OTHER_50:
                    x = 50;
                    break;
                case OWN_10:
                    x = -10;
                    break;
                case OWN_20:
                    x = -20;
                    break;
                case OWN_30:
                    x = -30;
                    break;
                case OWN_40:
                    x = -40;
                    break;
                case OWN_50:
                    x = -50;
                    break;
                case RIGHT:
                    break;
                case LEFT:
                    break;
                case CENTER:
                    break;
                default:
                    throw new AssertionError(flag.name());
            }
        } else {
            //we can only do this for non-flag objects
            //if Player::position() doesn't return null
            throw new UnsupportedOperationException();
        }
        //create a point from x and y then return it.
        return new Point(x, y);
    }
    
}
