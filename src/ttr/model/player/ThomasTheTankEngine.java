package ttr.model.player;

import java.util.ArrayList;
import ttr.model.destinationCards.*;
import java.util.HashMap;

import ttr.model.destinationCards.Destination;
import ttr.model.destinationCards.Route;
import ttr.model.trainCards.TrainCardColor;
import ttr.model.player.HumanPlayer;


public class ThomasTheTankEngine extends Player {

	public ThomasTheTankEngine(String name) {
		super(name);
	}

	public ThomasTheTankEngine() {
		super("ThomasTheTankEngine");
	}

//	@Override
//	public void makeMove() {
//		// TODO Auto-generated method stub
//		//use dijkstras on both destination cards
//		//tabulate cards needed
//		//pick cards until card values are fulfilled
//		//when card value met, 
//		//check for critical points
//		//manhattan distance + longest piece
//		
//	}
	
	/**
	 * MUST override the makeMove() method and implement it.
	 * */
	@Override
	public void makeMove(){
		
		/* Always draw train cards (0 means we are drawing from the pile, not from the face-up cards) */
		super.drawTrainCard(0);
		
		/* This call would allow player to draw destination tickets*/
		super.drawDestinationTickets();
		
		/* Something like this will allow an AI to attempt to buy a route on the board. The first param is the route they wish */
		/* ...to buy, the second param is the card color they wish to pay for the route with (some routes have options here) */
		super.claimRoute(new Route(Destination.Atlanta,  Destination.Miami, 6, TrainCardColor.blue), TrainCardColor.blue);
		this.itinerary();
		
		
		/* NOTE: This is just an example, a player cannot actually do all three of these things in one turn. The simulator won't allow it */

	}
	
	
	
	
	
	//testing dijkstra's
	public void itinerary() {
		for (DestinationTicket ticket : this.getDestinationTickets()){			
			System.out.println(shortestPathcost(ticket.getFrom(), ticket.getTo()).toString());
		}
		
		
	}
	
	
	
	
	
	public ArrayList<Destination> shortestPathcost(Destination from, Destination to){
		
		/* If same, just return false */
		ArrayList<Destination> shortestPath = new ArrayList<Destination>();
		if(from == to) return shortestPath;
		
		/* Open and Closed lists (breadth first search) */
		HashMap<Destination, Integer> openList = new HashMap<Destination, Integer>();
		HashMap<Destination, Integer> closedList = new HashMap<Destination, Integer>();
		HashMap<Destination, Destination> parent = new HashMap<Destination, Destination>();
		
		openList.put(from, 0);
		
		while(openList.size() > 0){
			
			/* Pop something off the open list, if destination then return true */
			Destination next = null;
			int minCost = 9999;
			for(Destination key : openList.keySet()){
				if(openList.get(key) < minCost){
					next = key;
					minCost = openList.get(key);
				}
			
			}
			
			/* Take it off the open list and put on the closed list */
			openList.remove(next);
			closedList.put(next, minCost);
			
			/* If this is the destination, then return!!!! */
			if(next == to) break;
			
			/* Get all the neighbors of the next city that aren't on open or closed lists already */
			for(Destination neighbor : Routes.getInstance().getNeighbors(next)){
				if(closedList.containsKey(neighbor)) continue;
				
				/* get route between next and neighbor and see if better than neighbor's value */
				ArrayList<Route> routesToNeighbor = Routes.getInstance().getRoutes(next, neighbor);
				for(Route routeToNeighbor : routesToNeighbor){
					int newCost = closedList.get(next) + routeToNeighbor.getCost();
					
					if(Routes.getInstance().ownsRoute(this, routeToNeighbor) | !Routes.getInstance().isRouteClaimed(routeToNeighbor)){	
						if(openList.containsKey(neighbor)){	
							if(newCost < openList.get(neighbor)){
								openList.put(neighbor, newCost);
								parent.put(neighbor, next);
							}
						}
						else{
							openList.put(neighbor, newCost);
							parent.put(neighbor, next);
						}
					}

				}
			}
		}
		/*Closed list and parent are complete, retrace path*/
		
		Destination current = to;
		while(current != from){
			shortestPath.add(current);
			current = parent.get(current);		
		}
		shortestPath.add(from); //list is reversed but we don't carrrre
			
		return shortestPath;
	}
}
