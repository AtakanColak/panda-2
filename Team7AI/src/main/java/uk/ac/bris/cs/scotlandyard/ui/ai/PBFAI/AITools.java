package uk.ac.bris.cs.scotlandyard.ui.ai.PBFAI;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;
import java.util.function.Predicate;

/**
 * Created by ac16438 on 04/04/17.
 */
public class AITools {

    //Returns the smallest of two given numbers
    static int              set_closest_distance(int get, int set) {
        if ( set < get )    return set;
        else                return get;
    }

    //Returns the ticket type string for a given move
    static String           ticket_diversity_string(Move move) {
        if (move instanceof TicketMove) {
            TicketMove tm = (TicketMove) move;
            return ticket_diversity_char(tm.ticket());
        }
        else {
            StringBuilder sb = new StringBuilder();
            DoubleMove dm = (DoubleMove) move;
            sb.append(ticket_diversity_char(Ticket.Double));
            sb.append(ticket_diversity_char(dm.firstMove().ticket()));
            sb.append(ticket_diversity_char(dm.secondMove().ticket()));
            return sb.toString();
        }
    }

    //Returns valid moves for given player at the given location after the given move is played, which removes some of the moves due to ticket count
    static Set<Move>        valid_moves_after_played(ScotlandYardView view, Colour player_colour, Move played_move) {

        Ticket played_ticket = Ticket.Double;
        int location = 0;

        if (played_move instanceof TicketMove) {
            TicketMove tm = (TicketMove) played_move;
            location = tm.destination();
            played_ticket = tm.ticket();
        }
        else if (played_move instanceof DoubleMove) {
            DoubleMove dm = (DoubleMove) played_move;
            location = dm.finalDestination();
        }

        Set<Move> valid_moves = valid_moves(view,player_colour,location);

        valid_moves = remove_moves_of_ticket(view,valid_moves,player_colour,played_ticket);

        //If the played move is a double move, do the same for its first and second tickets as well
        if (played_move instanceof DoubleMove) {
            DoubleMove dm = (DoubleMove) played_move;
            valid_moves = remove_moves_of_ticket(view,valid_moves,player_colour,dm.firstMove().ticket());
            valid_moves = remove_moves_of_ticket(view,valid_moves,player_colour,dm.secondMove().ticket());
        }

        return valid_moves;
    }

    //Returns valid moves for the given player at the given location
    static Set<Move>        valid_moves(ScotlandYardView view, Colour player_colour, int location) {

        // 1. Instantiate necessary components
        List<Boolean> rounds = view.getRounds();
        int current_round = view.getCurrentRound();
        Set<Move> valid_moves = new HashSet<>();

        // 2. Get the ticket moves and iterate through them
        Set<Move> first_ticket_moves = valid_ticket_moves(view,player_colour,location);


        for (Move move1 : first_ticket_moves) {

            // 3. Add the ticket move
            valid_moves.add(move1);

            // 4. Check if the player has double tickets and if the round count allows double moves
            boolean has_double      = AITools.has_tickets(view,player_colour, Ticket.Double,1);
            boolean enough_rounds   = (rounds.size() - 1 > current_round);

            if (has_double && enough_rounds) {

                // 5. Get the ticket moves for each possible destination of the first ticket moves list
                TicketMove first = (TicketMove) move1;
                Set<Move> second_ticket_moves = valid_ticket_moves(view,player_colour,first.destination());

                // 6. Iterate through the second ticket moves list
                for (Move move2 : second_ticket_moves) {

                    // 7. Create a double move by combining the first and second move
                    TicketMove second = (TicketMove) move2;
                    DoubleMove dm = new DoubleMove(player_colour,first.ticket(),first.destination(),second.ticket(), second.destination());

                    // 8. If the first and second move are of the same type, conduct ticket checking for two instead of one
                    boolean same_type           = (dm.firstMove().ticket().compareTo(dm.secondMove().ticket()) == 0);
                    boolean doesnt_have_two     = !AITools.has_tickets(view,player_colour,first.ticket(),2);
                    if (same_type && doesnt_have_two) continue;

                    // 9. Add the double move
                    valid_moves.add(dm);
                }
            }
        }

        // 10. Return the valid moves
        return valid_moves;
    }

