package edu.kit.checkstyle.checks;

import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import edu.kit.checkstyle.TokenSearcherCheck;


/**
 * Checks for the existence of instanceof keywords. It these keywords are found
 * outside of an equals method an error is reported.
 *
 * @since JDK1.7, Jun 3, 2013
 */
public class InstanceofUsageCheck extends TokenSearcherCheck {

  @Override
  public int[] getDefaultTokens() {
    return new int[] {
        TokenTypes.LITERAL_INSTANCEOF
    };
  }

  @Override
  public void visitToken(final DetailAST ast) {
    final DetailAST methodDef = getContainingMethod(ast);
    final boolean isEqualsMethod = methodDef == null ? false : isEqualsMethod(methodDef);

    if (!isEqualsMethod) {
      log(ast.getLineNo(), ast.getColumnNo(), "instanceof should only be used in the equals method.");
    }
  }

  private boolean isEqualsMethod(final DetailAST methodDef) {
    final DetailAST methodName = methodDef.findFirstToken(TokenTypes.IDENT);
    if (!eqName(methodName, "equals")) {
        return false;
    }

    final DetailAST methodType = methodDef.findFirstToken(TokenTypes.TYPE);
    if (!eqTypeASTName(methodType, "boolean")) {
        return false;
    }

    final DetailAST params = methodDef.findFirstToken(TokenTypes.PARAMETERS);
    if (!eqParamCount(params, 1)) {
        return false;
    }

    final Set<String> mods = modifierNames(methodDef);
    return mods.contains("public") && !mods.contains("static");
  }

  private boolean eqParamCount(final DetailAST ast, final int count) {
    require(ast.getType() == TokenTypes.PARAMETERS, "not a parameter list");
    return ast.getChildCount() == count;
  }

  private boolean eqTypeASTName(final DetailAST ast, final String name) {
    require(ast.getType() == TokenTypes.TYPE, "not a type");
    return ast.getFirstChild().getText().equals(name);
  }
}
