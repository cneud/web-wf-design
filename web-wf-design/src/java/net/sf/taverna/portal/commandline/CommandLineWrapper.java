package net.sf.taverna.portal.commandline;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import net.sf.taverna.portal.baclava.DataThingBasedBaclava;
import net.sf.taverna.portal.workflow.TavernaWorkflow;
import net.sf.taverna.portal.workflow.XMLBasedT2Flow;

/**
 * Method that wraps the command line tool.
 * 
 * Allows the user to call the various methods to set the parameters before run a Workflow.
 * <p>
 * Minimal checking is done, but there is no guarantee that just because variable setting did not thrown an exception 
 *    that the workflow will run successfully.
 * @author Christian
 */
public class CommandLineWrapper extends ChangeFirer{
    
    private File tavernaHome;
    
    private File outputRoot;
    
    private String workflowURI;
    
    private String workflowName;
    
    private Map<String,Integer>inputs;
    
    private String inputsURI;
    
    private TavernaInput[] inputValues;
    
    
    /**
     * Creates an empty Command Line wrapper,
     */
    public CommandLineWrapper(){
        clearWorkflow();
    }
    
   /**
    * Sets the directory in which the Taverna Command Line tool can be found.
    * 
    * Carries out a minimal validation that the directory at least exists and contains a file with the expected name.
    * The lack of an exception is not a guarantee that the command line tool will run.
    * <p>
    * Fires a ChangeEvent if successful.
    * 
    * @param tavernaHome Directory that contains the scripts to run the taverna command line.
    *    May not be null.
    * @throws Exception Normally caused by null or an unusable location. 
    * This includes a location that can not be found, one that can not be read, one that can executed or 
    * one that does not contain the expected script file, or where the script is not executable.
    */
    public void setTavernaHome(File newHome) throws IOException, TavernaException{
        Utils.checkDirectory(newHome);
        //File libDir = new File (newHome,"../lib");
        //Utils.checkDirectory(libDir);
        File tavernaLaunch = CommandLineRun.getLaunchFile(newHome);
        if (!tavernaLaunch.exists()){
            throw new TavernaException (newHome.getAbsolutePath() + 
                    " does not contain an \"" + tavernaLaunch + "\" File.");
        }
        Utils.checkFileExecutable(tavernaLaunch);
        this.tavernaHome = newHome;
        fireStateChanged();
    }

    /* Retrieves a pointer to the directory that holds the bat or script file.
     * 
     * Because the setter will only set a correct value this method can be assumed to return a correct directory
     *    or throw an exception. 
     * @return Directory that holds the bat or script file.
     * @throws TavernaException Thrown if the directory has not been set.
     */
    public File getTavernaHome() throws TavernaException{
        if (tavernaHome == null){
            throw new TavernaException ("Illegal call to getTavernaHome before it was set");
        }
        return tavernaHome;
    }
    
    /**
     * Check to see if tavernaHome has bee set successfully.
     * 
     * The setter is responsible for checking 
     * so in the rare case where the bat file or sh script was moved or deleted after TavernaHome was set
     * this method will return a no longer correct result.
     * 
     * @return True if and only if TavernaHome was successfully set. 
     */
    public boolean isTavernaHomeOk(){
       return (tavernaHome != null);
    }
   
    /**
     * Sets the root directory into which the output directories and their files will be written.
     *
     * Checks that the directory exists and can be written to.
     * 
     * Fires a ChangeEvent if successful.
     * 
     * @param rootDir Directory to be uses as the parent for the output directory of each run.
     * @throws TavernaException Thrown when the directory can not be used including because it was null, could not be found, 
     *     was not a directory or could not be written to.
     */
    public void setOutputRootDirectory(File rootDir) throws IOException {
        Utils.checkDirectory(rootDir);
        if (!rootDir.canWrite()){
            throw new IOException ("Unable to write to " + rootDir.getAbsolutePath());
        }
        this.outputRoot = rootDir;
        fireStateChanged();
    }
    
    public File getOutputRootDirectory(){
        return this.outputRoot;
    }
 
