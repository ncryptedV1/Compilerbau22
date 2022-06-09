package compiler;

import java.io.OutputStreamWriter;
import java.util.List;

public abstract class Instr {

    public static class PrintInstr extends InstrIntf {
        private InstrIntf m_expr;

        public PrintInstr(InstrIntf expr) {
            m_expr = expr;
        }

        public void execute(ExecutionEnvIntf env) {
            int expr = m_expr.getValue(); 
            try {
                env.getOutputStream().write(Integer.toString(expr));
                env.getOutputStream().write('\n');
                env.getOutputStream().flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("PRINT\n");
        }
    }
    
    public static class CallInstr extends InstrIntf {

        private String m_identifier;
        private List<InstrIntf> m_args;

        public CallInstr(String identifier, List<InstrIntf> args) {
            m_identifier = identifier;
            m_args = args;
        }

        public void execute(ExecutionEnvIntf env) {
            // Put values onto stack
            m_args.forEach(arg -> env.pushNumber(arg.getValue()));
            
            // Put return function on stack
            env.pushFunction(env.getFunctionTable().getFunction(m_identifier));
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write(String.format("CALL %s\n", m_identifier));
        }
    }
    
    public static class ReturnInstr extends InstrIntf {
        
        private FunctionInfo m_context;
        private InstrIntf m_result;
        
        public ReturnInstr(FunctionInfo context, InstrIntf result) {
            m_context = context;
            m_result = result;
        }

        public void execute(ExecutionEnvIntf env) {
            // Retrieve return value
            int value = m_result.getValue();
            
            // Pop arguments from stack
            for (int i = 0; i < m_context.varNames.size(); i++) {
                env.popNumber();
            }
            
            // Pop values
            // TODO: Figure out how to handle this.
            // Should the values be remaining on the stack when
            // calling the function or are they meant to be popped?
            // I think, they should remain on the stack because otherwise
            // they would be lost when another function is called
            
            
            // Go back to calling function
            env.popFunction();
            
            // Assign return value to instruction:
            // The previous element must be read, because the
            // iterator points after the call instruction.
            // It previously has been read using next(), after
            // which the iterator (now pointing after it) has
            // been put onto the stack as a return address.
            InstrIntf call = env.getInstrIter().previous();
            call.m_value = value;
            
            // After assigning the value, the old iterator
            // state must be restored, so that the instruction
            // following the call is executed next.
            env.getInstrIter().next();
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("RETURN\n");
        }
    }

    public static class AddInstr extends InstrIntf {
        private InstrIntf m_lhs;
        private InstrIntf m_rhs;

        public AddInstr(InstrIntf lhs, InstrIntf rhs) {
            m_lhs = lhs;
            m_rhs = rhs;                   
        }

        public void execute(ExecutionEnvIntf env) {
            m_value = m_lhs.getValue() + m_rhs.getValue();
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("ADD\n");
        }
    }

    public static class BitAndInstr extends InstrIntf {
        private InstrIntf m_lhs;
        private InstrIntf m_rhs;

        public BitAndInstr(InstrIntf lhs, InstrIntf rhs) {
            m_lhs = lhs;
            m_rhs = rhs;                   
        }

        public void execute(ExecutionEnvIntf env) {
            m_value = m_lhs.getValue() & m_rhs.getValue();
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("BITAND\n");
        }
    }

    public static class BitOrInstr extends InstrIntf {
        private InstrIntf m_lhs;
        private InstrIntf m_rhs;

        public BitOrInstr(InstrIntf lhs, InstrIntf rhs) {
            m_lhs = lhs;
            m_rhs = rhs;                   
        }

        public void execute(ExecutionEnvIntf env) {
            m_value = m_lhs.getValue() | m_rhs.getValue();
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("BITOR\n");
        }
    }

    public static class SubInstr extends InstrIntf {
        private InstrIntf m_lhs;
        private InstrIntf m_rhs;

        public SubInstr(InstrIntf lhs, InstrIntf rhs) {
            m_lhs = lhs;
            m_rhs = rhs;                   
        }

        public void execute(ExecutionEnvIntf env) {
            m_value = m_lhs.getValue() - m_rhs.getValue();
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("SUB\n");
        }
    }

    public static class IntegerLiteralInstr extends InstrIntf {
        public IntegerLiteralInstr(int value) {
            m_value = value;
        }

        public void execute(ExecutionEnvIntf env) {
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write(String.format("INTEGER %s\n", m_value));
        }
    }

    public static class AndInstr extends InstrIntf {
        private InstrIntf m_lhs;
        private InstrIntf m_rhs;

        public AndInstr(InstrIntf lhs, InstrIntf rhs) {
            m_lhs = lhs;
            m_rhs = rhs;
        }

        public void execute(ExecutionEnvIntf env) {
            if(m_lhs.getValue() != 0 && m_rhs.getValue() != 0) {
                m_value = 1;
            } else {
                m_value = 0;
            }
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("AND\n");
        }
    }

    public static class OrInstr extends InstrIntf {
        private InstrIntf m_lhs;
        private InstrIntf m_rhs;

        public OrInstr(InstrIntf lhs, InstrIntf rhs) {
            m_lhs = lhs;
            m_rhs = rhs;
        }

