package uk.ac.bris.cs.scotlandyard.ui.ai.PBFAI;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;

/**
 * Created by ac16438 on 21/04/17.
 */
public class AIAvocadoPanda {


    //Selects a move from the given set of moves for the given game using Avocado Panda Algorithm
    public static Move select_move (ScotlandYardView view, Set<Move> moves, Colour player_colour) {
        // 1. Initialise necessary components
        List<APNode> apnodes = new ArrayList<>();

        for (Move move : moves) {
            APNode node = new APNode(view,player_colour,move);
//            System.out.println("Move:" + node.move);
//            System.out.println("Apn :" + node.ap);
//            System.out.println();
            apnodes.add(node);

        }

        APNode selected = selected_node(apnodes);
        System.out.println("Selected Move :");
        System.out.println("Move:" + selected.move);
        System.out.println("Apn :" + selected.ap);

        return selected.move;
    }

    //Selects a move from given moves according to their APN and random
    private static APNode   selected_node(List<APNode> nodes) {
        // 1. Initialise double named sum
        double sum = 0;

        // 2. Recursively add all the ap_numbers together in sum
        for (APNode apnode : nodes) sum += apnode.ap;

        // 3. Initialise random method rand
        Random rand = new Random();

        // 4. Create random number n, uses the method of rand and input of sum which has just been calculated
        int              n = rand.nextInt((int) sum + 1);
        double double_part = rand.nextDouble();

        // 5. Cast the random number to double for further use
        double d = ((double) n) + double_part;

        // 6. Iterate
        for (APNode node : nodes) {
            if (d <= node.ap) return node;
            else d -= node.ap;
        }

        return nodes.get(0);
    }

    //Returns the algorithmic number of the destination node in the given view, higher meaning more desirable
    //Ranging between 0 and estimated 625
    static double           avocado_panda_number    (ScotlandYardView view, Colour player_colour, Move played_move) {

        int destination = 0;

        if (played_move instanceof TicketMove) {
            TicketMove tm = (TicketMove) played_move;
            destination = tm.destination();
        }
        else if (played_move instanceof DoubleMove) {
            DoubleMove dm = (DoubleMove) played_move;
            destination = dm.finalDestination();
        }

        // 1. Get the factors for the algorithm
        int drn = detective_reach_number(view,destination);
        int cdn = closest_distance_number(view,destination);
        int tdn = ticket_diversity_number(view,player_colour,played_move);
        int mvn = move_number(view,player_colour,played_move);

        // 2. Handle situations that are exceptionally undesirable

//        // 2.1. Do not move to a location where a detective can reach in a reveal round
//        if (AITools.is_reveal(view) && cdn == 1)   return 0;
//
//        // 2.2. Do not move to the location where only a single/none ticket can be used
//        if (tdn < 2)                            return 1;
//
//        // 2.3. Do not move to the location where the amount of moves available is less than 2
//        if (mvn < 2 )                           return 1;
//
//        // 2.4. If more than 3 detectives can move there, it is highly likely that one of them will
//        if (drn >= 3)                           return 1;


//        System.out.println("Detective Reach  Number : " + drn);
//        System.out.println("Closest Distance Number : " + cdn);
//        System.out.println("Ticket Diversity Number : " + tdn);
//        System.out.println("Move             Number : " + mvn);

        // 3. Apply the actual algorithm
        return avocado_panda(cdn,drn,mvn,tdn);
    }

    //Applies The Avocado Panda Algorithm
    private static double   avocado_panda           (int cdn, int drn, int mvn, int tdn) {
        double c = (double) cdn;
        double d = (double) drn;
        double m = (double) mvn;
        double t = (double) tdn;

        if  (m == 0 || c == 0) return 0;


        return (Math.pow(16,c) * Math.pow(2,(5-d)) * (Math.log10(m) + 1.4) * Math.pow(t,2)) / ( 2.5 * Math.pow(10,6));
        //return ((Math.pow(16,c) / Math.pow(10,5)) * Math.pow(2,(5-d)) * (Math.log10(m) + 1.4) * (Math.pow((t/5),2)));
    }

