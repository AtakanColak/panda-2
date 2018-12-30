package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.function.Consumer;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.PBFAI.AIAvocadoPanda;
import uk.ac.bris.cs.scotlandyard.ui.ai.PBFAI.AITest;

@ManagedAI("Avocado Panda AI")
public class AvocadoPandaAI implements PlayerFactory {

	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
		//System.out.println("Why have you created me?");
		return new panda_bro_mrx();
	}

	//Creates a new player with Avocado Panda AI
	private static class panda_bro_mrx implements Player {
		@Override
		public void makeMove(ScotlandYardView view, int i, Set<Move> moves, Consumer<Move> consumer) {
			//---------Optional testing
            //AITest.test_mrx_moves(view,moves,i);
            //AITest.test_possible_mrx_moves(view,moves);
            //AITest.test_all(view,moves,i);

			//Select move using the avocado panda algorithm
			consumer.accept(AIAvocadoPanda.select_move(view,moves,Colour.Black));
		}
	}
}
