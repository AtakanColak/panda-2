package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Objects.isNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;

import java.util.*;
import java.util.function.Consumer;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.scotlandyard.PBFScotlandYardPackage.ScotlandYardModelSetupCheck;
import uk.ac.bris.cs.scotlandyard.PBFScotlandYardPackage.SpectatorInformer;

public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move> {

    private List<Boolean> rounds;
    private Graph<Integer, Transport> graph;
    private SpectatorInformer informer = new SpectatorInformer();
    private int current_player_index = 0;
    private int current_round_number = 0;
    private int mrXs_last_location = 0;

    public List<ScotlandYardPlayer> players;

    private List<ScotlandYardPlayer> detectives() {
        return players.subList(1, players.size());
    }

    private ScotlandYardPlayer mrX() {
        return players.get(0);
    }

    public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
                             PlayerConfiguration mrX, PlayerConfiguration firstDetective,
                             PlayerConfiguration... restOfTheDetectives) {

        // 1. Create a List<ScotlandYardPlayers> from given PlayerConfigurations
        players = ScotlandYardModelSetupCheck.configurationsToPlayers(mrX, firstDetective, restOfTheDetectives);

        // 2. Check the arguments for potential errors
        ScotlandYardModelSetupCheck.ConductCheck(rounds, graph, players);

        // 3. Set rounds and graph
        this.rounds = rounds;
        this.graph = graph;
    }

    //Returns the player of the given colour
    private ScotlandYardPlayer getPlayer(Colour colour) {
        for (ScotlandYardPlayer player : players) {
            if (player.colour() == colour) {
                return player;
            }
        }

        throw new IllegalArgumentException("There is no player of given colour");
    }

    //Returns detectives as a set of colours
    public Set<Colour> getDetectivesAsSet() {
        Set<Colour> detectives = new HashSet<>();
        for (ScotlandYardPlayer detective : detectives()) {
            detectives.add(detective.colour());
        }
        return detectives;
    }

    //Returns Mr X as a set of colours
    public Set<Colour> getMrXasSet() {
        Set<Colour> mrX = new HashSet<>();
        mrX.add(Black);
        return mrX;
    }

    //Returns the set of valid moves of the given player colour
    private Set<Move> validMoves(Colour player_colour) {

        // 1. Instantiate the player and valid moves
        ScotlandYardPlayer player = getPlayer(player_colour);
        Set<Move> validMoves = new HashSet<>();
        // 2. Get the ticket moves and iterate through them
        Set<Move> firstTicketMoves = validTicketMoves(player,player.location());
        for (Move move1 : firstTicketMoves) {
            // 3. Add the ticket move
            validMoves.add(move1);
            // 4. Check if the player has double tickets and if the round count allows double moves
            if (player.hasTickets(Ticket.Double,1) && (rounds.size() - 1 > current_round_number)) {
                // 5. Get the ticket moves for each possible destination of the first ticket moves list
                TicketMove first = (TicketMove) move1;
                Set<Move> secondTicketMoves = validTicketMoves(player,first.destination());
                // 6. Iterate through the second ticket moves list
                for (Move move2 : secondTicketMoves) {
                    // 7. Create a double move by combining the first and second move
                    TicketMove second = (TicketMove) move2;
                    DoubleMove dm = new DoubleMove(player_colour,first.ticket(),first.destination(),second.ticket(), second.destination());
                    // 8. If the first and second move are of the same type, conduct ticket checking for two instead of one
                    boolean same_type = (dm.firstMove().ticket().compareTo(dm.secondMove().ticket()) == 0);
                    if (same_type && !player.hasTickets(dm.firstMove().ticket(),2)) {
                        continue;
                    }
                    // 9. Add the double move
                    validMoves.add(dm);
                }
            }
        }

        // 10. If the player has no moves after this point and is not MrX, add a pass move
        if (player_colour != Black && validMoves.isEmpty()) {
            Move pm = new PassMove(player_colour);
            validMoves.add(pm);
        }

        // 11. Return the valid moves as unmodifiable
        return Collections.unmodifiableSet(validMoves);
    }

    //Checks if the given round is a reveal round or not
    private boolean isRevealRound(int round) {
        return rounds.get(round - 1);
    }

    //Checks if the given move is invalid for the current player
    private boolean invalidMove(Move move) {
        return !validMoves(getCurrentPlayer()).contains(move);
    }

    @Override
    public void registerSpectator(Spectator spectator) {
        informer.register(spectator);
    }

    @Override
    public void unregisterSpectator(Spectator spectator) {
        informer.unregister(spectator);
    }

    @Override
    public void startRotate() {
        // 1. Check if the configuration is correct to start rotation
        if (isGameOver()) throw new IllegalStateException("Rotation can not start: configuration is incorrect.");

        // 2. Get the arguments for make move method
        Colour player_colour = getCurrentPlayer();
        Player pl = getPlayer(player_colour).player();
        int location = getPlayer(player_colour).location();

        // 3. Call make move method
        pl.makeMove(this, location, validMoves(player_colour), this);
    }

    @Override
    public Collection<Spectator> getSpectators() {
        return informer.getSpectators();
    }

    @Override
    public List<Colour> getPlayers() {
        List<Colour> colours = new ArrayList<>();

        for (ScotlandYardPlayer player : players) {
            colours.add(player.colour());
        }

        return Collections.unmodifiableList(colours);
    }

    @Override
    public Set<Colour> getWinningPlayers() {
        // 1. Instantiate the set and boolean
        Set<Colour> winners = new HashSet<>();
        boolean detectives_win = false;

        // 2. Iterate through all detectives to see if any of them win
        for (ScotlandYardPlayer detective : detectives()) {
            if (detective.location() == mrX().location()) detectives_win = true;
        }

        // 3. Check if the game is over
        if (isGameOver()) {
            // 4. If detectives win return them as winners
            if (detectives_win) winners = getDetectivesAsSet();
            // 5. If MrX wins return only him as winners
            else winners = getMrXasSet();
        }

        // 6. Return the winners as unmodifiable
        return Collections.unmodifiableSet(winners);
    }

    @Override
    public int getPlayerLocation(Colour colour) {
        if (colour != Black) return getPlayer(colour).location();

        else return mrXs_last_location;
    }

    @Override
    public int getPlayerTickets(Colour colour, Ticket ticket) {
        ScotlandYardPlayer player = getPlayer(colour);
        if (player.hasTickets(ticket)) return player.tickets().get(ticket);

        else return 0;
    }

    @Override
    public boolean isGameOver() {
        //Conditions that lead to game over:

        //1. If MrX is caught
        int location = mrX().location();
        for (ScotlandYardPlayer detective : detectives()) {
            if (location == detective.location()) return true;
        }
        //2. Final round has ended
        if (current_round_number == rounds.size() && current_player_index == players.size()) {
            return true;
        }
        //3. No detective can move
        boolean detectives_stuck = true;
        for (ScotlandYardPlayer detective : detectives()) {
            for (Move move : validMoves(detective.colour())) {
                if (move instanceof TicketMove) detectives_stuck = false;
            }
        }
        if (detectives_stuck) return true;
        //4. MrX is can not move
        if (current_player_index == 0) {
            if (validMoves(Black).isEmpty()) return true;
        }
        //5. Time limit is reached (not coded in this class)
        return false;
    }

    @Override
    public Colour getCurrentPlayer() {
        return players.get(current_player_index).colour();
    }

    @Override
    public int getCurrentRound() {
        return current_round_number;
    }

    @Override
    public boolean isRevealRound() {
        if (current_round_number == 0) return false;

        else return rounds.get(current_round_number - 1);
    }

    @Override
    public List<Boolean> getRounds() {
        return Collections.unmodifiableList(rounds);
    }

    @Override
    public Graph<Integer, Transport> getGraph() {
        return new ImmutableGraph(graph);
    }

    @Override
    public void accept(Move move) {
        // 1. Perform null/invalid move checks
        if (isNull(move)) throw new NullPointerException("Null move");
        if (invalidMove(move)) throw new IllegalArgumentException("Move not in set");

        // 2. Get the player and view for future method calls
        ScotlandYardPlayer player = getPlayer(move.colour());
        ScotlandYardView view = this;

        // 3. Use visitor class to declare methods that handle making the moves
        move.visit(new MoveVisitor() {
            @Override
            public void visit(PassMove move) {
                // 3.1    If pass move:
                // 3.1.1  Just inform it
                informer.moveMade(view, move);
            }

            @Override
            public void visit(TicketMove move) {
                // 3.2.   If ticket move :
                // 3.2.1. Update the location to the destination
                player.location(move.destination());
                if (player.colour() == Black) {

                    //3.2.2. If the player is MrX, perform reveal checks
                    boolean reveal = isRevealRound(current_round_number + 1);
                    TicketMove blank = MrXInformingBlankMove(move,reveal);

                    //3.2.3. Inform move made
                    MrXInformTicketMove(view,player,reveal,move,blank);
                }

                else {
                    //3.2.4. If the player is a detective, remove the used ticket
                    player.removeTicket(move.ticket());
                    //3.2.5. And give the used ticket to MrX
                    players.get(0).addTicket(move.ticket());
                    //3.2.6. Inform move made
                    informer.moveMade(view, move);
                }
            }

            @Override
            public void visit(DoubleMove move) {
                // 3.3.   If double move :
                // 3.3.1. Update the location to the destination
                player.location(move.finalDestination());
                // 3.3.2. Remove the used ticket
                player.removeTicket(Ticket.Double);
                // 3.3.3. Get reveal checks for next two rounds
                boolean reveal_first = isRevealRound(current_round_number + 1);
                boolean reveal_second = isRevealRound(current_round_number + 2);

                // 3.3.4. Instantiate blank tickets for non-reveal rounds, and set last location if reveal round
                TicketMove blank_first = MrXInformingBlankMove(move.firstMove(),reveal_first);
                TicketMove blank_second = MrXInformingBlankMove(move.secondMove(),reveal_second);

                // 3.3.5. Inform Double Move
                TicketMove first  =   (TicketMove) MrXInformingMove(reveal_first,move.firstMove(),blank_first);
                TicketMove second =   (TicketMove) MrXInformingMove(reveal_second,move.secondMove(),blank_second);
                informer.moveMade(view, new DoubleMove(Black,first,second));

                // 3.3.6. Inform First Move
                MrXInformTicketMove(view, player, reveal_first, move.firstMove(), blank_first);
                // 3.3.7. Inform Second Move
                MrXInformTicketMove(view, player, reveal_second, move.secondMove(), blank_second);
            }
        });

        //4. Increment current player index
        current_player_index++;

        //5. If game is over, end the game by informing
        if (isGameOver()) {
            informer.gameOver(this, getWinningPlayers());
        }

        //6. If rotation is at the end, finish the rotation cycle
        else if (current_player_index == (players.size())) {
            current_player_index = 0;
            informer.rotationComplete(this);
        }
        //7. Else continue the rotation
        else {
            ScotlandYardPlayer next_player = players.get(current_player_index);
            next_player.player().makeMove(this, next_player.location(), validMoves(getCurrentPlayer()), this);
        }
    }

    // Generates a blank move for MrX, and also will set the current location if reveal round
    private TicketMove MrXInformingBlankMove(TicketMove move, boolean reveal) {
        if (reveal) mrXs_last_location = move.destination();
        return new TicketMove(Black,move.ticket(),mrXs_last_location);
    }

    // Generates the informing move, by choosing between the actual move and blank move according to reveal
    private Move MrXInformingMove(boolean reveal,TicketMove move, TicketMove blank) {
        if (reveal) return move;
        else        return blank;
    }

    //Used to inform moves made by MrX
    private void MrXInformTicketMove(ScotlandYardView view, ScotlandYardPlayer player, boolean reveal, TicketMove move, TicketMove blank) {
        // 1. Increment the round count
        current_round_number++;
        // 2. Inform the round start
        informer.roundStart(view, current_round_number);
        // 3. Remove the used ticket
        player.removeTicket(move.ticket());
        // 4. Inform move made
        informer.moveMade(view, MrXInformingMove(reveal,move,blank));
    }

    //Returns all valid ticket moves for the given player at the given location
    private Set<Move> validTicketMoves(ScotlandYardPlayer player, int location) {
        // 1. Instantiate the list
        Set<Move> validTicketMoves = new HashSet<>();
        // 2. Get the edges
        Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(location));
        // 3. Iterate through the edges
        for (Edge<Integer, Transport> edge : edges) {
            // 4. Get the ticket type
            Ticket type = Ticket.fromTransport(edge.data());
            // 5. Check if the player has enough tickets for the move
            if (player.hasTickets(type, 1) || player.hasTickets(Ticket.Secret, 1)) {
                //6. Check if the destination is empty
                int destination = edge.destination().value().intValue();
                boolean destination_is_empty = true;
                for (ScotlandYardPlayer detective : detectives()) {
                    if (detective.location() == destination) destination_is_empty = false;
                }
                if (destination_is_empty) {
                    //7. Add ticket if possible
                    if (player.hasTickets(type, 1)) {
                        Move move = new TicketMove(player.colour(), type, destination);
                        validTicketMoves.add(move);
                    }
                    //8. Add secret ticket for MrX if possible
                    if (player.hasTickets(Ticket.Secret, 1)) {
                        Move move = new TicketMove(player.colour(), Ticket.Secret, destination);
                        validTicketMoves.add(move);
                    }
                }
            }
        }
        return validTicketMoves;
    }
}
