package sapphire.appexamples.hankstodo.app;

import java.io.Serializable;

public class TodoElement implements Serializable {
	private String name;
	private String description;
	private boolean isDone;
	
	public TodoElement(String name, String description) {
		this.name = name;
		this.description = description;
		this.isDone = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDone() {
		return isDone;
	}

	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}
}