    //Returns the detective locations as a Set<Integer>
    static Set<Integer>     detective_locations(ScotlandYardView view) {
        // 1. Instantiate the set
        Set<Integer> detective_locations = new HashSet<>();

        // 2. Iterate through players
        for (Colour color : view.getPlayers()) {
            if (color != Colour.Black) {
                // 3. If not MrX then add its location to the set
                detective_locations.add(view.getPlayerLocation(color));
            }
        }

        // 4. Return
        return detective_locations;
    }

    //Return a char corresponding to each ticket type, used to generate ticket diversity string
    private static String           ticket_diversity_char(Ticket t) {
        switch (t) {
            case Bus:
                return "b";
            case Taxi:
                return "t";
            case Underground:
                return "u";
            case Double:
                return "d";
            case Secret:
                return "s";
        }
        return "";
    }

    //Removes the moves that use the ticket if there is only one ticket remaining
    private static Set<Move>        remove_moves_of_ticket(ScotlandYardView view, Set<Move> moves, Colour player_colour, Ticket played_ticket) {
        boolean only_had_one_ticket = (view.getPlayerTickets(player_colour,played_ticket) == 1);
        if (only_had_one_ticket) {
            moves.removeIf(remove_predicate(played_ticket));
        }
        return moves;
    }

    //Predicate used for remove_moves_of_tickets
    private static Predicate<Move>  remove_predicate(Ticket type) {
        return m -> ((m instanceof TicketMove && (((TicketMove) m).ticket().compareTo(type) == 0))  ||
                     (m instanceof DoubleMove && (((type.compareTo(Ticket.Double) == 0))            ||
                     (((DoubleMove) m).firstMove().ticket().compareTo(type) == 0)                   ||
                     (((DoubleMove) m).secondMove().ticket().compareTo(type) == 0))));
    }

    //Returns all valid ticket moves for the given player at the given location
    private static Set<Move>        valid_ticket_moves(ScotlandYardView view, Colour player_colour, int location) {

        // 1. Instantiate necessary components
        Graph<Integer, Transport> graph = view.getGraph();
        Set<Integer> detective_locations = AITools.detective_locations(view);
        Set<Move> valid_ticket_moves = new HashSet<>();

        // 2. Get the edges
        Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(location));

        // 3. Iterate through the edges
        for (Edge<Integer, Transport> edge : edges) {

            // 4. Get the ticket type
            Ticket type = Ticket.fromTransport(edge.data());

            // 5. Check if the player has enough tickets for the move
            boolean has_type    = AITools.has_tickets(view,player_colour,type,1);
            boolean has_secret  = AITools.has_tickets(view,player_colour,Ticket.Secret,1);
            boolean is_not_boat = (type.compareTo(Ticket.Secret) != 0) ;

            //6. Check if the destination is empty
            int destination = edge.destination().value().intValue();

            if (detective_locations.contains(destination)) continue;

            //7. Add ticket if possible
            if (has_type) 		            valid_ticket_moves.add(new TicketMove(player_colour, type, destination));

            //8. Add secret ticket for MrX if possible
            if (has_secret && is_not_boat) 	valid_ticket_moves.add(new TicketMove(player_colour, Ticket.Secret, destination));
        }
        // 9. Return
        return valid_ticket_moves;
    }

    //Checks if the player of the given colour has at least the amount of specified ticket amount
    private static boolean          has_tickets(ScotlandYardView view, Colour player_colour, Ticket ticket, int amount) {
        return (view.getPlayerTickets(player_colour,ticket) >= amount);
    }

}
