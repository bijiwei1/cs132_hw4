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

			LinkedList<Func_env> all_funcs = new LinkedList<Func_env>();

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

				//finally print out all codes
				/*System.out.print("func " + func.ident +  "[in " + fe.params.size() + ", out " 
								+ fe.out_num + ", local "+ fe.getStackNum() +"]"); 
				System.out.println(fe.message);
				System.out.println("");*/
				/*
				fe.addToBegin("func " + func.ident +  "[in " + fe.params.size() + ", out " 
								+ fe.out_num + ", local "+ fe.getStackNum() +"]");
				fe.addMessg("");
				*/

				all_funcs.add(fe); 
			}

			//Allocate all data to as many register as possible
			//RegAllocation(all_funcs); 

			//Print out all codes
			for (Func_env fe: all_funcs){
				fe.addToBegin("func " + fe.func_name +  "[in " + fe.params.size() + ", out " 
								+ fe.out_num + ", local "+ fe.getStackNum() +"]");
			
				System.out.println("");
				System.out.println(fe.message);
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

	public static void RegAllocation(LinkedList<Func_env> all_funcs){

		//Collect all information needed for register allocation
		for (Func_env fe: all_funcs){
			String code = fe.message;
			String lines[] = code.split("\\r?\\n");
			for (int i = 1; i < lines.length; i ++){
				String left; 
				String right;

				if (lines[i].contains("=")){
					String tmp[] = lines[i].split(" = ");
					if (tmp[0].contains("local")){
						left = tmp[0]; 
						fe.getVarLeft(left, i);
					}
					if (tmp[1].contains("local")){
						right = tmp[1]; 
						fe.getVarRight(right, i);
					}
				}

				if (lines[i].contains("vmt")){
					String tmp[] = lines[i].split("vmt_");
					fe.addSubCall(tmp[1]); 
				}

				if (lines[i].contains("call")){
					fe.addSubCall("tmp ");
				}
			}

			/*
			System.out.println("Function " + fe.func_name);
			fe.printVarPos();
			fe.printSubCall();
			*/
		}

		int avail_reg = 2; 

		//Assign register now
		for (Func_env fe: all_funcs){

			//Assign register to bottom-level function first ($t2 - $t8)
			if (fe.sub_call.size() == 0){
				avail_reg = 2; 
				int var_num = fe.var_left.size();
				int begin = 0;
				int end = 0;
				if (avail_reg + var_num <= 9){
					begin = avail_reg;
					end = avail_reg + var_num - 1;
				}else {
					begin = avail_reg;
					end = 8;
				}
				fe.assignReg(begin, end);
				//System.out.println("Assigned bottom-level function" + fe.func_name + " begin: " + begin + " end: "  + end);
				
				//Update the stack(local) with register
				fe.localToreg(); 
			}

		}

	}

}

