package uk.ac.bris.cs.scotlandyard.ui.ai.PBFAI;

import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.Set;

import static uk.ac.bris.cs.scotlandyard.ui.ai.PBFAI.AITools.valid_moves;
import static uk.ac.bris.cs.scotlandyard.ui.ai.PBFAI.AITools.valid_moves_after_played;


/**
 * Created by ac16438 on 20/04/17.
 */
public class AITest {

    //Tests valid_moves_after_played method
    public static void test_possible_mrx_moves(ScotlandYardView view, Set<Move> moves) {
        for (Move move : moves) {
            System.out.println("After " + move + " is played;");
            Set<Move> valid_moves = valid_moves_after_played(view,Colour.Black,move);
            test_print_moves(view, valid_moves, move, Colour.Black);
        }
    }

    //Tests valid moves method for detectives
    public static void test_detective_moves(ScotlandYardView view) {
        for (Colour detective_colour : view.getPlayers()) {
            if (detective_colour == Colour.Black) continue;

            int player_location = view.getPlayerLocation(detective_colour);

            System.out.println(detective_colour + " player");

            System.out.println("Location: " + player_location);

            Set<Move> valid_moves = valid_moves(view,detective_colour,player_location);
            test_print_moves(valid_moves);
        }
    }

    //Tests valid moves method for mrx and compares it to valid moves given by the game
    public static void test_mrx_moves(ScotlandYardView view, Set<Move> moves, int location) {
        System.out.println(Colour.Black + " player");

        System.out.println("Location: " + location);

        System.out.println("Given valid moves: ");
        test_print_moves(moves);

        Set<Move> valid_moves = valid_moves(view,Colour.Black,location);
        System.out.println("Result of the method: ");
        test_print_moves(valid_moves);
    }

    //Tests valid moves method for all players
    public static void test_all(ScotlandYardView view, Set<Move> mrx_moves, int mrx_location) {
        System.out.println();
        System.out.println("Testing MrX's Valid Moves: ");
        System.out.println();
        test_mrx_moves(view,mrx_moves,mrx_location);

        System.out.println();
        System.out.println("Testing Detectives' Valid Moves: ");
        System.out.println();
        test_detective_moves(view);
    }

    //Method that prints a list of moves along with their avocado panda numbers
    private static void test_print_moves(ScotlandYardView view, Set<Move> moves, Move played_move, Colour player_colour) {
        System.out.println("Avocado Panda    Number : " + AIAvocadoPanda.avocado_panda_number(view,player_colour,played_move));
        System.out.println("Size                    : " + moves.size());
        System.out.println(moves);
        System.out.println();
    }

    //Method that prints a list of moves
    private static void test_print_moves(Set<Move> moves) {
        System.out.println("Size        : " + moves.size());
        System.out.println(moves);
        System.out.println();
    }
}
