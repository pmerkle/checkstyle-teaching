package edu.kit.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.Check;
import java.util.Arrays;
import java.util.List;


/**
 * Checks the nesting depth of control flow constructs.
 *
 */
public class ControlFlowNestingDepthCheck extends Check {

  private int max = 4;

  /**
   * Configure the allowed control flow nesting depth.
   */
  public void setMax(int limit) {
      max = limit;
  }

  /**
   * Return true iff the ast element is a literal control flow element.
   */
  private boolean isControlFlowLiteral(DetailAST ast) {
      for (int controlFlowToken : getDefaultTokens()) {
          if (ast.getType() == controlFlowToken) {
              return true;
          }
      }

      return false;
  }

  // we use these tokens to trigger the check.
  // not a perfect choice but i guess there is no best choice for this.
  @Override
  public int[] getDefaultTokens() {
    return new int[] {
        TokenTypes.LITERAL_IF,
        TokenTypes.LITERAL_ELSE,
        TokenTypes.LITERAL_TRY,
        TokenTypes.LITERAL_CATCH,
        TokenTypes.LITERAL_WHILE,
        TokenTypes.LITERAL_FOR,
        TokenTypes.LITERAL_SWITCH
    };
  }

  /**
   * Skip past chains of if-else-ifs.
   *
   * This represents code such as:
   * if (...) {
   *   ...
   * } else if (...) {
   *   ...
   * } else if (...) {
   *   ...
   * } else {
   * }
   */
  public DetailAST skipIfElse(DetailAST ast) {
    DetailAST parent = ast.getParent();
    if (parent == null) {
      return ast;
    }

    if ((ast.getType() == TokenTypes.LITERAL_IF &&
         parent.getType() == TokenTypes.LITERAL_ELSE) ||
        (ast.getType() == TokenTypes.LITERAL_ELSE &&
         parent.getType() == TokenTypes.LITERAL_IF)) {
      return skipIfElse(parent);
    } else {
        return ast;
    }
  }

  /**
   * Skip past any matching control flow elements.
   *
   * The checkstyle parsers treats CATCH as a child of try and else as a child
   * of if, but we don't want this.
   *
   */
  public DetailAST skipTryCatch(DetailAST ast) {
    DetailAST parent = ast.getParent();
    if (parent == null) {
      return ast;
    }

    if (ast.getType() == TokenTypes.LITERAL_CATCH && (
        parent.getType() == TokenTypes.LITERAL_CATCH ||
        parent.getType() == TokenTypes.LITERAL_TRY)) {
      return skipTryCatch(parent);
    } else {
      return ast;
    }
  }

  /**
   * Skip past control flow tokens of the same group.
   *
   * For catches, this skips past other catches and the try.
   */
  public DetailAST skipGroup(DetailAST ast) {
    DetailAST skipped = skipTryCatch(ast);
    if (skipped != ast) {
      return skipped;
    }

    skipped = skipIfElse(ast);
    if (skipped != ast) {
      return skipped;
    }

    return ast;
  }

  /**
   * Return the next parent that is an unrelated control flow statement of ast.
   */
  public DetailAST getControlFlowParent(DetailAST ast) {
      DetailAST current = ast;
      do {
        current = skipGroup(current);
        current = current.getParent();
      } while (current != null && !isControlFlowLiteral(current));

      return current;
  }

  @Override
  public void visitToken(final DetailAST ast) {
    final int line = ast.getLineNo();
    final int column = ast.getColumnNo();

    int depth = 1;
    String nesting = new String(ast.getText());

    DetailAST token = getControlFlowParent(ast);
    while (token != null) {
      nesting = token.getText() + "/" + nesting;
      ++depth;

      token = getControlFlowParent(token);
    }

    if (ast.getType() == TokenTypes.LITERAL_ELSE &&
        ast.getFirstChild().getType() == TokenTypes.LITERAL_IF) {
        // we don't want to report an else if has as its child and if directly.
        // this is likely a '} else if(...) {' construct.
        return;
    }

    if (depth > max) {
      log(line, column, "Control flow is nested " + depth + " levels deep (limit is " + max + ").");
    }
  }
}
