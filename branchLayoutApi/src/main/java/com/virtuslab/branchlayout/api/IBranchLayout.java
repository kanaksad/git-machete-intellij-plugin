package com.virtuslab.branchlayout.api;

import io.vavr.collection.List;
import io.vavr.control.Option;

public interface IBranchLayout {
  List<IBranchLayoutEntry> getRootEntries();

  Option<IBranchLayoutEntry> findEntryByName(String branchName);

  IBranchLayout slideIn(String parentBranchName, IBranchLayoutEntry entryToSlideIn) throws BranchLayoutException;

  IBranchLayout slideOut(String branchName) throws BranchLayoutException;
}
