package instance;

import java.util.ArrayList;
import characters.Entity;
import display.Display;

public abstract class Instance {
	public boolean awaitingInput;
	protected ArrayList<ArrayList<Entity>> team = new ArrayList<ArrayList<Entity>>(); //array for teams that holds array for Entitys
	protected ArrayList<Entity> initiativeList = new ArrayList<Entity>();
	protected RPGAction currentAction;
	protected int currentTeam;

	public ArrayList<Entity> getInitiativeList() {
		return initiativeList;
	}
	public ArrayList<ArrayList<Entity>> getTeams() {
		return team;
	}
	public int getCurrentTeam() {
		return currentTeam;
	}
	public Entity getEntity(int i, int teamNum) { //unclear why anything would request this as of yet.
		return team.get(teamNum).get(i);
	}

	public void addEntity(Entity add, int teamNum) { // Add people to the combat.
		if (!initiativeList.contains(add)) {
			if (teamNum>(team.size()-1))
				for (int i=0; i<=(teamNum-(team.size()-1)); i++)
					team.add(new ArrayList<Entity>());
			team.get(teamNum).add(add);
			Display.debug(add.getName() + " was added to instance."); // Change this line if we add more people. Also more of a console thing rather than interface, intentionally.

			// Adds sorted to IntiativeList.
			/*
		boolean found = false;
		int index=0;
		while(index<=InitiativeList.size() && !found) { // Someone check this and remove this note or tell me. I'm not sure about this.
			if (add.getPerception()>InitiativeList.get(index).getPerception()) {
				if (!(add.getPerception()<=InitiativeList.get(index+1).getPerception())) {
					InitiativeList.add(index+1, add);
					found = true;
				}
			}
		}*/
			initiativeList.add(add); // Temp
		} 
	}

	public abstract void launch();

	public abstract boolean checkActive();

}
