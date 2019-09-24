/**
 * Developed  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2019.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2019 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2019
 */

package cop5556fa19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.print.DocFlavor.STRING;

import cop5556fa19.AST.Block;
import cop5556fa19.AST.Exp;
import cop5556fa19.AST.ExpBinary;
import cop5556fa19.AST.ExpFalse;
import cop5556fa19.AST.ExpFunction;
import cop5556fa19.AST.ExpInt;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpNil;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTable;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpUnary;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.FieldNameKey;
import cop5556fa19.AST.FuncBody;
import cop5556fa19.AST.Name;
import cop5556fa19.AST.ParList;
import cop5556fa19.Token.Kind;
import static cop5556fa19.Token.Kind.*;

public class ExpressionParser {
	
	@SuppressWarnings("serial")
	class SyntaxException extends Exception {
		Token t;
		
		
		public SyntaxException(Token t, String message) {
			super(t.line + ":" + t.pos + " " + message);
		}
	}
	
	final Scanner scanner;
	Token t;  //invariant:  this is the next token
	Exp e10;

	ExpressionParser(Scanner s) throws Exception {
		this.scanner = s;
		t = scanner.getNext(); //establish invariant
	}


	Exp exp() throws Exception {
		Token first = t;
		
		Exp e0=null;
		switch(first.kind) {
		case KW_true:{
			e10 = new ExpTrue(t);
			consume();
			e0=exp();
			break;
		}
			
		case KW_false:{
			e10 = new ExpFalse(t);
			consume();
			e0=exp();
			break;
		}
		case INTLIT:{
			e10 = new ExpInt(t);
			consume();
			if(t.kind.equals(OP_TIMES) || t.kind.equals(OP_DIV)) {
				e0 = createMultiplyStringLit();
			}
			else if(t.kind.equals(OP_PLUS)|| t.kind.equals(OP_MINUS) || t.kind.equals(OP_MOD) || t.kind.equals(OP_DIVDIV) ||t.kind.equals(DOTDOT) || t.kind.equals(DOTDOTDOT) ) {
			e0=createaddStringLit();
			}else if(t.kind.equals(EOF)) {
				e0  = e10;
			}else {
				e0 = exp();
			}
			//e0=exp();
			break;
		}
		case STRINGLIT:{
			e10 = new ExpString(t);
			consume();
			if(t.kind.equals(OP_TIMES) || t.kind.equals(OP_DIV)) {
				e0 = createMultiplyStringLit();
			}
			else if(t.kind.equals(OP_PLUS)|| t.kind.equals(OP_MINUS) || t.kind.equals(OP_MOD) || t.kind.equals(OP_DIVDIV) ||t.kind.equals(DOTDOT) || t.kind.equals(DOTDOTDOT) ) {
			e10=createaddStringLit();
			}
			e0 = exp();
			
			//e0=exp();
			break;
		}
		case DOTDOTDOT:{
			e10 = new ExpVarArgs(t);
			consume();
			e0=exp();
			break;
		}
		case NAME:{
			e10 = new ExpName(t);
			consume();
			if(t.kind.equals(ASSIGN)) {
				consume();
				e0 = exp();
			}
			else {
				e0=exp();
			}
			break;
		}
		case OP_MINUS:{
			e10 = new ExpUnary(consume(), OP_MINUS, exp());
			consume();
			e0=exp();
			break;
		}
		case KW_not :{
			e10 = new ExpUnary(t, KW_not, e10);
			consume();
			e0 = exp();
			break;
		}
		case  OP_HASH :{
			e10 = new ExpUnary(t, OP_MINUS, e10);
			consume();
			e0 = exp();
			break;
		}
		case LPAREN:{
			match(LPAREN);
			e0 = exp();
			break;
		}
		case RPAREN:{
			match(RPAREN);
			e0 = exp();
			break;
		}
		case BIT_XOR:{
			e10 = new ExpUnary(t, OP_MINUS, e10);
			consume();
			e0 = exp();
			break;
		}
		case EOF:{
			e0 = e10;
			break;
		}
		case OP_TIMES:{
			throw new SyntaxException(t, "This OP_TIMES(*) token is not valid at this position");
		}
		}		//Exp e0 = andExp();
		while (isKind(KW_or)) {
			Token op = consume();
			Exp e1 = andExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}

	
private Exp createMultiplyStringLit() throws Exception{
	Exp e0=null;
	switch(t.kind) {
	case OP_TIMES:{
		match(OP_PLUS);
		e0 = new ExpBinary(e10.firstToken,e10,OP_PLUS,exp());
		break;
	}
	case OP_DIV:{
		match(OP_MINUS);
		e10 = new ExpBinary(e10.firstToken,e10,OP_MINUS,exp());
		break;
	}
	case EOF:{
		e0 = e10;
		break;
	}
	default:{
		if(t.kind.equals(STRINGLIT)) {
			e0 = new ExpString(t);
		}else if(t.kind.equals(INTLIT)) {
			e0 = new ExpInt(t);
		}
	}
		
	}
	
	return e0;
}


private Exp createaddStringLit() throws Exception {
	Exp e0=null;
		switch(t.kind) {
		case OP_PLUS:{
			match(OP_PLUS);
			e0 = new ExpBinary(e10.firstToken,e10,OP_PLUS,createMultiplyStringLit());
			break;
		}
		case OP_MINUS:{
			match(OP_MINUS);
			e0= new ExpBinary(e10.firstToken,e10,OP_MINUS,createMultiplyStringLit());
			break;
		}
		case DOTDOTDOT:{
			match(DOTDOTDOT);
			e0 = new ExpBinary(e10.firstToken,e10,DOTDOTDOT,exp());
			break;
		}
		case DOTDOT:{
			match(DOTDOT);
			e0 = new ExpBinary(e10.firstToken,e10,DOTDOT,exp());
			break;
		}
		case EOF:{
			e0 = e10;
			break;
		}
		}
		
		return e0;
	}


private Exp andExp() throws Exception{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("andExp");  //I find this is a more useful placeholder than returning null.
	}


	private Block block() {
		return new Block(null);  //this is OK for Assignment 2
	}


	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}

	/**
	 * @param kind
	 * @return
	 * @throws Exception
	 */
	Token match(Kind kind) throws Exception {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		error(kind);
		return null; // unreachable
	}

	/**
	 * @param kind
	 * @return
	 * @throws Exception
	 */
	Token match(Kind... kinds) throws Exception {
		Token tmp = t;
		if (isKind(kinds)) {
			consume();
			return tmp;
		}
		StringBuilder sb = new StringBuilder();
		for (Kind kind1 : kinds) {
			sb.append(kind1).append(kind1).append(" ");
		}
		error(kinds);
		return null; // unreachable
	}

	Token consume() throws Exception {
		Token tmp = t;
        t = scanner.getNext();
		return tmp;
	}
	
	void error(Kind... expectedKinds) throws SyntaxException {
		String kinds = Arrays.toString(expectedKinds);
		String message;
		if (expectedKinds.length == 1) {
			message = "Expected " + kinds + " at " + t.line + ":" + t.pos;
		} else {
			message = "Expected one of" + kinds + " at " + t.line + ":" + t.pos;
		}
		throw new SyntaxException(t, message);
	}

	void error(Token t, String m) throws SyntaxException {
		String message = m + " at " + t.line + ":" + t.pos;
		throw new SyntaxException(t, message);
	}
	


}
