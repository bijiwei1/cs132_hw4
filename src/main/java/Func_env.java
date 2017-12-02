import java.util.*;
import cs132.vapor.ast.VFunction;


// Should store the variable name
public class Func_env {

	private VFunction vf;
	private HashMap<String, Integer> stack;
	public Integer stack_num;
	public String message; 
	public LinkedList<String> params;
	public int out_num;

	public int instr_line;

	public Func_env(VFunction vfunction) {
		vf = vfunction;
		stack = new HashMap<String, Integer>();
		message = ""; 
		stack_num = 0; 
		params = new LinkedList<String>();
		out_num = 0; 
		instr_line = 0; 
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
		
		if (getStack(var_name) == null){ 
			stack.put(var_name, stack_num);
			stack_num ++;
			return "local[" + getStack(var_name) + "]"; 
		}else {
			return "local[" + getStack(var_name) + "]"; 
		}

	}

	public Integer getStack(String var_name){
		return stack.get(var_name);
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
}