        public void execute(ExecutionEnvIntf env) {
            if(m_lhs.getValue() != 0 || m_rhs.getValue() != 0) {
                m_value = 1;
            } else {
                m_value = 0;
            }
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("OR\n");
        }
    }



    public static class JumpInstr extends InstrIntf {
        InstrBlock m_target;

        public JumpInstr(InstrBlock target) {
            m_target = target;
        }

        public void execute(ExecutionEnvIntf env) {
            env.setInstrIter(m_target.getIterator());
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("JUMP ");
            os.write(m_target.getName());
            os.write("\n");
        }
    }

    public static class JumpCondInstr extends InstrIntf {
        InstrIntf m_cond;
        InstrBlock m_targetTrue;
        InstrBlock m_targetFalse;

        public JumpCondInstr(InstrIntf cond, InstrBlock targetTrue, InstrBlock targetFalse) {
            m_cond = cond;
            m_targetTrue = targetTrue;
            m_targetFalse = targetFalse;
        }

        public void execute(ExecutionEnvIntf env) {
            int condition = m_cond.getValue();
            if (condition != 0) {
                env.setInstrIter(m_targetTrue.getIterator());
            } else {
                env.setInstrIter(m_targetFalse.getIterator());
            }
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("JUMP COND ");
            os.write(m_targetTrue.getName());
            os.write(", ");
            os.write(m_targetFalse.getName());
            os.write("\n");
        }
    }
    public static class QuestionMarkInstr extends InstrIntf {
    	InstrIntf m_cond;
    	InstrIntf m_targetTrue;
    	InstrIntf m_targetFalse;
    	
    	public QuestionMarkInstr(InstrIntf cond, InstrIntf targetTrue, InstrIntf targetFalse) {
    		m_cond = cond;
    		m_targetTrue = targetTrue;
    		m_targetFalse = targetFalse;
    	}
    	
    	public void execute(ExecutionEnvIntf env) {
    		if(m_cond.getValue() ==1) {
    			m_value = m_targetTrue.getValue();
    		}else {	
    			m_value = m_targetFalse.getValue();
    		}
    	}
    	
    	public void trace(OutputStreamWriter os) throws Exception {
    		os.write("QUESTIONMARK ");
    		os.write(String.valueOf(m_targetTrue.getValue()));
    		os.write(", ");
    		os.write(String.valueOf(m_targetFalse.getValue()));
    		os.write("\n");
    	}
    }

    public static class CompareLessInstr extends InstrIntf {
        private InstrIntf m_lhs;
        private InstrIntf m_rhs;

        public CompareLessInstr(InstrIntf lhs, InstrIntf rhs) {
            m_lhs = lhs;
            m_rhs = rhs;                   
        }

        public void execute(ExecutionEnvIntf env) {
            m_value = (m_lhs.getValue() <  m_rhs.getValue()) ? 1 : 0;
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("CMPLESS\n");
        }
    }
    public static class CompareGreaterInstr extends InstrIntf {
        private InstrIntf m_lhs;
        private InstrIntf m_rhs;

        public CompareGreaterInstr(InstrIntf lhs, InstrIntf rhs) {
            m_lhs = lhs;
            m_rhs = rhs;                   
        }

        public void execute(ExecutionEnvIntf env) {
            m_value = (m_lhs.getValue() >  m_rhs.getValue()) ? 1 : 0;
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("CMPGREATER\n");
        }
    }
    public static class CompareEqualInstr extends InstrIntf {
        private InstrIntf m_lhs;
        private InstrIntf m_rhs;

        public CompareEqualInstr(InstrIntf lhs, InstrIntf rhs) {
            m_lhs = lhs;
            m_rhs = rhs;                   
        }

        public void execute(ExecutionEnvIntf env) {
            m_value = (m_lhs.getValue() ==  m_rhs.getValue()) ? 1 : 0;
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("CMPEQUAL\n");
        }
    }

    public static class VarAccessInstr extends InstrIntf {
        private String m_identifier;

        public VarAccessInstr(String identifier) {
            m_identifier = identifier;
        }

        public void execute(ExecutionEnvIntf env) {
            m_value = env.getSymbol(m_identifier).m_number;
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write(String.format("VARIABLE %s\n", m_identifier));
        }
    }

    public static class VarAssignInstr extends InstrIntf {
        InstrIntf m_expr;
        Symbol m_symbol;

        public VarAssignInstr(InstrIntf expr, Symbol symbol) {
            m_expr = expr;
            m_symbol = symbol;
        }

        public void execute(ExecutionEnvIntf env) {
            env.getSymbol(m_symbol.m_name).m_number = m_expr.getValue();
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("ASSIGN\n");
        }
    }

    public static class NotInstr extends InstrIntf {
        private final InstrIntf operand;

        public NotInstr(InstrIntf operand) {
            this.operand = operand;
        }

        public void execute(ExecutionEnvIntf env) {
            m_value = operand.getValue() == 0 ? 1 : 0;
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("NOT\n");
        }
    }

    public static class MinusInstr extends InstrIntf {
        private final InstrIntf operand;

        public MinusInstr(InstrIntf operand) {
            this.operand = operand;
        }

        public void execute(ExecutionEnvIntf env) {
            m_value = -operand.getValue();
        }

        public void trace(OutputStreamWriter os) throws Exception {
            os.write("MINUS\n");
        }
    }


}