    /**
     * Loads a file as a workflow and extracts a small subset of its information.
     * 
     * The file is assumed to be in XML format and contain the workflow name 
     *    as well as the names and depths of any input ports.
     * <p>
     * The lack of exception is not a guarantee that the file is a correct workflow but only that it contains then 
     *     element the wrapper is interested in, in the excepted place.
     * 
     * @param file A workflow file
     * @throws IOException Thrown if the file can not be read.
     * @throws TavernaException Thrown if the workflow file does not have the expected xml format.
     *     This could either be because the workflow is incorrect 
     *     or because its format is different to the one in hard code.
     */
    public void setWorkflowFile(File file) throws IOException, TavernaException{
        Utils.checkFile(file);
        clearWorkflow();
        if (outputRoot == null){
            outputRoot = file.getParentFile();
        }
        XMLBasedT2Flow workflow = new XMLBasedT2Flow(file);
        updateWorkflow(workflow);
        workflowURI = "file:" + file.getAbsolutePath();
        fireStateChanged();
    } 
   
   /**
     * Loads a URI containing a workflow file and extracts a small subset of its information.
     * 
     * The file is assumed to be in XML format and contains the workflow name 
     *    as well as the names and depths of any input ports.
     * <p>
     * The lack of exception is not a guarantee that the file is a correct workflow but only that it contains then 
     *     element the wrapper is interested in, in the excepted place.
     * 
     * @param uri pointer to a workflow file (local or remote)
     * @throws IOException Thrown if the file can not be read.
     * @throws TavernaException Thrown if the workflow file does not have the expected xml format.
     *     This could either be because the workflow is incorrect 
     *     or because its format is different to the one hard-coded in.
     */
    public void setWorkflowURI(String uri) throws TavernaException{
        clearWorkflow();
        XMLBasedT2Flow workflow = new XMLBasedT2Flow(uri);
        updateWorkflow(workflow);
        workflowURI = uri;
    } 
    
    //private void updateBasedOnWorkflowDocument(Document workflow) throws TavernaException{
    //    clearWorkflow();
    //    updateWorkflow(workflow);
    //}
        
    private void updateWorkflow(TavernaWorkflow workflow) throws TavernaException{
        workflowName = workflow.getWorkflowName();
        inputs = workflow.getInputs();
        fireStateChanged();
   }
   
   private void clearWorkflow(){
       workflowURI = null;
       workflowName = null;
       inputs = new HashMap<String,Integer>();
       clearInputs();
   }

   /**
    * Gets the workflow name if any or null if no workflow was set.
    * <p>
    * As workflow setters throw an exception if the workflow to be set does not have the workflow name 
    *    in the expected place, a null result here can be assumed to mean no workflow set.
    * 
    * @return The name of the workflow or null if no workflow was set.
    */
    public String getWorkflowName(){
       return workflowName;
    }
 
    /**
     * Checks to see if the current state of the loaded parameters require inputs.
     * 
     * If not workflow has been set then no inputs are required yet.
     * 
     * @return True if and only if a workflow has been set that requires parameters.
     */
    public boolean needsInputs(){
         return (inputs.size() > 0);
    }
   
    /**
     * Returns a mapping of input names to the depth of that input.
     * 
     * This method will always return a map not null.
     * If the map is empty (size = 0) this could mean either, no workflow has been set
     *    or that the workflow set does not require inputs.
     * @return 
     */
    public Map<String,Integer> getInputNamesAndDepths(){
        return inputs; 
    }
   
    /**
     * Individually sets the values to be used for each input.
     * <p>
     * Includes a minimal correctness checking which could cause Exceptions to be thrown 
     *     but in no way guarantees that the failure to throw an exception means the inputs are valid.
     * <p>
     * Running this method even if it throws an exception clears any previously set inputs, including file or uri.
     * 
     * @param newInputs Array of the inputs which are assumed to have values associated with them.
     * @throws TavernaException Thrown if no workflow has been correctly set.
     *     Also thrown it the newInputs names do not map one to one to the expected inputs (case sensitive),
     *     including if there are few or nor than the expected inputs.
     */
    public void setInputs(TavernaInput[] newInputs) throws TavernaException{
        clearInputs();
        if (newInputs == null){
            //calling with null could happen if user cancels setting inputs
            //Acts like a clear
            return;
        }
        if (workflowName == null){
            throw new TavernaException ("Illegal attempt to load inputs before workflow");
        }
        if (this.inputs.size() != newInputs.length){
            throw new TavernaException("Expected " + this.inputs.size() + " but found " + newInputs.length + " inputs");
        }
        for (TavernaInput input:newInputs){
            if (inputs.containsKey(input.getName())){
                if (!input.hasValue()){
                    throw new TavernaException(input.getName() + " does not have a value, list or uri set");
                }
            } else  {
                throw new TavernaException("Incorrect input found: " + input.getName());
            }   
        }
        this.inputValues = newInputs;                
        fireStateChanged();
    }
   
