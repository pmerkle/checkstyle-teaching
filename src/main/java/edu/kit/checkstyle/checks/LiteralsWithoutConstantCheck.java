package edu.kit.checkstyle.checks;

import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Map;
import java.util.HashMap;

/**
 * Detect usage of literals in the middle of the code.
 */
public class LiteralsWithoutConstantCheck extends Check {

  private Map<String, Integer> counts = new HashMap<String,Integer>();

  private int stringMax = -1;

  /** Provide a specific limit for strings */
  public void setStringMax(int maximum) {
      stringMax = maximum;
  }

  private int max = 5;

  public void setMax(int maximum) {
      max = maximum;
  }

  /**
   * Literals are accepted in attribute definitions.
   *
   * Should check if they are final, though.
   */
  private boolean isInAttributeDefinition(DetailAST ast) {
    DetailAST current = ast;
    while (current != null) {
      if (current.getType() == TokenTypes.VARIABLE_DEF &&
        current.getParent().getType() == TokenTypes.OBJBLOCK) {
        return true;
      }
      current = current.getParent();
    }

    return false;
  }

  @Override
  public int[] getDefaultTokens() {
    return new int[] {
      TokenTypes.NUM_INT,
      TokenTypes.CHAR_LITERAL,
      TokenTypes.STRING_LITERAL,
      TokenTypes.NUM_FLOAT,
      TokenTypes.NUM_LONG,
      TokenTypes.NUM_DOUBLE
    };
  }

  @Override
  public void visitToken(final DetailAST ast) {
    final int line = ast.getLineNo();
    final int column = ast.getColumnNo();

    String text = ast.getText();

    // rule out the most simple constants
    if (text.equals("0") || text.equals("1") ||
        text.equals("0L") || text.equals("1L") ||
        text.equals("0.0") || text.equals("1.0") ||
        text.equals("")) {
      return;
    }

    // an everything in an attribute definition is also fine
    if (isInAttributeDefinition(ast)) {
      return;
    }

    // get actual count and limit
    int maximum = ast.getType() == TokenTypes.STRING_LITERAL && stringMax != -1 ? stringMax : max;
    int count = counts.containsKey(text) ? counts.get(text) + 1 : 1;

    // count them and only report them if they are repeatedly used
    if (count == maximum + 1) {
      // report the count once
      log(line, column, "literals.without.constant", text, maximum);
    } else {
        // do nothing, because the value was either less than allowed or it was already reported
    }
    counts.put(text, count);
  }
}
