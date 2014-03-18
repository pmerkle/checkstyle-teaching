package edu.kit.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Set;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Check if spelling is corrrect according to the system dictionary.
 */
public class SpellingCheck extends Check {

  private Set<String> dictionary;

  /** Initialize the checker, populate its dictionary with words */
  public void init() {
    // populate the dictionary from the system's word list
    dictionary = new HashSet<String>();

    BufferedReader br;
    try {
       br = new BufferedReader(new InputStreamReader(new FileInputStream("/usr/share/dict/words")));
    } catch (FileNotFoundException e) {
        throw new RuntimeException("Dictionary not found", e);
    }

    String line;
    try {
        while ((line = br.readLine()) != null) {
            dictionary.add(line.trim().toLowerCase());
        }
    } catch (IOException e) {
        throw new RuntimeException("Dictionary not readable", e);
    }

    try {
      br.close();
    } catch (IOException e) {
        throw new RuntimeException("Dictionary not closeable", e);
    }
  }

  @Override
  public int[] getDefaultTokens() {
    return new int[] {
      TokenTypes.METHOD_DEF,
      TokenTypes.VARIABLE_DEF,
      TokenTypes.CLASS_DEF,
      TokenTypes.INTERFACE_DEF,
      TokenTypes.ENUM_DEF,
      TokenTypes.ENUM_CONSTANT_DEF,
      TokenTypes.PARAMETER_DEF,
      TokenTypes.PACKAGE_DEF,
      TokenTypes.ANNOTATION_DEF,
      TokenTypes.ANNOTATION_FIELD_DEF
    };
  }

  /** Return the identifier for a definition */
  private DetailAST identifier(final DetailAST ast) {
    DetailAST current = ast.getFirstChild();
    while (current != null) {
      if (current.getType() == TokenTypes.IDENT) {
        return current;
      }
      current = current.getNextSibling();
    }

    return null;
  }

  /** Capitalize the first character of a string */
  private static String capitalized(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

  private static boolean isAttributeDefinition(final DetailAST ast) {
    return (ast.getType() == TokenTypes.VARIABLE_DEF &&
            ast.getParent().getParent().getType() == TokenTypes.CLASS_DEF);
  }

  /** Return a string representation of the definition type */
  private static String definitionType(final DetailAST ast) {

    // variable -> attribute if the definition is directly in a class
    if (isAttributeDefinition(ast)) {
        return "attribute";
    }

    String type = ast.getText().toLowerCase();

    // strip trailing "_DEF" if necessary
    if (ast.getType() != TokenTypes.PACKAGE_DEF) {
      type = type.substring(0, type.length() - 4);
    }

    return capitalized(type).replace("_", " ");
  }

  private static String[] splitCamelCase(String camelCase) {
    return camelCase.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
  }

  /** Return true iff the candidate is a known word. */
  private boolean isKnownWord(String candidate) {
    String normalized = candidate.trim().toLowerCase();
    return dictionary.contains(normalized);
  }

  /** Return true iff the word, split by camel case, contains only known words. */
  private boolean isKnownWithCamelCase(String candidate) {
    for (String word : splitCamelCase(candidate)) {
      if (!isKnownWord(word)) {
          return false;
      }
    }
    return true;
  }

  /** Return true iff the word, split by underscore, contains only known words. */
  private boolean isKnownWithUnderscores(String candidate) {
    for (String word : candidate.split("_")) {
        if (!isKnownWord(word)) {
            return false;
        }
    }
    return true;
  }

  @Override
  public void visitToken(final DetailAST ast) {
    DetailAST ident = identifier(ast);
    if (ident == null) {
      // this should not happen
      return;
    }

    // remove trailing numbers
    String id = ident.getText().replaceAll("[0-9]*$", "");

    // check if it is good spelling in camel case
    if (isKnownWithCamelCase(id)) {
      return;
    }

    // check if it is good spelling with underscores
    if (isKnownWithUnderscores(id)) {
      return;
    }

    // check if it is a local variable and short
    if (ast.getType() == TokenTypes.VARIABLE_DEF &&
            !isAttributeDefinition(ast) &&
            id.length() <= 3) {
      return;
    }

    // if everything fails it looks like bad spelling
    log(ast.getLineNo(), ast.getColumnNo(), definitionType(ast) + " declaration \'" + id + "\' looks like bad spelling.");
  }
}