    /**
     * Collectively sets all inputs as coming from a single file.
     * 
     * The input file must be in XML Baclava format and contain at least the expected inputs (case sensitive).
     * Extra values can be included but ignored as Taverna also allow extra values which are ignored.
     * <p>
     * Running this method with an existing file clears any previously set inputs, including file or uri.
     * 
     * @param inputs Single Baclava file containing at least the required inputs (case sensitive).
     * @throws IOException Error accessing the file.
     * @throws TavernaException Thrown if no workflow has been set or if an expected input is missing
     * @throws ParserConfigurationException Not expected to be thrown by this method.
     * @throws SAXException Thrown if the file can not be parsed as XML.
     */
    public void setInputsFile(File inputs) 
            throws TavernaException, IOException{
        Utils.checkFile(inputs);
        setInputsURI("file:" + inputs.getAbsolutePath());
    }
    
    /**
     * Collectively sets all inputs as coming from a single file pointed to by this uri
     * 
     * The input file must be in xml (baclava format) and contain at least the expected inputs (case sensitive).
     * Extra values can be included but ignored as Taverna also allow extra values which are ignored.
     * <p>
     * Running this method even if an exception is thrown, clears any previously set inputs, including file or uri.
     * 
     * @param uri Points to a single baclava file containing at least the required inputs (case sensitive).
     * @throws TavernaException Thrown if no workflow has been set or if an expected input is missing
     * @throws ParserConfigurationException Thrown if the String can not be converted to a correct uri
     * @throws SAXException Thrown if the file can not be parsed as xml.
     */
    public void setInputsURI(String uri) 
            throws TavernaException {
        clearInputs();
        if (workflowName == null){
            throw new TavernaException ("Illegal attempt to load inputs before workflow");
        }
        DataThingBasedBaclava baclava = new DataThingBasedBaclava(uri);
        for (String inputName: inputs.keySet().toArray(new String[inputs.keySet().size()])){
            if (!baclava.hasValue(inputName)) {
                throw new TavernaException("Unable to find input " + inputName + " in " + uri);
            }
        }
        inputsURI = uri;
        fireStateChanged();
    }
    
    private void clearInputs(){
       inputValues = null;
       inputsURI = null;
    }

    /**
     * Check to see if teh wrapper is ready to call a run of the workflow.
     * <p>
     * False is returned if:
     * <ul>
     *   <li> TavernaHome has not be set
     *   <li> No workflow has been set
     *   <li> No output directory has been set
     *   <li> The workflow requires inputs but no inputs, input file or input uri has been set.
     * </ul>
     * <p>
     * Note true does not guarantee that the workflow wil run correctly only that the wrapper is ready to try.
     * 
     * @return False if a reason not to run the workflow is unknown otherwise true.
     */
    public boolean readyToRun(){
        if (tavernaHome == null){
            return false;
        }
        return workflowURI != null && outputRoot != null && (inputs.size() <= 0 || (inputValues != null || inputsURI != null));
    }

    /**
     * Runs the workflow based on previously set criteria.
     * 
     * @return A wrapper around a started run of the workflow
     * @throws TavernaException thrown if one of the required parameters what not set. Including
     * <ul>
     *   <li> TavernaHome has not be set
     *   <li> No workflow has been set
     *   <li> No output directory has been set
     *   <li> The workflow requires inputs but no inputs, input file or input uri has been set.
     * </ul>
     */
    public CommandLineRun runWorkFlow() throws TavernaException, URISyntaxException {
        if (tavernaHome == null){
            throw new TavernaException("TavernaHome has not be set.");
        }
        if (workflowURI == null){
            throw new TavernaException("Workflow has not been set.");
        }
        if (outputRoot == null){
            throw new TavernaException("Output Directory has not been set.");
        }
        if (inputs.size() > 0){
            if (inputValues == null && inputsURI == null){
               throw new TavernaException("Inputs must be supplied before workflow can run."); 
            }
        }
        try {
            return new CommandLineRun(tavernaHome, outputRoot, inputValues, inputsURI, workflowURI);
        } catch (IOException ex) {
            throw new TavernaException("IOException setting up workflow to run", ex);
        }
    }
 
}
