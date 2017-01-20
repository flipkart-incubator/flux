/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.flux.examples;

import com.flipkart.flux.initializer.FluxInitializer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

/**
 * <code>WorkflowExecutionDemo</code> is a helper class used to run a sample/user workflow.
 *
 * Usage: WorkflowExecutionDemo <module_name> <workflow_class_fqn>
 *          module_name : name of user module in which workflow code is present, for ex: examples
 *          workflow_class_fqn: fully qualified name of main class which triggers user workflow execution at client side
 *                              ex: com.flipkart.flux.examples.concurrent.RunEmailMarketingWorkflow
 *
 * This demo class requires maven to copy dependencies to create a sample deployment unit structure.
 * If maven is NOT present on the class path pass it's absolute location as third argument
 *
 *      for ex: WorkflowExecutionDemo <module_name> <workflow_class_fqn> /opt/apache-maven-3.3.3/bin/mvn
 *
 * @author shyam.akirala
 */
public class WorkflowExecutionDemo {

    public static void main(String[] args) {

        try {

            if (args.length < 2) {
                System.err.println("Usage: WorkflowExecutionDemo <module_name> <workflow_class_fqn>");
                System.exit(1);
            }

            String moduleName = args[0];
            String workflowClassFQN = args[1];
            String configFileName = "flux_config.yml"; //the configuration file name is flux_config.yml, to match production setup

            String mavenPath = args.length > 2 ? args[2] : "mvn";

            runExample(moduleName, workflowClassFQN, configFileName, mavenPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Does necessary actions to run an example/user's workflow.
     * @param moduleName name of user module in which workflow code is present
     * @param workflowClassFQN fully qualified name of main class which triggers user workflow execution at client side.
     *                         @see com.flipkart.flux.examples.concurrent.RunEmailMarketingWorkflow for example.
     * @param configFileName "flux_config.yml" which contains workflow related configuration.
     *                       @see flux/examples/src/main/resources/flux_config.yml for example.
     * @throws Exception
     */
    private static void runExample(String moduleName, String workflowClassFQN, String configFileName, String mavenPath) throws Exception {

        //copy dependencies to module's target directory
        executeCommand(mavenPath+" -pl " + moduleName + " -q package dependency:copy-dependencies -DincludeScope=runtime -DskipTests");

        //get deployment path from configuration.yml
        FileReader reader = new FileReader(WorkflowExecutionDemo.class.getResource("/packaged/configuration.yml").getFile());
        String deploymentUnitsPath = (String) ((Map)new Yaml().load(reader)).get("deploymentUnitsPath");
        if(!deploymentUnitsPath.endsWith("/")) {
            deploymentUnitsPath = deploymentUnitsPath + "/";
        }
        reader.close();

        //create deployment structure
        String deploymentUnitName = "DU1/1";
        String mainDirPath = deploymentUnitsPath + deploymentUnitName + "/main";
        String libDirPath = deploymentUnitsPath + deploymentUnitName + "/lib";
        executeCommand("mkdir -p " + mainDirPath);
        executeCommand("mkdir -p " + libDirPath);

        //copy dependencies to deployment unit
        FileUtils.copyFile(new File(moduleName + "/target/").listFiles((FilenameFilter) new WildcardFileFilter(moduleName + "*.jar"))[0], new File(mainDirPath + "/" + moduleName + ".jar"));
        FileUtils.copyDirectory(new File(moduleName + "/target/dependency"), new File(libDirPath));
        FileUtils.copyFile(new File(moduleName + "/src/main/resources/" + configFileName), new File(deploymentUnitsPath + deploymentUnitName + "/flux_config.yml"));

        //start flux runtime
        FluxInitializer.main(new String[]{});

        //Invoke workflow in separate process, the below system out prints this process's output in blue color
        System.out.println((char)27 + "[34m"+executeCommand("java -cp " + moduleName +"/target/*:" + moduleName + "/target/dependency/* " + workflowClassFQN) + (char)27 + "[0m");
    }

    /**
     * Helper method to execute a shell command
     * @param command shell command to run
     * @return shell command's output
     */
    private static String executeCommand(String command) throws IOException, InterruptedException {

        StringBuilder output = new StringBuilder();

        Process p;

        p = Runtime.getRuntime().exec(command);
        p.waitFor();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

        //prints the output of the process
        String line = "";
        while ((line = reader.readLine())!= null) {
            output.append(line).append("\n");
        }

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));

        //prints the error stream of the process
        String errorLine = "";
        while ((errorLine = stdError.readLine()) != null) {
            System.out.println(errorLine);
        }

        return output.toString();

    }
}
