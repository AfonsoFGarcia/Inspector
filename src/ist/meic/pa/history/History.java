package ist.meic.pa.history;

import ist.meic.pa.Inspector;

import java.util.Stack;

/**
 * The Class History.
 */
public class History {

    /**
     * The Class State.
     */
    private static class State {

        public Object inspectTarget;
        public Class<?> inspectClass;
        public boolean fullInspector;

        /**
         * Instantiates a new state.
         * 
         * @param o the inspectTarget of the current state
         * @param c the inspectClass of the current state
         * @param b the fullInspector flag of the current state
         */
        public State(Object o, Class<?> c, boolean b) {
            inspectTarget = o;
            inspectClass = c;
            fullInspector = b;
        }
    }

    private static Stack<State> st = new Stack<State>();

    /**
     * Saves the current state.
     * 
     * @param i the inspector object of which the state will be pushed into the stack
     */
    public static void save(Inspector i) {
        st.push(new State(i.getInspectTarget(), i.getInspectClass(), i.isFullInspector()));
    }

    /**
     * Rollbacks the previous state.
     * 
     * @param i the inspector object of which the state will be rolled back.
     * @return true, if successful
     */
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

    /**
     * Creates a new stack.
     */
    public static void createNewStack() {
        st = new Stack<State>();
    }

}
