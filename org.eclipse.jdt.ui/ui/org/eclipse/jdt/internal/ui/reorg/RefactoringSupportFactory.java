/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.reorg;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.corext.refactoring.base.Refactoring;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameCompilationUnitRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameJavaProjectRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameMethodRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenamePackageRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameResourceRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameSourceFolderRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.tagging.IRenameRefactoring;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringWizard;
import org.eclipse.jdt.internal.ui.refactoring.RenameRefactoringWizard;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringAction;

public class RefactoringSupportFactory {

	private abstract static class RenameSupport implements IRefactoringRenameSupport {

		private IRenameRefactoring fRefactoring;

		public boolean canRename(Object element) throws JavaModelException{
			fRefactoring= createRefactoring(element);
			boolean canRename= canAddToMenu(fRefactoring);
			 if (!canRename)	
			 	fRefactoring= null;
			 return canRename;	
		}
		
		public void rename(Object element) throws JavaModelException{
			Assert.isNotNull(fRefactoring);
			RefactoringWizard wizard= createWizard(fRefactoring);
			if (wizard != null)
				RefactoringAction.activateRefactoringWizard((Refactoring)fRefactoring, wizard, "Rename", true);
			else	
				RefactoringAction.activateRenameRefactoringDialog(fRefactoring, "Rename", getNameEntryMessage(), false, element);
			fRefactoring= null;
		}
		
		abstract IRenameRefactoring createRefactoring(Object element) throws JavaModelException;

		RefactoringWizard createWizard(IRenameRefactoring ref){
			return null;
		}
				
		abstract boolean canAddToMenu(IRenameRefactoring refactoring) throws JavaModelException;

		String getNameEntryMessage(){
			return "";
		}
	}
	
	private static RefactoringWizard createRenameWizard(IRenameRefactoring ref, String title, String message, String wizardPageHelp, String errorPageHelp, ImageDescriptor image){
		RenameRefactoringWizard w= new RenameRefactoringWizard(ref, title, message, wizardPageHelp, errorPageHelp);
		w.setInputPageImageDescriptor(image);
		return w;
	}
	
	private static RenameSupport createJavaProjectRename(){
		return new RenameSupport(){
			IRenameRefactoring createRefactoring(Object element) {
				return new RenameJavaProjectRefactoring((IJavaProject)element);
			}
			public boolean canAddToMenu(IRenameRefactoring refactoring) throws JavaModelException{
				return ((RenameJavaProjectRefactoring)refactoring).checkActivation(new NullProgressMonitor()).isOK();
			}
			String getNameEntryMessage(){
				return "Enter the new name for this Java project:";
			}	
		};
	}
	
	private static RenameSupport createSourceFolderRename(){
		return new RenameSupport(){
			IRenameRefactoring createRefactoring(Object element) {
				return new RenameSourceFolderRefactoring((IPackageFragmentRoot)element);
			}
			public boolean canAddToMenu(IRenameRefactoring refactoring) throws JavaModelException{
				return ((RenameSourceFolderRefactoring)refactoring).checkActivation(new NullProgressMonitor()).isOK();
			}
			String getNameEntryMessage(){
				return "Enter the new name for this source folder:";
			}
		};
	}
	
	private static RenameSupport createResourceRename(){
		return new RenameSupport(){
			IRenameRefactoring createRefactoring(Object element) {
				return new RenameResourceRefactoring((IResource)element);
			}
			public boolean canAddToMenu(IRenameRefactoring refactoring) throws JavaModelException{
				return ((RenameResourceRefactoring)refactoring).checkActivation(new NullProgressMonitor()).isOK();
			}
			String getNameEntryMessage(){
				return "Enter the new name for this resource:";
			}
		};
	}

	private static RenameSupport createPackageRename(){
		return new RenameSupport(){
			IRenameRefactoring createRefactoring(Object element) {
				return new RenamePackageRefactoring((IPackageFragment)element);
			}
			public boolean canAddToMenu(IRenameRefactoring refactoring) throws JavaModelException{
				return ((RenamePackageRefactoring)refactoring).checkActivation(new NullProgressMonitor()).isOK();
			}
			RefactoringWizard createWizard(IRenameRefactoring refactoring) {
				String title= "Rename Package";
				String message= "Enter the new name for this package. References to all types declared in it will be updated.";
				String wizardPageHelp= IJavaHelpContextIds.RENAME_PACKAGE_WIZARD_PAGE; 
				String errorPageHelp= IJavaHelpContextIds.RENAME_PACKAGE_ERROR_WIZARD_PAGE;
				ImageDescriptor imageDesc= JavaPluginImages.DESC_WIZBAN_REFACTOR_PACKAGE;
				return createRenameWizard(refactoring, title, message, wizardPageHelp, errorPageHelp, imageDesc);
			}
		};
	}
	
