package net.sf.taverna.portal.wireit;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

//Json element are optional and can be commented out.

/**
 * Reads the save WireIt workings from database and returns then to WireIt.
 * @author Christian
 */
public class ListWireit extends WireitSQLBase {

    public static final String JSON_WIRINGS_DIR_PARAMETER = "JSON_WIRINGS_DIR";
    public static final String SAVE_TO_PARAMETER = "SAVE_TO";
    public static final String SAVE_TO_FILESYSTEM = "filesystem";
    public static final String SAVE_TO_DATABASE = "database";
    private File jsonWiringsDir = null; // directory where we save JSON wirings as individual files
    private String SAVE_TO; // wirings will be saved to/loaded from filesystem or database based on the value of this context parameter
    
    @Override
    public void init(){
        String jsonWiringsDirPath = getServletContext().getInitParameter(JSON_WIRINGS_DIR_PARAMETER);
        SAVE_TO = getServletContext().getInitParameter(SAVE_TO_PARAMETER);
        if (SAVE_TO == null){
            throw new RuntimeException("Method of saving JSON wirings (filesystem or database) has not been configured in web.xml. Check context parameter " + SAVE_TO_PARAMETER +".");
        }
        else{
            if (SAVE_TO.toLowerCase().equals(SAVE_TO_FILESYSTEM)) {
                if (jsonWiringsDirPath != null) {
                    jsonWiringsDir = new File(jsonWiringsDirPath);
                    if (!jsonWiringsDir.exists()) {
                        jsonWiringsDir.mkdirs();
                    }
                } else {
                    throw new RuntimeException("Directory where to save to/load from JSON wirings has not been configured in web.xml. Check context parameter " + JSON_WIRINGS_DIR_PARAMETER + ".");
                }
            }
        }
    }
    
    /**
     * Sets up the servlet and creates an SQL statement against which queries can be run.
     * 
     * @throws ServletException Thrown if the SQL connection and statement can not be created.
     *     Including if the hard coded database, user and password are not found.
     */
    public ListWireit() throws ServletException{
        super();
    }
        
    /**
     * Reads the save WireIt workings for this language from database and returns then to WireIt.
     * <p>
     *     See WireIt\examples\ajaxAdapter\listWirings.json for an example format.
     * <p>
     * Notable points include
     * <ul>
     * <li>Wireit sends out URLEncoded but expects URLDecoded back</li>
     * <li>Wireit expects back Json (even though that is not sent out)</li>
     * <li>All the objects are Strings so must be wrapped in quotes</li>
     * <li>The quotes within the working String but all be preceded by a \</li>
     * </ul>
     * @param request Should have a language parameter.
     * @param response Returns the wirings in the format WireIt expects.
     *     See WireIt\examples\ajaxAdapter\listWirings.json for an example format.
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println();
        System.out.println((new Date()) + "in ListWireit.doGet");
        
        String language = request.getParameter("language");
        
        // Set the MIME type for the response message
        response.setContentType("text/x-json;charset=UTF-8");  
        // Get a output writer to write the response message into the network socket
        PrintWriter out = response.getWriter();
 
        try {
            if (SAVE_TO.toLowerCase().equals(SAVE_TO_FILESYSTEM)){
                out.println(getJSONWiringsFromFiles(language));                
            }
            else{
                out.println(getJSONWiringsFromDatabase(language));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServletException(ex);
        }
    }
    
    private String getJSONWiringsFromFiles(String language) throws IOException{
        
        // Get all files from the JSON directory and append them 
        // into one big JSON string
        FileFilter jsonFileFilter = new FileFilter(){

            public boolean accept(File file) {
                return !file.isDirectory() && file.getName().endsWith(".json");
            }
        };
        
        File[] jsonFiles = jsonWiringsDir.listFiles(jsonFileFilter);
        
        StringBuilder strBuffer = new StringBuilder();
        strBuffer.append("[");
        boolean notFirst = false;
        int i = 0;
        for (File jsonFile : jsonFiles) {
            System.out.println("Found JSON wiring " + jsonFile.getAbsolutePath());
            if (notFirst){
                strBuffer.append(",\n");
            } else {
                notFirst = true;
            }
            String jsonString = FileUtils.readFileToString(jsonFile);
            strBuffer.append("{\"id\":\"");
            strBuffer.append(i++);
            strBuffer.append("\",\n");
            String name = URLDecoder.decode(jsonFile.getName().substring(0, jsonFile.getName().indexOf(".json")));
            strBuffer.append("\"name\":\"");
            strBuffer.append(name);
            strBuffer.append("\",\n");
            //Json needs quotes " to be escaped
            jsonString = jsonString.replace("\"", "\\\"");
            //Json needs carriage returns to be escaped.
            jsonString = jsonString.replace("\\n", "\\\\n");
            strBuffer.append("\"working\":\"");
            //JSONObject workingJson = new JSONObject(working);
            //builder.append(workingJson.toString(4));
            strBuffer.append(jsonString);
            strBuffer.append("\",\n");
            strBuffer.append("\"language\":\"");
            strBuffer.append(language);
            strBuffer.append("\"}");            
        }
        // Get rid of the last ",\n"
        //strBuffer.substring(0, strBuffer.lastIndexOf(",\n")-1);
        strBuffer.append("]");
        System.out.println(strBuffer.toString());
       
        return strBuffer.toString();
    }
 
    
    /**
     * Runs a select * query where language = language and returns the results as a Json String.
     * 
     * @param language Language to be selected for null for all languages
     * @return json String without spaces or line breaks.
     * @throws SQLException Thrown if the query fails
     */
    private String getJSONWiringsFromDatabase(String language) throws SQLException{
        String sqlStr;
        if (language == null){
            sqlStr = "select * from wirings";
        } else {
            sqlStr = "select * from wirings where language = \"" + language + "\"";
        }
        System.out.println("Executing SQL query: " + sqlStr);
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean notFirst = false;
        ResultSet rset = executeQuery(sqlStr);  // Send the query to the server
        while(rset.next()) {
            if (notFirst){
                builder.append(",\n");
            } else {
                notFirst = true;
            }
            appendResult(rset, builder, language);
        }
        builder.append("]");
        closeResultSet(rset);
        return builder.toString();
    }

