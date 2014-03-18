package edu.kit.checkstyle.checks.lists;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


/**
 * List all classes.
 */
public class ClassListCheck extends BaseListCheck {

  @Override
  public int[] getDefaultTokens() {
    return new int[] {
      TokenTypes.CLASS_DEF
    };
  }

  @Override
  public void visitToken(final DetailAST ast) {
    log(ast.getLineNo(), ast.getColumnNo(), getScope(ast) + getNameOfClassDef(ast));
  }
}
