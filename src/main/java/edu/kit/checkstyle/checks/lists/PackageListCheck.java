package edu.kit.checkstyle.checks.lists;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


/**
 * List all packages, imports, classes and methods.
 */
public class PackageListCheck extends BaseListCheck {

  @Override
  public int[] getDefaultTokens() {
    return new int[] {
      TokenTypes.PACKAGE_DEF
    };
  }

  @Override
  public void visitToken(final DetailAST ast) {
    log(ast.getLineNo(), ast.getColumnNo(), getNameOfPackageDef(ast));
  }
}
