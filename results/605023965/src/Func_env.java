import java.util.*;
import cs132.vapor.ast.VFunction;


// Should store the variable name
public class Func_env {

 	public static final int MAX_REG = 8;
 	public String func_name; 
	private VFunction vf;
	private HashMap<String, Integer> stack;
	private HashMap<String, Integer> reg; 
	private Integer stack_num;
	private Integer reg_num;
	public String message; 
	public LinkedList<String> params;
	public int out_num;

	public int instr_line;
	//used for reg allocation
	public HashMap<String, Integer> var_right; 
	public HashMap<String, Integer> var_left; 
	public LinkedList<String> sub_call;
	public LinkedList<String> assigned_reg;
	//public HashMap<String, String> new_stack;


	public Func_env(VFunction vfunction) {
		vf = vfunction;
		stack = new HashMap<String, Integer>();
		reg = new HashMap<String, Integer>();
		message = ""; 
		stack_num = 0; 
		reg_num = 2; 
		params = new LinkedList<String>();
		out_num = 0; 
		instr_line = 0; 
		func_name = vfunction.ident;
		//used for reg allocation
		var_left = new HashMap<String, Integer>();
		var_right = new HashMap<String, Integer>();
		sub_call = new LinkedList<String>();
		assigned_reg = new LinkedList<String>();
		//new_stack = new HashMap<String, String>();
		//new_code = "";
	}


	public String getVar(String var_name){

		//check if var is number 
		if (var_name.matches("\\d+")){
			return var_name;
		}

		//check if var is a string ""
		if (var_name.charAt(0) == '"' && var_name.charAt(var_name.length()-1) == '"')
			return var_name; 

		//check if var is ":func1"
		if (var_name.charAt(0) == ':')
			return var_name;

		//check if var is param
		if (params.contains(var_name)){
			/*
			if (params.size() > 4){
				return "$a" + params.indexOf(var_name); 
			}
			*/
			return "in[" + params.indexOf(var_name)+"]";
		}
		
		//check if var is in reg
		/*
		if (getReg(var_name) != null){
			return "$t" + getReg(var_name);
		} */

		//check if var is on stack
		if (getStack(var_name) != null){
			return "local[" + getStack(var_name) + "]"; 
		}
/*
		//add var - check register first ($t2 - $t8 available)
		if (getReg(var_name) == null && reg_num <= MAX_REG){
			reg.put(var_name, reg_num);
			reg_num ++;
			return "$t" + getReg(var_name);
		}*/

		//add var - if all register filled, put spilled value on local
		if (getStack(var_name) == null){ 
			stack.put(var_name, stack_num);
			stack_num ++;
			return "local[" + getStack(var_name) + "]"; 
		}

		return null; 

	}

	public Integer getStack(String var_name){
		return stack.get(var_name);
	}

	public Integer getStackNum(){
		return stack_num;
	}

	public Integer getReg(String var_name){
		return reg.get(var_name);
	}

	public void addMessg (String newLine){
		message = message + "\n" + newLine; 
	}

	public void setMaxOut(int i){
		if (i > out_num){
			out_num = i;
		}
	}

	public void addInstrLine(int i){
		instr_line += i; 
	}

	public void addToBegin (String begin_messg){
		//new_code = begin_messg + new_code; 
		message = begin_messg + message;
	}

	//following function is used for register allocation
	public void getVarRight(String curr, int line_num){
		var_right.put(curr, line_num);
	}

	public void getVarLeft(String curr, int line_num){
		if (var_left.get(curr) != null ){
			return ;
		}
		//Only put in the first current of var
		var_left.put(curr, line_num);
	}

	public void printVarPos(){
		for (String var: var_left.keySet()){
            Integer left = var_left.get(var);
            Integer right = var_right.get(var);

            if (right == null){
            	System.out.println(var + "is not used anywhere");
            }else {
            	System.out.println( var + " : " + left + " - " + right);  
        	}
		} 
	}

	public void printSubCall(){
		System.out.print("Sub call functions: ");
		for (String call_name: sub_call){
            System.out.print(call_name + "  ");
		} 
		System.out.println(""); 
	}

	public void addSubCall(String call_name){
		sub_call.add(call_name); 
	}

	public void assignReg(int begin, int end){
		for (int i = begin; i <= end; i++){
			//assigned_reg.put("local[" + local_idx + "]", "$t" + i );
			assigned_reg.add("\\$t" + i );
		}
	}

	public void localToreg (){
		for (String reg_i : assigned_reg){
			String old_local = "local\\[" + assigned_reg.indexOf(reg_i) + "\\]";
			message = message.replaceAll(old_local, reg_i); 
			//message = tmp;
		}
		
		for (int i = assigned_reg.size(); i < stack_num ; i++){
			String old_local = "local\\[" + i + "\\]";
			int new_idx = i - assigned_reg.size(); 
			String new_local = "local\\[" + new_idx + "\\]";
			message = message.replaceAll(old_local, new_local); 
		}

		//update the new stack (local) num
		stack_num = stack_num - assigned_reg.size();
	}


}