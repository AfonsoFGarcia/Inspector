package ist.meic.pa.history;

import ist.meic.pa.Inspector;

import java.util.EmptyStackException;
import java.util.Stack;

public class History {

	private static Stack<State> st = new Stack<State>();

	private static class State {
		public Object inspectTarget;
		public Class<?> inspectClass;
		public boolean fullInspector;

		public State(Object o, Class<?> c, boolean b) {
			inspectTarget = o;
			inspectClass = c;
			fullInspector = b;
		}

	}

	public static void save(Inspector i) {
		st.push(new State(i.getInspectTarget(), i.getInspectClass(), i
				.isFullInspector()));
	}

	public static boolean rollback(Inspector i) {
		if (st.size() > 1) {
			State s = st.pop();

			i.setFullInspector(s.fullInspector);
			i.setInspectClass(s.inspectClass);
			i.setInspectTarget(s.inspectTarget);
			return true;
		} else {
			return false;
		}
	}

	public static void createNewStack() {
		st = new Stack<State>();
	}

}
