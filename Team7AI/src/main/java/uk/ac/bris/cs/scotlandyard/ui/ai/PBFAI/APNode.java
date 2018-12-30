package uk.ac.bris.cs.scotlandyard.ui.ai.PBFAI;

import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;

/**
 * Created by ac16438 on 06/04/17.
 */
public class APNode {
    public final double ap;
    public final Move move;

    public APNode (ScotlandYardView view, Colour player_colour, Move played_move) {
        this.move   = played_move;
        this.ap     = AIAvocadoPanda.avocado_panda_number(view, player_colour, played_move);
    }
}
