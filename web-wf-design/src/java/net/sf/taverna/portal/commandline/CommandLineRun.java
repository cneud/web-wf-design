package net.sf.taverna.portal.commandline;

import net.sf.taverna.portal.baclava.DataThingBasedBaclava;
import net.sf.taverna.t2.baclava.DataThing;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Class to handle the running of a Taverna Workflow using the command line.
 * 
 * @author Christian
 */
public class CommandLineRun  implements Destroyable {
    
    /** Name of the bat file this is run if windows */
    public static String WINDOWS_LAUCH_FILE = "executeworkflow.bat";
    
    /** Name of the sh script file run if not windows */
    public static String LINUX_LAUCH_FILE = "executeworkflow.sh";
    
    private ProcessRunner runner;
    
    private File runDirectory;
    
    private File outputFile;
      
    private File baclavaFile;
    
    /**
     * Starts a run of Tavernas Command Line tool based on the provided parameters.
     * 
     * @param tavernaHome Location of the bat or sh file to be run.
     * @param outputRoot Location where the different outputs will be written. This are:
     * <dl>
     *   <dt>BaclavaOutput.xml</dt>
     *     <dd>Output saved by the workflow</dd>
     *   <dt>Output.txt</dt>
     *     <dd>Output of this class including parameters passed to the bat.script and a 
     *          capture of anything that sends to output or error streams.</dd>
     *   <dt>TavernaLog.txt</dt>
     *      <dd>Log file generated by the workflow</dd>
     * </dl> 
     * @param inputs Array of the inputs plus the values provided by the users. 
     *    If not null this is the preferred source of inputs.
     * @param inputsURI URI to a Baclava file that holds the inputs.
     *    File uris should begin with "file:"
     *    Ignored if null or "inputs" not null.
     *    If both "inputs" and "inputsURI" are null workflow is assumed not to have any inputs.
     * @param workflowURI URI to a workflow file that holds the inputs.
     *    File uris should begin with "file:"
     * @throws TavernaException Throw if the inputs passed in are determined as incorrect before the workflow is run.
     * @throws IOException Throw if accessing any of the File parameters, or child files created,
     *      causes such an exception.
     */
    public CommandLineRun(File tavernaHome, File outputRoot, TavernaInput[] inputs, String inputsURI, String workflowURI) 
        throws TavernaException, IOException, URISyntaxException{
        ArrayList<String> cmd = getLaunch(tavernaHome);
        runDirectory = Utils.createCalendarBasedDirectory(outputRoot);
        baclavaFile = new File (runDirectory, "BaclavaOutput.xml");
        cmd.add("-outputdoc=" + baclavaFile.getAbsolutePath());
        File logFile = new File(runDirectory, "TavernaLog.txt");
        cmd.add("-logfile=" + logFile.getAbsolutePath());
        if (inputs != null){ // the useris passing all inputs individually
            System.out.append("Running workflow with individual inputs.");
//            for (TavernaInput input:inputs){
//               cmd.addAll(input.getInputArguments());
//            }
 
            // Create Baclava input file from all the inputs gathered and pass that as an input to
            // the Taverna Command Line Tool/Server - this is more convenient as we use just one file
            // for all the inputs and we can also pass more complex inputs rather than just one-line strings
            Map<String, Object> valueMap = new HashMap<String, Object>();
            for (TavernaInput input : inputs) {
                    valueMap.put(input.getName(), input.getValue());                    
            }
            Map<String, DataThing> dataThingsMap = DataThingBasedBaclava.bakeDataThingMap(valueMap);

            // Build the XML Baclava document containing the workflow inputs
            Document document = DataThingBasedBaclava.getDataDocument(dataThingsMap);

            Random randomGenerator = new Random();

            File baclavaInputFile = new File(System.getProperty("java.io.tmpdir"), Integer.toString(randomGenerator.nextInt(999999)) + "-baclava.xml");

            java.io.FileWriter writer = null;
            try {
                XMLOutputter out = new XMLOutputter();
                writer = new java.io.FileWriter(baclavaInputFile);
                out.output(document, writer);
                writer.flush();
                writer.close();
            } catch (Exception ex) {
                System.out.println("Failed to save Baclava inputs to file " + baclavaInputFile.getAbsolutePath());
                ex.printStackTrace();
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (Exception ex2) {
                    // Ignore
                }
            }

            // Probably won't work on Windows
            String baclavaInputURL = "file://" + new URI(baclavaInputFile.getAbsolutePath()).toASCIIString();

            System.out.println("Converted all individual inputs to a local Baclava file : " + baclavaInputURL);
            cmd.add("-inputdoc=" + baclavaInputURL);
        } else if (inputsURI != null){ // the user is passing a Baclava file with all the inputs
            System.out.println("Running workflow with Baclava inputs from " + inputsURI);
            cmd.add("-inputdoc=" + inputsURI);
        }
        cmd.add(workflowURI);

        String[] command = new String[0];
        command = cmd.toArray(command);
        System.out.println("Executing command " + cmd.toString());
        runner = new ProcessRunner(command, tavernaHome);
        runner.start();
    }

