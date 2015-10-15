package ttr.main;

import ttr.model.destinationCards.Routes;
import edu.virginia.engine.display.Game;

public class TicketToRide extends Game{

	public TicketToRide() {
		super("TICKET_TO_RIDE", 1500, 800);
		
		/* Initialize the routes */
		Routes.initialise();
	}
}