	private static RenameSupport createCompilationUnitRename(){
		return new RenameSupport(){
			IRenameRefactoring createRefactoring(Object element) {
				ICompilationUnit cu= (ICompilationUnit)element;
				if (cu.isWorkingCopy())
					return new RenameCompilationUnitRefactoring((ICompilationUnit)cu.getOriginalElement());
				return new RenameCompilationUnitRefactoring(cu);	
			}
			public boolean canAddToMenu(IRenameRefactoring refactoring) throws JavaModelException{
				return ((RenameCompilationUnitRefactoring)refactoring).checkPreactivation().isOK();
			}
			RefactoringWizard createWizard(IRenameRefactoring refactoring) {
				String title= "Rename Compilation Unit";
				String message= "Enter the new name for this compilation unit. Refactoring will also rename and update references to the type (if any exists) that has the same name as this compilation unit.";
				String wizardPageHelp= IJavaHelpContextIds.RENAME_CU_WIZARD_PAGE; 
				String errorPageHelp= IJavaHelpContextIds.RENAME_CU_ERROR_WIZARD_PAGE;
				ImageDescriptor imageDesc= JavaPluginImages.DESC_WIZBAN_REFACTOR_CU;
				return createRenameWizard(refactoring, title, message, wizardPageHelp, errorPageHelp, imageDesc);
			}
		};
	}
		
	private static RenameSupport createTypeRename(){
		return new RenameSupport(){
			IRenameRefactoring createRefactoring(Object element) {
				return new RenameTypeRefactoring((IType)element);
			}
			public boolean canAddToMenu(IRenameRefactoring refactoring) throws JavaModelException{
				return ((RenameTypeRefactoring)refactoring).checkPreactivation().isOK();
			}
			RefactoringWizard createWizard(IRenameRefactoring refactoring) {
				String title= RefactoringMessages.getString("RefactoringGroup.rename_type_title"); //$NON-NLS-1$
				String message= RefactoringMessages.getString("RefactoringGroup.rename_type_message"); //$NON-NLS-1$
				String wizardPageHelp= IJavaHelpContextIds.RENAME_TYPE_WIZARD_PAGE; 
				String errorPageHelp= IJavaHelpContextIds.RENAME_TYPE_ERROR_WIZARD_PAGE;
				ImageDescriptor imageDesc= JavaPluginImages.DESC_WIZBAN_REFACTOR_TYPE;
				return createRenameWizard(refactoring, title, message, wizardPageHelp, errorPageHelp, imageDesc);
			}
		};
	}
	
	private static RenameSupport createMethodRename(){
		return new RenameSupport(){
			IRenameRefactoring createRefactoring(Object element) throws JavaModelException{
				return RenameMethodRefactoring.createInstance((IMethod)element);
			}
			public boolean canAddToMenu(IRenameRefactoring refactoring) throws JavaModelException{
				return ((RenameMethodRefactoring)refactoring).checkPreactivation().isOK();
			}
			RefactoringWizard createWizard(IRenameRefactoring refactoring) {
				String title= RefactoringMessages.getString("RefactoringGroup.rename_method_title"); //$NON-NLS-1$
				String message= RefactoringMessages.getString("RefactoringGroup.rename_method_message"); //$NON-NLS-1$
				String wizardPageHelp= IJavaHelpContextIds.RENAME_METHOD_WIZARD_PAGE;
				String errorPageHelp= IJavaHelpContextIds.RENAME_METHOD_ERROR_WIZARD_PAGE;
				ImageDescriptor imageDesc= JavaPluginImages.DESC_WIZBAN_REFACTOR_METHOD;
				return createRenameWizard(refactoring, title, message, wizardPageHelp, errorPageHelp, imageDesc);
			}	
		};
	}
	
	private static RenameSupport createFieldRename(){
		return new RenameSupport(){
			IRenameRefactoring createRefactoring(Object element) {
				return new RenameFieldRefactoring((IField)element);
			}
			public boolean canAddToMenu(IRenameRefactoring refactoring) throws JavaModelException{
				return ((RenameFieldRefactoring)refactoring).checkPreactivation().isOK();
			}
			RefactoringWizard createWizard(IRenameRefactoring refactoring){
				String title= RefactoringMessages.getString("RefactoringGroup.rename_field_title"); //$NON-NLS-1$
				String message= RefactoringMessages.getString("RefactoringGroup.rename_field_message"); //$NON-NLS-1$
				String wizardPageHelp= IJavaHelpContextIds.RENAME_FIELD_WIZARD_PAGE; 
				String errorPageHelp= IJavaHelpContextIds.RENAME_FIELD_ERROR_WIZARD_PAGE;
				//XXX: missing icon for field
				ImageDescriptor imageDesc= JavaPluginImages.DESC_WIZBAN_REFACTOR_CU;
				return createRenameWizard(refactoring, title, message, wizardPageHelp, errorPageHelp, imageDesc);
			}	
		};
	}

	public static IRefactoringRenameSupport createRenameSupport(Object element) {
		if (element instanceof IResource)
			return createResourceRename();
		
		if (!(element instanceof IJavaElement))
			return null;
			
		switch (((IJavaElement)element).getElementType()){
			
			case IJavaElement.PACKAGE_FRAGMENT:
					return createPackageRename();
					
			case IJavaElement.COMPILATION_UNIT: 
				return createCompilationUnitRename();
				
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				 return createSourceFolderRename();
				
			case IJavaElement.JAVA_PROJECT:	 
				return createJavaProjectRename();
				 
			case IJavaElement.TYPE:
				return createTypeRename();
			
			case IJavaElement.METHOD:
				return createMethodRename();
				
			case IJavaElement.FIELD:
				return createFieldRename();
				
			default: 	
				return null;	
		}	
	}
}
