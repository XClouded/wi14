package sapphire.appexamples.hankstodo.device;

import sapphire.app.AndroidSapphireActivity;
import sapphire.appexamples.hankstodo.R;
import sapphire.appexamples.hankstodo.app.TodoList;
import sapphire.appexamples.hankstodo.app.TodoListManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;

public class TodoActivityTablet extends AndroidSapphireActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new MeasureTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class MeasureTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {

			try {
				TodoListManager tlm = (TodoListManager) getAppEntryPoint();

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
				e.printStackTrace();
			}
			return null;
		}
	}

}
