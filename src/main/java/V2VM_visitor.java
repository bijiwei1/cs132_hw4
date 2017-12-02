import java.util.*;
import cs132.vapor.ast.*;

public class V2VM_visitor extends VInstr.Visitor<Throwable>{
	
	public static Func_env fe;

	public V2VM_visitor(Func_env curr_func) {
		fe = curr_func;
	}

	//Note: $t0 and $t1 is reserved to do all instruction translation as tmp register
	public void visit(VAssign a) throws Throwable{
	/*
		dest (local var, reg) = source (param, local var, reg, constant)
		Psedo code : 
		$t0 = source
		$dest = $t0
	*/
		String right = "$t0 = " + fe.getVar(a.source.toString());
		String left = fe.getVar(a.dest.toString()) + " = $t0";
		fe.addMessg(right);
		fe.addMessg(left); 
		fe.addInstrLine(1); 
	} 


	public void visit(VBranch b) throws Throwable {
	/*
		dest = OP(args...)
		Psedo code : 
		$t0 = op(args ...);
		dest = $t0

	*/

		//System.out.println("Branch: " + b.value.toString() + " "+b.target.toString());
		//System.out.println("Branch pos is " + b.sourcePos.line);

		String l1 = "$t0 = " + fe.getVar(b.value.toString());
		String ifline = "if";
		if (!b.positive) {
			ifline += "0";
		}

		ifline += " $t0 goto " + b.target.toString();
		fe.addMessg(l1);
		fe.addMessg(ifline);
		fe.addInstrLine(1); 
	}

	public void visit(VBuiltIn c) throws Throwable {
	/*
		dest = OP(args...)
		Psedo code (Print / Error): 
		PrintIntS()

		Psedo code (Other ops)
		$t0 = op(args ...);
		dest = $t0

	*/
		/*
		System.out.println("Build in" + c.dest.toString() + "  " + c.op.name + " ");
		for (int i = 0; i < c.op.numParams; i++) {
			String arg = c.args[i].toString();
			System.out.println(arg);
		}*/


		// Number of the current temp we are using
		int curT = 1;

		// Begin to build translation. 
		String biCall = "";
		String biCallPostamble = "";
		String biCallPreamble = "";

		if (c.op.name.equals("Error")){
			biCall += c.op.name + "(" + fe.getVar(c.args[0].toString()) + ")";
			fe.addMessg(biCall);
			fe.addInstrLine(1); 
			return; 
		}

		if (c.dest != null) {
			biCall = "$t0 = ";
			biCallPostamble = "\n" + fe.getVar(c.dest.toString()) + " = $t0";
		}

		biCall += c.op.name + "(";

		// Parameters
		for (int i = 0; i < c.op.numParams; i++) {
			String arg = c.args[i].toString();
			if (i != 0)
				biCall += " ";

			//check if arg is a number 
			if (arg.matches("\\d+")){
				biCall += arg;
				continue;
			}

			biCallPreamble += "$t" + curT + " = " + fe.getVar(arg) + "\n";
			biCall += "$t" + curT;
			curT++;
		}

		biCall += ")";
		biCall = biCallPreamble + biCall + biCallPostamble;

		fe.addMessg(biCall);
		fe.addInstrLine(1); 

	}

	public void visit(VCall c) throws Throwable {
	/*
		t.0 = call t.1(t.2, t.3 ...)
		Psedo code:
		for loop : $t0 = t.2 (local[j]); out[i] = $t0
		$t1 = t.1 
		call $t1
		local [w] = $v0
	*/
		int outlength = c.args.length;
		fe.setMaxOut(outlength);

		for (int i = 0; i < outlength; i++) {
			String l1 = "$t0 = " + fe.getVar(c.args[i].toString()) + "\n";
			l1 += "out[" + i + "] = $t0";
			fe.addMessg(l1);
		}

		String call = "$t1 = " + fe.getVar(c.addr.toString()) + "\n";
		call += "call $t1\n";
		call += fe.getVar(c.dest.toString()) + " = $v0";

		fe.addMessg(call);
		fe.addInstrLine(1); 
	}
	
	public void visit(VGoto g) throws Throwable {
		fe.addMessg("goto " + g.target.toString());
		fe.addInstrLine(1); 
	}

	public void visit(VMemRead r) throws Throwable {
	/*
		dest = [t.x + offset]
		Psedo code : 
		$t0 = t.x
		$t1 = [$t0 + offset] 
	*/
		VMemRef.Global source = (VMemRef.Global) r.source;
		String l0 = "$t0 = " + fe.getVar(source.base.toString());

		String memref = "$t0";
		if (source.byteOffset != 0)
			memref += "+" + source.byteOffset;

		String l1 = "$t1 = [" + memref + "]";
		String l2 = fe.getVar(r.dest.toString()) + " = $t1";
		fe.addMessg(l0);
		fe.addMessg(l1);
		fe.addMessg(l2);
		fe.addInstrLine(1); 
	}

	public void visit(VMemWrite w) throws Throwable {
	/*
		[t.x + offset] = source (:vmt_BT)
		Psedo code : 
		$t0 = t.x;
		$t1 = source
		[$t0 + offset] = $t1
	*/
		//System.out.println(dest.base.toString() + "  " + dest.byteOffset + " " + w.source.toString());
		VMemRef.Global dest = (VMemRef.Global) w.dest;
		String l0 = "$t0 = " + fe.getVar(dest.base.toString());

		String memref = "$t0";
		if (dest.byteOffset != 0) 
			memref += "+" + dest.byteOffset;

		String l1 = "$t1 = " + fe.getVar(w.source.toString());
		String l2 = "[" + memref + "] = $t1";
		fe.addMessg(l0);
		fe.addMessg(l1);
		fe.addMessg(l2);
		fe.addInstrLine(1); 
	}

	public void visit(VReturn r) throws Throwable {

		if (r.value != null) {
			String l1 = "$v0 = " + fe.getVar(r.value.toString());
			fe.addMessg(l1);
		}

		String l2 = "ret";
		fe.addMessg(l2);
	}
}