    private  ArrayList<String> getLaunch(File tavernaHome){
        ArrayList<String> cmd = new ArrayList<String>();
        if (Utils.isWindows()){
            cmd.add(tavernaHome.getAbsolutePath() + File.separator + WINDOWS_LAUCH_FILE);
        } else {
            cmd.add("sh");
            cmd.add(tavernaHome.getAbsolutePath() + File.separator + LINUX_LAUCH_FILE);
        }
        return cmd;
        //cmd.add("java");
        //cmd.add("-Xmx300m");
        //cmd.add("-XX:MaxPermSize=140m");
 //       cmd.add("-Draven.profile=file:\""+ tavernaHome.getAbsolutePath() + File.separator + "conf/current-profile.xml\"");
        //cmd.add("-Draven.profile=file:conf/current-profile.xml");
        //cmd.add("-Djava.system.class.loader=net.sf.taverna.raven.prelauncher.BootstrapClassLoader");
        //cmd.add("-Draven.launcher.app.main=net.sf.taverna.t2.commandline.CommandLineLauncher");
        //cmd.add("-Draven.launcher.show_splashscreen=false");
        //cmd.add("-Djava.awt.headless=true");
  //      cmd.add("-Dtaverna.startup=\"" + tavernaHome.getAbsolutePath() + File.separator + "\".");
        //cmd.add("-jar");
        //cmd.add("lib/" + CommandLineWrapper.LAUNCH_FILE);
    }
    
    /**
     * Given a directory this method returns the theoretical file that would be used to launch the workflow.
     * 
     * No checking is done to see if it actually exists.
     * 
     * @param tavernaHome Directory expected to hold the bat or sh file.
     * @return Pointer to the expected file.
     */
    public static File getLaunchFile(File tavernaHome){
        if (Utils.isWindows()){
            return new File (tavernaHome, WINDOWS_LAUCH_FILE);
        } else {
            return new File (tavernaHome, LINUX_LAUCH_FILE);            
        }
    }
         
    /**
    * Blocks until the workflow run has finished and then reports if it was successful.
    * 
    * A workflow run is considered successful if it returns with a 0 result.
    * @return True is and only if the workflow run ended with a 0.
    * @throws TavernaException Wrapper around any InterruptedException, or if the process was never started.
    * @throws ProcessException If the process was not started.
    */
    public boolean wasSuccessful() throws TavernaException, ProcessException {
        try {
            boolean result = runner.waitFor() == 0;
            if (outputFile == null){
               saveOutput();
            }
            return result;
        } catch (InterruptedException ex) {
            throw new TavernaException("Error checking if run was successfull", ex);
        }
    }
    
    /**
     * Blocks until the workflow run has finished returns a pointer to the baclavaFile if generated by Taverna.
     * 
     * @return Pointer to an existing baclava output file or null if the workflow finished successfully (return 0)
     *    but did not generate an output file.
     * 
     * @throws TavernaException Thrown if either the workflow run through an exception 
     *    or it finished with a non zero result.
     */
    public File getOutputFile() throws TavernaException, ProcessException{
        if (!wasSuccessful()){
            throw new TavernaException("workflow was not run successfully.");
        }
        if (baclavaFile.exists()){
            return baclavaFile;
        } else {
            return null;
        }
    }
    
    /**
     * Returns the output of the workflow run up to this point.
     * 
     * Non blocking method that returns the output of the workflow up to this point.
     * Expected to hold the parameters used to start the workflow. 
     * Depending on when it is called may (but is never guaranteed to) include intermediate workflow output 
     *    or even results.
     * <p> Unlike other methods this one is not expected to fail or throw an Exception even if the run fails 
     *    or was interrupted but this behaviour is not guaranteed.
     * @return Partial workflow output
     */
    public String getRunInfo(){
        return runner.getRunInfo();
    }

    private String saveOutput() throws TavernaException, ProcessException{
        String output = null;
        FileWriter writer = null;
        try {
            output = runner.getOutput();
            outputFile = new File (runDirectory, "Output.txt");
            writer = new FileWriter(outputFile);
            writer.write(output);
            writer.close();
        } catch (InterruptedException ex) {
           throw new TavernaException ("Workflow was interrupted.", ex);
        } catch (IOException ex) {
            throw new TavernaException ("Workflow output could not be saved.", ex);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
                throw new TavernaException ("Workflow output could not be saved.", ex);
            }
        }    
        return output;
    }
    
    /**
     * Blocking method that saves (if required) a finished workflow runs output.
     * 
     * Blocks until the workflow run has finished.
     * Works even if the workflow result was not 0.
     * The first time this method is called it saves the output to file.
     * 
     * @return Output of this class including parameters passed to the bat.script and a 
     *         capture of anything that sends to output or error streams.
     *         Does not include the results of the workflow run use getOutputFile() for these.
     * @throws TavernaException Thrown if there is an InterruptedException, ProcessException or error saving the output.
     * @throws ProcessException If the process was not started.
     */
    public String getOutput() throws TavernaException, ProcessException{
        if (outputFile == null){
            return saveOutput();
        }
        try {
            return runner.getOutput();
        } catch (InterruptedException ex) {
           throw new TavernaException ("Workflow was interrupted.", ex);
        }
    }
    
    /** 
     * Used to destroy a workflow run.
     * 
     * Stops a workflow run by passing a destroy request to the process that is running it.
     */
    public void destroy(){
        runner.destroy();
    }

}
