/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2014
 *
 *  [Component.java]
 *  JACPFX Project (https://github.com/JacpFX/JacpFX/)
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 * *********************************************************************
 */
package org.jacp.test.perspectives;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jacp.test.components.ComponentIds;
import org.jacp.test.dialogs.DialogDialogInPerspectiveTest;
import org.jacp.test.dialogs.DialogXMLDialogInPerspectiveTest;
import org.jacp.test.main.ApplicationLauncherDialogInPerspectiveTest;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.lifecycle.OnShow;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.annotations.perspective.Perspective;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.PerspectiveLayout;
import org.jacpfx.rcp.components.managedFragment.ManagedFragmentHandler;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.perspective.FXPerspective;
import org.jacpfx.rcp.util.FXUtil.MessageUtil;

import java.util.ResourceBundle;

/**
 * A simple perspective defining a split pane
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */

@Perspective(id = PerspectiveIds.PerspectiveDialogInPerspectiveTest,
        components = {
                ComponentIds.ComponentDialogInPerspective},
        viewLocation = "/fxml/perspectiveOne.fxml",
        resourceBundleLocation = "bundles.languageBundle",
        localeID = "en_US")
public class PerspectiveDialogInPerspectiveTest implements FXPerspective {
    @FXML
    private HBox content1;
    @FXML
    private HBox content2;
    @FXML
    private HBox content3;

    @Resource
    private static Context context;

    @Override
    public void handlePerspective(final Message<Event, Object> action,
                                  final PerspectiveLayout perspectiveLayout) {
        if (action.messageBodyEquals(MessageUtil.INIT)) {

            //perspectiveLayout.registerRootComponent(createRoot());
            GridPane.setVgrow(perspectiveLayout.getRootComponent(),
                    Priority.ALWAYS);
            GridPane.setHgrow(perspectiveLayout.getRootComponent(),
                    Priority.ALWAYS);

            // register left panel
            perspectiveLayout.registerTargetLayoutComponent("content0",
                    this.content1);
            // perspectiveLayout.registerTargetLayoutComponent("content1",
            //         this.content2);
            // perspectiveLayout.registerTargetLayoutComponent("content2",
            //         this.content3);


            ApplicationLauncherDialogInPerspectiveTest.latch.countDown();
        } else if (action.messageBodyEquals("dialog1")) {
            ManagedFragmentHandler<DialogDialogInPerspectiveTest> handler = context.getManagedFragmentHandler(DialogDialogInPerspectiveTest.class);
            if (handler.getController() != null) {
                DialogDialogInPerspectiveTest.latch.countDown();
            }
            if (handler.getFragmentNode() != null) {
                DialogDialogInPerspectiveTest.latch.countDown();
            }
            handler.getController().init();
            this.content3.getChildren().addAll(handler.getFragmentNode());
        } else if (action.messageBodyEquals("dialog2")) {
            ManagedFragmentHandler<DialogXMLDialogInPerspectiveTest> handler = context.getManagedFragmentHandler(DialogXMLDialogInPerspectiveTest.class);
            if (handler.getController() != null) {
                DialogXMLDialogInPerspectiveTest.latch.countDown();
            }
            if (handler.getFragmentNode() != null) {
                DialogXMLDialogInPerspectiveTest.latch.countDown();
            }
            handler.getController().init();
            this.content2.getChildren().addAll(handler.getFragmentNode());
        }


    }

    public static void initDialog1() {
        context.send("dialog1");
    }

    public static void initDialog2() {
        context.send("dialog2");
    }


    @OnShow
    public void onShow(final FXComponentLayout layout) {

    }

    @PostConstruct
    /**
     * @OnStart annotated method will be executed when component is activated.
     * @param layout
     * @param resourceBundle
     */
    public void onStartPerspective(final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) {

    }

    @PreDestroy
    /**
     * @OnTearDown annotated method will be executed when component is deactivated.
     * @param arg0
     */
    public void onTearDownPerspective(final FXComponentLayout arg0) {
        // remove toolbars and menu entries when close perspective

    }

}
