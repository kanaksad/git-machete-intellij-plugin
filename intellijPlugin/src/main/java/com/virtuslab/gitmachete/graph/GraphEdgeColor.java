package com.virtuslab.gitmachete.graph;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GraphEdgeColor {
  TRANSPARENT(0), GRAY(1), YELLOW(2), RED(3), GREEN(4);

  @Getter
  private final int id;

  public static GraphEdgeColor getById(int id) {
    return Arrays.stream(values()).filter(e -> e.getId() == id).findFirst().orElse(TRANSPARENT);
  }
}
