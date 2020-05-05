package com.virtuslab.gitmachete.frontend.ui.impl.root;

import static com.virtuslab.gitmachete.frontend.datakeys.DataKeys.typeSafeCase;
import static com.virtuslab.gitmachete.frontend.ui.impl.root.DispatchThreadUtils.getIfOnDispatchThreadOrNull;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

import java.awt.BorderLayout;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.ScrollPaneFactory;
import org.checkerframework.checker.guieffect.qual.UIEffect;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.virtuslab.binding.RuntimeBinding;
import com.virtuslab.gitmachete.frontend.actionids.ActionGroupIds;
import com.virtuslab.gitmachete.frontend.actionids.ActionPlaces;
import com.virtuslab.gitmachete.frontend.datakeys.DataKeys;
import com.virtuslab.gitmachete.frontend.ui.api.table.BaseGraphTable;
import com.virtuslab.gitmachete.frontend.ui.api.table.IGraphTableFactory;
import com.virtuslab.logger.IPrefixedLambdaLogger;
import com.virtuslab.logger.PrefixedLambdaLoggerFactory;

public final class GitMachetePanel extends SimpleToolWindowPanel implements DataProvider {
  private static final IPrefixedLambdaLogger LOG = PrefixedLambdaLoggerFactory.getLogger("frontendUiRoot");

  private final VcsRootComboBox vcsRootComboBox;
  private final BaseGraphTable graphTable;

  @UIEffect
  public GitMachetePanel(Project project) {
    super(/* vertical */ false, /* borderless */ true);
    LOG.debug("Instantiating");

    this.vcsRootComboBox = new VcsRootComboBox(project);
    this.graphTable = RuntimeBinding
        .instantiateSoleImplementingClass(IGraphTableFactory.class).create(project, vcsRootComboBox);

    graphTable.queueRepositoryUpdateAndModelRefresh();

    // This class is final, so the instance is `@Initialized` at this point.

    setToolbar(createGitMacheteVerticalToolbar().getComponent());
    add(VcsRootComboBox.createShrinkingWrapper(vcsRootComboBox), BorderLayout.NORTH);
    setContent(ScrollPaneFactory.createScrollPane(graphTable));
  }

  @Override
  public @Nullable Object getData(String dataId) {
    return Match(dataId).of(
        // IntelliJ Platform seems to always invoke `getData` on the UI thread anyway;
        // `getIfOnDispatchThreadOrNull` is more to ensure correctness wrt. `@UIEffect`,
        // and to handle the unlikely case when someone invokes `getData` directly from the our codebase.
        typeSafeCase(DataKeys.KEY_SELECTED_VCS_REPOSITORY,
            getIfOnDispatchThreadOrNull(() -> vcsRootComboBox.getSelectedRepository().getOrNull())),
        Case($(), (Object) null));
  }

  @UIEffect
  private ActionToolbar createGitMacheteVerticalToolbar() {
    var actionManager = ActionManager.getInstance();
    var toolbarActionGroup = (ActionGroup) actionManager.getAction(ActionGroupIds.ACTION_GROUP_TOOLBAR);
    var toolbar = actionManager.createActionToolbar(ActionPlaces.ACTION_PLACE_TOOLBAR, toolbarActionGroup,
        /* horizontal */ false);
    toolbar.setTargetComponent(graphTable);
    return toolbar;
  }
}
