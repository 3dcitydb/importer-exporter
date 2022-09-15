/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2022
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citydb.gui.components;

import org.citydb.util.log.Logger;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FileListTransferHandler extends TransferHandler implements DropTargetListener {
    private final Logger log = Logger.getInstance();
    private final JList<File> fileList;
    private final DefaultListModel<File> model;

    private Mode mode = Mode.FILES_AND_DIRECTORIES;
    private Consumer<DefaultListModel<File>> filesAddedHandler;
    private Consumer<DefaultListModel<File>> filesRemovedHandler;

    public enum Mode {
        FILES_ONLY,
        FILES_AND_DIRECTORIES
    }

    public FileListTransferHandler(JList<File> fileList) {
        this.fileList = fileList;
        model = (DefaultListModel<File>) fileList.getModel();

        ActionMap actionMap = fileList.getActionMap();
        actionMap.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
        actionMap.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
        actionMap.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());

        InputMap inputMap = fileList.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK), TransferHandler.getCutAction().getValue(Action.NAME));
        inputMap.put(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK), TransferHandler.getCopyAction().getValue(Action.NAME));
        inputMap.put(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK), TransferHandler.getPasteAction().getValue(Action.NAME));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), TransferHandler.getCutAction().getValue(Action.NAME));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), TransferHandler.getCutAction().getValue(Action.NAME));

        fileList.setDropTarget(new DropTarget(fileList, this));
    }

    public FileListTransferHandler withMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public FileListTransferHandler withFilesAddedHandler(Consumer<DefaultListModel<File>> filesAddedHandler) {
        this.filesAddedHandler = filesAddedHandler;
        return this;
    }

    public FileListTransferHandler withFilesRemovedHandler(Consumer<DefaultListModel<File>> filesRemovedHandler) {
        this.filesRemovedHandler = filesRemovedHandler;
        return this;
    }

    @Override
    public boolean importData(TransferSupport info) {
        if (info.isDataFlavorSupported(DataFlavor.stringFlavor) && !info.isDrop()) {
            try {
                String value = (String) info.getTransferable().getTransferData(DataFlavor.stringFlavor);
                List<File> files = getFiles(value.split(System.lineSeparator()));
                if (!files.isEmpty()) {
                    addFiles(files);
                    return true;
                }
            } catch (UnsupportedFlavorException | IOException e) {
                //
            }
        }

        return false;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        int[] indices = fileList.getSelectedIndices();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indices.length; i++) {
            builder.append(fileList.getModel().getElementAt(indices[i]));
            if (i < indices.length - 1) {
                builder.append(System.lineSeparator());
            }
        }

        return new StringSelection(builder.toString());
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        if (action == MOVE && !fileList.isSelectionEmpty()) {
            int[] indices = fileList.getSelectedIndices();
            int first = indices[0];

            for (int i = indices.length - 1; i >= 0; i--) {
                model.remove(indices[i]);
            }

            if (!model.isEmpty()) {
                if (first > model.size() - 1) {
                    first = model.size() - 1;
                }

                fileList.setSelectedIndex(first);
            } else if (filesRemovedHandler != null) {
                filesRemovedHandler.accept(model);
            }
        }
    }

    @Override
    public void dragEnter(DropTargetDragEvent event) {
        event.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void drop(DropTargetDropEvent event) {
        for (DataFlavor dataFlavor : event.getCurrentDataFlavors()) {
            if (dataFlavor.isFlavorJavaFileListType()) {
                try {
                    event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    List<File> files = getFiles((List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                    if (!files.isEmpty()) {
                        if (event.getDropAction() != DnDConstants.ACTION_COPY) {
                            model.clear();
                        }

                        addFiles(files);
                    }

                    event.getDropTargetContext().dropComplete(true);
                } catch (UnsupportedFlavorException | IOException e) {
                    //
                }
            }
        }
    }

    private boolean isValidFile(File file) {
        if (!file.exists()) {
            log.warn("'" + file.getAbsolutePath() + "' is not a valid file.");
            return false;
        } else if (mode == Mode.FILES_ONLY && file.isDirectory()) {
            log.warn("'" + file.getAbsolutePath() + "' is a directory but directories are not supported.");
            return false;
        } else {
            return true;
        }
    }

    private List<File> getFiles(String[] candidates) {
        return getFiles(Arrays.stream(candidates)
                .map(File::new)
                .collect(Collectors.toList()));
    }

    private List<File> getFiles(List<File> candidates) {
        return candidates.stream()
                .filter(this::isValidFile)
                .collect(Collectors.toList());
    }

    private void addFiles(List<File> files) {
        int index = fileList.getSelectedIndex() + 1;
        for (File file : files) {
            model.add(index++, file);
        }

        if (filesAddedHandler != null) {
            filesAddedHandler.accept(model);
        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent event) {
    }

    @Override
    public void dragExit(DropTargetEvent event) {
    }

    @Override
    public void dragOver(DropTargetDragEvent event) {
    }
}
