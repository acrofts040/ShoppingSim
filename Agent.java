import java.util.Arrays;
import com.supermarket.*;

enum State {
	GETTING_GROCERY,
	GET_CART,
	MOVE_OUT_OF_AISLE,
	MOVE_TO_CHECKOUT,
	SEEK_EXIT,
	LEAVE,
}	

enum MoveStatus {
	PENDING,
	FAILED,
	SUCCEEDED
}

public class Agent extends SupermarketComponentImpl {

    public Agent() {
	super();
	shouldRunExecutionLoop = true;
	log.info("In Agent constructor.");
    }

	String goalLocation = "";
	String goalType = "";
	int shoppingListIndex = 0;
	// getting cart, getting a grocery item, moving out of the aisle, moving to the return	
	State state = State.GET_CART;
	MoveStatus status = MoveStatus.PENDING;


	// Function that gets the user a gart
	void getCart(Observation obs) {
		for (int i = 0; i < 15; i++) {
			goSouth();
		}
		interactWithObject();
		interactWithObject();
		for (int i = 0; i < 20; i++) {
			goEast();
		}
		state = State.GETTING_GROCERY;
	}

	void getShelfItem(Observation obs, int goalIndex) {
		double xComp = obs.players[0].position[0] - (obs.shelves[goalIndex].position[0] + (.5 * obs.shelves[goalIndex].width));
		double yComp = obs.players[0].position[1] - (obs.shelves[goalIndex].position[1] + (.5 * obs.shelves[goalIndex].height));
		if (status == MoveStatus.SUCCEEDED) {
			toggleShoppingCart();
			// move some direction
			if (yComp < 0) {
				goSouth();
			} else {
				goNorth();
			}

			// special instructions for milk aisle
			if (obs.players[0].shopping_list[shoppingListIndex].contains("milk")) {
				goNorth();
				goNorth();
			}

			// pick up item
			interactWithObject();

			if (obs.players[0].shopping_list[shoppingListIndex].contains("milk")) {
				goSouth();
				goSouth();
			}

			// turn back
			if (xComp < 0) {
				goEast();
			} else {
				goWest();
			}

			// put item in shopping cart
			interactWithObject();

			// closes food item message
			interactWithObject();

			// pick up shopping cart again
			toggleShoppingCart();

			state = State.MOVE_OUT_OF_AISLE;
			status = MoveStatus.PENDING;
			shoppingListIndex += 1;
			return;
		}
		move(obs, xComp, yComp);
	}

	// In charge of moving person out of aisle after they get an item
	void moveOutOfAisle(Observation obs, int goalIndex) {
		// Goes east if they need to get to the counters
		if (goalType == "counters") {
			if (obs.inRearAisleHub(0)) {
				state = State.GETTING_GROCERY;
			} else {
				goEast();
			}
		// Goes west if they need to go to the registers
		} else if (goalType == "registers") {
			if (obs.inAisleHub(0)) {
				state = State.GETTING_GROCERY;
			} else {
				goWest();
			}
		// Goes east or west depending on which way is faster for exiting aisle
		} else if (goalType == "shelves") {
			double yComp = obs.players[0].position[1] - (obs.shelves[goalIndex].position[1] + (.5 * obs.shelves[goalIndex].height));
			if (obs.inRearAisleHub(0) || obs.inAisleHub(0)) {
				state = State.GETTING_GROCERY;
			} else if (obs.players[0].position[0] > 10) {
				goEast();
			} else {
				goWest();
			}
		}
	}
	
    //This will go to a counter and get a specific item from the counter. This function assumes you are southwest of the counter
	void getCounterItem(Observation obs, int goalIndex) {
		double xComp = obs.players[0].position[0] - obs.counters[goalIndex].position[0] + .7;
		double yComp = obs.players[0].position[1] - (obs.counters[goalIndex].position[1] + (.5 * obs.counters[goalIndex].height));
		if (status == MoveStatus.SUCCEEDED) {
			// let go of the cart
			toggleShoppingCart();
			// go north a bit
			for (int i = 0; i < 5; i++) {
				goNorth();
			}

   			// move user to the counter
			for (int i = 0; i < 5; i++) {
				goEast();
			}

			// pick up item from counter
			interactWithObject();

			// face back towards the cart
			goSouth();

			// put food item in shopping cart
			interactWithObject();

			// cancel menu
			interactWithObject();

   
			for (int i = 0; i < 5; i++) {
				goWest();
			}

			for (int i = 0; i < 5; i++) {
				goSouth();
			}

			// pick up shopping cart again
			goEast();
			toggleShoppingCart();

			for (int i = 0; i < 4; i++) {
				goWest();
			}

			state = State.GETTING_GROCERY;
			status = MoveStatus.PENDING;
			shoppingListIndex += 1;
			return;
		}
		move(obs, xComp, yComp);
	}

