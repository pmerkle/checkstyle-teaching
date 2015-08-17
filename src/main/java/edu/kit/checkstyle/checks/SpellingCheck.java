package edu.kit.checkstyle.checks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Check if spelling is corrrect according to the system dictionary.
 */
public class SpellingCheck extends Check {

  private static final String DICT_FILENAME = "aspell.dict";

  private static final String CUSTOM_DICT_FILENAME = "custom.dict";

  private Set<String> dictionary = new HashSet<String>();

  private void add(String word) {
    dictionary.add(word.trim().toLowerCase());
  }

  public void setAllowedWords(String words) {
      for (String word : words.split(",")) {
        add(word);
      }
  }

  /** Initialize the checker, populate its dictionary with words */
  public void init() {

    File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    String dictPath = jarFile.getParent() + File.separator + DICT_FILENAME;
    String customDictPath = jarFile.getParent() + File.separator + CUSTOM_DICT_FILENAME;

    // populate the dictionary from the specified word list
    try {
      populateDictionaryFromFile(dictPath);
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Could not find dictionary file " + dictPath);
    }

    // extend the dictionary with words from a custom word list
    try {
      populateDictionaryFromFile(customDictPath);
    } catch (FileNotFoundException e) {
      // custom dictionary is optional -> do nothing when file could not be found
    }
  }

  private void populateDictionaryFromFile(String pathToDictionary) throws FileNotFoundException {
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathToDictionary)));

    String line;
    try {
      while ((line = br.readLine()) != null) {
        add(line);
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
        return "Attribute";
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
    log(ast.getLineNo(), ast.getColumnNo(), "spelling", definitionType(ast), id);
  }
}
