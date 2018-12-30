package uk.ac.bris.cs.scotlandyard.PBFScotlandYardPackage;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;

/**
 * Created by ac16438 on 22/03/17.
 * A static tool class to check game setup and throw exceptions when necessary
 */
public class ScotlandYardModelSetupCheck {

    private ScotlandYardModelSetupCheck(){
        //Privatized so that it can not be instantiated
    }

    //Checks if all PlayerConfigurations are not null and converts them to a List<ScotlandYardPlayer>
    public static List<ScotlandYardPlayer> configurationsToPlayers(PlayerConfiguration mrX, PlayerConfiguration firstDetective, PlayerConfiguration... restOfTheDetectives) {

        List<ScotlandYardPlayer> players = new ArrayList<>();

        // 1. Convert restOfTheDetectives
        for (PlayerConfiguration configuration : restOfTheDetectives) {
            PlayerConfiguration detective = requireNonNull(configuration);
            players.add(new ScotlandYardPlayer(detective.player,detective.colour,detective.location,detective.tickets));
        }

        // 2. Convert first detective
        PlayerConfiguration first = requireNonNull(firstDetective);
        players.add(0, new ScotlandYardPlayer(first.player,first.colour,first.location,first.tickets));

        // 3. Convert Mr X
        PlayerConfiguration x = requireNonNull(mrX);
        players.add(0, new ScotlandYardPlayer(x.player,x.colour,x.location, x.tickets));


        // 4. Return the players
        return players;
    }

    //After players are known to be non null, conducts the rest of checks
    public static void ConductCheck(List<Boolean> rounds, Graph<Integer, Transport> graph, List<ScotlandYardPlayer> players){

        // 1. Check if rounds is null
        if(Objects.isNull(rounds))                      throw new NullPointerException("Rounds can not be null");

        // 2. Check if rounds is empty
        if (rounds.isEmpty())                           throw new IllegalArgumentException("Rounds can not be empty");

        // 3. Check if graph is null
        if(Objects.isNull(graph))                       throw new NullPointerException("Graph can not be null");

        // 4. Check if graphs is empty
        if (graph.isEmpty())                            throw new IllegalArgumentException("Empty graph");

        // 5. Check if MrX has the colour black
        if (players.get(0).colour() != Black)           throw new IllegalArgumentException("MrX should be Black");

        // 6. Check if number of detectives is right
        if (players.size() < 2 || players.size() > 6)   throw new IllegalArgumentException("Number of detectives must be between 1 and 5");



        Set<Integer> locations = new HashSet<>();
        Set<Colour> colours = new HashSet<>();
        // 7. Iterate through all players
        for (ScotlandYardPlayer player : players) {

            // 8. Check location overlapping
            if (locations.contains(player.location()))  throw new IllegalArgumentException("Duplicate location, each player's location must be unique");

            // 9. Check colour overlapping
            else if (colours.contains(player.colour())) throw new IllegalArgumentException("Duplicate colour, each player's colour must be unique");

            // 10. Add to list if no overlapping is present
            else {
                locations.add(player.location());
                colours.add(player.colour());
            }

            //11. Check if the player has all ticket types initialized
            Set<Ticket> ticketTypes = new HashSet<>();
            for (Ticket ticket : player.tickets().keySet()) {
                if (!ticketTypes.contains(ticket)) {
                    ticketTypes.add(ticket);
                }
            }

            if (ticketTypes.size() != 5)                throw new IllegalArgumentException("All 5 ticket types must be initialized in each player");

            //12. Check if only MrX has secret and double tickets
            if (player.colour() != Black && (player.hasTickets(Ticket.Secret,1) || player.hasTickets(Ticket.Double,1))) {
                                                        throw new IllegalArgumentException("Detectives can not have secret or double tickets.");
            }
        }
    }
}
