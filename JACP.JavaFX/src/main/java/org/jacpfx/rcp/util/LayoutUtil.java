/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [LayoutUtil.java]
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

package org.jacpfx.rcp.util;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * The Class LayoutUtil.
 *
 * Util to group some Layout-Function.
 *
 * @author Patrick Symmangk
 *
 */
public class LayoutUtil {

    public static class GridPaneUtil {


        /**
         *
         * Set GridPane hGrow AND vGrow to multiple Nodes
         *
         * @param priority - priority to set
         * @param nodes - the nodes
         */
        public static void setFullGrow(Priority priority, Node... nodes) {
            for (Node node : nodes) {
                if (node != null) {
                    GridPane.setVgrow(node, priority);
                    GridPane.setHgrow(node, priority);
                }
            }
        }

        /**
         *
         * Set GridPane hGrow to multiple Nodes
         *
         * @param priority - priority to set
         * @param nodes - the nodes
         */
        public static void setHGrow(Priority priority, Node... nodes) {
            for (Node node : nodes) {
                if (node != null) {
                    GridPane.setHgrow(node, priority);
                }
            }
        }


        /**
         *
         * Set GridPane vGrow to multiple Nodes
         *
         * @param priority - priority to set
         * @param nodes - the nodes
         */
        public static void setVGrow(Priority priority, Node... nodes) {
            for (Node node : nodes) {
                if (node != null) {
                    GridPane.setVgrow(node, priority);
                }
            }
        }
        
    }

    /**
     * The HBoxUtil subclass.
     *
     */
    public static class HBoxUtil {

        /**
         *
         * Set HBox hGrow to multiple Nodes
         *
         * @param priority - priority to set
         * @param nodes - the nodes
         */
        public static void setHGrow(Priority priority, Node... nodes) {
            for (Node node : nodes) {
                if (node != null) {
                    HBox.setHgrow(node, priority);
                }
            }
        }

        /**
         *
         * Set margin to multiple Nodes.
         *
         * @param insets - the margin insets
         * @param nodes - the nodes to receive the margin
         */
        public static void setMargin(Insets insets, Node... nodes) {
            setMargin(insets, Arrays.asList(nodes));

        }

        /**
         *
         * Set margin to a collection of Nodes.
         *
         * @param insets - the margin insets
         * @param nodes - the nodes to receive the margin
         */
        public static void setMargin(Insets insets, Collection<Node> nodes) {
            nodes.stream().filter(node -> node != null).forEach(node -> HBox.setMargin(node, insets));
        }

    }

    /**
     * The VBoxUtil subclass.
     *
     */
    public static class VBoxUtil {

        /**
         *
         * Set VBox vGrow to multiple Nodes
         *
         * @param priority - priority to set
         * @param nodes - the nodes
         */
        public static void setVGrow(Priority priority, Node... nodes) {
            for (Node node : nodes) {
                if (node != null) {
                    VBox.setVgrow(node, priority);
                }
            }
        }

        /**
         *
         * Set margin to multiple Nodes.
         *
         * @param insets - the margin insets
         * @param nodes - the nodes to receive the margin
         */
        public static void setMargin(Insets insets, Node... nodes) {
            setMargin(insets, Arrays.asList(nodes));

        }

        /**
         *
         * Set margin to a collection of Nodes.
         *
         * @param insets - the margin insets
         * @param nodes - the nodes to receive the margin
         */
        public static void setMargin(Insets insets, Collection<Node> nodes) {
            nodes.stream().filter(node -> node != null).forEach(node -> VBox.setMargin(node, insets));
        }
    }

    public static void hideAllChildren(Region parent){
        for (Iterator<Node> iterator = parent.getChildrenUnmodifiable().iterator(); iterator.hasNext(); ) {
            Node node = iterator.next();
            node.setVisible(false);
        }
    }
}
