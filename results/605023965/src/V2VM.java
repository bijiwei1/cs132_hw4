import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.*;

import java.io.*; 
import java.util.*;


public class V2VM{ 
    
    public static void main (String [] args) {
		try { 

			VaporProgram program = parseVapor(System.in, System.err); 

			//Allocate global data (global variables and functions)
			if (program.dataSegments != null) {
				for (VDataSegment data: program.dataSegments) {
					System.out.println("const " + data.ident);
					if (data.values != null) {
						for (VOperand.Static v: data.values) {
							System.out.println("  " + v.toString());
						}
					}
				}
			}

			System.out.println("");

			for (VFunction func: program.functions){

				Func_env fe = new Func_env(func); 
				for (VVarRef.Local p: func.params) {
					fe.params.add(p.toString());
				}
				/*
				System.out.println("Function is " + func.toString()); 

				System.out.println("Parameters:"); 
				for (VVarRef.Local p: func.params) {
					System.out.println(p.toString()); 
				}

				System.out.println("Local variables: "); 
				for (String p: func.vars) {
					System.out.println(p.toString()); 
				}


				System.out.println("Instr :");
				for (VInstr inst: func.body) {
					System.out.println(inst.toString());
				}

				for (int i = 0; i < func.labels.length; i++) {
					System.out.println("Label " + func.labels[i].ident + "at " + func.labels[i].instrIndex);
				}*/

				V2VM_visitor vvistor = new V2VM_visitor(fe);
				for (VInstr instr: func.body) {
					//check if reach the label line
					for (int i = 0; i < func.labels.length; i++) {
						if(func.labels[i].instrIndex == fe.instr_line){
							fe.addMessg(func.labels[i].ident + ": ");
						}
					}

					try {
						instr.accept(vvistor); 
					}catch (Throwable t) {
					System.out.println("Failed to parse current instruction");
					}
				}

				System.out.print("func " + func.ident +  "[in " + fe.params.size() + ", out " 
								+ fe.out_num + ", local "+ fe.stack_num +"]"); 
				System.out.println(fe.message);
				System.out.println("");
			}

		}catch (IOException ex) {
			System.out.println("Parsed unsuccessfully");
		}
    }

    public static VaporProgram parseVapor(InputStream in, PrintStream err)
  	throws IOException
	{
  		Op[] ops = {
    	Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
    	Op.PrintIntS, Op.HeapAllocZ, Op.Error,
    	};
  		boolean allowLocals = true;
  		String[] registers = null;
  		boolean allowStack = false;

  		VaporProgram program;
  		try {
    		program = VaporParser.run(new InputStreamReader(in), 1, 1,
                              java.util.Arrays.asList(ops),
                              allowLocals, registers, allowStack);
  		}catch (ProblemException ex) {
    		err.println(ex.getMessage());
    		return null;
  		}

  		return program;
	}

}

