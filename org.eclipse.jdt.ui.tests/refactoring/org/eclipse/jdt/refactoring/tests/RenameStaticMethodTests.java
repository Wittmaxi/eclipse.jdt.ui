/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.refactoring.tests;

import junit.framework.Test;import junit.framework.TestSuite;import org.eclipse.core.runtime.NullProgressMonitor;import org.eclipse.jdt.core.ICompilationUnit;import org.eclipse.jdt.core.IType;import org.eclipse.jdt.core.Signature;import org.eclipse.jdt.internal.core.refactoring.base.ChangeContext;import org.eclipse.jdt.internal.core.refactoring.base.IRefactoring;import org.eclipse.jdt.internal.core.refactoring.base.Refactoring;import org.eclipse.jdt.internal.core.refactoring.base.RefactoringStatus;import org.eclipse.jdt.internal.core.refactoring.methods.RenameStaticMethodRefactoring;import org.eclipse.jdt.refactoring.tests.infra.TestExceptionHandler;import org.eclipse.jdt.testplugin.JavaTestSetup;import org.eclipse.jdt.testplugin.TestPluginLauncher;import org.eclipse.jdt.testplugin.ui.TestPluginUILauncher;

public class RenameStaticMethodTests extends RefactoringTest {
	private static final String REFACTORING_PATH= "RenameStaticMethod/";

	public RenameStaticMethodTests(String name) {
		super(name);
	}

	public static void main(String[] args) {
		TestPluginUILauncher.run(TestPluginLauncher.getLocationFromProperties(), RenameStaticMethodTests.class, args);
	}
	
	public static Test suite() {
		TestSuite suite= new TestSuite();
		suite.addTest(noSetupSuite());
		return new JavaTestSetup(suite);
	}

	public static Test noSetupSuite() {
		return new TestSuite(RenameStaticMethodTests.class);
	}

	protected String getRefactoringPath() {
		return REFACTORING_PATH;
	}

	private void helper1_0(String methodName, String newMethodName, String[] signatures) throws Exception{
		IType classA= getType(createCUfromTestFile(getPackageP(), "A"), "A");
		RenameStaticMethodRefactoring ref= new RenameStaticMethodRefactoring(fgChangeCreator, classA.getMethod(methodName, signatures));
		ref.setNewName(newMethodName);
		RefactoringStatus result= performRefactoring(ref);
		assertNotNull("precondition was supposed to fail", result);
	}
	
	private void helper1() throws Exception{
		helper1_0("m", "k", new String[0]);
	}
	
	private void helper2_0(String methodName, String newMethodName, String[] signatures) throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType classA= getType(cu, "A");
		RenameStaticMethodRefactoring ref= new RenameStaticMethodRefactoring(fgChangeCreator, classA.getMethod(methodName, signatures));
		ref.setNewName(newMethodName);
		assertEquals("was supposed to pass", null, performRefactoring(ref));
		assertEquals("invalid renaming", getFileContents(getOutputTestFileName("A")), cu.getSource());
		
		assert("anythingToUndo", Refactoring.getUndoManager().anythingToUndo());
		assert("! anythingToRedo", !Refactoring.getUndoManager().anythingToRedo());
		//assertEquals("1 to undo", 1, Refactoring.getUndoManager().getRefactoringLog().size());
		
		Refactoring.getUndoManager().performUndo(new ChangeContext(new TestExceptionHandler()), new NullProgressMonitor());
		assertEquals("invalid undo", getFileContents(getInputTestFileName("A")), cu.getSource());

		assert("! anythingToUndo", !Refactoring.getUndoManager().anythingToUndo());
		assert("anythingToRedo", Refactoring.getUndoManager().anythingToRedo());
		//assertEquals("1 to redo", 1, Refactoring.getUndoManager().getRedoStack().size());
		
		Refactoring.getUndoManager().performRedo(new ChangeContext(new TestExceptionHandler()), new NullProgressMonitor());
		assertEquals("invalid redo", getFileContents(getOutputTestFileName("A")), cu.getSource());
	}
	
	private void helper2() throws Exception{
		helper2_0("m", "k", new String[0]);
	}
	

	/********** tests *********/
	public void testFail0() throws Exception{
		helper1();
	}
	
	public void testFail1() throws Exception{
		helper1();
	}
	
	public void testFail2() throws Exception{
		helper1();
	}
	
	public void testFail3() throws Exception{
		helper1();
	}
	
	public void testFail4() throws Exception{
		helper1();
	}
	
	public void testFail5() throws Exception{
		helper1();
	}
	
	public void testFail6() throws Exception{
		helper1();
	}
	
	public void testFail7() throws Exception{
		helper1();
	}
	
	public void testFail8() throws Exception{
		helper1();
	}
	
	public void test0() throws Exception{
		helper2();
	}
	
	public void test1() throws Exception{
		helper2();
	}
	
	public void test2() throws Exception{
		helper2();
	}
	
	public void test3() throws Exception{
		helper2();
	}
	
	public void test4() throws Exception{
		helper2();
	}
	
	public void test5() throws Exception{
		helper2();
	}
	
	public void test6() throws Exception{
		helper2();
	}
	
	public void test7() throws Exception{
		helper2_0("m", "k", new String[]{Signature.SIG_INT});
	}
	
	public void test8() throws Exception{
		helper2_0("m", "k", new String[]{Signature.SIG_INT});
	}
}