	// Deals with instruction on going to a register
	void goToRegister(Observation obs, int goalIndex) {
		double xComp = obs.players[0].position[0] - (obs.registers[goalIndex].position[0] + (obs.registers[goalIndex].width * .5));
		double yComp = obs.players[0].position[1] - (obs.registers[goalIndex].position[1] + (obs.registers[goalIndex].height * .75));
		if (status == MoveStatus.SUCCEEDED) {
			toggleShoppingCart();
			goNorth();
			interactWithObject();
			interactWithObject();
			goWest();
			toggleShoppingCart();
			state = State.LEAVE;
			status = MoveStatus.PENDING;
			return;
		}
		moveToRegister(xComp, yComp);
	}

	// Movement for the register
	void moveToRegister (double xComp, double yComp) {
		if (yComp > .70) {
			goNorth();
		} else if (yComp < .60) {
			goSouth();
		} else if (xComp < -.8) {
			goEast();
		} else if (xComp > .5) {
			goWest();
		} else {
			status = MoveStatus.SUCCEEDED;
			return;
		}

		status = MoveStatus.PENDING;
	}

	// Movement function that moves depending on difference in x and y positions of person and goal
	void move (Observation obs, double xComp, double yComp) {
		double playery = obs.players[0].position[1];
		if (yComp > .75 && playery > 3.05) {
			goNorth();
		} else if (yComp < -1.25) {
			goSouth();
		} else if (xComp < -.8) {
			goEast();
		} else if (xComp > .5) {
			goWest();
		} else {
			status = MoveStatus.SUCCEEDED;
			return;
		}

		status = MoveStatus.PENDING;
	}
	
    @Override
    protected void executionLoop() {
		String goal = "cart return";
	// this is called every 100ms
	// put your code in here
		Observation obs = getLastObservation();
		System.out.println(obs.players.length + " players");
		System.out.println(obs.carts.length + " carts");
		System.out.println(obs.shelves.length + " shelves");
		System.out.println(obs.counters.length + " counters");
		System.out.println(obs.registers.length + " registers");
		System.out.println(obs.cartReturns.length + " cartReturns");
		// print out the shopping list
		System.out.println("Shoppping list: " + Arrays.toString(obs.players[0].shopping_list));
		// now run around in circles
		// find location of item
		int goalIndex = -1;
		
		if (shoppingListIndex < obs.players[0].shopping_list.length) {
			goalLocation = obs.players[0].shopping_list[shoppingListIndex];
		} else {
			if (state != State.MOVE_OUT_OF_AISLE && state != State.LEAVE) {
				goalLocation = "checkout";
				state = State.MOVE_TO_CHECKOUT;
			}
		}
		
		for(int i = 0; i < obs.shelves.length; i++) {
			if (obs.shelves[i].food.equals(goalLocation)) {
				goalIndex = i;
				goalType = "shelves";
				break;
			}
		} 
		if (goalIndex == -1) {
			for(int i = 0; i < obs.counters.length; i++) {
				if (obs.counters[i].food.equals(goalLocation)) {
					goalIndex = i;
					goalType = "counters";
					break;
				}
			}
		}
	
		if (goalLocation == "checkout") {
			goalIndex = 0;
			goalType = "registers";
		}

		System.out.println("Goal Index= " + goalIndex + ", state=" + state + ", shoppingListIndex" + String.valueOf(shoppingListIndex));
		if (state == State.GET_CART) {
			getCart(obs);
		} else if (state == State.GETTING_GROCERY) {
			if (goalType == "shelves") {
				getShelfItem(obs, goalIndex);
			} else if (goalType == "counters") {
				getCounterItem(obs, goalIndex);
			}
		} else if (state == State.MOVE_OUT_OF_AISLE) {
			moveOutOfAisle(obs, goalIndex);
		} else if (state == State.MOVE_TO_CHECKOUT) {
			goToRegister(obs, goalIndex);
		} else if (state == State.LEAVE) {
			goWest();
		}
	}
}