    //Returns the number of detectives that can reach the given destination in one move
    //Ranging between 0 and 5
    private static int      detective_reach_number  (ScotlandYardView view, int destination) {

        // 1. Initialize necessary components
        int detective_reach_number = 0;

        // 2. Iterate through all detectives
        for (Colour player_colour : view.getPlayers()) {
            // 3. Do not accept MrX
            if (player_colour == Colour.Black) continue;

            // 4. Iterate through all their moves and see if they can reach the given destination
            for (Move move : AITools.valid_moves(view,player_colour,view.getPlayerLocation(player_colour))) {
                TicketMove tm = (TicketMove) move;
                if (destination == tm.destination()) {
                    detective_reach_number++;
                    break;
                }
            }
        }

        // 5. Returns the amount of detectives that can reach there
        return detective_reach_number;
    }

    //Returns the number of turns the closest detective will take to reach the given destination
    //Ranging between 0 and 4 (4 used for all numbers bigger than 3)
    private static int      closest_distance_number (ScotlandYardView view, int destination) {

        // 1. Get detective locations
        Set<Integer> detective_locations = AITools.detective_locations(view);
        Graph<Integer, Transport> graph = view.getGraph();

        int closest_distance_number = 4;

        // 2. If there is a detective at the location return 0 already
        if (detective_locations.contains(destination)) closest_distance_number = AITools.set_closest_distance(closest_distance_number,0);

        // 3. If not then loop through all edges of the first dimension
        Collection<Edge<Integer, Transport>> edges1 = graph.getEdgesFrom(graph.getNode(destination));
        for (Edge<Integer,Transport> edge1 : edges1) {

            // 4. If there is a detective at the location return 1
            int edge1_location = edge1.destination().value();
            if (detective_locations.contains(edge1_location)) closest_distance_number = AITools.set_closest_distance(closest_distance_number,1);

            // 5. If not then loop through all edges of the second dimension
            Collection<Edge<Integer, Transport>> edges2 = graph.getEdgesFrom(graph.getNode(edge1_location));
            for (Edge<Integer,Transport> edge2 : edges2) {

                // 6. If there is a detective at the location return 2
                int edge2_location = edge2.destination().value();
                if (detective_locations.contains(edge2_location)) closest_distance_number = AITools.set_closest_distance(closest_distance_number,2);

                // 7. If not then loop through all edges of the third dimension
                Collection<Edge<Integer, Transport>> edges3 = graph.getEdgesFrom(graph.getNode(edge2_location));
                for (Edge<Integer,Transport> edge3 : edges3) {

                    // 8. If there is a detective at the location return 3
                    int edge3_location = edge3.destination().value();
                    if (detective_locations.contains(edge3_location)) closest_distance_number = AITools.set_closest_distance(closest_distance_number,3);
                }
            }
        }

        // 9. If there are no detectives in the three node range, return 4 as >3
        return closest_distance_number;
    }

    //Returns the number of the possible ticket types at the given destination,
    //Ranging between 0 and 20. 4 being taxi, bus, underground and secret, 16 being their double move combinations
    private static int      ticket_diversity_number (ScotlandYardView view, Colour player_colour, Move played_move) {

        // 1. Get valid moves and initialize the custom diversity set
        Set<Move> moves = AITools.valid_moves_after_played(view,player_colour,played_move);
        Set<String> ticket_diversity = new HashSet<>();

        // 2. Add each ticket diversity if the list does not contain
        for (Move move : moves) {
            String diversity_string = AITools.ticket_diversity_string(move);
            if (!ticket_diversity.contains(diversity_string)) ticket_diversity.add(diversity_string);
        }

        // 3. Return the amount of diversity
        return ticket_diversity.size();
    }

    //Returns the number of moves at the given destination
    //Ranging between 0 and >200
    private static int      move_number             (ScotlandYardView view, Colour player_colour, Move played_move) {
        //Applies valid moves after played and return its size
        return AITools.valid_moves_after_played(view,player_colour,played_move).size();
    }

}