    /**
     * Added the data from the SQL result set to the output.
     * 
     * @param rset Result of the SQl query.
     * @param builder Builder for the result to be sent back to the client
     * @param language The WireIt language that for which the list of pipes was requested.
     * @throws SQLException error reading the SQL result.
     */
    private void appendResult(ResultSet rset, StringBuilder builder, String language) throws SQLException{
        // Print a paragraph <p>...</p> for each record
        builder.append("{\"id\":\"");
        builder.append(rset.getInt("id"));
        builder.append("\",\n");
        String name = URLDecoder.decode(rset.getString("name"));
        builder.append("\"name\":\"");
        builder.append(name);
        builder.append("\",\n");
        String working = URLDecoder.decode(rset.getString("working"));
        //Json needs quotes " to be escaped
        working = working.replace("\"","\\\"");
        //Json needs carriage returns to be escaped.
        working = working.replace("\\n","\\\\n");
        builder.append("\"working\":\"");
        //JSONObject workingJson = new JSONObject(working);
        //builder.append(workingJson.toString(4));
        builder.append(working);
        builder.append("\",\n");
        language = URLDecoder.decode(rset.getString("language"));
        builder.append("\"language\":\"");
        builder.append(rset.getString("language"));
        builder.append("\"}");
    }
    
    /**
     * Testing and showing method.
     * 
     * Reads the data from the database prints it out unformatted.
     * Checks it is parsable json
     * Prints it out formatted.
     * <p>
     * This method is optional. Comment out if org.json not included.
     * 
     * @param args All ignored.
     * @throws ServletException If the SQL connection could not be made.  
     *          Including if the hard coded database, user and password are not found.
     * @throws SQLException Thrown if the query fails
     * @throws JSONException Thrown if the result is not in json parsable format.
     */
    public static void main(String[] args) throws ServletException, SQLException, JSONException {
        ListWireit tester = new ListWireit();
        String input = tester.getJSONWiringsFromDatabase(null);
        System.out.println(input);
        JSONArray json = new JSONArray(input);
        System.out.println(json.toString(4));
    }

}
