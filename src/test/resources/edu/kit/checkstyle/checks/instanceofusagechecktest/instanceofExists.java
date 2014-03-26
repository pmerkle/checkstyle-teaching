package edu.kit.checkstyle.checks.instanceofusagechecktest;

public class instanceofExists {
  // correct signature
  public final boolean equals(Object i) {
    if (i instanceof String) {}
    return false;
  }

  // wrong signature, but might happen in beginners code
  public boolean equals(String i) {
    if (i instanceof String) {}
    return false;
  }
}
