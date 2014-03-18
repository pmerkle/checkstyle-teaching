package edu.kit.checkstyle.checks.lists;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


/**
 * List all imports.
 */
public class ImportListCheck extends BaseListCheck {

  @Override
  public int[] getDefaultTokens() {
    return new int[] {
      TokenTypes.IMPORT
    };
  }

  @Override
  public void visitToken(final DetailAST ast) {
    log(ast.getLineNo(), ast.getColumnNo(), getImportString(ast));
  }

  private static String getImportString(DetailAST ast) {
    if (ast.getType() == TokenTypes.IMPORT) {
      return getImportString(ast.getFirstChild());
    } else if (ast.getType() == TokenTypes.DOT) {
        return getImportString(ast.getFirstChild()) + "." + getImportString(ast.getLastChild());
    } else {
        return ast.getText();
    }
  }

}
