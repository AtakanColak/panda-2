package uk.ac.bris.cs.scotlandyard.PBFScotlandYardPackage;

import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Spectator;

import java.util.*;

/**
 * Created by ac16438 on 22/03/17.
 * A class used to inform spectators
 */
public class SpectatorInformer {

    private List<Spectator> spectators;

    public SpectatorInformer() {
        this.spectators = new ArrayList<>();
    }

    public Collection<Spectator> getSpectators() {
        return Collections.unmodifiableCollection(spectators);
    }

    //Registers the spectator after performing null/contain checks
    public void register(Spectator s) {
        if (Objects.isNull(s))              throw new NullPointerException("Spectator can not be null");

        else if(spectators.contains(s))     throw new IllegalArgumentException("Spectator is already registered");

        else spectators.add(s);
    }

    //Unregisters the spectator after performing null/contain checks
    public void unregister(Spectator s) {
        if (Objects.isNull(s))              throw new NullPointerException("Spectator can not be null");

        else if(!spectators.contains(s))    throw new IllegalArgumentException("Spectator is not registered");

        else spectators.remove(s);
    }

    //Informs the spectators that game is over
    public void gameOver(ScotlandYardView view, Set<Colour> winners) {
        for (Spectator s : spectators) {
            s.onGameOver(view,winners);
        }
    }

    //Informs the spectators that a move is made
    public void moveMade(ScotlandYardView view, Move move) {
        for (Spectator s : spectators) {
            s.onMoveMade(view,move);
        }
    }

    //Informs the spectators that a rotation is complete
    public void rotationComplete(ScotlandYardView view) {
        for (Spectator s : spectators) {
            s.onRotationComplete(view);
        }
    }

    //Informs the spectators that a round has started
    public void roundStart(ScotlandYardView view, int round) {
        for (Spectator s : spectators) {
            s.onRoundStarted(view, round);
        }
    }
}
