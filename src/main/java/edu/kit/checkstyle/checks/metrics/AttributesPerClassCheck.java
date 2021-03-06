package edu.kit.checkstyle.checks.metrics;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


/**
 * Counts the number of attributes in a class or in an enum.
 *
 * @since JDK1.7, Jul 14, 2013
 */
public class AttributesPerClassCheck extends MetricCheck {

  public static final String METRIC = "attributes-per-class";

  @Override
  public int[] getDefaultTokens() {
    return new int[] { TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF };
  }

  @Override
  protected String metric() {
    return METRIC;
  }

  /*
    example class:

    public class X {
      private int i = 0;
    }

    corresponding AST:

    ( CLASS_DEF[4x0] (
      MODIFIERS[4x0]
        public[4x0] )
      class[4x7]
      foundAttribute[4x13] (
        OBJBLOCK[4x28]
        {[4x28] (
          VARIABLE_DEF[6x2] (
            MODIFIERS[6x2]
            private[6x2] ) (
            TYPE[6x10]
            int[6x10] )
          i[6x14] (
            =[6x16] (
              EXPR[6x18]
              0[6x18] ) )
          ;[6x19] )
        }[7x0] ) )
   */

  @Override
  protected void execute(final DetailAST ast) {
    final DetailAST body = ast.findFirstToken(TokenTypes.OBJBLOCK);
    final int count = countTokenType(body, TokenTypes.VARIABLE_DEF);
    logMetric(ast, count);
  }

}
