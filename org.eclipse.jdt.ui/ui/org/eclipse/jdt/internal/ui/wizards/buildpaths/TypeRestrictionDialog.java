/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.wizards.buildpaths;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.PlatformUI;

import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.corext.util.Messages;

import org.eclipse.jdt.ui.JavaElementLabels;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;

public class TypeRestrictionDialog extends StatusDialog {
	
	private static class TypeRestrictionLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		public TypeRestrictionLabelProvider() {
		}
		
		public Image getImage(Object element) {
			if (element instanceof IAccessRule) {
				IAccessRule rule= (IAccessRule) element;
				switch (rule.getKind()) {
					case IAccessRule.K_ACCESSIBLE:
						return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_NLS_TRANSLATE);
					case IAccessRule.K_DISCOURAGED:
						return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_REFACTORING_WARNING);
					case IAccessRule.K_NON_ACCESSIBLE:
						return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_REFACTORING_ERROR);
				}
			}
			return null;
		}

		public String getText(Object element) {
			StringBuffer buf= new StringBuffer();
			if (element instanceof IAccessRule) {
				IAccessRule rule= (IAccessRule) element;
				buf.append(rule.getPattern().toString()).append(": "); //$NON-NLS-1$
				
				switch (rule.getKind()) {
					case IAccessRule.K_ACCESSIBLE:
						buf.append(NewWizardMessages.TypeRestrictionDialog_kind_accessible); 
						break;
					case IAccessRule.K_DISCOURAGED:
						buf.append(NewWizardMessages.TypeRestrictionDialog_kind_discouraged); 
						break;
					case IAccessRule.K_NON_ACCESSIBLE:
						buf.append(NewWizardMessages.TypeRestrictionDialog_kind_non_accessible); 
						break;
				}
			}
			return buf.toString();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return getImage(element);
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IAccessRule) {
				IAccessRule rule= (IAccessRule) element;
				if (columnIndex == 0) {
					return getResolutionLabel(rule.getKind());
				} else {
					return rule.getPattern().toString();
				}
			}
			return element.toString();
		}
		
		public static String getResolutionLabel(int kind) {
			switch (kind) {
				case IAccessRule.K_ACCESSIBLE:
					return NewWizardMessages.TypeRestrictionDialog_kind_accessible; 
				case IAccessRule.K_DISCOURAGED:
					return NewWizardMessages.TypeRestrictionDialog_kind_discouraged; 
				case IAccessRule.K_NON_ACCESSIBLE:
					return NewWizardMessages.TypeRestrictionDialog_kind_non_accessible; 
			}
			return ""; //$NON-NLS-1$
		}
	}
	
	private ListDialogField fAccessRulesList;
	private SelectionButtonDialogField fCombineRulesCheckbox;
	private CPListElement fCurrElement;
	
	private static final int IDX_ADD= 0;
	private static final int IDX_EDIT= 1;
	private static final int IDX_UP= 3;
	private static final int IDX_DOWN= 4;
	private static final int IDX_REMOVE= 6;
	
	public TypeRestrictionDialog(Shell parent, CPListElement entryToEdit) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		fCurrElement= entryToEdit;

		setTitle(NewWizardMessages.TypeRestrictionDialog_title); 
		
		fAccessRulesList= createListContents(entryToEdit);
		
		fCombineRulesCheckbox= new SelectionButtonDialogField(SWT.CHECK);
		fCombineRulesCheckbox.setLabelText(NewWizardMessages.TypeRestrictionDialog_combine_label); 
	}
	
	
	private ListDialogField createListContents(CPListElement entryToEdit) {
		String label= NewWizardMessages.TypeRestrictionDialog_rules_label; 
		String[] buttonLabels= new String[] {
				NewWizardMessages.TypeRestrictionDialog_rules_add, 
				NewWizardMessages.TypeRestrictionDialog_rules_edit, 
				null,
				NewWizardMessages.TypeRestrictionDialog_rules_up, 
				NewWizardMessages.TypeRestrictionDialog_rules_down, 
				null,
				NewWizardMessages.TypeRestrictionDialog_rules_remove
		};
		
		TypeRestrictionAdapter adapter= new TypeRestrictionAdapter();
		TypeRestrictionLabelProvider labelProvider= new TypeRestrictionLabelProvider();
		
		ListDialogField patternList= new ListDialogField(adapter, buttonLabels, labelProvider);
		patternList.setDialogFieldListener(adapter);

		patternList.setLabelText(label);
		patternList.setRemoveButtonIndex(IDX_REMOVE);
		patternList.setUpButtonIndex(IDX_UP);
		patternList.setDownButtonIndex(IDX_DOWN);
		patternList.enableButton(IDX_EDIT, false);
	
		IAccessRule[] rules= (IAccessRule[]) entryToEdit.getAttribute(CPListElement.ACCESSRULES);
		ArrayList elements= new ArrayList(rules.length);
		for (int i= 0; i < rules.length; i++) {
			elements.add(rules[i]);
		}
		patternList.setElements(elements);
		patternList.selectFirstElement();
		return patternList;
	}


	protected Control createDialogArea(Composite parent) {
		Composite composite= (Composite) super.createDialogArea(parent);
				
		int maxLabelSize= 0;
		GC gc= new GC(composite.getDisplay());
		try {
			maxLabelSize= gc.textExtent(TypeRestrictionLabelProvider.getResolutionLabel(IAccessRule.K_ACCESSIBLE)).x;
			int len2= gc.textExtent(TypeRestrictionLabelProvider.getResolutionLabel(IAccessRule.K_DISCOURAGED)).x;
			if (len2 > maxLabelSize) {
				maxLabelSize= len2;
			}
			int len3= gc.textExtent(TypeRestrictionLabelProvider.getResolutionLabel(IAccessRule.K_NON_ACCESSIBLE)).x;
			if (len3 > maxLabelSize) {
				maxLabelSize= len3;
			}
		} finally {
			gc.dispose();
		}
		
		ColumnLayoutData[] columnDta= new ColumnLayoutData[] {
				new ColumnPixelData(maxLabelSize + 40),
				new ColumnWeightData(1),
		};
		fAccessRulesList.setTableColumns(new ListDialogField.ColumnsDescription(columnDta, null, false));
		

		Composite inner= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		inner.setLayout(layout);
		inner.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label description= new Label(inner, SWT.WRAP);

		description.setText(getDescriptionString()); 
		
		GridData data= new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		data.widthHint= convertWidthInCharsToPixels(70);
		description.setLayoutData(data);
		
		fAccessRulesList.doFillIntoGrid(inner, 3);
				
		LayoutUtil.setHorizontalSpan(fAccessRulesList.getLabelControl(null), 2);
		
		data= (GridData) fAccessRulesList.getListControl(null).getLayoutData();
		data.grabExcessHorizontalSpace= true;
		data.heightHint= SWT.DEFAULT;
		
		if (fCurrElement.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
			fCombineRulesCheckbox.doFillIntoGrid(inner, 2);
		}
		
		applyDialogFont(composite);		
		return composite;
	}
	
	private String getDescriptionString() {
		String desc;
		String name= fCurrElement.getPath().lastSegment();
		switch (fCurrElement.getEntryKind()) {
			case IClasspathEntry.CPE_CONTAINER:
				try {
					name= JavaElementLabels.getContainerEntryLabel(fCurrElement.getPath(), fCurrElement.getJavaProject());
				} catch (JavaModelException e) {
				}
				desc= NewWizardMessages.TypeRestrictionDialog_container_description;
				break;
			case IClasspathEntry.CPE_PROJECT:
				desc=  NewWizardMessages.TypeRestrictionDialog_project_description;
				break;
			default:
				desc=  NewWizardMessages.TypeRestrictionDialog_description;
		}
		
		return Messages.format(desc, name);
	}


	protected void doCustomButtonPressed(ListDialogField field, int index) {
		if (index == IDX_ADD) {
			addEntry(field);
		} else if (index == IDX_EDIT) {
			editEntry(field);
		}
	}
	
	protected void doDoubleClicked(ListDialogField field) {
		editEntry(field);
	}
	
	protected void doSelectionChanged(ListDialogField field) {
		List selected= field.getSelectedElements();
		field.enableButton(IDX_EDIT, canEdit(selected));
	}
	
	private boolean canEdit(List selected) {
		return selected.size() == 1;
	}
	
	private void editEntry(ListDialogField field) {
		
		List selElements= field.getSelectedElements();
		if (selElements.size() != 1) {
			return;
		}
		IAccessRule rule= (IAccessRule) selElements.get(0);
		TypeRestrictionEntryDialog dialog= new TypeRestrictionEntryDialog(getShell(), rule, fCurrElement);
		if (dialog.open() == Window.OK) {
			field.replaceElement(rule, dialog.getRule());
		}
	}

	private void addEntry(ListDialogField field) {
		TypeRestrictionEntryDialog dialog= new TypeRestrictionEntryDialog(getShell(), null, fCurrElement);
		if (dialog.open() == Window.OK) {
			field.addElement(dialog.getRule());
		}
	}	
	
	
		
	// -------- TypeRestrictionAdapter --------

	private class TypeRestrictionAdapter implements IListAdapter, IDialogFieldListener {
		/**
		 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter#customButtonPressed(org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField, int)
		 */
		public void customButtonPressed(ListDialogField field, int index) {
			doCustomButtonPressed(field, index);
		}

		/**
		 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter#selectionChanged(org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField)
		 */
		public void selectionChanged(ListDialogField field) {
			doSelectionChanged(field);
		}
		/**
		 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter#doubleClicked(org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField)
		 */
		public void doubleClicked(ListDialogField field) {
			doDoubleClicked(field);
		}

		/**
		 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener#dialogFieldChanged(org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField)
		 */
		public void dialogFieldChanged(DialogField field) {
		}
		
	}
	
	protected void doStatusLineUpdate() {
	}		
	
	protected void checkIfPatternValid() {
	}
	
	public IAccessRule[] getAccessRules() {
		List elements= fAccessRulesList.getElements();
		return (IAccessRule[]) elements.toArray(new IAccessRule[elements.size()]);
	}
	
	public boolean doCombineAccessRules() {
		return fCombineRulesCheckbox.isSelected();
	}
	
	/*
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IJavaHelpContextIds.ACCESS_RULES_DIALOG);
	}
}
