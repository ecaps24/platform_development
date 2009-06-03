/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdkuilib.internal.repository;

import com.android.sdklib.internal.repository.IDescription;
import com.android.sdklib.internal.repository.ITask;
import com.android.sdklib.internal.repository.ITaskMonitor;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/*
 * TODO list
 * - select => update desc, enable update + delete, enable home page if url
 * - home page callback
 * - update callback
 * - delete callback
 * - refresh callback
 */

public class LocalPackagesPage extends Composite {
    private UpdaterData mUpdaterData;

    private Label mSdkLocLabel;
    private Text mSdkLocText;
    private Button mSdkLocBrowse;
    private TableViewer mTableViewerPackages;
    private Table mTablePackages;
    private TableColumn mColumnPackages;
    private Group mDescriptionContainer;
    private Composite mContainerButtons;
    private Button mUpdateButton;
    private Label mPlaceholder1;
    private Button mDeleteButton;
    private Label mPlaceholder2;
    private Button mHomePageButton;
    private Label mDescriptionLabel;

    /**
     * Create the composite.
     * @param parent The parent of the composite.
     * @param updaterData An instance of {@link UpdaterData}. If null, a local
     *        one will be allocated just to help with the SWT Designer.
     */
    public LocalPackagesPage(Composite parent, UpdaterData updaterData) {
        super(parent, SWT.BORDER);

        mUpdaterData = updaterData != null ? updaterData : new UpdaterData();

        createContents(this);
        postCreate();  //$hide$
    }

    private void createContents(Composite parent) {
        parent.setLayout(new GridLayout(3, false));

        createSdkLocation(parent);

        mTableViewerPackages = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
        mTablePackages = mTableViewerPackages.getTable();
        mTablePackages.setHeaderVisible(true);
        mTablePackages.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        mTablePackages.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onTreeSelected(); //$hide$
            }
        });

        mColumnPackages = new TableColumn(mTablePackages, SWT.NONE);
        mColumnPackages.setWidth(377);
        mColumnPackages.setText("Installed Packages");

        mDescriptionContainer = new Group(parent, SWT.NONE);
        mDescriptionContainer.setLayout(new GridLayout(1, false));
        mDescriptionContainer.setText("Description");
        mDescriptionContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

        mDescriptionLabel = new Label(mDescriptionContainer, SWT.NONE);
        mDescriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
        mDescriptionLabel.setText("Line1\nLine2\nLine3");

        mContainerButtons = new Composite(parent, SWT.NONE);
        mContainerButtons.setLayout(new GridLayout(5, false));
        mContainerButtons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

        mUpdateButton = new Button(mContainerButtons, SWT.NONE);
        mUpdateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onUpdateInstalledPackage();  //$hide$ (hide from SWT designer)
            }
        });
        mUpdateButton.setText("Update...");

        mPlaceholder1 = new Label(mContainerButtons, SWT.NONE);
        mPlaceholder1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

        mDeleteButton = new Button(mContainerButtons, SWT.NONE);
        mDeleteButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        mDeleteButton.setText("Delete...");

        mPlaceholder2 = new Label(mContainerButtons, SWT.NONE);
        mPlaceholder2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

        mHomePageButton = new Button(mContainerButtons, SWT.NONE);
        mHomePageButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        mHomePageButton.setText("Home Page...");
    }

    private void createSdkLocation(Composite parent) {
        mSdkLocLabel = new Label(parent, SWT.NONE);
        mSdkLocLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        mSdkLocLabel.setText("SDK Location:");

        // If the sdk path is not user-customizable, do not create
        // the browse button and use horizSpan=2 on the text field.

        mSdkLocText = new Text(parent, SWT.BORDER);
        mSdkLocText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        if (mUpdaterData.canUserChangeSdkRoot()) {
            mSdkLocBrowse = new Button(parent, SWT.NONE);
            mSdkLocBrowse.setText("Browse...");
        } else {
            mSdkLocText.setEditable(false);
            ((GridData)mSdkLocText.getLayoutData()).horizontalSpan++;
        }

        if (mUpdaterData.getOsSdkRoot() != null) {
            mSdkLocText.setText(mUpdaterData.getOsSdkRoot());
        }
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    // -- Start of internal part ----------
    // Hide everything down-below from SWT designer
    //$hide>>$

    /**
     * Must be called once to set the adapter input for the package table viewer.
     */
    public void setInput(LocalSdkAdapter localSdkAdapter) {
        mTableViewerPackages.setLabelProvider(  localSdkAdapter.getLabelProvider());
        mTableViewerPackages.setContentProvider(localSdkAdapter.getContentProvider());
        mTableViewerPackages.setInput(localSdkAdapter);
        onTreeSelected();
    }

    /**
     * Called by the constructor right after {@link #createContents(Composite)}.
     */
    private void postCreate() {
        adjustColumnsWidth();
    }

    /**
     * Adds a listener to adjust the columns width when the parent is resized.
     * <p/>
     * If we need something more fancy, we might want to use this:
     * http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet77.java?view=co
     */
    private void adjustColumnsWidth() {
        // Add a listener to resize the column to the full width of the table
        mTablePackages.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Rectangle r = mTablePackages.getClientArea();
                mColumnPackages.setWidth(r.width);
            }
        });
    }

    /**
     * Called when an item in the package table viewer is selected.
     * If the items is an {@link IDescription} (as it should), this will display its long
     * description in the description area. Otherwise when the item is not of the expected
     * type or there is no selection, it empties the description area.
     */
    private void onTreeSelected() {
        ISelection sel = mTableViewerPackages.getSelection();
        if (sel instanceof IStructuredSelection) {
            Object elem = ((IStructuredSelection) sel).getFirstElement();
            if (elem instanceof IDescription) {
                mDescriptionLabel.setText(((IDescription) elem).getLongDescription());
                mDescriptionContainer.layout(true);
                return;
            }
        }
        mDescriptionLabel.setText("");  //$NON-NLS1-$
    }

    private void onUpdateInstalledPackage() {
        // TODO just a test, needs to be removed later.
        new ProgressTask(getShell(), "Test", new ITask() {
            public void run(ITaskMonitor monitor) {
                monitor.setDescription("Test");
                monitor.setProgressMax(100);
                int n = 0;
                int d = 1;
                while(!monitor.cancelRequested()) {
                    monitor.incProgress(d);
                    n += d;
                    if (n == 0 || n == 100) d = -d;
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        });
    }

    // End of hiding from SWT Designer
    //$hide<<$
}
