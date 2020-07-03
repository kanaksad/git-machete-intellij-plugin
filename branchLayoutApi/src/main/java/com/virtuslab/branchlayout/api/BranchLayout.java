package com.virtuslab.branchlayout.api;

import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import lombok.Getter;
import org.checkerframework.checker.interning.qual.UsesObjectEquals;

@UsesObjectEquals
public class BranchLayout implements IBranchLayout {

  @Getter
  private final List<IBranchLayoutEntry> rootEntries;

  private final Map<String, IBranchLayoutEntry> entryByName;

  public BranchLayout(List<IBranchLayoutEntry> rootEntries) {
    this.rootEntries = rootEntries;
    this.entryByName = rootEntries.flatMap(BranchLayout::collectEntriesRecursively)
        .toMap(entry -> Tuple.of(entry.getName(), entry));
  }

  private static List<IBranchLayoutEntry> collectEntriesRecursively(IBranchLayoutEntry entry) {
    return entry.getChildren().flatMap(BranchLayout::collectEntriesRecursively).prepend(entry);
  }

  @Override
  public Option<IBranchLayoutEntry> findEntryByName(String branchName) {
    return entryByName.get(branchName);
  }

  @Override
  public IBranchLayout slideOut(String branchName) throws BranchLayoutException {
    var entryToSlideOut = findEntryByName(branchName).getOrNull();
    if (entryToSlideOut == null) {
      throw new BranchLayoutException("Branch entry '${branchName}' does not exist");
    }
    if (rootEntries.contains(entryToSlideOut)) {
      throw new BranchLayoutException("Cannot slide out root branch entry ${entryToSlideOut}");
    }
    return new BranchLayout(rootEntries.flatMap(rootEntry -> slideOut(rootEntry, entryToSlideOut)));
  }

  @SuppressWarnings("interning:not.interned") // to allow for `entry == entryToSlideOut`
  private List<IBranchLayoutEntry> slideOut(IBranchLayoutEntry entry, IBranchLayoutEntry entryToSlideOut) {
    var children = entry.getChildren();
    if (entry == entryToSlideOut) {
      return children;
    } else {
      return List.of(entry.withChildren(children.flatMap(child -> slideOut(child, entryToSlideOut))));
    }
  }
}
