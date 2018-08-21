package se.lnu.siq.asm_gs_test.experiments;

import org.graphstream.graph.Graph;
import se.lnu.siq.asm_gs_test.cmd.HuGMe;
import se.lnu.siq.asm_gs_test.cmd.LoadJar;
import se.lnu.siq.asm_gs_test.cmd.Selector;
import se.lnu.siq.asm_gs_test.cmd.ShowNode;

import java.io.IOException;

public class TeamMates1 {

    private final String m_metricTag = "metric";

    public void run(Graph a_g) {

        load(a_g);
        HuGMe.ArchDef arch = createAndMapArch(a_g);
        assignMetric();
        assignInitialClusters(a_g);
    }


    private void assignMetric() {

    }

    private void assignInitialClusters(Graph a_g) {

    }

    private HuGMe.ArchDef.Component createAndMapComponent(Graph a_g, HuGMe.ArchDef a_ad, String a_componentName, String a_package) {
        HuGMe.ArchDef.Component c = a_ad.addComponent(a_componentName);
        c.mapToNodes(a_g, new Selector.Pkg(a_package));
        return c;
    }

    private HuGMe.ArchDef createAndMapArch(Graph a_g) {
        HuGMe.ArchDef ad = new HuGMe.ArchDef();

        HuGMe.ArchDef.Component commonUtil = createAndMapComponent(a_g, ad, "common.util","teammates/common/util/");
        HuGMe.ArchDef.Component commonException = createAndMapComponent(a_g, ad,"common.exception", "teammates/common/exception/");
        HuGMe.ArchDef.Component commonDataTransfer = createAndMapComponent(a_g, ad,"common.datatransfer", "teammates/common/datatransfer/");

        HuGMe.ArchDef.Component uiAutomated = createAndMapComponent(a_g, ad,"ui.automated","teammates/ui/automated/");
        HuGMe.ArchDef.Component uiController = createAndMapComponent(a_g, ad,"ui.controller","teammates/ui/controller/");
        HuGMe.ArchDef.Component uiView = createAndMapComponent(a_g, ad, "ui.view", "teammates/ui/view/");

        HuGMe.ArchDef.Component logicCore = createAndMapComponent(a_g, ad,"logic.core", "teammates/logic/core/");
        HuGMe.ArchDef.Component logicApi = createAndMapComponent(a_g, ad,"logic.api", "teammates/logic/api/");
        HuGMe.ArchDef.Component logicBackdoor = createAndMapComponent(a_g, ad, "logic.backdoor", "teammates/logic/backdoor/");

        HuGMe.ArchDef.Component storageEntity = createAndMapComponent(a_g, ad,"storage.entity", "teammates/storage/entity/");
        HuGMe.ArchDef.Component storageApi = createAndMapComponent(a_g, ad,"storage.api", "teammates/storage/api/");
        HuGMe.ArchDef.Component storageSearch = createAndMapComponent(a_g, ad,"storage.search", "teammates/storage/search/");

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

    private void load(Graph a_g) {
        {
            LoadJar c = new LoadJar("data/teammatesV5.110.jar", "");
            try {
                c.run(a_g);
            } catch (IOException e) {
                System.out.println(e);
                //return false;
            }
        }

        //hide NOT pkg:/jabref/
        {
            //ShowNode c = new ShowNode(new Selector.Not(new Selector.Pkg("teammates/")), false);
            //c.run(a_g);
        }

       // return true;
    }
}
