package edu.kit.checkstyle.checks.lists;

import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


/**
 * Base class for all listing checks.
 *
 * Provides some convenience methods for assembling the names.
 */
public abstract class BaseListCheck extends Check {

  protected static String getScope(DetailAST ast) {
    String scope = new String();

    if (ast.getType() == TokenTypes.CLASS_DEF) {
      DetailAST classParent = getParent(ast, TokenTypes.CLASS_DEF);
      if (classParent != null) {
        return getScope(classParent) + getNameOfClassDef(classParent) + "$";
      }
    } else {
      DetailAST classParent = getParent(ast, TokenTypes.CLASS_DEF);
      if (classParent != null) {
        return getScope(classParent) + getNameOfClassDef(classParent) + ".";
      }

    }

    return "";
  }

  protected static DetailAST getParent(DetailAST ast, int tokenType) {
    DetailAST current = ast.getParent();
    while (current != null && current.getType() != tokenType) {
        current = current.getParent();
    }

    return current;
  }

  protected static String getNameOfPackageDef(DetailAST ast) {
    switch (ast.getType()) {
    case TokenTypes.PACKAGE_DEF:
        return getNameOfPackageDef(ast.getFirstChild().getNextSibling());
    case TokenTypes.DOT:
        DetailAST lhs = ast.getFirstChild();
        DetailAST rhs = lhs.getNextSibling();
        return getNameOfPackageDef(lhs) + "." + getNameOfPackageDef(rhs);
    case TokenTypes.IDENT:
        return ast.getText();
    default:
        throw new RuntimeException("Failed parsing package declaration");
    }
  }

  protected static String getNameOfClassDef(DetailAST ast) {
    return ast.getFirstChild().getNextSibling().getNextSibling().getText();
  }

  protected static String getNameOfMethodDef(DetailAST ast) {
    return ast.getFirstChild().getNextSibling().getNextSibling().getText();
  }
}
