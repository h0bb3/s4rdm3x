package se.lnu.siq.s4rdm3x.experiments.system;

import org.graphstream.graph.Graph;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.LoadJar;
import se.lnu.siq.s4rdm3x.cmd.util.Selector;

import java.io.IOException;

public class TeamMates extends System {

    @Override
    public String getName() {
        return "TeamMates";
    }

    @Override
    public HuGMe.ArchDef createAndMapArch(Graph a_g) {
        HuGMe.ArchDef ad = new HuGMe.ArchDef();

        HuGMe.ArchDef.Component commonUtil = createAddAndMapComponent(a_g, ad, "common.util","teammates/common/util/");
        HuGMe.ArchDef.Component commonException = createAddAndMapComponent(a_g, ad,"common.exception", "teammates/common/exception/");
        HuGMe.ArchDef.Component commonDataTransfer = createAddAndMapComponent(a_g, ad,"common.datatransfer", "teammates/common/datatransfer/");

        HuGMe.ArchDef.Component uiAutomated = createAddAndMapComponent(a_g, ad,"ui.automated","teammates/ui/automated/");
        HuGMe.ArchDef.Component uiController = createAddAndMapComponent(a_g, ad,"ui.controller","teammates/ui/controller/");
        HuGMe.ArchDef.Component uiView = createAddAndMapComponent(a_g, ad, "ui.view", new String[]{"teammates/ui/datatransfer/", "teammates/ui/pagedata/", "teammates/ui/template/"});

        HuGMe.ArchDef.Component logicCore = createAddAndMapComponent(a_g, ad,"logic.core", "teammates/logic/core/");
        HuGMe.ArchDef.Component logicApi = createAddAndMapComponent(a_g, ad,"logic.api", "teammates/logic/api/");
        HuGMe.ArchDef.Component logicBackdoor = createAddAndMapComponent(a_g, ad, "logic.backdoor", "teammates/logic/backdoor/");

        HuGMe.ArchDef.Component storageEntity = createAddAndMapComponent(a_g, ad,"storage.entity", "teammates/storage/entity/");
        HuGMe.ArchDef.Component storageApi = createAddAndMapComponent(a_g, ad,"storage.api", "teammates/storage/api/");
        HuGMe.ArchDef.Component storageSearch = createAddAndMapComponent(a_g, ad,"storage.search", "teammates/storage/search/");

        uiAutomated.addDependencyTo(commonUtil);
        uiAutomated.addDependencyTo(commonException);
        uiAutomated.addDependencyTo(commonDataTransfer);
        uiAutomated.addDependencyTo(logicApi);
        uiController.addDependencyTo(commonUtil);
        uiController.addDependencyTo(commonException);
        uiController.addDependencyTo(commonDataTransfer);
        uiController.addDependencyTo(logicApi);
        uiAutomated.addDependencyTo(commonUtil);
        uiAutomated.addDependencyTo(commonException);
        uiAutomated.addDependencyTo(commonDataTransfer);
        uiView.addDependencyTo(commonUtil);
        uiView.addDependencyTo(commonException);
        uiView.addDependencyTo(commonDataTransfer);

        logicCore.addDependencyTo(commonUtil);
        logicCore.addDependencyTo(commonException);
        logicCore.addDependencyTo(commonDataTransfer);
        logicCore.addDependencyTo(storageApi);
        logicApi.addDependencyTo(commonUtil);
        logicApi.addDependencyTo(commonException);
        logicApi.addDependencyTo(commonDataTransfer);
        logicBackdoor.addDependencyTo(commonUtil);
        logicBackdoor.addDependencyTo(commonException);
        logicBackdoor.addDependencyTo(commonDataTransfer);
        logicBackdoor.addDependencyTo(storageApi);

        storageEntity.addDependencyTo(commonUtil);
        storageEntity.addDependencyTo(commonException);
        storageEntity.addDependencyTo(commonDataTransfer);
        storageApi.addDependencyTo(commonUtil);
        storageApi.addDependencyTo(commonException);
        storageApi.addDependencyTo(commonDataTransfer);
        storageSearch.addDependencyTo(commonUtil);
        storageSearch.addDependencyTo(commonException);
        storageSearch.addDependencyTo(commonDataTransfer);

        commonDataTransfer.addDependencyTo(storageEntity);

        return ad;
    }

    @Override
    public boolean load(Graph a_g) {
        LoadJar c = new LoadJar("data/teammatesV5.110.jar", "");
        try {
            c.run(a_g);
        } catch (IOException e) {
            java.lang.System.out.println(e);
            return false;
        }
        return true;
    }

    private HuGMe.ArchDef.Component createAddAndMapComponent(Graph a_g, HuGMe.ArchDef a_ad, String a_componentName, String a_package) {
        HuGMe.ArchDef.Component c = a_ad.addComponent(a_componentName);
        c.mapToNodes(a_g, new Selector.Pkg(a_package));
        return c;
    }
}
