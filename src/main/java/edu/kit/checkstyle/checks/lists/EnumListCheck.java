package edu.kit.checkstyle.checks.lists;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


/**
 * List all enums.
 */
public class EnumListCheck extends BaseListCheck {

  @Override
  public int[] getDefaultTokens() {
    return new int[] {
      TokenTypes.ENUM_DEF
    };
  }

  @Override
  public void visitToken(final DetailAST ast) {
    log(ast.getLineNo(), ast.getColumnNo(), getScope(ast) + getNameOfClassDef(ast));
  }
}
