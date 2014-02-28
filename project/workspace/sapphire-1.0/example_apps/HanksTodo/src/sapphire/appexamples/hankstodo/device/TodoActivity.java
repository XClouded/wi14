package sapphire.appexamples.hankstodo.device;

import sapphire.app.SapphireActivity;
import sapphire.app.SapphireObject;
import sapphire.appexamples.hankstodo.app.TodoList;
import sapphire.appexamples.hankstodo.app.TodoListManager;

public class TodoActivity implements SapphireActivity {

	public void onCreate(SapphireObject appEntryPoint) {
		try {
			TodoListManager tlm = (TodoListManager) appEntryPoint;

			TodoList tl = tlm.newTodoList("Hanks");	
			System.out.println("Received tl1: " + tl);
			System.out.println(tl.addToDo("First todo"));
			System.out.println(tl.addToDo("Second todo"));
			System.out.println(tl.addToDo("Third todo"));

			TodoList tl2 = tlm.newTodoList("AAA");
			System.out.println("Received tl2: " + tl2);
			System.out.println(tl2.addToDo("First todo"));
			System.out.println(tl2.addToDo("Second todo"));
			System.out.println(tl2.addToDo("Third todo"));

			TodoList tl3 = tlm.newTodoList("HHH");	
			System.out.println("Received tl3: " + tl3);
			System.out.println(tl3.addToDo("First todo"));
			System.out.println(tl3.addToDo("Second todo"));
			System.out.println(tl3.addToDo("Third todo"));

		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
